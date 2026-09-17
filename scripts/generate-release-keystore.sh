#!/usr/bin/env bash
# Release keystore üretme - Codespaces için
# Bu script Codespaces terminalinde çalıştırılır (tarayıcıdan).
# Java (keytool) Codespaces imajında hazır gelir.

set -e

# Ayarlar - istersen değiştir
ALIAS="nefszikirleri-upload"
STORE_PASS=$(openssl rand -base64 18 | tr -d '\n/+=' | cut -c1-20)
KEY_PASS="$STORE_PASS"
KEYSTORE_FILE="release.keystore"

echo "=== Nefs Zikirleri Release Keystore Üret ==="
echo "Alias: $ALIAS"
echo "Store Pass: $STORE_PASS"
echo "Key Pass: $KEY_PASS"
echo "Dosya: $KEYSTORE_FILE"
echo ""

# Eski dosya varsa yedekle
if [ -f "$KEYSTORE_FILE" ]; then
  mv "$KEYSTORE_FILE" "${KEYSTORE_FILE}.bak.$(date +%s)"
  echo "Eski keystore yedeklendi."
fi

# keytool ile üret (PKCS12 formatı önerilir, JKS yerine)
keytool -genkeypair -v \
  -keystore "$KEYSTORE_FILE" \
  -alias "$ALIAS" \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -storetype PKCS12 \
  -storepass "$STORE_PASS" \
  -keypass "$KEY_PASS" \
  -dname "CN=Nefs Zikirleri, OU=Mobile, O=matufekci, L=Istanbul, ST=Istanbul, C=TR"

echo ""
echo "=== Keystore oluşturuldu ==="
ls -lh "$KEYSTORE_FILE"

echo ""
echo "=== SHA-1 ve SHA-256 (Firebase için) ==="
keytool -list -v -keystore "$KEYSTORE_FILE" -storepass "$STORE_PASS" | grep -E "SHA1|SHA256|Alias"

echo ""
echo "=== Base64 (GitHub Secret için) ==="
B64=$(base64 -w 0 "$KEYSTORE_FILE")
echo "Base64 uzunluk: ${#B64}"
echo "$B64" > release.keystore.b64
echo "Base64 dosyası: release.keystore.b64"

echo ""
echo "=== GitHub Secret komutları (Codespaces terminalinde) ==="
echo "Aşağıdaki 4 komutu Codespaces terminalinde çalıştır:"
echo ""
echo "gh secret set RELEASE_KEYSTORE_BASE64 < release.keystore.b64"
echo "gh secret set RELEASE_STORE_PASSWORD --body \"$STORE_PASS\""
echo "gh secret set RELEASE_KEY_ALIAS --body \"$ALIAS\""
echo "gh secret set RELEASE_KEY_PASSWORD --body \"$KEY_PASS\""
echo ""
echo "Alternatif (tarayıcıdan): GitHub repo -> Settings -> Secrets and variables -> Actions -> New repository secret"
echo "  RELEASE_KEYSTORE_BASE64 = release.keystore.b64 içeriği"
echo "  RELEASE_STORE_PASSWORD = $STORE_PASS"
echo "  RELEASE_KEY_ALIAS = $ALIAS"
echo "  RELEASE_KEY_PASSWORD = $KEY_PASS"
echo ""
echo "!!! DİKKAT: release.keystore ve release.keystore.b64 dosyalarını ASLA GitHub'a commit etme !!!"
echo "Repo PUBLIC, bu dosyalar .gitignore'da engelli."
echo "İş bitince Codespaces'ten sil veya güvenli bir yere yedekle (USB, şifreli disk)."
echo ""
echo "Son adım: Firebase Console -> Proje ayarları -> Android uygulaması -> Parmak izi ekle"
echo "Buraya App signing key SHA-1'ini değil, YUKARIDAKİ upload key SHA-1'ini şimdilik ekle,"
echo "ama Play Console'a ilk AAB yükledikten sonra App integrity -> App signing key SHA-1'ini de ekle."
