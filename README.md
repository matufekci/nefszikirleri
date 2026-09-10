# Nefs Zikirleri - Zikir Takip Uygulaması

Kadiri tarikatı 15 Nefs Zikri için offline-first, Jetpack Compose ile yazılmış manevi takip uygulaması.

View in AI Studio: https://ai.studio/apps/c9228cef-058c-44d2-94cc-33e100b16bd8

**Versiyon**: 2.1 (versionCode 3) - Tüm temizlik hamleleri tamamlandı (1-15)

## Özellikler

- **15 Sabit Zikir**: Kelime-i Tevhid, Allah, Hu, Ya Hak, Ya Hay, Ya Kayyum, Ya Kahhar, Ya Vahid, Ya Aziz, Ya Vedud, Ya Vahhab, Ya Müheymin, Ya Basıt, Ya Rahman, Ya Rahim
- **Tertip Emniyeti**: Sıra zorunlu, önceki bitmeden sonrakine geçiş engellenir + FastJump ile toplu tamamlama
- **Sayaç**: +1 tap, quick add (1k/5k/10k), manual add/remove, undo, reset, geri/ileri sayım, tam ekran, Zen mode, haptic (light/medium/strong + 33/100 milestone), TalkBack semantics + minimum 48dp touch target
- **İstatistik**: SQL aggregate (günlük, streak, best streak), 7/30 gün ve 6 ay grafik, vakit dağılımı (seher/gündüz/akşam/gece), rozet sistemi, memory-hardened pagination (2000 chunk, observeHistoryPaged)
- **Hatırlatıcı**: 5'e kadar özel saat, 24h inaktivite, 21:00 hedef hatırlatıcı (remaining>0 check), adaptive manevi hatırlatıcı (WorkManager DailyEvaluationWorker 08:00, haftada max 4, günlük max 1, quota reservation+rollback), ghost notification koruması, POST_NOTIFICATIONS rationale + permanently denied -> app settings yönlendirme
- **Yedekleme**: Lokal şifreli + GZIP v2 (0x02) (AES/GCM 256, PBKDF2 120k, %70 sıkıştırma), v1 backward compat (0x01), FileProvider share, zip-bomb koruması (25MB backup, 50MB decompress), theme normalization (legacy emerald->hadra_gece), Firestore snapshot versioning (chunked history 500, revision conflict, merge/overwrite/remote çözümleri, garbage collection)
- **Çok Dil & Tema**: tr/ar/en/de/fr, RTL, 3 kanonik tema (hadra_gunduz/Beyaz, hadra_gece/Yeşil, siyah/Siyah) + 10 legacy + 8 alias (kudus/iznik/gul/amber/kandil/hadra_white/hadra/leyl), otomatik migration, fontScale 0.7-1.5 clamped, effectiveScale 0.7-2.0 (system*app), dokular
- **Güvenlik**: Firebase App Check (Play Integrity prod, Debug dev, token .env'den), Firestore rules strict (24 tema whitelist), backup_rules include-only prefs, ProGuard minimal precise keeps + log stripping

## Mimari

- **MVVM + StateFlow**, `SavedStateHandle`, Channel batching (40ms), `Mutex` + `withTransaction`, fencing via MonotonicTime.now()
- **Room** v8: zikirs, zikir_history (indices: zikirId, dateKey, timestamp, zikirId+dateKey, eventId unique), reminder_slots, app_settings, pending_operations. Migrations 1-8 + composite 1_4,2_4,1_6
- **Firebase**: Auth (CredentialManager, GoogleIdOption), Firestore (users/{uid}/snapshots/{id}/history chunk verification: hole/extra/timestamp mismatch), App Check
- **AlarmManager** + **BootReceiver** (BOOT_COMPLETED, MY_PACKAGE_REPLACED, TIMEZONE_CHANGED, TIME_SET) + **WorkManager** (08:00 hizalı periyodik) + **AdaptiveReminderManager** (synchronized quota)
- **CI**: GitHub Actions (unit tests Robolectric, lint, Room schema check, debug APK)

## Kurulum

1. Android Studio ile aç (JDK 17, AGP 9.1.1, Kotlin 2.2.10, compileSdk 36)
2. `.env.example` -> `.env` kopyala:
   - `GEMINI_API_KEY` (gerekirse)
   - `FIREBASE_APPCHECK_DEBUG_TOKEN` (logcat'te DebugAppCheckProvider token'i ara, Firebase Console -> App Check -> debug tokens ekle)
   - Release için `RELEASE_KEYSTORE_PATH` vb.
3. `app/google-services.json.example` -> `app/google-services.json` kopyala ve Firebase console'dan gerçek değerleri doldur
4. `debug.keystore` yoksa AGP otomatik üretir, veya `keytool -genkey ... -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android` ile üret
5. `./gradlew testDebugUnitTest` ile testler, sonra Run

## Güvenlik Notları

- `google-services.json` ve `.env` `.gitignore`'da, örnek dosyalar commitli
- Room `backup_rules.xml` & `data_extraction_rules.xml` include-only prefs, DB hariç (otomatik yedeklemeden hariç)
- Firestore rules: slots <=5, zikir validasyonu, snapshot koleksiyonu için ayrı kurallar, theme whitelist 24 eleman senkron (Color.kt + validator + rules)
- Backup: v2 GZIP + AES/GCM, zip-bomb koruması (50MB decompress, 25MB backup limit), WrongPasswordException vs PasswordRequiredException ayrımı
- App Check: debug token .env'den, prod Play Integrity, init non-fatal
- ProGuard: minimal keeps (Room entities, Moshi JsonClass, backup models, cloud, credentials, WorkManager), log stripping, optimization passes 5

## Temizlik Hamleleri (Bu Branch - Tamamlandı)

- **Hamle 1 (8f53113)**: .gitignore, junk silme, debug signing fallback, google-services.example
- **Hamle 2 (a1dbf10)**: Veri bütünlüğü (Mutex+withTransaction, atomic snapshot, fence/cancel), BootReceiver 4 action, firestore rules snapshot, WorkManager
- **Hamle 3 (f676e00)**: Backup GZIP v2, zip-bomb, Worker cleanup (pending 7 gün, temp backups), AuthManager fail-fast, fontScale clamp
- **Hamle 4 (3e8972e)**: Safe settings Mutex, .env.example, README rewrite
- **Hamle 5 (3fedaa9)**: Notification hardening (empty slots, isSlotActive ghost fix, target remaining>0)
- **Hamle 6 (e520d99)**: Tema tutarlılığı (3 kanonik + 18 alias, normalizeId, AppearanceSection isSelected fix, default hadra_gece, validator & rules senkron)
- **Hamle 7 (4581955)**: Settings validation (theme/language/texture/haptic whitelist, fontScale coerce), theme migration (ensureInitialized legacy->canonical), restore normalize, backup import normalize
- **Hamle 8 (ProGuard)**: Minimal precise keeps, log stripping, optimization
- **Hamle 9 (CI)**: GitHub Actions workflow (unit tests, lint, Room schema check, debug APK), schemas 5-8.json mevcut
- **Hamle 10 (Backup Tests)**: BackupManagerCompressionTest (v2 roundtrip, compression size, wrong password WrongPasswordException, no password PasswordRequiredException, zip-bomb constants, legacy theme normalization 5 variant, large history 500)
- **Hamle 11 (Notification UX)**: POST_NOTIFICATIONS rationale + permanently denied -> openAppNotificationSettings (ACTION_APP_NOTIFICATION_SETTINGS fallback), appSettingsLauncher, prefs tracking notification_rationale_shown
- **Hamle 12 (App Check)**: libs.versions.toml appcheck-playintegrity + debug, build.gradle.kts implementation, NefsApplication initializeAppCheck (Debug provider debug builds, Play Integrity release, debug token reflection from BuildConfig, non-fatal)
- **Hamle 13 (Pagination)**: HistoryDao observeHistoryPaged, getHistoryByDatePagedDirect, getHistoryCountByDateDirect, getHistoryCountForZikirDirect (memory-hardened, 10k+ entries için)
- **Hamle 14 (a11y)**: AccessibilityHelper (MIN_TOUCH_TARGET 48dp, isExtremeFontScale, getAccessibleDescription), minimumTouchTarget Modifier, DhikrCircleSemantics zaten var (TalkBack role Button, stateDescription, liveRegion), Theme.kt effectiveScale 0.7-2.0 clamp
- **Hamle 15 (Version)**: versionCode 2->3, versionName 2.0->2.1, release signing error message iyileştirme (env var listesi), isShrinkResources true, .env.example detaylı yorumlar

## Test

```bash
./gradlew testDebugUnitTest --stacktrace
./gradlew lintDebug
./gradlew assembleDebug
```

Test dosyaları:
- `BackupManagerDataIntegrityTest` (15 zikir, history, slots, settings, corrupted json, invalid schema, negative values, strict validators)
- `BackupManagerCompressionTest` (v2 GZIP, compression size, wrong password, no password, zip-bomb constants, legacy theme normalization, large history 500)
- `DataAtomicityRegressionTest`, `ProductionRegressionSuiteTest`, `SyncManagerTest`, `AdaptiveReminderQuotaTest`, `NotificationAlarmReceiverTest`, `LargeHistoryMemoryHardeningTest`, `DeterministicHistoryOrderTest`

## Röntgen Raporu

Detaylı analiz için `RONTGEN_ANALIZ.md` dosyasına bakın (mimari, özellikler, 15 eksik, dosya haritası).

## Lisans

Özel proje, Kadiri manevi terbiye için.

