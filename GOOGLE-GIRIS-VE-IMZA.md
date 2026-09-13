# Google ile Giriş ve APK İmzası — Kurulum / Sorun Giderme

> Güncelleme: 2026-09-10 · Branch: `arena/01a08b35-nefszikirleri`

## Kullanıcının bildirdiği hata

> "Google ile giriş yapmaya çalıştığımda **hesap bulunamadı** uyarısı alıyorum."

## Kök neden (kanıtlı)

İki ayrı hata üst üste binmişti; ikisi de APK'nın kendisindeydi, telefonda değil:

1. **CI, gerçek Firebase yapılandırmasını sahtesiyle eziyordu.**
   `app/google-services.json` git tarafından izleniyor ve içinde gerçek proje
   (`nefs-zikirleri`, 5 Android OAuth istemcisi + 1 web istemcisi) var.
   Buna rağmen `.github/workflows/ci.yml` üç job'da da koşulsuz olarak
   `cp app/ci-dummy-google-services.json app/google-services.json` çalıştırıyordu.
   Sonuç: CI'dan çıkan her APK `nefs-zikirleri-dummy` adlı **uydurma** bir projeyle
   derleniyordu. Web istemci kimliği (`123456789012-abcdef...`) gerçek olmadığı için
   Google Play Servisleri isteği reddediyor, uygulama `NoCredentialException`
   alıyor ve kullanıcıya "hesap bulunamadı" yazıyordu.

2. **APK her build'de farklı bir anahtarla imzalanıyordu.**
   Repoda sabit bir keystore yoktu, bu yüzden her CI koşucusu kendi geçici
   debug anahtarını üretiyordu. Google girişi, uygulamanın imza sertifikası
   SHA-1 değerinin Firebase Console'da kayıtlı olmasını şart koşar; her build'de
   değişen bir SHA-1 kaydedilemez. Yani 1. madde düzeltilse bile giriş yine
   `DEVELOPER_ERROR` ile patlardı.

## Yapılan düzeltmeler

| # | Değişiklik | Dosya |
|---|---|---|
| 1 | Sabit debug keystore repoya eklendi (PKCS12, alias `androiddebugkey`, parola `android`) | `debug.keystore` |
| 2 | Keystore tipi magic baytlardan tespit ediliyor (PKCS12 `3082` / JKS `FEEDFEED`); yanlış `storeType` imzalamayı patlatır | `app/build.gradle.kts` |
| 3 | CI artık gerçek `google-services.json`'ı **koruyor**; yalnızca dosya yoksa/geçersizse sahteye düşüyor ve bunu log'da ilan ediyor | `.github/scripts/prepare-google-services.sh`, `.github/workflows/ci.yml` |
| 4 | CI, ürettiği APK'yı hangi sertifikanın imzaladığını doğrulayıp annotation olarak basıyor | `.github/scripts/report-apk-signer.sh` |
| 5 | Giriş hataları kök nedene göre ayrıştırılıyor; sahte yapılandırmayla derlenmiş APK daha butona basılır basılmaz net mesajla reddediliyor | `app/src/main/java/com/example/data/cloud/AuthManager.kt` |

## ⚠️ Senin yapman gereken tek adım (bir kez)

Google girişi, imza SHA-1'inin Firebase'de kayıtlı olmasını ister. Aşağıdaki değeri
**Firebase Console → Proje ayarları → Genel → Android uygulaman
(`com.aistudio.nefszikir.kdhrmq`) → "Parmak izi ekle"** alanına birebir ekle:

```
SHA-1   : B8:63:99:47:20:3F:F0:78:DF:3F:7C:77:50:B7:34:2B:D7:BD:6F:58
SHA-256 : 00:E2:8C:77:1F:4E:06:0F:9D:7D:79:B1:18:3A:7B:11:1E:11:14:BC:5F:51:95:5D:D1:1F:1A:74:FB:74:2B:FC
```

Bu değer artık **hiç değişmez** — her CI build'i aynı `debug.keystore` ile imzalar.
CI log'unda `Report APK signing certificate` adımı bunu her build'de doğrular ve
"Firebase'e kaydedilecek SHA-1" başlıklı bir not basar.

Not: Firebase'de kayıtlı mevcut parmak izlerinden biri
`da39a3ee5e6b4b0d3255bfef95601890afd80709` — bu, **boş bir metnin** SHA-1'idir,
yani yanlışlıkla yapıştırılmış geçersiz bir kayıttır. Silmen güvenli.

## Doğrulama (CI'da okunabilir çıktı)

`build-apk-only` job'ının son iki adımı:

```
Fail loudly if no APK was produced
Report APK signing certificate (Firebase SHA-1 kaniti)
```

İkinci adım şunu basar:

```
APK        : app/build/outputs/apk/debug/app-debug.apk
APK SHA-1  : B8:63:99:47:...
Keystore   : debug.keystore (SHA-1: B8:63:99:47:...)
SONUC: APK, repodaki sabit debug.keystore ile imzalanmis. Imza build'den build'e DEGISMIYOR.
```

İmza keystore ile uyuşmazsa adım **uyarı** basar; yani sorun log'da gizlenmez.

## Kendi keystore'unu kullanmak istersen

`debug.keystore` dosyasını kendi JKS dosyanla değiştirmen yeterli: build.gradle
dosya tipini magic baytlardan okuyup `storeType` değerini kendisi seçer.
Parola/alias beklentisi: `android` / `androiddebugkey`.

## Release (Play Store) imzası

Release build'i hâlâ ortam değişkenleri ister:
`RELEASE_KEYSTORE_PATH`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`,
`RELEASE_KEY_PASSWORD`. Play'e çıkacak sürümde **o** anahtarın SHA-1'i de
Firebase'e eklenmelidir — debug keystore'un SHA-1'i release APK'da geçmez.
