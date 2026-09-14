#!/usr/bin/env bash
# Gradle log'larindaki gorev hatalarini annotation olarak yayınlar.
#
# Neden: CI'da zaman zaman "Cannot invoke ksp...ApplicationManager..." gibi bir
# arıza notu görünüyor ama log dosyalari disaridan indirilemedigi icin HANGI
# Gradle gorevinin patladigi okunamiyordu. Testler `--continue` ile kostugu
# icin is devam ediyor ve job yesil kaliyor; hata da görünmez oluyor.
#
# Bu script yalnizca TESHIS koyar: job'u kirmiziya cevirmez (gercek test
# basarisizligi zaten report-unit-tests.sh tarafindan kirmiziya cevriliyor).
#
# Usage: report-gradle-failures.sh [log...]

set -uo pipefail

if [ "$#" -eq 0 ]; then
  set -- unit-test.log lint.log
fi

found=0

for log in "$@"; do
  [ -f "$log" ] || continue

  # 1) Hangi Gradle gorevi patladi?
  tasks="$(grep -o "Execution failed for task '[^']*'" "$log" 2>/dev/null | sort -u | head -5)"
  if [ -n "$tasks" ]; then
    found=1
    printf '%s\n' "$tasks" | while IFS= read -r line; do
      echo "::warning title=Gradle görev hatası (${log})::${line}"
    done
  fi

  # 2) KSP / derleyici ic hatalari: ilk eslesmenin 4 satirlik baglami
  ctx="$(grep -n -i -m 3 -A 4 'ksp\.\|internal error\|What went wrong' "$log" 2>/dev/null | head -40)"
  if [ -n "$ctx" ]; then
    found=1
    # Annotation icin tek satira indir (uzun satirlar kirpilir)
    flat="$(printf '%s' "$ctx" | tr '\n' '|' | cut -c1-900)"
    echo "::warning title=Gradle log özeti (${log})::${flat}"
  fi
done

if [ "$found" -eq 0 ]; then
  echo "::notice title=Gradle logları temiz::Görev hatası veya KSP iç hatası bulunamadı"
fi

exit 0
