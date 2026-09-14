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

echo "lint: ${fatal} fatal, ${err} error, ${warn} warning (${XML})"
echo "::notice title=Lint sonucu::${fatal} fatal, ${err} error, ${warn} warning"

if [ "${fatal:-0}" -gt 0 ]; then
  echo "::error title=Lint fatal::${fatal} fatal lint sorunu var"
  exit 1
fi

exit 0
