# UI FREEZE — Kaskatı Kural (2026-09-13)

Kullanıcı onayıyla **UI (görsel) tarafı dondurulmuştur**. Bu tarihten itibaren:

1. **YASAK:** Mevcut ekranların görünümü, yerleşimi, renkleri, kart/buton biçimleri,
   animasyonların görsel karakteri üzerinde değişiklik yapılmaz.
   Yeni görsel bileşen, yeni ekran, yeni tema EKLENMEZ; mevcutlar taşınmaz/yeniden biçimlendirilmez.
2. **SERBEST:** UX (akış, gecikme hissi, geri bildirim netliği, erişilebilirlik),
   performans, arka plan doğruluğu, hata düzeltmeleri ve **dead-code temizliği**.
3. Temizlik sırasında bir görsel davranışın değişmesi gerekiyorsa önce kullanıcıya sorulur.
4. Bu dosya, kod incelemelerinin "hakem" belgesidir: bir değişiklik UI'yi değiştirmiyor
   ama kullanıcıya hissedilir bir UX kazanımı sağlamıyorsa yine de ancak temizlik/performans
   başlığı altındaysa kabul edilir.

Not: Hata düzeltmesi görsel sonuç doğursa bile (örn. ham şablon metni gösterimi) düzeltme
"bug fix" sayılır ve serbesttir; keyfî stil değişikliği sayılmaz.

---

## Temizlik kaydı (görsel değişiklik içermeyen kaldırma/düzeltmeler)

Hakem belgesi olarak: aşağıdaki değişiklikler "görsel değişiklik değil, ölü kod / arka plan
doğruluğu" kapsamında yapıldı. Bir satırın görsel etkisi olduğunu düşünüyorsan bu tabloya bak.

| Tarih | Değişiklik | Gerekçe |
|---|---|---|
| 2026-09-13 | `ui/screens/PermissionPage.kt`, `ui/components/DhikrTopBar.kt`, `ui/components/QuickAccessDrawer.kt` silindi | Hiçbir ekrandan çağrılmıyordu |
| 2026-09-13 | 3 ölü tema paleti, 4 `ThemeStyle` dalı, 10 renk alias, 9 composable, 4 VM fonksiyonu, 2 `ReminderDao` metodu, 140 ölü `AppStrings` alanı | Tanımsız/erişilmez kod |
| 2026-09-14 | `util/AccessibilityHelper.kt` silindi | `object AccessibilityHelper`, `Modifier.minimumTouchTarget`, `shouldUseCompactLayout`, `ComposableHelper` — **hiçbirinin çağrısı yok** (`shouldUseCompactLayout` sabit `false` döndüren bir taslaktı). Dokunma hedefini gerçekten değiştirmek görsel etki doğuracağı için freeze kapsamında "bağla" değil "kaldır" seçildi. `util/MotionUtils.kt` **duruyor**: `rememberShouldReduceMotion` 5 UI dosyasında canlı. |
| 2026-09-14 | `AboutSection` sürüm etiketi `BuildConfig.VERSION_NAME`'e bağlandı | Sabit `"v2.0"` yazıyordu, `versionName = "2.1"` — uygulama kendi sürümünü yanlış gösteriyordu (bug fix) |
| 2026-09-14 | `BackupPasswordDialog` parola etiketi 5 dile alındı | Sabit `"Parola"` metni Arapça/Almanca/Fransızca kullanıcıya Türkçe görünüyordu |
| 2026-09-14 | `OverallStatsSection` / `SyncConflictDialog` / `CloudSection`: satır başına üretilen `SimpleDateFormat` → `remember` | Performans; desen ve locale aynı, görünen metin aynı |
| 2026-09-14 | `AppThemeColors`'tan 5 ölü alan (`name`, `success`, `glowColor`, `themeStyle`, `specularIntensity`) + ölü `ThemeStyle` enum'u silindi | Hiçbiri okunmuyordu; tema adları zaten `UiText.theme*` üzerinden 5 dilde geliyor. Renk değerleri değişmedi |
| 2026-09-14 | Karşılama alt barında `contentDescription` sabit `"Geri"`/`"İleri"` → görünen 5 dilli etiketle birleştirildi | **Erişilebilirlik bug fix:** TalkBack kullanıcısı dili ne olursa olsun Türkçe duyuyordu. Görünen metin birebir aynı |
| 2026-09-14 | Ölü `UndoSnapshot` data class'ı silindi | Geri-al katmanı kaldırıldığından beri hiçbir yerde kullanılmıyordu |
| 2026-09-14 | En uzun seri (best streak) hesabı `StreakCalculator`'a taşındı | **Bug fix:** milisaniye bölmesi DST bahar geçişinde (Europe/Berlin: 31.03→01.04 = 23 saat) seriyi yanlışlıkla koparıyordu; tek günlük geçmişte 0 veriyordu. Doğru değerler ekranda artık farklı görünebilir — bu bir düzeltmedir, stil değişikliği değil |
| 2026-09-14 | `DailyEvaluationWorker` bildirimine `open_tab = "zikir"` eklendi | **Bug fix:** adaptif "manevi hareketsizlik" bildirimine dokununca uygulama son açık sekmede kalıyordu; diğer iki bildirim kaynağı zaten sayaç sekmesine iniyordu. Sözleşme `NotificationDeepLinkConsistencyTest` ile kilitlendi |
| 2026-09-14 | `DailyEvaluationWorker.checkStreakStatus` + `ZikirHistoryDao.getDistinctActiveDatesDirect` silindi | **Arka plan temizliği:** worker her çalıştığında tüm history tablosundan ayrı günleri çekip seri hesaplıyor, sonucu yalnızca `Log.d`'ye yazıyordu. Görünen seri zaten `ZikirViewModel` + `StreakCalculator` ile hesaplanıyor; ikinci (DST-güvensiz) kopya bakım tuzağıydı |
| 2026-09-14 | `ZikirHistoryDao`'dan 6 ölü metot silindi (`observeHistoryPaged`, `getHistoryByDatePagedDirect`, `getHistoryCountByDateDirect`, `getHistoryCountForZikirDirect`, `getTodayRecitedForZikirDirect`, `get30DayAverageDirect`) | "Hamle 13 pagination" için eklenmiş ama hiçbir zaman bağlanmamışlardı; uygulamanın kullandığı canlı çift `getHistoryPagedDirect` + `getHistoryCountDirect` |
| 2026-09-14 | `getAllHistoryInChunksDirect` artık kopya gövde taşımıyor, `...Internal`'ı sarıyor; iki sınıf-içi üye `private` yapıldı | Sayfalama mantığı tek yerde; bakım kayması riski kalktı |
| 2026-09-14 | `AppStrings`'ten 4 ölü çeviri alanı silindi (`backBtn`, `confirmAction`, `deleteBtn`, `nextBtn`) | Hiçbir ekran okumuyordu; 5 dilde toplam 20 ölü metin gitti. Görünen metin değişmedi |
| 2026-09-14 | Ölü `DhikrDataValidator.validateZikir` (esnek sürüm) silindi | Hiçbir çağıranı yoktu; kullanılan sürümler `validateZikirStrict` (bulut) ve diğer esnek doğrulayıcılar. README + RÖNTGEN_ANALİZ'de silinen DAO metotlarını anlatan bayat satırlar da gerçeğe çekildi |
| 2026-09-14 | Ölü splash zinciri kaldırıldı: `Theme.App.Starting` stili, `ic_splash_logo`, `splash_bg` rengi (colors.xml) ve `androidx.core:core-splashscreen` bağımlılığı | Kütüphane bağlıydı ama `installSplashScreen()` hiç çağrılmıyordu ve stil hiçbir yerde uygulanmıyordu (manifest `Theme.MyApplication` kullanıyor). Görünen splash değişmedi: o zaten Compose tarafında (`MainApp.kt`, 3000ms) |
| 2026-09-14 | Bağımlılık dalgası: 26 kütüphane güncellendi, 11 ölü bağımlılık silindi (retrofit/okhttp/camera/datastore/navigation/konum), compileSdk 36.1 → 37, AGP 9.4.0 + Gradle 9.7.1 | Hiçbir arayüz kodu değişmedi. `targetSdk` 36'da bırakıldı (targetSdk yeni çalışma zamanı davranışlarına sokar, compileSdk yalnızca derleme yüzeyini genişletir); minSdk 24 aynı, desteklenen cihaz aralığı değişmedi. compose-bom bilerek 2024.09.00'de: iki yıllık arayüz kütüphanesi farkı görünümü değiştirebilir |
| 2026-09-14 | `AppearanceSection` yazı ölçeğini `LocalConfiguration`'dan okuyor; `AnimatedIconSplash` ikonu `ContextCompat.getDrawable` ile yüklüyor; 21 `quadraticBezierTo` → `quadraticTo`; `ZikirViewModel.onCleared` içindeki boş `super` çağrısı silindi | Dördü de aynı davranışı veren eşdeğer çağrılar: lint hataları ve 21 kullanımdan kalkmış API uyarısı giderildi, çizim geometrisi ve görünüm birebir aynı |
| 2026-09-14 | `kotlin = 2.2.10` sürümü katalogda gerekçesiyle sabitlendi | AGP 9.4.0'ın POM'u gömülü Kotlin olarak 2.2.10 taşıyor; compose derleyici eklentisi bu sürümle eşleşmek zorunda. Lint'in 2.4.20 önerisi uygulanırsa derleme kırılır |
| 2026-09-15 | **compose-bom 2024.09.00 → 2026.09.00** (kullanıcı onayıyla) ve APK artifact adlarına Compose sürümü işlendi | İki yıllık arayüz kütüphanesi farkı. CI doğruladı: 4 görev yeşil, 182 test geçti, lint 0 fatal/0 error. **Görsel fark yalnızca cihazda görülür** — bu yüzden ayrı APK üretildi: `release-apk-compose-2026.09.00`. Eski sürümle karşılaştırmak için önceki run'ın `release-apk-compose-2024.09.00` artifact'ı kullanılabilir. Release APK boyutu 5.470.479 → 5.734.257 bayt (+%4,8) |
