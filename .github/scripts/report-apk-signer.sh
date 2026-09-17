#!/usr/bin/env bash
# CI'da uretilen APK'yi HANGI sertifikanin imzaladigini kanit olarak basar.
#
# Neden gerekiyor:
# Google ile giris, uygulamanin imza sertifikasinin SHA-1 degerinin Firebase
# Console'da kayitli olmasini sart kosar. CI her kosuda kendi gecici debug
# anahtarini urettigi icin SHA-1 her build'de degisiyordu ve giris daima
# "hesap bulunamadi / DEVELOPER_ERROR" ile patliyordu. Artik repoda sabit bir
# debug.keystore var; bu script APK'nin gercekten o anahtarla imzalandigini
# dogrular ve Firebase'e yazilmasi gereken degeri annotation olarak basar.
#
# Kullanim: report-apk-signer.sh [apk-dosyasi] [keystore]
set -uo pipefail

APK="${1:-app/build/outputs/apk/debug/app-debug.apk}"
KEYSTORE="${2:-debug.keystore}"

norm() { echo "$1" | tr -d ':' | tr 'A-F' 'a-f'; }

APKSIGNER=""
for base in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"; do
  [ -n "$base" ] || continue
  found=$(ls -1 "$base"/build-tools/*/apksigner 2>/dev/null | sort -V | tail -1 || true)
  if [ -n "$found" ]; then APKSIGNER="$found"; break; fi
done

apk_sha1=""
apk_sha256=""

if [ -f "$APK" ] && [ -n "$APKSIGNER" ]; then
  echo "apksigner: $APKSIGNER"
  out=$("$APKSIGNER" verify --print-certs "$APK" 2>/dev/null || true)
  apk_sha1=$(echo "$out"   | grep -m1 -i "SHA-1 digest"   | awk '{print $NF}')
  apk_sha256=$(echo "$out" | grep -m1 -i "SHA-256 digest" | awk '{print $NF}')
fi

if [ -z "$apk_sha1" ] && [ -f "$APK" ]; then
  echo "apksigner bulunamadi, keytool ile deneniyor"
  out=$(keytool -printcert -jarfile "$APK" 2>/dev/null || true)
  apk_sha1=$(echo "$out"   | grep -m1 -iE "^\s*SHA1:"   | sed 's/.*SHA1:[[:space:]]*//')
  apk_sha256=$(echo "$out" | grep -m1 -iE "^\s*SHA256:" | sed 's/.*SHA256:[[:space:]]*//')
fi

ks_sha1=""
if [ -f "$KEYSTORE" ]; then
  ks_sha1=$(keytool -list -v -keystore "$KEYSTORE" -storepass android -alias androiddebugkey 2>/dev/null \
            | grep -m1 -iE "^\s*SHA1:" | sed 's/.*SHA1:[[:space:]]*//')
fi

if [ -z "$apk_sha1" ]; then
  echo "::warning title=Imza dogrulamasi::APK imza sertifikasi okunamadi (APK: $APK)"
  exit 0
fi

echo "APK        : $APK"
echo "APK SHA-1  : $apk_sha1"
echo "APK SHA-256: $apk_sha256"

if [ -n "$ks_sha1" ]; then
  echo "Keystore   : $KEYSTORE (SHA-1: $ks_sha1)"
  if [ "$(norm "$apk_sha1")" = "$(norm "$ks_sha1")" ]; then
    echo "SONUC: APK, repodaki sabit debug.keystore ile imzalanmis. Imza build'den build'e DEGISMIYOR."
  else
    echo "::warning title=Imza uyusmuyor::APK repodaki debug.keystore ile imzalanmamis; Google girisi bu APK'da calismaz."
  fi
else
  echo "UYARI: $KEYSTORE okunamadi, karsilastirma yapilamadi."
fi

echo "::notice title=Firebase'e kaydedilecek SHA-1::APK imza SHA-1 = $apk_sha1  (Firebase Console > Proje Ayarlari > Android uygulamasi > SHA-1 alanina birebir eklenmeli)"
exit 0
