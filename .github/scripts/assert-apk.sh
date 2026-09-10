#!/usr/bin/env bash
# Assert that an APK was actually produced by a CI build step.
#
# The Gradle steps in ci.yml run with `continue-on-error: true` so that the raw
# build log still gets uploaded as an artifact when the build fails. That makes
# every job look green even when `assembleDebug` blew up and the artifact only
# contains the log file. This script closes that hole: it inspects the output
# directory, prints the real Gradle error as a job annotation (readable from the
# Checks API, unlike the raw log), and exits non-zero when there is no APK.
#
# Usage: assert-apk.sh <apk-dir> <build-log> <artifact-name>

set -uo pipefail

APK_DIR="${1:?usage: assert-apk.sh <apk-dir> <build-log> <artifact-name>}"
BUILD_LOG="${2:-}"
ARTIFACT="${3:-debug-apk}"

# GitHub annotation/summary payloads need these escaped.
escape_data() {
  sed -e 's/%/%25/g' -e 's/\r/%0D/g' | tr '\n' ' ' | sed -e 's/  */ /g' | cut -c1-1800
}

apk_path="$(find "$APK_DIR" -maxdepth 1 -name '*.apk' -type f 2>/dev/null | head -n 1 || true)"

if [ -n "$apk_path" ]; then
  apk_size="$(du -h "$apk_path" | cut -f1)"
  apk_bytes="$(stat -c %s "$apk_path" 2>/dev/null || echo "?")"
  echo "::notice title=APK OK::${apk_path} (${apk_size}, ${apk_bytes} bytes)"
  {
    echo "### APK üretildi ✅"
    echo ""
    echo "| alan | değer |"
    echo "| --- | --- |"
    echo "| dosya | \`$(basename "$apk_path")\` |"
    echo "| boyut | ${apk_size} (${apk_bytes} bytes) |"
    echo "| artifact | \`${ARTIFACT}\` |"
  } >> "$GITHUB_STEP_SUMMARY" 2>/dev/null || true
  echo "APK_PATH=$apk_path" >> "$GITHUB_ENV" 2>/dev/null || true
  exit 0
fi

# --- no APK: surface why -----------------------------------------------------
reason="APK bulunamadı: ${APK_DIR} içinde hiç .apk dosyası yok"
detail=""
if [ -n "$BUILD_LOG" ] && [ -f "$BUILD_LOG" ]; then
  # Prefer Gradle's own "What went wrong" block, fall back to the log tail.
  detail="$(awk '/\* What went wrong:/{flag=1} flag{print; n++} n>=25{exit}' "$BUILD_LOG")"
  if [ -z "$detail" ]; then
    detail="$(grep -E 'FAILURE:|BUILD FAILED|Execution failed|error:|Caused by:|e: ' "$BUILD_LOG" | tail -n 15)"
  fi
  if [ -z "$detail" ]; then
    detail="$(tail -n 25 "$BUILD_LOG")"
  fi
else
  detail="build logu yok: ${BUILD_LOG}"
fi

echo "=============================================="
echo "APK BUILD FAILED — ${reason}"
echo "----------------------------------------------"
echo "$detail"
echo "=============================================="

printf '::error title=APK üretilmedi::%s | %s\n' "$reason" "$(printf '%s' "$detail" | escape_data)"

{
  echo "### APK üretilemedi ❌"
  echo ""
  echo "\`${ARTIFACT}\` artifact'ında APK yok — sadece build logu yüklendi."
  echo ""
  echo '```'
  printf '%s\n' "$detail" | head -n 40
  echo '```'
} >> "$GITHUB_STEP_SUMMARY" 2>/dev/null || true

exit 1
