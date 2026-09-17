#!/usr/bin/env bash
# Release imza DURUMUNU dogrular ve CI'da gorunur kilar.
#
# Neden var: release-build job'i secret olmadan da "yesil" oluyordu; cikan
# artifact app-release-unsigned.apk idi ve bu "Play'e hazir" sanilabiliyordu.
# Ayrica secret VARKEN bile imzanin gercekten uygulandigi (ve debug anahtari
# OLMADIGI) hic kontrol edilmiyordu. Bu script ikisini de kapatir:
#
#   expected=true  -> APK/AAB imzali OLMALI, imza debug.keystore'a ait OLMAMALI,
#                     aksi halde exit 1 (job kirmizi).
#   expected=false -> unsigned beklenir; annotation ile "Play'e hazir DEGIL"
#                     uyarisi basilir, job kirmizi olmaz (secret'siz fork/PR).
#
# GUVENLIK: hicbir secret degeri, parola veya keystore icerigi loglanmaz;
# yalnizca sertifika parmak izleri (public bilgi) yazilir.
#
# Kullanim: assert-release-signing.sh <expected: true|false> [apk-dir] [aab-dir] [debug-keystore]
set -uo pipefail

EXPECTED="${1:?usage: assert-release-signing.sh <true|false> [apk-dir] [aab-dir] [debug-keystore]}"
APK_DIR="${2:-app/build/outputs/apk/release}"
AAB_DIR="${3:-app/build/outputs/bundle/release}"
DEBUG_KS="${4:-debug.keystore}"

norm() { echo "$1" | tr -d ':' | tr 'A-F' 'a-f'; }

apk="$(find "$APK_DIR" -maxdepth 1 -name '*.apk' -type f 2>/dev/null | head -n 1 || true)"
aab="$(find "$AAB_DIR" -maxdepth 1 -name '*.aab' -type f 2>/dev/null | head -n 1 || true)"

APKSIGNER=""
for base in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"; do
  [ -n "$base" ] || continue
  found=$(ls -1 "$base"/build-tools/*/apksigner 2>/dev/null | sort -V | tail -1 || true)
  if [ -n "$found" ]; then APKSIGNER="$found"; break; fi
done

# --- APK imza tespiti -------------------------------------------------------
apk_signed="unknown"
apk_sha1=""
if [ -n "$apk" ]; then
  if [ -n "$APKSIGNER" ]; then
    if "$APKSIGNER" verify --print-certs "$apk" >/tmp/apksigner.out 2>&1; then
      apk_signed="true"
      apk_sha1=$(grep -m1 -i "SHA-1 digest" /tmp/apksigner.out | awk '{print $NF}')
    else
      apk_signed="false"
    fi
  else
    # apksigner yoksa: v1 imzasi META-INF/*.RSA|DSA|EC ile anlasilir (v2/v3-only APK'da
    # bu dosya olmayabilir; o durumda 'unknown' kalir ve dosya adina bakilir).
    if unzip -l "$apk" 2>/dev/null | grep -qiE 'META-INF/.*\.(RSA|DSA|EC)$'; then
      apk_signed="true"
      apk_sha1=$(keytool -printcert -jarfile "$apk" 2>/dev/null | grep -m1 -iE "^\s*SHA1:" | sed 's/.*SHA1:[[:space:]]*//')
    fi
  fi
  case "$(basename "$apk")" in
    *unsigned*) [ "$apk_signed" = "unknown" ] && apk_signed="false" ;;
  esac
fi

# --- AAB imza tespiti (jarsigner; AAB v1/JAR imzasi tasir) -------------------
aab_signed="unknown"
aab_sha1=""
if [ -n "$aab" ]; then
  if command -v jarsigner >/dev/null 2>&1; then
    if jarsigner -verify "$aab" >/tmp/jarsigner.out 2>&1 && grep -q "jar verified" /tmp/jarsigner.out; then
      aab_signed="true"
      aab_sha1=$(keytool -printcert -jarfile "$aab" 2>/dev/null | grep -m1 -iE "^\s*SHA1:" | sed 's/.*SHA1:[[:space:]]*//')
    else
      aab_signed="false"
    fi
  elif unzip -l "$aab" 2>/dev/null | grep -qiE 'META-INF/.*\.(RSA|DSA|EC)$'; then
    aab_signed="true"
  else
    aab_signed="false"
  fi
fi

# --- debug keystore parmak izi (release bununla imzalanmis OLMAMALI) ----------
debug_sha1=""
if [ -f "$DEBUG_KS" ]; then
  debug_sha1=$(keytool -list -v -keystore "$DEBUG_KS" -storepass android -alias androiddebugkey 2>/dev/null \
    | grep -m1 -iE "^\s*SHA1:" | sed 's/.*SHA1:[[:space:]]*//')
fi

echo "Beklenen imza durumu : signed=$EXPECTED"
echo "APK                  : ${apk:-YOK}  (signed=$apk_signed${apk_sha1:+, SHA-1=$apk_sha1})"
echo "AAB                  : ${aab:-YOK}  (signed=$aab_signed${aab_sha1:+, SHA-1=$aab_sha1})"

fail=0
if [ "$EXPECTED" = "true" ]; then
  if [ -z "$apk" ] || [ -z "$aab" ]; then
    echo "::error title=Release artifact eksik::APK veya AAB uretilmedi"
    fail=1
  fi
  if [ "$apk_signed" != "true" ]; then
    echo "::error title=Release APK IMZASIZ::Secret tanimli oldugu halde APK imzali degil (signed=$apk_signed). build.gradle.kts signing yapilandirmasini ve RELEASE_* secret'larini kontrol et."
    fail=1
  fi
  if [ "$aab_signed" = "false" ]; then
    echo "::error title=Release AAB IMZASIZ::Secret tanimli oldugu halde AAB imzali degil. Play Console bu dosyayi reddeder."
    fail=1
  fi
  if [ -n "$debug_sha1" ]; then
    for s in "$apk_sha1" "$aab_sha1"; do
      if [ -n "$s" ] && [ "$(norm "$s")" = "$(norm "$debug_sha1")" ]; then
        echo "::error title=Release DEBUG anahtariyla imzalanmis::Release artifact repodaki debug.keystore ile imzalanmis. Bu Play'e yuklenemez ve guvenlik ihlalidir."
        fail=1
      fi
    done
  fi
  if [ "$fail" -eq 0 ]; then
    echo "::notice title=Release imzali (upload key)::APK SHA-1=$apk_sha1 - Play App Signing 'upload key' parmak izi bu olmali. Firebase'e de eklenmeli (Google girisi icin)."
    {
      echo "### Release imza durumu ✅ imzali"
      echo ""
      echo "| artifact | imza | SHA-1 |"
      echo "| --- | --- | --- |"
      echo "| APK | $apk_signed | \`$apk_sha1\` |"
      echo "| AAB | $aab_signed | \`${aab_sha1:-?}\` |"
    } >> "${GITHUB_STEP_SUMMARY:-/dev/null}" 2>/dev/null || true
  fi
else
  if [ "$apk_signed" = "true" ] || [ "$aab_signed" = "true" ]; then
    # Secret yok dendi ama artifact imzali: beklenmedik bir imza kaynagi var
    # (ornegin env'den sizan eski degisken). Bu durum incelenmeli.
    echo "::error title=Beklenmedik release imzasi::RELEASE_KEYSTORE_BASE64 secret'i yokken artifact imzali cikti. Imza kaynagi belirsiz; inceleyin."
    fail=1
  else
    echo "::warning title=Release UNSIGNED - Play'e hazir DEGIL::RELEASE_KEYSTORE_BASE64 + RELEASE_STORE_PASSWORD + RELEASE_KEY_ALIAS + RELEASE_KEY_PASSWORD secret'lari tanimli degil. Bu job yalnizca R8/derleme saglamligini kanitlar; cikan APK/AAB imzasizdir ve Play Console'a yuklenemez. Kurulum: docs/PLAY_KURULUM_REHBERI.md"
    {
      echo "### Release imza durumu ⚠️ UNSIGNED"
      echo ""
      echo "Release secret'lari tanimli degil; artifact yalnizca derleme dogrulamasi icindir, **Play'e yuklenemez**."
    } >> "${GITHUB_STEP_SUMMARY:-/dev/null}" 2>/dev/null || true
  fi
fi

exit $fail
