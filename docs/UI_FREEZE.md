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
