#!/usr/bin/env bash
# CI icin google-services.json hazirligi.
#
# ONEMLI / KOK NEDEN:
# Repoda GERCEK app/google-services.json git tarafindan izleniyor
# (Firebase projesi "nefs-zikirleri"). Bu adim eskiden dosyayi UC job'da da
# kosulsuz olarak sahte (ci-dummy) surumle eziyordu. Sonuc: CI'dan cikan
# APK "nefs-zikirleri-dummy" adli uydurma bir proje ile derleniyordu ve
# Google ile giris telefonda asla calismiyordu ("hesap bulunamadi").
#
# Artik gercek dosya KORUNUR; yalnizca yoksa veya gecersizse sahteye dusulur
# ve bu durum log'da buyuk harfle ilan edilir.
set -euo pipefail

REAL="app/google-services.json"
DUMMY="app/ci-dummy-google-services.json"

describe() {
  python3 - "$1" <<'PY'
import json, sys
path = sys.argv[1]
try:
    d = json.load(open(path))
except Exception as e:
    print("  OKUNAMADI: %s" % e)
    sys.exit(1)
proj = d.get("project_info", {}).get("project_id", "?")
clients = d.get("client", [])
oauth = [o for c in clients for o in c.get("oauth_client", [])]
web = [o.get("client_id") for o in oauth if o.get("client_type") == 3]
android = [o for o in oauth if o.get("client_type") == 1]
pkgs = [c.get("client_info", {}).get("android_client_info", {}).get("package_name") for c in clients]
print("  project_id    : %s" % proj)
print("  package_name  : %s" % ", ".join(str(p) for p in pkgs))
print("  android oauth : %d istemci" % len(android))
print("  web oauth     : %s" % (web[0] if web else "YOK -> Google girisi CALISMAZ"))
PY
}

is_real() {
  [ -f "$REAL" ] || return 1
  python3 - "$REAL" <<'PY'
import json, sys
try:
    d = json.load(open(sys.argv[1]))
except Exception:
    sys.exit(1)
pid = d.get("project_info", {}).get("project_id", "")
web = [o for c in d.get("client", []) for o in c.get("oauth_client", []) if o.get("client_type") == 3]
if not web or "dummy" in pid or "REDACTED" in pid:
    sys.exit(1)
PY
}

if is_real; then
  echo "GERCEK google-services.json kullaniliyor (Google girisi bu APK'da calisabilir):"
  describe "$REAL"
elif [ -f "$DUMMY" ]; then
  echo "### UYARI ###"
  echo "Gercek app/google-services.json bulunamadi -> sahte (dummy) yapilandirma kullaniliyor."
  echo "Bu APK'da Google ile giris CALISMAZ."
  cp "$DUMMY" "$REAL"
  describe "$REAL" || true
else
  echo "HATA: ne gercek ne de sahte google-services.json bulundu."
  exit 1
fi
