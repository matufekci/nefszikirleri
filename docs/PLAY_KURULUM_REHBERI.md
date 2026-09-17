# Play Console Kurulum Rehberi — Nefs Zikirleri

> Son güncelleme: 2026-09-17
> Diller: tr, ar, en, de, fr (aşağıda her adım 5 dilde özet var)

Bu dosya, market yayını için kalan manuel adımları anlatır. Kod değişikliği içermez, sadece işlem rehberi.

---

## 1) Site kökü 404 çözümü (YAPILDI)

- `docs/index.html` oluşturuldu, 5 dilli.
- Pages kaynağı: `main` + `/docs` → https://matufekci.github.io/nefszikirleri/
- İki sayfaya bağlanıyor:
  - https://matufekci.github.io/nefszikirleri/privacy/
  - https://matufekci.github.io/nefszikirleri/account-deletion/
- E-posta düzeltildi: `destek@example.com` → `nefszikirleri@gmail.com` (iki dosyada)

CI: `[full]` ile tam regresyon tetiklendi, PR #3 birleşti.

---

## 2) Play Console — Veri Güvenliği Formu

**Play Console → Uygulaman → Politika → Veri güvenliği → Hesap silme**

- Hesap silme adresi olarak şunu yaz:
  ```
  https://matufekci.github.io/nefszikirleri/account-deletion/
  ```
- Bu sayfa 5 dilli, 3 silme yolunu anlatıyor (uygulama içi anında, web talebi, yerel).

5 dilde etiket:
- tr: Hesap silme adresi
- ar: رابط حذف الحساب
- en: Account deletion URL
- de: URL zur Kontolöschung
- fr: URL de suppression de compte

---

## 3) Release İmzası (Market Sürümü)

Repo PUBLIC, keystore ASLA artifact/log içine konmaz.

### Codespaces'te üret (önerilen, keytool ile)

Codespaces terminalinde:

```bash
# 1) Scripti çalıştır
bash scripts/generate-release-keystore.sh

# Çıktıda 4 değer verilir:
# - release.keystore (PKCS12)
# - release.keystore.b64 (base64)
# - STORE_PASS, KEY_PASS, ALIAS
```

### GitHub'a 4 Secret ekle

GitHub web (tarayıcıdan):

1. Repo → Settings → Secrets and variables → Actions → New repository secret
2. 4 tane ekle:

| Secret Adı | Değer | Açıklama |
|---|---|---|
| `RELEASE_KEYSTORE_BASE64` | `release.keystore.b64` içeriği (tek satır) | İmza dosyası base64 |
| `RELEASE_STORE_PASSWORD` | `KFdQP0k...` gibi | Store şifresi |
| `RELEASE_KEY_ALIAS` | `nefszikirleri-upload` | Alias |
| `RELEASE_KEY_PASSWORD` | aynı veya farklı | Key şifresi |

Alternatif: Codespaces terminalinde `gh secret set ...`

### Güvenlik

- `release.keystore` ve `*.b64` dosyaları `.gitignore`'da → commit edilmez.
- İş bitince Codespaces'ten sil, yedeği şifreli USB'de sakla.
- CI workflow'u (`ci.yml` → `release-build` job) keystore'u sadece `$RUNNER_TEMP` içinde çözer, artifact'a koymaz.

---

## 4) Firebase SHA-1 (Google Girişi Parmak İzi)

**ÖNEMLİ: Upload key YETMEZ, App signing key gerekir.**

Akış:

1. Play Console → Uygulaman → Kurulum → Uygulama bütünlüğü (App integrity)
2. Orada iki anahtar var:
   - **Upload key** (senin yüklediğin AAB'yi imzalayan) → senin `release.keystore`'un
   - **App signing key** (Google'ın son kullanıcaya dağıttığı APK'yı imzalayan) → Google yönetir
3. **App signing key** bölümündeki SHA-1'i kopyala.
4. Firebase Console → Proje ayarları → Genel → Android uygulaması `com.aistudio.nefszikir.kdhrmq` → Parmak izi ekle → SHA-1 yapıştır.
5. Ayrıca SHA-256'yı da ekle (önerilir).
6. `google-services.json`'u yeniden indirmen gerekmez (SHA istemci dosyasında değil, sunucu tarafında doğrulanır).

CI debug SHA-1 (sabit, değişmez):
```
b8639947203ff078df3f7c7750b7342bd7bd6f58
→ B8:63:99:47:20:3F:F0:78:DF:3F:7C:77:50:B7:34:2B:D7:BD:6F:58
```
Bu zaten Firebase'de olmalı (GOOGLE-GIRIS-VE-IMZA.md).

Sandbox'ta üretilen upload key SHA-1 (örnek, senin Codespaces üretimin farklı olacak):
```
E7:20:78:23:84:E4:67:E6:79:0D:F9:AD:35:6D:E0:64:5D:2B:2B:EE
```

---

## 5) Kişisel Hesap Şartı: 12 Tester + 14 Gün Closed Test

Google, kişisel hesapla yayın için zorunlu kılıyor:

- Play Console → Test → Kapalı test (Closed testing) → Yeni sürüm
- En az **12 tester** (deneyici) e-posta listesi ekle.
- Sürümü kapalı teste gönder, **kesintisiz 14 gün** bekle.
- Bu sürede tester'lar uygulamayı yükleyip kullanmalı (crash olmamalı).
- 14 gün dolmadan üretime (Production) gönderemezsin.

Öneri:
- Tester listesini bir Google Group yap, oraya 12+ kişi ekle.
- Her gün WorkManager ve bildirimlerin çalıştığını doğrula.

---

## 6) Değerler (Sabitler)

- applicationId: `com.aistudio.nefszikir.kdhrmq`
- Destek e-postası: `nefszikirleri@gmail.com`
- Hesap silme URL: `https://matufekci.github.io/nefszikirleri/account-deletion/`
- Gizlilik URL: `https://matufekci.github.io/nefszikirleri/privacy/`
- Ana site: `https://matufekci.github.io/nefszikirleri/`

---

## 7) Kontrol Listesi (Yayın Öncesi)

- [x] docs e-posta düzeltildi
- [x] docs/index.html kapak sayfası 5 dilli
- [x] Pages building → https://matufekci.github.io/nefszikirleri/ açılıyor mu?
- [ ] Release keystore üretildi (Codespaces)
- [ ] 4 Secret eklendi (GitHub)
- [ ] Release AAB CI'da üretildi mi? (release-build job yeşil)
- [ ] Play Console Veri güvenliği formuna hesap silme URL yazıldı
- [ ] Firebase'e App signing key SHA-1 eklendi (Play Console'dan)
- [ ] Kapalı test: 12 tester + 14 gün tamamlandı

---

## 5 Dilde Kısa Özet (Play Store açıklaması için değil, sadece rehber)

- **tr:** Site kökü düzeltildi, e-posta güncellendi, release anahtarı ve SHA-1 adımları yukarıda.
- **ar:** تم إصلاح الصفحة الرئيسية وتحديث البريد، وخطوات مفتاح الإصدار وSHA-1 أعلاه.
- **en:** Root 404 fixed, email updated, release key and SHA-1 steps above.
- **de:** Root-404 behoben, E-Mail aktualisiert, Release-Key und SHA-1 Schritte oben.
- **fr:** 404 racine corrigée, e-mail mis à jour, étapes clé release et SHA-1 ci-dessus.
