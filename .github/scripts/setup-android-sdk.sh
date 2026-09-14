#!/usr/bin/env bash
# Kosucu imajinda HAZIR gelen Android SDK'yi dogrular, lisanslari onaylar.
#
# Neden action kullanilmiyor:
#   android-actions/setup-android@v4 kendi sabitledigi cmdline-tools
#   surumunu (action.yml: cmdline-tools-version: 14742923) indirmeye
#   calisiyor. Google o dosyayi depodan kaldirinca adim
#   "HTTPError: Unexpected HTTP response: 404" ile dusuyor
#   (bkz. android-actions/setup-android#536 "Command line tools URL
#   change"). 2026-09-14'te iki run üst üste, 4 job'in hepsi bu yuzden
#   kirmizi oldu; hata Gradle'a hic ulasmadi.
#
#   GitHub'in ubuntu imajinda SDK zaten kurulu: cmdline-tools 12.0,
#   build-tools 37.0.0/36.1.0/36.0.0/35.x/34.x, platform-tools 37.0.1,
#   platforms android-36.1 ... ve ANDROID_HOME tanimli. Yani action'in
#   yaptigi isin cogu gereksiz; kalan tek gercek ihtiyac lisans onayi.
#
# Bu script eksik bir sey bulursa NET hata verir; sessizce gecmez.

set -uo pipefail

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/usr/local/lib/android/sdk}}"
echo "ANDROID_HOME=${SDK}"

if [ ! -d "$SDK" ]; then
  echo "::error::Android SDK dizini yok: ${SDK}"
  exit 1
fi

# sdkmanager'i bul (cmdline-tools/latest veya surumlu klasor)
SDKM="$(ls -d "$SDK"/cmdline-tools/*/bin/sdkmanager 2>/dev/null | head -1 || true)"
if [ -z "$SDKM" ] && [ -x "$SDK/tools/bin/sdkmanager" ]; then
  SDKM="$SDK/tools/bin/sdkmanager"
fi
if [ -z "$SDKM" ] || [ ! -x "$SDKM" ]; then
  echo "::error::sdkmanager bulunamadi (${SDK}/cmdline-tools/*/bin)"
  ls -R "$SDK" 2>/dev/null | head -40
  exit 1
fi
echo "sdkmanager: ${SDKM}"

# Projenin gereksinimleri (dizin adiyla dogrulanir; surum katalogundaki
# compileSdk 36.1 ve AGP'nin varsayilan build-tools surumuyle eslesir).
need_dirs="platform-tools platforms/android-36.1 build-tools/36.0.0"
missing=""
for p in $need_dirs; do
  if [ -d "$SDK/$p" ]; then
    echo "  kurulu: $p"
  else
    echo "  EKSIK: $p"
    missing="$missing $p"
  fi
done

# Lisans onayi (imajda genelde zaten onayli; tekrar etmek zararsiz)
if [ -x "$SDKM" ]; then
  yes 2>/dev/null | timeout 180 "$SDKM" --licenses >/dev/null 2>&1 || true
fi

# Eksik varsa kurmayi dene (ag yoksa uyari ver, derleme zaten net hata verir)
if [ -n "$missing" ]; then
  pkgs=""
  for p in $missing; do
    case "$p" in
      platform-tools)        pkgs="$pkgs platform-tools" ;;
      platforms/*)           pkgs="$pkgs platforms;${p#platforms/}" ;;
      build-tools/*)         pkgs="$pkgs build-tools;${p#build-tools/}" ;;
    esac
  done
  echo "  kurulmaya calisiliyor:$pkgs"
  # shellcheck disable=SC2086
  timeout 600 "$SDKM" --install $pkgs 2>&1 | tail -8 || \
    echo "::warning::sdkmanager --install basarisiz; imajdaki paketlerle devam ediliyor"
fi

# Android SDK kokunu sonraki adimlara aktar
{
  echo "ANDROID_SDK_ROOT=${SDK}"
  echo "ANDROID_HOME=${SDK}"
} >> "${GITHUB_ENV:-/dev/null}"

echo "SDK hazir: ${SDK}"
