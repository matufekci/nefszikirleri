#!/usr/bin/env bash
# Android lint sonucunu CI'da görünür yapar.
#
# `Run Android lint` adımı `continue-on-error: true` ile çalışıyor; yani lint
# patlasa (ör. bir KSP/derleme hatası yüzünden hiç rapor üretmese) bile job
# yeşil kalıyordu. Bu script:
#   1) lint raporu HİÇ üretilmemişse -> exit 1 (lint çalışmadı demektir),
#   2) rapor varsa fatal/error/warning sayılarını annotation olarak yayınlar
#      ve yalnızca FATAL sorun varsa exit 1 verir.
#
# Lint'in kendi error/warning bulguları bilinçli olarak job'u kırmaz: proje
# şu an onları politika olarak tolere ediyor. Ama "lint hiç çalışmadı" durumu
# sahte yeşildir ve burada kırmızıya döner.
#
# Usage: report-lint.sh [lint-xml-report]

set -uo pipefail

XML="${1:-app/build/reports/lint-results-debug.xml}"

# Verilen yol yoksa bilinen alternatifleri dene (AGP sürümüne göre ad değişebilir).
if [ ! -f "$XML" ]; then
  for candidate in \
    app/build/reports/lint-results-debug.xml \
    app/build/reports/lint-results.xml \
    app/build/reports/lint/lint-results-debug.xml; do
    if [ -f "$candidate" ]; then XML="$candidate"; break; fi
  done
fi

if [ ! -f "$XML" ]; then
  echo "::error title=Lint raporu yok::${XML} bulunamadı — lintDebug çalışmadı mı? (KSP/derleme hatası olabilir; lint.log'a bakın)"
  exit 1
fi

count() { grep -o "severity=\"$1\"" "$XML" 2>/dev/null | wc -l | tr -d ' '; }

fatal="$(count Fatal)"
err="$(count Error)"
warn="$(count Warning)"

# En sik tekrar eden sorun kimlikleri: sayiyi gormek yetmiyor, HANGI kuralin
# tekrarladigini da gormek gerekiyor (log dosyalari bu ortamdan indirilemiyor).
# Lint XML'i her niteligi AYRI SATIRA yaziyor (id, severity, message ...),
# bu yuzden tek satirlik grep ile id+severity cifti yakalanmiyor. Asagidaki
# awk hem cok satirli (gercek lint cikti) hem tek satirli bicimi isler:
# <issue ile baslar, id/severity'yi biriktirir, satirda ">" gorunce yazar.
issue_pairs() {
  # Her <issue ...> blogunu tampona biriktir (cok satirli olabilir), tag
  # kapaninca (satirda ">") ilk id= ve severity= degerlerini yaz.
  # id, lint XML'inde ilk nitelik oldugu icin "ilk eslesme" dogru degeri verir.
  awk '
    /<issue/ { inissue = 1; buf = "" }
    inissue {
      buf = buf " " $0
      if (index($0, ">") > 0) {
        id = ""; sev = ""
        if (match(buf, /id="[^"]*"/))       id  = substr(buf, RSTART + 4,  RLENGTH - 5)
        if (match(buf, /severity="[^"]*"/)) sev = substr(buf, RSTART + 10, RLENGTH - 11)
        if (id != "" && sev != "") print sev, id
        inissue = 0
      }
    }
  ' "$XML" 2>/dev/null
}

top_ids() {
  issue_pairs | awk -v s="$1" '$1 == s { print $2 }' \
    | sort | uniq -c | sort -rn | head -5 \
    | awk '{ printf "%s(%s) ", $2, $1 }'
}

# Bagimlilik guncelleme onerileri: "A newer version of X than Y is available: Z"
# mesaji lint'in kendi hesapladigi KESIN listedir (Google Maven/Plugin Portal
# indeksinden). Disaridan surum tahmin etmek guvenilmez: ornegin Maven Central
# "en yeni" diye alpha surumleri donduruyor. Bu yuzden listeyi lint'ten okuyoruz.
dep_updates() {
  awk '
    /<issue/ { inissue = 1; buf = "" }
    inissue {
      buf = buf " " $0
      if (index($0, ">") > 0) {
        id = ""; msg = ""
        if (match(buf, /id="[^"]*"/))      id  = substr(buf, RSTART + 4,  RLENGTH - 5)
        if (match(buf, /message="[^"]*"/)) msg = substr(buf, RSTART + 9,  RLENGTH - 10)
        if (id ~ /^(GradleDependency|NewerVersionAvailable|AndroidGradlePluginVersion|GradlePluginVersion|KotlinGradlePluginVersion)$/)
          print id "\t" msg
        inissue = 0
      }
    }
  ' "$XML" 2>/dev/null \
    | sed -E 's/^[A-Za-z]+\tA newer version of ([^ ]+) than ([^ ]+) is available: (.*)$/\1 \2 -> \3/' \
    | sed -E 's/^[A-Za-z]+\t(.*)$/[diger] \1/' \
    | sort -u
}

# Error/Fatal sorunlarin DOSYA:SATIR bilgisi. Sayi ve kural adi yetmiyor:
# "2 error" gorup hangi satir oldugunu bilemeyince duzeltme turu kayboluyor
# (2026-09-15'te yasandi). Konum, <issue> blogunun icindeki <location>
# ogesinde ve blogun SONUNDA oldugu icin tampon </issue>'e kadar birikir.
issue_locations() {
  awk '
    function flush(   id, sev, f, l) {
      id = ""; sev = ""; f = ""; l = ""
      if (match(buf, /id="[^"]*"/))       id  = substr(buf, RSTART + 4, RLENGTH - 5)
      if (match(buf, /severity="[^"]*"/)) sev = substr(buf, RSTART + 10, RLENGTH - 11)
      if (match(buf, / file="[^"]*"/))    f   = substr(buf, RSTART + 7, RLENGTH - 8)
      if (match(buf, / line="[^"]*"/))    l   = substr(buf, RSTART + 7, RLENGTH - 8)
      if (sev == "Error" || sev == "Fatal") print sev, id, f ":" l
      buf = ""
    }
    /<issue/    { if (inissue) flush(); inissue = 1; buf = "" }
    inissue     { buf = buf " " $0 }
    /<\/issue>/ { if (inissue) { flush(); inissue = 0 } }
    END         { if (inissue) flush() }
  ' "$XML" 2>/dev/null
}

error_locs="$(issue_locations | head -10 | awk '{ printf "%s %s %s | ", $1, $2, $3 }')"

top_fatal="$(top_ids Fatal)"
top_error="$(top_ids Error)"
top_warn="$(top_ids Warning)"

echo "lint: ${fatal} fatal, ${err} error, ${warn} warning (${XML})"
echo "  en sik fatal : ${top_fatal:--}"
echo "  en sik error : ${top_error:--}"
echo "  en sik warning: ${top_warn:--}"

detail="${fatal} fatal, ${err} error, ${warn} warning"
if [ -n "${top_fatal}" ]; then detail="${detail} | fatal: ${top_fatal}"; fi
if [ -n "${top_error}" ]; then detail="${detail} | error: ${top_error}"; fi
if [ -n "${error_locs}" ]; then detail="${detail} | KONUM: ${error_locs}"; fi
if [ -n "${top_warn}" ]; then detail="${detail} | warning: ${top_warn}"; fi
echo "::notice title=Lint sonucu::${detail}"

# Guncelleme onerilerini ayrica yayinla: 40 uyarinin buyuk cogunlugu bu ve
# "hangi kutuphane hangi surume" bilgisi olmadan aksiyon alinamiyor.
deps="$(dep_updates)"
if [ -n "$deps" ]; then
  ndeps="$(printf '%s\n' "$deps" | wc -l | tr -d ' ')"
  echo "  guncelleme onerisi: ${ndeps}"
  printf '%s\n' "$deps" | sed 's/^/    /'
  flat="$(printf '%s' "$deps" | tr '\n' '|' | sed 's/|/ | /g')"
  echo "::notice title=Bagimlilik guncellemeleri (${ndeps})::${flat}"
fi


if [ "${fatal:-0}" -gt 0 ]; then
  echo "::error title=Lint fatal::${fatal} fatal lint sorunu var"
  exit 1
fi

exit 0
