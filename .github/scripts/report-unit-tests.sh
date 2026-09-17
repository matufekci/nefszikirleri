#!/usr/bin/env bash
# Unit test sonuçlarını CI'da görünür yapar.
#
# `unit-tests` job'ındaki adımlar `continue-on-error: true` ile çalıştığı için
# testler patlasa bile job yeşil kalıyordu. Bu script JUnit XML'lerini okur,
# toplam sayıyı ve her bir başarısız testi job annotation olarak yayınlar
# (annotation'lar Checks API'den okunabiliyor) ve başarısızlık varsa exit 1 verir.
#
# Usage: report-unit-tests.sh [test-results-dir]

set -uo pipefail

DIR="${1:-app/build/test-results/testDebugUnitTest}"

NL='
'

shopt -s nullglob
files=("$DIR"/TEST-*.xml)

if [ ${#files[@]} -eq 0 ]; then
  echo "::error title=Unit test sonucu yok::$DIR altında TEST-*.xml bulunamadı — testDebugUnitTest çalışmadı mı?"
  exit 1
fi

total=0
failures=0
errors=0
skipped=0
failed_lines=""
suite_lines=""

for f in "${files[@]}"; do
  # DOSYADAKI TUM <testsuite> etiketlerinin sayaclari TOPLANIYOR.
  # Onceki surum `head -1` ile yalnizca ILK etiketi okuyordu. Android
  # connected XML tek dosyada sinif basina AYRI <testsuite> yaziyor;
  # sonuc: 6 test kosarken 5 raporlandi (run 35116431365) ve daha
  # onemlisi, ikinci testsuite icindeki bir failure ATLANIP sahte yesil
  # uretebilirdi. Artik hepsi toplanıyor.
  suite_tags="$(grep -o '<testsuite [^>]*>' "$f")"
  get() {
    printf '%s\n' "$suite_tags" | grep -o "$1=\"[0-9]*\"" | grep -o '[0-9]*' \
      | awk '{ s += $1 } END { print s + 0 }'
  }

  t="$(get tests)";   t="${t:-0}"
  fl="$(get failures)"; fl="${fl:-0}"
  er="$(get errors)";  er="${er:-0}"
  sk="$(get skipped)"; sk="${sk:-0}"

  total=$((total + t))
  failures=$((failures + fl))
  errors=$((errors + er))
  skipped=$((skipped + sk))

  suite_name="$(basename "$f" .xml)"; suite_name="${suite_name#TEST-}"
  # Birden fazla testsuite varsa hangi siniflarin kosuldugu da gorunsun.
  # `name=` ARANIRKEN basa bosluk sart: yoksa hostname="..." icindeki
  # 'name=' alt dizesi de eslesip sinif listesi yerine cihaz adi yaziyordu
  # (yerel fixture ile yakalandi).
  suite_classes="$(printf '%s\n' "$suite_tags" | grep -o ' name="[^"]*"' \
    | sed 's/^ name="//; s/"$//' \
    | awk '{ printf "%s%s", (NR > 1 ? ", " : ""), $0 }')"
  suite_lines="${suite_lines}${suite_name} [${suite_classes:-?}]: ${t} test, ${fl} failure, ${er} error${NL}"

  # Başarısız testlerin adları: <testcase> içinde <failure>/<error> geçenler.
  names="$(awk '
    function clean(c, m) {
      gsub(/&#10;/, " ", m); gsub(/&#9;/, " ", m)
      gsub(/&quot;/, sprintf("%c", 39), m)
      gsub(/&lt;/, "<", m); gsub(/&gt;/, ">", m); gsub(/&amp;/, "&", m)
      sub(/^<(failure|error)[^>]*>/, "", m)
      sub(/<\/failure>.*$/, "", m); sub(/<\/error>.*$/, "", m)
      gsub(/[\r\n\t]/, " ", m)
      sub(/^[ \t]+/, "", m)
      if (length(m) > 400) m = substr(m, 1, 400) "..."
      return (m == "" ? c : c " :: " m)
    }
    /<testcase / {
      n=""; c=""
      if (match($0, /name="[^"]*"/))     n = substr($0, RSTART+6,  RLENGTH-7)
      if (match($0, /classname="[^"]*"/)) c = substr($0, RSTART+11, RLENGTH-12)
      cur = c "#" n
      inbody = 0
      # `next` YOK: bazi JUnit ciktilari <testcase ...><failure ...> ikilisini
      # TEK satira yaziyor. next ile satir atlaninca failure kurali hic
      # calismiyor ve basarisiz testin adi annotation icine hic dusmuyordu
      # (yerel sentetik XML ile yakalandi). Satir akisa birakiliyor.
    }
    /<failure|<error/ {
      if (cur == "") next
      msg = ""
      if (match($0, /message="[^"]*"/)) msg = substr($0, RSTART+9, RLENGTH-10)
      if (msg == "") {
        # Android connected-test XML cogu zaman message ATTRIBUTE yazmaz;
        # hata metni elementin GOVDESINDE durur. Onceki surum yalnizca
        # attribute a bakiyordu, bu yuzden emulator jobinda 4 basarisiz test
        # sadece ADIYLA gorundu, nedeni gorunmedi. Artik govde de okunuyor.
        body = $0
        sub(/^[^>]*>/, "", body)
        sub(/^[ \t]+/, "", body)
        if (body == "") { inbody = 1; next }
        msg = body
      }
      print clean(cur, msg)
      cur = ""; inbody = 0
      next
    }
    inbody == 1 {
      line = $0
      sub(/^[ \t]+/, "", line)
      inbody = 0
      print clean(cur, line)
      cur = ""
      next
    }
  ' "$f")"
  if [ -n "$names" ]; then
    failed_lines="${failed_lines}${names}
"
  fi
done

bad=$((failures + errors))

echo "unit tests: ${total} test, ${failures} failure, ${errors} error, ${skipped} skipped (${#files[@]} suite)"

{
  echo "### Unit testler"
  echo ""
  echo "| toplam | failure | error | skipped |"
  echo "| --- | --- | --- | --- |"
  echo "| ${total} | ${failures} | ${errors} | ${skipped} |"
  if [ -n "$failed_lines" ]; then
    echo ""
    echo "Başarısız testler:"
    echo ""
    printf '%s\n' "$failed_lines" | sed '/^[[:space:]]*$/d' | sed 's/^/- `/' | sed 's/$/`/'
  fi
} >> "$GITHUB_STEP_SUMMARY" 2>/dev/null || true

if [ "$bad" -gt 0 ]; then
  printf '%s\n' "$failed_lines" | sed '/^[[:space:]]*$/d' | head -n 10 | while IFS= read -r line; do
    printf '::error title=Unit test başarısız::%s\n' "$(printf '%s' "$line" | tr '\n' ' ' | cut -c1-900)"
  done
  echo "::error title=Unit testler kırmızı::${total} test içinde ${failures} failure, ${errors} error"
  exit 1
fi

echo "::notice title=Unit testler yeşil::${total} test geçti (${skipped} atlandı)"
printf '::notice title=Koşan test sınıfları::%s\n' "$(printf '%s' "$suite_lines" | sed '/^[[:space:]]*$/d' | sed 's/$/%0A/' | tr -d '\n')"
exit 0
