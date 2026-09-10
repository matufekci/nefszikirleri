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
  # İlk <testsuite ...> etiketindeki sayaçlar.
  suite_tag="$(grep -o '<testsuite [^>]*>' "$f" | head -1)"
  get() { printf '%s' "$suite_tag" | grep -o "$1=\"[0-9]*\"" | head -1 | grep -o '[0-9]*'; }

  t="$(get tests)";   t="${t:-0}"
  fl="$(get failures)"; fl="${fl:-0}"
  er="$(get errors)";  er="${er:-0}"
  sk="$(get skipped)"; sk="${sk:-0}"

  total=$((total + t))
  failures=$((failures + fl))
  errors=$((errors + er))
  skipped=$((skipped + sk))

  suite_name="$(basename "$f" .xml)"; suite_name="${suite_name#TEST-}"
  suite_lines="${suite_lines}${suite_name}: ${t} test, ${fl} failure, ${er} error${NL}"

  # Başarısız testlerin adları: <testcase> içinde <failure>/<error> geçenler.
  names="$(awk '
    /<testcase / {
      n=""; c=""
      if (match($0, /name="[^"]*"/))     n = substr($0, RSTART+6,  RLENGTH-7)
      if (match($0, /classname="[^"]*"/)) c = substr($0, RSTART+11, RLENGTH-12)
      cur = c "#" n
      next
    }
    /<failure|<error/ { if (cur != "") { print cur; cur="" } }
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
    printf '::error title=Unit test başarısız::%s\n' "$line"
  done
  echo "::error title=Unit testler kırmızı::${total} test içinde ${failures} failure, ${errors} error"
  exit 1
fi

echo "::notice title=Unit testler yeşil::${total} test geçti (${skipped} atlandı)"
printf '::notice title=Koşan test sınıfları::%s\n' "$(printf '%s' "$suite_lines" | sed '/^[[:space:]]*$/d' | sed 's/$/%0A/' | tr -d '\n')"
exit 0
