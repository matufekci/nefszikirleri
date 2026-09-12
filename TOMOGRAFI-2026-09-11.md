# Uygulama Tomografisi — 2026-09-11

Kapsam: `app/src/main/java/com/example/**` (~23.700 satır). Yöntem: katman katman
kod okuma + desen taraması + CI birim testleri. Bu belge yalnızca **araçlarla
doğrulanmış** bulguları içerir; doğrulanamayanlar "Açık Riskler" altında işaretlidir.

---

## 0) Bu turda düzeltilen istek: "+1'de rakam anlık geri düşüyor"

**Kök neden (doğrulandı):** `incrementCount` optimistic güncelleme yapıyor (ekran hemen
N+1), ama kalıcı yazma 40 ms'lik toplu pencereden geçiyor. Bu arada Room'dan gelen bir
emission HAM (henüz artmamış) sayıyı getiriyor ve `combine` bloğu `_uiState`'i DB değeriyle
eziyordu → ekran `N+1 → N → N+1` titriyordu; hızlı dokunuşta her toplu işlemde tekrarlanıyordu.

**Çözüm (`a3380a9`):** `_pendingIncrements: MutableStateFlow<Map<Int,Long>>` eklendi.
- `+1`'de katman artar;
- toplu işlem DB'ye düşünce worker katmandan düşer;
- `combine` artık DB değerine bekleyen katmanı ekler **ve** katmanı bir combine kaynağı olarak
  izler → her emission doğru toplamı gösterir, sayı asla geriye düşmez, DB'ye yakınsar.
- `decrement/undo/reset` doğrudan DB'ye yazdığı için katman onları etkilemez; gerçek azalmalar
  ekrana normal yansır.

CI: 4 job success, 126 test / 0 failure.

---

## 1) Katman katman bulgular

### Veri katmanı (Repository / DAO / fence)
- `applyBatchOperations` tek transaction içinde: fence kontrolü → geçerli op'ları grupla →
  `incrementZikirCount` + tek aggregate history kaydı → `applied`. **Tutarlı.** Batch'in tek
  history kaydı olarak yazılması, `undoLastAction`'ın tüm batch'i tek seferde geri almasını
  sağlar (doğru davranış).
- `removeDhikrCount / undoLastAction / resetSingleZikir / resetAllZikirs` fence +
  `cancelPendingForZikir` kullanıyor; bekleyen op'lar asla sonradan uygulanamıyor. **Tutarlı.**
- `getEffectiveFence = max(zikirFences[id], globalFence)`. Restore işlemleri fence'i
  ileriden kurup `cancelAllPending` çağırıyor → restore sonrası eski pending op'lar veri
  bozamaz. **Tutarlı.**

### Bulut (SyncManager / AuthManager)
- Yazıcı `null` zaman damgalarını `0` yazar; okuyucu eskiden `0`'ı bozuk sayıp reddederdi
  (uygulama kendi yedeğini okuyamazdı). Düzeltildi: `0 → null` normalize, **negatif** hâlâ
  bozuk (`bfd6d60`). Throw bekleyen 3 birim test korunuyor (CI 23/0).
- Giriş akışı artık bulut-öncelikli (`a5690c8`): önce oku → yoksa yereli yükle → yerel boşsa
  otomatik geri yükle → ikisi de doluysa **sor**. `SyncConflictDialog` artık gerçekten render
  ediliyor (eskiden `_syncConflictState` hiç izlenmiyordu).
- `restoreFromCloud` çakışmada artık `onComplete(false, msg)` dönüyor (eskiden yalan `true`).

### Geri yükleme sonrası seçim (`50690cf`)
- `combine` çözülen id'yi `savedStateHandle`'a geri yazıp `savedId` settings'i gölgeliyordu;
  taze kurulumda ilk emission `1` yazdığı için ekran ilk zikire kilitleniyordu.
  `alignSelectionAfterRestore()` her tam geri yüklemede seçimi "son çekilen zikir"e hizalayıp
  hem `savedStateHandle`'a hem DB settings'e yazıyor.

### ViewModel sayaç hattı
- Optimistic + channel batching + fence birbirini doğru tamamlıyor (yukarıdaki flicker
  düzeltmesiyle ekran artık DB gecikmesinden etkilenmiyor).
- `SelectedZikirResolver` hızlı-intikal yarışını çözüyor; dokunulmadı (testleri 10/0).

### UI / i18n
- `ui/` altında sabit Türkçe `Text(` taraması **boş** → kullanıcıya görünen metinler
  `AppStrings`/`UiText` üzerinden 5 dilde. (Kural ihlali kalmadı.)
- `SyncConflictDialog` 5 dile çevrildi ve bulut yedeği tarihini gösteriyor.

### Tarih/saat
- `NumberFormatter.getDateKey` cihaz-yerel saat dilimini tutarlı kullanıyor; tek cihazda
  günlük agregasyon doğru. **Kabul edilebilir.**

---

## 2) Açık Riskler (buradan doğrulanamaz — eylem gerektirir)

1. **Firestore App Check zorlaması:** debug build `DebugAppCheckProviderFactory` kullanır;
   otomatik üretilen token konsola kayıtlı değilse ve zorlama AÇIKSA bulut çağrıları reddedilir.
   Play yayınana kadar App Check'i **Unenforced** tutun. (Kod tarafı doğrulandı; konsol tarafı sizde.)
2. **Sahte Firebase parmak izi `da39a3ee…` (boş string'in SHA-1'i)** konsoldan silinmeli.
3. **Play release anahtarı:** mevcut APK'ler committed debug keystore ile imzalı
   (`b8639947203ff078df3f7c7750b7342bd7bd6f58`). Play'e çıkarken `RELEASE_*` ile kendi
   anahtarınızı verin ve o anahtarın SHA-1'ini Firebase'e ekleyin.
4. **Buluttaki eski verinin varlığı** ancak Firebase Console → Firestore →
   `users/<uid>/snapshots` ile görülür. Önceki sürümlerdeki sessiz yükleme kopyayı ezmiş olabilir.
5. **Cihaz/emülatör testi yok:** geçiş hissi, ışıltı, flicker'ın cihazda kaybolması ancak
   gerçek kurulumla teyit edilir. CI yalnızca derleme + JVM birim testlerini kanıtlar.

---

## 3) Bu oturumda push edilen commit zinciri (hepsi CI-yeşil)

| Commit | Konu |
|---|---|
| `4d408a3` | i18n bütünlüğü + zen temelleri + manevi bildirim + sade bilgi sekmesi |
| `0843f55` | Crossfade if/else tam sarma (derleme hatası düzeltmesi) |
| `9f78b74` | `statusBarsIgnoringVisibility` @OptIn (derleme hatası düzeltmesi) |
| `a5690c8` | girişte bulut-öncelikli senkron + çakışma diyaloğu render |
| `bfd6d60` | 0-sentinel normalizasyonu (kendi yedeğini okuyabilme) |
| `50690cf` | restore sonrası son çekilen zikir ekranda |
| `a3380a9` | +1 flicker düzeltmesi (bekleyen artış katmanı) |
| `eb419e6` | senkron sayaç (titreme kökten) + restore seçim gölgelemesi kaldırma |

---

## 4) Cihaz doğrulamaları ve konsol çözümleri (11.09.2026 gecesi)

- **PERMISSION_DENIED ("Buluta Yedekle") ÇÖZÜLDÜ — kod değil, konsol:** kök neden
  eski Firestore kurallarıydı; (a) kurallar hiyerarşik olmadığı için
  `snapshots/` ve `history/` alt koleksiyonlarını kapsamıyordu ve (b) eski kural
  `users/<uid>` yazmasını `isValidSnapshot` payload doğrulamasına bağlıyordu —
  oysa uygulama artık kullanıcı belgesine tam snapshot değil pointer yazıyor.
  Repo köküne `firestore.rules` eklendi (sadece sahiplik: `request.auth.uid ==
  userId`, üç seviye de kapsamlı); kullanıcı konsolda yayınladı. Rules
  Playground'da "Simulated write allowed" ve cihazda "Zikirleriniz buluta
  başarıyla yedeklendi!" + Son Eşitleme 22:03 ile doğrulandı.
- **Flicker (`eb419e6`) cihazda doğrulanmayı bekliyor:** CI 126 test yeşil;
  sayaç artık tek Room transaction'dan tek emission aldığı için yapısal olarak
  titreyemez — cihaz teyidi kullanıcıda.
- **Restore sonrası seçim (`eb419e6`):** `savedStateHandle` gölgelemesi
  kaldırıldı; aynı oturumda son zikir açılması beklenir — cihaz teyidi kullanıcıda.
- **Açık Risk 1 (App Check Unenforced) hâlâ geçerli:** Play yayınına kadar
  Unenforced kalması bilinçli tercih; yayın anında enforcement açılıp SHA-1
  (`b863994…`) konsola kaydedilmeli.

---

## 5) Tempo matematiği ve ayarsız adaptif bildirimler (12.09.2026)

- **Program gerçeği:** 15 zikir, toplam **1.140.000** (100k×3 + 70k×7 + 60k + 50k + 40k + 100k×2),
  hedef **6 ayda (182 gün)** bitirmek; en kötü ihtimalle **1 yılda 1 tur**.
- **Yeni formül (`AdaptiveReminderManager`):**
  `needDaily = clamp(ceil(kalan / (182 − geçenGün)), 3124, 5000)`.
  Alt sınır 3.124 = ceil(1.140.000/365) → bu tempoyla tur en geç 365 günde biter
  (3.123'te 366 güne taşar; sınır bu yüzden 3.124). Üst sınır 5.000 → kullanıcı
  asla günde 5 binden fazlasına zorlanmaz. Günlük ihtiyaç daima **3-5 bin bandında**.
- **Bantlar (avg7 = son 7 TAM günün ortalaması, boş gün 0 sayılır — katı):**
  ON_TRACK (≥%100) → haftada ≤1 müjde · MILD (≥%60) → ≤2 · BEHIND (≥%30) → ≤3 uyarı ağırlıklı ·
  CRITICAL (<%30) → ≤4 (günde 1, sıralı uyarı/müjde). Mutlak sınır: günde 1, haftada 4.
  Bant her gün çekilen zikirlere göre yeniden hesaplanır — "sürekli değişiklik" buradan gelir.
- **Ayarsızlık:** Tüm hatırlatıcı ayarları kaldırıldı (günlük slotlar, hedef hatırlatıcısı,
  hareketsizlik anahtarı). `reminderEnabled/inactivityAlertEnabled/targetReminderEnabled`
  kolonları yalnızca Room şema/yedek uyumluluğu için uykuda tutuluyor (bkz. hapticMilestoneMode emsali).
  Bildirim izni Android 13+'ta ilk açılışta bir kez MainApp'te isteniyor.
- **Hareketsizlik emniyet ağı katılaştı:** eşik 3→**2 gün**, tetik 4→**3 gün**
  (uygulama hiç açılmazsa 3. gün sıralı ayet bildirimi, ID 9999).
- **Günlük hedef bandı:** varsayılan 10.000→**4.000**, ayar aralığı **3.000–5.000**
  (eski değeri olan kullanıcı ilk ayarlamada banda çekilir).
- **Silinen altyapı:** slot/hedef/adaptif alarm tipleri, `scheduleDailyReminders`,
  `scheduleTargetReminder`, `getSlotRequestCode`, `DailyReminderMessages.kt`,
  VM slot fonksiyonları, `UiState.reminderSlots`, repository slot sarmalayıcıları,
  ayarlardaki hatırlatıcı UI'ı ve izin diyalogları. ReminderSlot tablosu/DAO'su ve
  yedek şeması bilinçli olarak korundu (mevcut yedekler bozulmasın).
- **Testler:** `AdaptivePaceMathTest` (8 saf birim testi: bant sınırları, 1-yıl garantisi,
  kota artışı), `NotificationSchedulerTest` (3 gün alarm + iptal + eşikler),
  `NotificationAlarmReceiverTest` güncellendi. Ayet dönüş testleri (5) değişmedi.
