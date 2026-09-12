# BİLDİRİM SİSTEMİ RAPORU — 12 Eylül 2026

Dal: `arena/01a08b35-nefszikirleri` (commit `a821f05`) · Kaynak: kodun birebir okunması
(`NefsApplication.kt`, `worker/DailyEvaluationWorker.kt`, `util/AdaptiveReminderManager.kt`,
`util/NotificationScheduler.kt`, `receiver/ReminderAlarmReceiver.kt`, `receiver/BootReceiver.kt`,
`ui/viewmodel/ZikirViewModel.kt`, `ui/MainApp.kt`)

Sistem **ayarsızdır**: bildirim temposunu kullanıcı ayarı değil, tempo matematiği belirler.
İki bildirim türü vardır; ikisi de aynı kanaldan gider ve Android 13+ için
`POST_NOTIFICATIONS` izni gerektirir (uygulama ilk açılışta sorar — `MainApp.kt`).

Ortak kanal: `dhikr_reminders_channel` · önem: DEFAULT · titreşim: açık
Ortak davranış: dokununca uygulamayı açar, dokununca kapanır (autoCancel), ikon = uygulama ikonu.
**Mutlak üst sınır: günde en fazla 1 bildirim** (iki tür birlikte bile).

---

## 1) GÜNLÜK TEMPO BİLDİRİMİ (DailyEvaluationWorker)

**Ne zaman:** Her gün **08:00**'de. Uygulama her açılışında WorkManager'a kurulur;
ilk çalışma bir sonraki 08:00'e hizalanır, sonra 24 saatte bir tekrarlar.

**Şartlar (HEPSİ sağlanırsa gönderilir):**
1. O gün henüz hiçbir bildirim gönderilmemiş (günde-1 kuralı)
2. En az bir zikir kaydı var ve kalan zikir > 0 (tur bitmişse sessiz)
3. Kullanıcı tura başlamış ve **ilk 3 gün geçmiş** (hoşgörü süresi — yeni başlayanı boğmaz)
4. Haftalık kota dolmamış (aşağıda)

**Matematik:**
- Toplam program: 1.140.000 zikir / 6 ay (182 gün); en kötü ihtimal kapısı 365 gün
- Günlük ihtiyaç = kalan ÷ (182 − geçen gün), **[3.124 – 5.000]** bandına kıstırılır
- Son 7 TAM günün ortalaması (avg7) hesaplanır: boş günler 0 sayılır; toplu/otomatik
  sıçramalar (>10.000) bilinçli tempoya dahil edilmez

**Bantlar ve haftalık kota (günde 1 sınırı saklı):**

| Bant | Şart (avg7 / ihtiyaç) | Haftada en fazla | İçerik |
|---|---|---|---|
| ON_TRACK | ≥ %100 | 1 | Müjde ayeti (rastgele) |
| MILD | %60–99 | 2 | Müjde ayeti (rastgele) |
| BEHIND | %30–59 | 3 | Uyarı ayeti (rastgele) |
| CRITICAL | < %30 | 4 | Hareketsizlik ayeti |

**İçerik:** Başlık = "adaptif hatırlatıcı" başlığı (seçili dilde); gövde =
`Sure adı + ayet metni` (5 dilde). Kullanıcının ayarladığı dilde gider.

> Not: Bu matematiğin 3–5 bin bandı YALNIZCA bildirim içindir; hedef seçim diyaloğu
> bundan bağımsızdır (1000/3000/5000/10000 — `a821f05` ile geri yüklendi).

---

## 2) HAREKETSİZLİK BİLDİRİMİ — emniyet ağı (ReminderAlarmReceiver)

**Ne zaman:** Son zikirden **3 gün sonrasına** kesin alarm kurulur
(`setAndAllowWhileIdle` — cihaz uyusa/Doze'da olsa çalar).

**Alarmı her seferinde SIFIRLAYAN olaylar** (yani aktif kullanıcıya asla çalmaz):
- Her zikir çekişinde (`ZikirViewModel`)
- Uygulama açılışında (`NefsApplication`)
- Cihaz açılışında / uygulama güncellemesinde (`BootReceiver`)
- Bildirim gönderildikten sonra bir sonraki döngü için kendisi

**Çaldığında:**
- Son **2 gün** içinde manuel zikir kaydı varsa → bildirim YOK, alarm sadece yeniden kurulur
- Kayıt yoksa → bildirim gönderilir: başlık = "hareketsizlik" başlığı (dilde),
  gövde = 5 uyarı + 5 müjde ayeti **sırayla dönüşümlü**; ardından +3 günlük yeni döngü kurulur

---

## 3) GÖNDERİLMEYENLER / KALDIRILMIŞ OLANLAR
- Eski slotlu günlük hatırlatıcı, hedef hatırlatıcısı ve eski adaptif alarm tipleri
  kodda artık işlenmez (Receiver tipini tanımaz, sessizce döner)
- Rozet / tur tamamlama / seri (streak) olayları bildirim DEĞİL, uygulama içi diyalogdur;
  Worker'daki streak kontrolü yalnız log üretir
- Yedek geri yükleme/sıfırlama sonrası tüm alarmlar temizlenir (`cancelAllScheduledAlarms`)

## 4) ÖZET TABLO
| Bildirim | Tetik | Zaman | En fazla | İçerik |
|---|---|---|---|---|
| Tempo | 08:00 değerlendirmesi + bant/kota şartları | günde 1 kez, 08:00 | banda göre haftada 1–4 | Bant ayeti (dilinde) |
| Hareketsizlik | Son zikirden 3 gün sessizlik | alarm anı (gün içinde herhangi bir saat) | günde 1 (genel kural) | Dönüşümlü uyarı/müjde ayeti |
