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
| 2026-09-14 | En uzun seri (best streak) hesabı `StreakCalculator`'a taşındı | **Bug fix:** milisaniye bölmesi DST bahar geçişinde (Europe/Berlin: 31.03→01.04 = 23 saat) seriyi yanlışlıkla koparıyordu; tek günlük geçmişte 0 veriyordu. Doğru değerler ekranda artık farklı görünebilir — bu bir düzeltmedir, stil değişikliği değil |
