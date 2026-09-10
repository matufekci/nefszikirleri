# Nefs Zikirleri - Zikir Takip Uygulaması

Kadiri tarikatı 15 Nefs Zikri için offline-first, Jetpack Compose ile yazılmış manevi takip uygulaması.

View in AI Studio: https://ai.studio/apps/c9228cef-058c-44d2-94cc-33e100b16bd8

## Özellikler

- **15 Sabit Zikir**: Kelime-i Tevhid, Allah, Hu, Ya Hak, Ya Hay, Ya Kayyum, Ya Kahhar, Ya Vahid, Ya Aziz, Ya Vedud, Ya Vahhab, Ya Müheymin, Ya Basıt, Ya Rahman, Ya Rahim
- **Tertip Emniyeti**: Sıra zorunlu, önceki bitmeden sonrakine geçiş engellenir + FastJump ile toplu tamamlama
- **Sayaç**: +1 tap, quick add (1k/5k/10k), manual add/remove, undo, reset, geri/ileri sayım, tam ekran, Zen mode, haptic (light/medium/strong + 33/100 milestone)
- **İstatistik**: SQL aggregate (günlük, streak, best streak), 7/30 gün ve 6 ay grafik, vakit dağılımı (seher/gündüz/akşam/gece), rozet sistemi
- **Hatırlatıcı**: 5'e kadar özel saat, 24h inaktivite, 21:00 hedef hatırlatıcı, adaptive manevi hatırlatıcı (WorkManager DailyEvaluationWorker, haftada max 4, ayetli bildirim)
- **Yedekleme**: Lokal şifreli + GZIP (AES/GCM 256, PBKDF2 120k, v2 sıkıştırmalı), FileProvider share, Firestore snapshot versioning (chunked history 500, revision conflict, merge/overwrite/remote çözümleri)
- **Çok Dil & Tema**: tr/ar/en/de/fr, RTL, 3 tema (Beyaz/Yeşil/Siyah) + 10 legacy alias, fontScale 0.7-1.5, dokular

## Mimari

- **MVVM + StateFlow**, `SavedStateHandle`, Channel batching (40ms), `Mutex` + `withTransaction`
- **Room** v8: zikirs, zikir_history, reminder_slots, app_settings, pending_operations. Fencing via MonotonicTime
- **Firebase**: Auth (CredentialManager), Firestore (snapshots/{id}/history)
- **AlarmManager** + **BootReceiver** + **WorkManager** (08:00 hizalı periyodik)

## Kurulum

1. Android Studio ile aç
2. `.env.example` -> `.env` kopyala ve `GEMINI_API_KEY` doldur (gerekirse)
3. `app/google-services.json.example` -> `app/google-services.json` kopyala ve Firebase console'dan gerçek değerleri doldur
4. `debug.keystore` yoksa AGP otomatik üretir, veya `keytool -genkey ... -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android` ile üret
5. Run

## Güvenlik Notları

- `google-services.json` ve `.env` artık `.gitignore`'da, örnek dosyalar commitli
- Room backup_rules.xml ile otomatik yedeklemeden hariç, sadece prefs dahil
- Firestore rules: slots <=5, zikir validasyonu, snapshot koleksiyonu için ayrı kurallar
- Backup: v2 GZIP + AES/GCM, zip-bomb koruması (50MB decompress limit)

## Son Düzeltmeler (Bu Branch)

- .gitignore, junk dosya temizliği, debug signing fallback
- getAllHistoryInChunksDirect transactional, getAtomicSnapshot pending flush
- firestore.rules snapshot desteği
- BootReceiver tüm alarm tiplerini + WorkManager yeniden planlar
- NotificationScheduler synchronized prefs + exact alarm check
- BackupManager sıkıştırma + Worker cleanup (pending_operations 7 gün, temp backups)
- AuthManager hardcoded fallback kaldırıldı, Theme fontScale clamp, incrementSettingUsage JSONObject

## Test

`./gradlew test` ile unit testler (Robolectric, Room testing, Work testing)

## Lisans

Özel proje, Kadiri manevi terbiye için.
