package com.example.data.model

interface IUiTranslationsPart1 {
    val title: String
    val subtitle: String
    val tabZikir: String
    val tabListe: String
    val tabIstatistik: String
    val tabBilgi: String
    val tabAyarlar: String
    val target: String
    val totalDone: String
    val remainingZikir: String
    val perDay: String
    val undo: String
    val manualTitle: String
    val manualDesc: String
    val manualPlaceholder: String
    val addBtn: String
    val removeBtn: String
    val resetThis: String
    val resetAll: String
    val completedBadge: String
    val listTitle: String
    val statTotalRecited: String
    val recentActivityTitle: String
    val noActivityYet: String
    val dayUnit: String
    val calculating: String
    val sheikhTitle: String
    val sheikhName: String
    val notesTitle: String
    val note1: String
    val note2: String
    val note3: String
    val note4: String
    val language: String
    val themeTitle: String
    val counterPrefsTitle: String
    val countdownMode: String
    val countdownModeDesc: String
    val dailyTargetTitle: String
    val dailyTargetLabel: String
    val keepAwake: String
    val keepAwakeDesc: String
}

interface IUiTranslationsPart2 {
    val roundsTitle: String
    val roundsDesc: String
    val inactivityNotifTitle: String
    val hapticTitle: String
    val hapticOff: String
    val roundCompletedTitle: String
    val roundCompletedDesc: String
    val startNewRoundBtn: String
    val nextZikirBtn: String
    val mashaallah: String
    val congratsDesc: String
    val alhamdulillah: String
    val resetConfirm: String
    val resetAllConfirm: String
    val cancel: String
    val reset: String
    val save: String
    val zikirInfoModalTitle: String
    val shareCardBtn: String
    val closeBtn: String
    val zenMode: String
    val exitZenMode: String
    val fontScaleTitle: String
    val fontScaleSmall: String
    val fontScaleNormal: String
    val fontScaleLarge: String
    val fontScaleHuge: String
    val hapticTapLight: String
    val hapticTapMedium: String
    val hapticTapStrong: String
}

interface IUiTranslationsPart3 {
    val levelFinished: String
    val badgeTitle: String
    val recordCount: String
    val streakDay: String
    val perDayShort: String
    val heatmapTitle: String
    val heatmapLess: String
    val heatmapMore: String
    val badgeStatusUnlocked: String
    val badgeStatusLocked: String
    val badgeRequirement: String
    val badgeCategoryTerkip: String
    val badgeCategoryStreak: String
    val statusActive: String
    val statusWaiting: String
    val statusDone: String
    val terkipLevelTitle: String
    val badgesEarnedCount: String
    val bestStreakLabel: String
    val overallSpeed: String
    val totalRemaining: String
    val allTerkipFinish: String
    val streakCardTitle: String
    val range7Days: String
    val range30Days: String
    val range6Months: String
    val chartTitle: String
    val sequenceWarningTitle: String
    val sequenceWarningDesc: String
    val goToRequiredZikir: String
    val currentActiveDhikrLabel: String
    val understandClose: String
    val badgeCelebrationHeader: String
    val badgeCelebrationBlessing: String
    val celebrationContinueBtn: String
    val selectZikirPrompt: String
    val lockReasonMsg: String
    val exportStatsBackupBtn: String
    val exportStatsBackupTitle: String
    val exportStatsBackupDesc: String
    val exportStatsFileHeader: String
    val exportStatsChooserTitle: String
    val importStatsBackupBtn: String
    val importStatsBackupError: String
    val importStatsConfirmTitle: String
    val importStatsConfirmMsg: String
    val confirmAction: String
    val cancelAction: String
    val fastJumpTitle: String
    val fastJumpConfirmMsg: String
    val fastJumpBtn: String
    val logoutText: String
    val signOutDesc: String
    val cloudWelcomeMessage: String
    val cloudSignOutMessage: String
    val cloudSignInRequiredForBackup: String
    val cloudSignInRequiredForRestore: String
    val cloudBackupSuccess: String
    val cloudRestoreSuccess: String
    val cloudGenericSignInError: String
    val cloudGenericBackupError: String
    val cloudGenericRestoreError: String
    val cloudSyncTitle: String
    val collapse: String
    val expand: String
    val deleteBtn: String
    val appLogo: String
    val backBtn: String
    val nextBtn: String
    val selected: String
    val adaptiveReminderNotifTitle: String
    val spiritualVerses: List<SpiritualVerse>
}

data class SpiritualVerse(
    val surah: String,
    val verseText: String,
    val type: String // "warning" or "glad_tidings"
)

data class UiTranslationsPart1(
    override val title: String,
    override val subtitle: String,
    override val tabZikir: String,
    override val tabListe: String,
    override val tabIstatistik: String,
    override val tabBilgi: String,
    override val tabAyarlar: String,
    override val target: String,
    override val totalDone: String,
    override val remainingZikir: String,
    override val perDay: String,
    override val undo: String,
    override val manualTitle: String,
    override val manualDesc: String,
    override val manualPlaceholder: String,
    override val addBtn: String,
    override val removeBtn: String,
    override val resetThis: String,
    override val resetAll: String,
    override val completedBadge: String,
    override val listTitle: String,
    override val statTotalRecited: String,
    override val recentActivityTitle: String,
    override val noActivityYet: String,
    override val dayUnit: String,
    override val calculating: String,
    override val sheikhTitle: String,
    override val sheikhName: String,
    override val notesTitle: String,
    override val note1: String,
    override val note2: String,
    override val note3: String,
    override val note4: String,
    override val language: String,
    override val themeTitle: String,
    override val counterPrefsTitle: String,
    override val countdownMode: String,
    override val countdownModeDesc: String,
    override val dailyTargetTitle: String,
    override val dailyTargetLabel: String,
    override val keepAwake: String,
    override val keepAwakeDesc: String,
) : IUiTranslationsPart1

data class UiTranslationsPart2(
    override val roundsTitle: String,
    override val roundsDesc: String,
    override val inactivityNotifTitle: String,
    override val hapticTitle: String,
    override val hapticOff: String,
    override val roundCompletedTitle: String,
    override val roundCompletedDesc: String,
    override val startNewRoundBtn: String,
    override val nextZikirBtn: String,
    override val mashaallah: String,
    override val congratsDesc: String,
    override val alhamdulillah: String,
    override val resetConfirm: String,
    override val resetAllConfirm: String,
    override val cancel: String,
    override val reset: String,
    override val save: String,
    override val zikirInfoModalTitle: String,
    override val shareCardBtn: String,
    override val closeBtn: String,
    override val zenMode: String,
    override val exitZenMode: String,
    override val fontScaleTitle: String,
    override val fontScaleSmall: String,
    override val fontScaleNormal: String,
    override val fontScaleLarge: String,
    override val fontScaleHuge: String,
    override val hapticTapLight: String,
    override val hapticTapMedium: String,
    override val hapticTapStrong: String,
) : IUiTranslationsPart2

data class UiTranslationsPart3(
    override val logoutText: String,
    override val signOutDesc: String,
    override val levelFinished: String,
    override val badgeTitle: String,
    override val recordCount: String,
    override val streakDay: String,
    override val perDayShort: String,
    override val heatmapTitle: String,
    override val heatmapLess: String,
    override val heatmapMore: String,
    override val badgeStatusUnlocked: String,
    override val badgeStatusLocked: String,
    override val badgeRequirement: String,
    override val badgeCategoryTerkip: String,
    override val badgeCategoryStreak: String,
    override val statusActive: String,
    override val statusWaiting: String,
    override val statusDone: String,
    override val terkipLevelTitle: String,
    override val badgesEarnedCount: String,
    override val bestStreakLabel: String,
    override val overallSpeed: String,
    override val totalRemaining: String,
    override val allTerkipFinish: String,
    override val streakCardTitle: String,
    override val range7Days: String,
    override val range30Days: String,
    override val range6Months: String,
    override val chartTitle: String,
    override val sequenceWarningTitle: String,
    override val sequenceWarningDesc: String,
    override val goToRequiredZikir: String,
    override val currentActiveDhikrLabel: String,
    override val understandClose: String,
    override val badgeCelebrationHeader: String,
    override val badgeCelebrationBlessing: String,
    override val celebrationContinueBtn: String,
    override val selectZikirPrompt: String,
    override val lockReasonMsg: String,
    override val exportStatsBackupBtn: String,
    override val exportStatsBackupTitle: String,
    override val exportStatsBackupDesc: String,
    override val exportStatsFileHeader: String,
    override val exportStatsChooserTitle: String,
    override val importStatsBackupBtn: String,
    override val importStatsBackupError: String,
    override val importStatsConfirmTitle: String,
    override val importStatsConfirmMsg: String,
    override val confirmAction: String,
    override val cancelAction: String,
    override val fastJumpTitle: String,
    override val fastJumpConfirmMsg: String,
    override val fastJumpBtn: String,
    override val cloudWelcomeMessage: String,
    override val cloudSignOutMessage: String,
    override val cloudSignInRequiredForBackup: String,
    override val cloudSignInRequiredForRestore: String,
    override val cloudBackupSuccess: String,
    override val cloudRestoreSuccess: String,
    override val cloudGenericSignInError: String,
    override val cloudGenericBackupError: String,
    override val cloudGenericRestoreError: String,
    override val cloudSyncTitle: String,
    override val collapse: String,
    override val expand: String,
    override val deleteBtn: String,
    override val appLogo: String,
    override val backBtn: String,
    override val nextBtn: String,
    override val selected: String,
    override val adaptiveReminderNotifTitle: String,
    override val spiritualVerses: List<SpiritualVerse>
) : IUiTranslationsPart3

class UiTranslations(
    val p1: UiTranslationsPart1,
    val p2: UiTranslationsPart2,
    val p3: UiTranslationsPart3
) : IUiTranslationsPart1 by p1, IUiTranslationsPart2 by p2, IUiTranslationsPart3 by p3


object AppStrings {
    private val TR = UiTranslations(
        p1 = UiTranslationsPart1(
            title = "Nefs Zikirleri",
            subtitle = "Zikir Takip Uygulaması",
            tabZikir = "Zikir",
            tabListe = "Liste",
            tabIstatistik = "İstatistik",
            tabBilgi = "Bilgi",
            tabAyarlar = "Ayarlar",
            target = "Hedef",
            totalDone = "Toplam Çekilen",
            remainingZikir = "Kalan Zikir",
            perDay = "/gün",
            undo = "Geri Al",
            manualTitle = "Ekle",
            manualDesc = "Dışarıda çektiğiniz zikirleri ekleyebilir veya yanlış girilen miktarı çıkarabilirsiniz.",
            manualPlaceholder = "Zikir adedi giriniz...",
            addBtn = "+ Ekle",
            removeBtn = "− Çıkar",
            resetThis = "Sıfırla",
            resetAll = "Tümünü Sıfırla",
            completedBadge = "Tamamlandı",
            listTitle = "Zikir Listesi",
            statTotalRecited = "Çekilen Zikir",
            recentActivityTitle = "Son İşlemler",
            noActivityYet = "Henüz işlem kaydı bulunmamaktadır.",
            dayUnit = "Gün",
            calculating = "Hesaplanıyor",
            sheikhTitle = "Kadiri Dervişlerinin Hizmetçisi",
            sheikhName = "Seyyid Şeyh Muhammed Ruhi Kadiriyyul Hüseyni",
            notesTitle = "Notlar",
            note1 = "1- Bu zikirlerin her biri imkânlarınıza göre birkaç gün içinde bitirilir. Mesela 100.000 La ilahe illallah 10 gün içinde bitirilebilir.",
            note2 = "2- Bu zikri şerifleri yaparken vazife olarak bize verilen günlük zikirlerimizi de yapmalıyız inşallah.",
            note3 = "3- Bu zikirlerde nefsi tarafından, insan tarafından, cin tarafından veya herhangi bir sebepten dolayı sıkıntısı olan kardeşimiz, zikre başlamadan önce Cenab-ı Allah, Resulullah Efendimiz ﷺ ve Seyyid Abdülkadir Geylani Hazretleri ve Seyyid Ubeydullah Hazretleri sırası ile meşayıhten imdat ve destur istesin.",
            note4 = "4- Yılda bir defa yapılması çok iyidir.",
            language = "Dil Seçimi",
            themeTitle = "Tema Seçimi",
            counterPrefsTitle = "Sayıcı, Vird ve Hatırlatıcılar",
            countdownMode = "Geri Sayım Modu",
            countdownModeDesc = "Hedef sayıdan geriye doğru eksilterek sayar",
            dailyTargetTitle = "Günlük Hedef",
            dailyTargetLabel = "Günlük Zikir Miktarı",
            keepAwake = "Ekranı Sürekli Açık Tut",
            keepAwakeDesc = "Zikir sekmesindeyken ekranın kararmasını önler",
        ),
        p2 = UiTranslationsPart2(
            roundsTitle = "Tamamlanan Tur",
            roundsDesc = "15 Nefs Zikrinin tamamını bitirdiğinizde tur sayısı artar.",
            inactivityNotifTitle = "⚠️ Zikir Vaktiniz Geçiyor",
            hapticTitle = "Titreşimli Tesbih Hissi",
            hapticOff = "Titreşimi Kapat",
            roundCompletedTitle = "{0}. Tur Tamamlandı!",
            roundCompletedDesc = "15 zikrin tamamının hedefine başarıyla ulaştınız. Cenab-ı Hak virdinizi dergah-ı izzetinde kabul eylesin.",
            startNewRoundBtn = "🔄 Yeni Tura Başla",
            nextZikirBtn = "Sıradaki Zikre Geç",
            mashaallah = "Maşallah!",
            congratsDesc = "Bu mübarek zikrin hedefini muvaffakiyetle tamamladınız.",
            alhamdulillah = "Elhamdülillah",
            resetConfirm = "sayacı sıfırlansın mı?",
            resetAllConfirm = "Tüm zikir sayaçları sıfırlansın mı?",
            cancel = "Vazgeç",
            reset = "Sıfırla",
            save = "Kaydet",
            zikirInfoModalTitle = "Manevi Tecellileri ve Fazileti",
            shareCardBtn = "Manevi Kartı Paylaş",
            closeBtn = "Kapat",
            zenMode = "Odaklanma / Zen Modu",
            exitZenMode = "Zen Modundan Çık",
            fontScaleTitle = "Yazı Boyutu",
            fontScaleSmall = "Küçük",
            fontScaleNormal = "Normal",
            fontScaleLarge = "Büyük",
            fontScaleHuge = "Çok Büyük",
            hapticTapLight = "Hafif Tık",
            hapticTapMedium = "Orta",
            hapticTapStrong = "Güçlü",
        ),
        p3 = UiTranslationsPart3(
            levelFinished = "Tamamlananlar",
            badgeTitle = "Zikir & İstikrar Nişanları",
            recordCount = "Kayıt",
            streakDay = "Gün",
            perDayShort = "/ g",
            heatmapTitle = "Aktivite Haritası",
            heatmapLess = "Az",
            heatmapMore = "Çok",
            badgeStatusUnlocked = "Kazanıldı",
            badgeStatusLocked = "Kilitli",
            badgeRequirement = "Gereksinim",
            badgeCategoryTerkip = "Zikir Sırası",
            badgeCategoryStreak = "İstikrar ve Sebat",
            statusActive = "Çekiliyor",
            statusWaiting = "Sırada",
            statusDone = "Bitti",
            terkipLevelTitle = "Sıra",
            badgesEarnedCount = "{0} / {1} Kazanıldı",
            bestStreakLabel = "En uzun: {0} gün",
            overallSpeed = "Genel Hız",
            totalRemaining = "Kalan:",
            allTerkipFinish = "Zikirlerin Bitişi",
            streakCardTitle = "Seri",
            range7Days = "7 Gün",
            range30Days = "30 Gün",
            range6Months = "6 Ay",
            chartTitle = "İlerleme Grafiği",
            sequenceWarningTitle = "Zikir Sırası Uyarısı",
            sequenceWarningDesc = "{0}. zikre geçmeden evvel, önceki zikirlerin tamamlanması gereklidir.",
            goToRequiredZikir = "Kaldığım Zikre Git ({0})",
            currentActiveDhikrLabel = "Kaldığınız Zikir:",
            understandClose = "Anladım, Kapat",
            badgeCelebrationHeader = "Yeni Nişan Kazanıldı",
            badgeCelebrationBlessing = "Manevi seyrinizdeki bu sebat ve gayretinizi tebrik ederiz. Cenâb-ı Hak feyzinizi ve istikrarınızı daim eylesin.",
            celebrationContinueBtn = "Elhamdülillah (Devam Et)",
            selectZikirPrompt = "Bu Zikre Geç",
            lockReasonMsg = "Bu zikre geçebilmek için önceki zikirleri tamamlamalısınız.",
            exportStatsBackupBtn = "📤 İstatistik Yedeğini Dışa Aktar",
            exportStatsBackupTitle = "İstatistik Yedeği",
            exportStatsBackupDesc = "Zikir sayılarınızı ve istatistiklerinizi dosya olarak dışa aktarıp paylaşın.",
            exportStatsFileHeader = "Nefs Zikirleri - Zikir Çekim ve İstatistik Raporu",
            exportStatsChooserTitle = "İstatistik Yedeğini Paylaş",
            importStatsBackupBtn = "📥 Yedekten Geri Yükle",
            importStatsBackupError = "Yedek dosyası okunamadı veya format geçersiz!",
            importStatsConfirmTitle = "Yedeği Geri Yükle",
            importStatsConfirmMsg = "Yedek dosyasındaki veriler mevcut verilerin üzerine yazılacaktır. Devam edilsin mi?",
            confirmAction = "Geri Yükle",
            cancelAction = "İptal",
            fastJumpTitle = "Hızlı İntikal (Sıçrama)",
            fastJumpConfirmMsg = "Bu zikirden ({0}) mi devam etmek istiyorsunuz? Onaylarsanız, önceki tüm zikirler ({1}) otomatik olarak tamamlanmış sayılacak ve {0} zikrinin kilidi açılacaktır.",
            fastJumpBtn = "Öncekileri Tamamla ve Buradan Başla",
            logoutText = "Çıkış Yap",
            signOutDesc = "Oturumu Kapat",
            cloudWelcomeMessage = "Hoş geldiniz, {0}!",
            cloudSignOutMessage = "Oturum kapatıldı.",
            cloudSignInRequiredForBackup = "Bulut yedeklemesi için lütfen önce Google ile giriş yapın.",
            cloudSignInRequiredForRestore = "Buluttan geri yüklemek için lütfen önce Google ile giriş yapın.",
            cloudBackupSuccess = "Zikirleriniz buluta başarıyla yedeklendi!",
            cloudRestoreSuccess = "Buluttaki zikir ve ayarlarınız başarıyla geri yüklendi!",
            cloudGenericSignInError = "Google girişi başarısız oldu.",
            cloudGenericBackupError = "Yedekleme sırasında hata oluştu.",
            cloudGenericRestoreError = "Geri yükleme sırasında hata oluştu.",
            cloudSyncTitle = "Bulut Eşitleme",
            collapse = "Daralt",
            expand = "Genişlet",
            deleteBtn = "Sil",
            appLogo = "Nefs Zikirleri Amblemi",
            backBtn = "Geri",
            nextBtn = "İleri",
            selected = "Seçili",
            adaptiveReminderNotifTitle = "Son zamanlarda zikrini azalttın",
            spiritualVerses = listOf(
                SpiritualVerse(
                    surah = "Tâhâ Suresi, 124. Ayet",
                    verseText = "Kim Benim zikrimden (Kur'an'dan ve Beni anmaktan) yüz çevirirse, şüphesiz onun sıkıntılı (dar) bir geçimi olur ve kıyamet günü onu kör olarak haşrederiz.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Zuhruf Suresi, 36. Ayet",
                    verseText = "Kim Rahman'ın zikrinden (Kur'an'dan ve ilahi hatırlatmadan) göz yumarsa (yüz çevirirse), Biz ona bir şeytan musallat ederiz; artık o, onun kesintisiz arkadaşıdır.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Cinn Suresi, 17. Ayet",
                    verseText = "O'nun zikrinden (Kur'an'dan) yüz çevirenleri Allah, sarp ve şiddetli bir azaba sürükler.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "En'âm Suresi, 44. Ayet",
                    verseText = "Kendilerine hatırlatılanı (zikri) unuttuklarında, üzerlerine her şeyin kapılarını açtık. Nihayet kendilerine verilenler yüzünden şımardıkları an, onları ansızın yakaladık; birdenbire hepsi ümitsizliğe kapıldılar.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Münâfikûn Suresi, 9. Ayet",
                    verseText = "Ey iman edenler! Mallarınız da çocuklarınız da sizi Allah'ı zikretmekten alıkoymasın. Kim bunu yaparsa, işte onlar ziyana uğrayanların ta kendileridir.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Bakara Suresi, 152. Ayet",
                    verseText = "Öyleyse yalnız Beni anın ki Ben de sizi anayım. Bana şükredin, nankörlük etmeyin.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Ahzâb Suresi, 41-42. Ayetler",
                    verseText = "Ey iman edenler! Allah'ı çokça zikredin ve O'nu sabah akşam tesbih edip yüceltin.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Rad Suresi, 28. Ayet",
                    verseText = "Onlar, iman edenler ve kalpleri Allah'ın zikriyle huzura kavuşanlardır. Dikkat edin! Kalpler ancak Allah'ın zikriyle mutmain olur.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Ahzâb Suresi, 35. Ayet",
                    verseText = "Allah'ı çok zikreden erkekler ve çok zikreden kadınlar var ya; işte Allah onlar için bir mağfiret ve büyük bir mükâfat hazırlamıştır.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Ankebût Suresi, 45. Ayet",
                    verseText = "Şüphesiz Allah'ı zikretmek en büyük (ibadet)tir. Allah ne yaptığınızı çok iyi bilir.",
                    type = "glad_tidings"
                )
            )
        )
    )

    private val AR = UiTranslations(
        p1 = UiTranslationsPart1(
            title = "أذكار النفس",
            subtitle = "تطبيق متابعة الأذكار",
            tabZikir = "الذكر",
            tabListe = "القائمة",
            tabIstatistik = "الإحصائيات",
            tabBilgi = "معلومات",
            tabAyarlar = "الإعدادات",
            target = "الهدف",
            totalDone = "المجموع المنجز",
            remainingZikir = "الذكر المتبقي",
            perDay = "/يوم",
            undo = "تراجع",
            manualTitle = "إضافة يدوية",
            manualDesc = "يمكنك إضافة الأذكار التي تلوتها خارج التطبيق أو تصحيح الأعداد.",
            manualPlaceholder = "أدخل عدد الأذكار...",
            addBtn = "+ إضافة",
            removeBtn = "− طرح",
            resetThis = "إعادة ضبط هذا الذكر",
            resetAll = "إعادة ضبط الكل",
            completedBadge = "تم الإنجاز",
            listTitle = "قائمة الأذكار",
            statTotalRecited = "مجموع الأذكار",
            recentActivityTitle = "آخر العمليات",
            noActivityYet = "لا توجد سجلات بعد.",
            dayUnit = "أيام",
            calculating = "جاري الحساب",
            sheikhTitle = "خادم دراويش القادرية",
            sheikhName = "الشيخ السيد محمد روحي القادري الحسيني",
            notesTitle = "تنبيهات هامة",
            note1 = "١- يتم إنجاز كل ذكر من هذه الأذكار خلال عدة أيام حسب الاستطاعة. مثال: ١٠٠ ألف \"لا إله إلا الله\" يمكن إتمامها في ١٠ أيام.",
            note2 = "٢- ينبغي عدم إهمال الأوراد اليومية المعتادة أثناء أداء هذه الأذكار المباركة.",
            note3 = "٣- من كان يعاني من ضيق أو بلاء، فليطلب الإمداد والدستور من الله تعالى ثم رسوله ﷺ ومشايخ الطريقة قبل البدء.",
            note4 = "٤- يستحسن أداء هذا الختم المبارك مرة واحدة كل عام.",
            language = "اختيار اللغة",
            themeTitle = "المظهر والألوان",
            counterPrefsTitle = "العداد والورد والتنبيهات",
            countdownMode = "نمط العد التنازلي",
            countdownModeDesc = "يعد تنازلياً من الهدف حتى الصفر",
            dailyTargetTitle = "هدف الورد اليومي",
            dailyTargetLabel = "كمية الذكر اليومية",
            keepAwake = "إبقاء الشاشة مفعلة",
            keepAwakeDesc = "يمنع انطفاء الشاشة أثناء التلاوة",
        ),
        p2 = UiTranslationsPart2(
            roundsTitle = "الدورات المكتملة",
            roundsDesc = "عدد الدورات التي تم فيها إتمام الأذكار الـ 15 كاملة.",
            inactivityNotifTitle = "⚠️ مضى وقت الذكر",
            hapticTitle = "الاهتزاز اللمسي للسبحة",
            hapticOff = "إيقاف الاهتزاز",
            roundCompletedTitle = "اكتملت الدورة رقم {0}!",
            roundCompletedDesc = "تم بحمد الله ختم جميع الأذكار الـ 15 بنجاح. تقبل الله طاعتكم.",
            startNewRoundBtn = "🔄 بدء دورة جديدة",
            nextZikirBtn = "الانتقال للذكر التالي",
            mashaallah = "ما شاء الله!",
            congratsDesc = "تم بحمد الله إتمام هذا الورد المبارك بنجاح.",
            alhamdulillah = "الحمد لله",
            resetConfirm = "هل تريد تصفير هذا الذكر؟",
            resetAllConfirm = "هل تريد تصفير جميع الأذكار؟",
            cancel = "إلغاء",
            reset = "تصفير",
            save = "حفظ",
            zikirInfoModalTitle = "التجليات الروحية والفضائل",
            shareCardBtn = "مشاركة البطاقة الروحية",
            closeBtn = "إغلاق",
            zenMode = "وضع التركيز (Zen)",
            exitZenMode = "الخروج من وضع التركيز",
            fontScaleTitle = "حجم الخط",
            fontScaleSmall = "صغير",
            fontScaleNormal = "عادي",
            fontScaleLarge = "كبير",
            fontScaleHuge = "كبير جداً",
            hapticTapLight = "نقرة خفيفة",
            hapticTapMedium = "متوسط",
            hapticTapStrong = "قوي",
        ),
        p3 = UiTranslationsPart3(
            levelFinished = "أذكار مكتملة",
            badgeTitle = "أوسمة الاستمرار والتسلسل",
            recordCount = "سجلات",
            streakDay = "أيام",
            perDayShort = "/ يوم",
            heatmapTitle = "تقويم النشاط",
            heatmapLess = "قليل",
            heatmapMore = "كثير",
            badgeStatusUnlocked = "تم الإنجاز",
            badgeStatusLocked = "مغلق",
            badgeRequirement = "المتطلبات",
            badgeCategoryTerkip = "مرتبة التركيب",
            badgeCategoryStreak = "الاستمرار والثبات",
            statusActive = "قيد التلاوة",
            statusWaiting = "في الانتظار",
            statusDone = "مكتمل",
            terkipLevelTitle = "الذكر",
            badgesEarnedCount = "تم إنجاز {0} من {1}",
            bestStreakLabel = "أطول تتابع: {0} أيام",
            overallSpeed = "السرعة الإجمالية",
            totalRemaining = "المتبقي:",
            allTerkipFinish = "ختم الأذكار",
            streakCardTitle = "التتابع",
            range7Days = "٧ أيام",
            range30Days = "٣٠ يوم",
            range6Months = "٦ أشهر",
            chartTitle = "رسم التقدم",
            sequenceWarningTitle = "تنبيه ترتيب الأذكار والمقامات",
            sequenceWarningDesc = "وفقاً للآداب والترتيب الروحي المبارك، كل مرتبة هي تمهيد لما بعدها. يرجى إتمام الأوراد السابقة قبل الانتقال إلى المرتبة رقم {0}.",
            goToRequiredZikir = "الانتقال إلى الذكر المطلوب ({0})",
            currentActiveDhikrLabel = "الذكر الحالي المطلوب:",
            understandClose = "فهمت، إغلاق",
            badgeCelebrationHeader = "تم الحصول على وسام جديد",
            badgeCelebrationBlessing = "نهنئكم على هذا الثبات والاجتهاد في السير الروحي. نسأل الله أن يديم عليكم الفيض والاستقامة.",
            celebrationContinueBtn = "الحمد لله (متابعة)",
            selectZikirPrompt = "الانتقال لهذا الذكر",
            lockReasonMsg = "يجب عليك إتمام المراتب السابقة لفتح هذه المرتبة.",
            exportStatsBackupBtn = "📤 تصدير نسخة الإحصائيات",
            exportStatsBackupTitle = "نسخة الإحصائيات",
            exportStatsBackupDesc = "تصدير أعداد الأذكار والإحصائيات كملف ومشاركتها.",
            exportStatsFileHeader = "أذكار النفس - تقرير إحصائيات وأعداد الأذكار",
            exportStatsChooserTitle = "مشاركة نسخة الإحصائيات",
            importStatsBackupBtn = "📥 استعادة من نسخة احتياطية",
            importStatsBackupError = "فشل في قراءة ملف النسخة الاحتياطية أو التنسيق غير صالح!",
            importStatsConfirmTitle = "استعادة النسخة الاحتياطية",
            importStatsConfirmMsg = "سيتم استبدال البيانات الحالية ببيانات النسخة. هل تريد المتابعة؟",
            confirmAction = "استعادة",
            cancelAction = "إلغاء",
            fastJumpTitle = "الانتقال السريع (التقدم المباشر)",
            fastJumpConfirmMsg = "هل ترغب في المتابعة من هذه المرتبة ({0})؟ عند التأكيد، سيتم اعتبار جميع الأذكار السابقة ({1}) مكتملة وسيتم فتح {0} مباشرة.",
            fastJumpBtn = "إكمال ما سبق والبدء من هنا",
            logoutText = "تسجيل الخروج",
            signOutDesc = "تسجيل الخروج",
            cloudWelcomeMessage = "مرحباً بك، {0}!",
            cloudSignOutMessage = "تم تسجيل الخروج بنجاح.",
            cloudSignInRequiredForBackup = "يرجى تسجيل الدخول باستخدام Google أولاً للنسخ الاحتياطي السحابي.",
            cloudSignInRequiredForRestore = "يرجى تسجيل الدخول باستخدام Google أولاً للاستعادة من السحابة.",
            cloudBackupSuccess = "تم حفظ أذكارك وإعداداتك في السحابة بنجاح!",
            cloudRestoreSuccess = "تمت استعادة أذكارك وإعداداتك من السحابة بنجاح!",
            cloudGenericSignInError = "فشل تسجيل الدخول باستخدام Google.",
            cloudGenericBackupError = "حدث خطأ أثناء النسخ الاحتياطي السحابي.",
            cloudGenericRestoreError = "حدث خطأ أثناء الاستعادة من السحابة.",
            cloudSyncTitle = "المزامنة السحابية",
            collapse = "طي",
            expand = "توسيع",
            deleteBtn = "حذف",
            appLogo = "شعار أذكار النفس",
            backBtn = "رجوع",
            nextBtn = "التالي",
            selected = "محدد",
            adaptiveReminderNotifTitle = "لقد قللت من أذكارك مؤخراً",
            spiritualVerses = listOf(
                SpiritualVerse(
                    surah = "سورة طه، الآية ١٢٤",
                    verseText = "وَمَنْ أَعْرَضَ عَن ذِكْرِي فَإِنَّ لَهُ مَعِيشَةً ضَنكًا وَنَحْشُرُهُ يَوْمَ الْقِيَامَةِ أَعْمَىٰ",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "سورة الزخرف، الآية ٣٦",
                    verseText = "وَمَن يَعْشُ عَن ذِكْرِ الرَّحْمَٰنِ نُقَيِّضْ لَهُ شَيْطَانًا فَهُوَ لَهُ قَرِينٌ",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "سورة الجن، الآية ١٧",
                    verseText = "وَمَن يُعْرِضْ عَن ذِكْرِ رَبِّهِ يَسْلُكْهُ عَذَابًا صَعَدًا",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "سورة الأنعام، الآية ٤٤",
                    verseText = "فَلَمَّا نَسُوا مَا ذُكِّرُوا بِهِ فَتَحْنَا عَلَيْهِمْ أَبْوَابَ كُلِّ شَيْءٍ حَتَّىٰ إِذَا فَرِحُوا بِمَا أُوتُوا أَخَذْنَاهُم بَغْتَةً فَإِذَا هُم مُّبْلِسُونَ",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "سورة المنافقون، الآية ٩",
                    verseText = "يَا أَيُّهَا الَّذِينَ آمَنُوا لَا تُلْهِكُمْ أَمْوَالُكُمْ وَلَا أَوْلَادُكُمْ عَن ذِكْرِ اللَّهِ ۚ وَمَن يَفْعَلْ ذَٰلِكَ فَأُولَٰئِكَ هُمُ الْخَاسِرُونَ",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "سورة البقرة، الآية ١٥٢",
                    verseText = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "سورة الأحزاب، الآيات ٤١-٤٢",
                    verseText = "يَا أَيُّهَا الَّذِينَ آمَنُوا اذْكُرُوا اللَّهَ ذِكْرًا كَثِيرًا ۝ وَسَبِّحُوهُ بُكْرَةً وَأَصِيلًا",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "سورة الرعد، الآية ٢٨",
                    verseText = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "سورة الأحزاب، الآية ٣٥",
                    verseText = "وَالذَّاكِرِينَ اللَّهَ كَثِيرًا وَالذَّاكِرَاتِ أَعَدَّ اللَّهُ لَهُم مَّغْفِرَةً وَأَجْرًا عَظِيمًا",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "سورة العنكبوت، الآية ٤٥",
                    verseText = "وَلَذِكْرُ اللَّهِ أَكْبَرُ ۗ وَاللَّهُ يَعْلَمُ مَا تَصْنَعُونَ",
                    type = "glad_tidings"
                )
            )
        )
    )

    private val EN = UiTranslations(
        p1 = UiTranslationsPart1(
            title = "Nafs Dhikr",
            subtitle = "Spiritual Dhikr Tracker",
            tabZikir = "Dhikr",
            tabListe = "List",
            tabIstatistik = "Statistics",
            tabBilgi = "Info",
            tabAyarlar = "Settings",
            target = "Target",
            totalDone = "Total Recited",
            remainingZikir = "Remaining Dhikr",
            perDay = "/day",
            undo = "Undo",
            manualTitle = "Manual Add Dhikr",
            manualDesc = "You can add counts done outside the app or correct mistaken entries.",
            manualPlaceholder = "Enter amount...",
            addBtn = "+ Add",
            removeBtn = "− Remove",
            resetThis = "Reset This Dhikr",
            resetAll = "Reset All",
            completedBadge = "Completed",
            listTitle = "Dhikr List",
            statTotalRecited = "Total Recited",
            recentActivityTitle = "Recent Activity",
            noActivityYet = "No activity recorded yet.",
            dayUnit = "Days",
            calculating = "Calculating",
            sheikhTitle = "Servant of the Qadiri Dervishes",
            sheikhName = "Sheikh Sayyid Muhammad Ruhi Al-Qadiri Al-Husayni",
            notesTitle = "Important Notes",
            note1 = "1- Each of these dhikrs is completed within several days according to your capacity. For instance, 100,000 La Ilaha Illallah can be completed in 10 days.",
            note2 = "2- While reciting these noble dhikrs, one must continue regular daily spiritual duties.",
            note3 = "3- Whoever suffers from spiritual heaviness or trials should seek permission and spiritual aid from Allah, the Prophet ﷺ, and the masters of the chain before starting.",
            note4 = "4- Performing this spiritual composition once a year is highly recommended.",
            language = "Language Selection",
            themeTitle = "Theme Selection",
            counterPrefsTitle = "Counter, Vird & Reminders",
            countdownMode = "Countdown Mode",
            countdownModeDesc = "Decrements from target down to zero",
            dailyTargetTitle = "Daily Vird Target",
            dailyTargetLabel = "Daily Dhikr Target",
            keepAwake = "Keep Screen Awake",
            keepAwakeDesc = "Prevents screen sleep while on Dhikr counter",
        ),
        p2 = UiTranslationsPart2(
            roundsTitle = "Completed Rounds",
            roundsDesc = "Increments each time you finish all 15 Nafs Dhikr targets.",
            inactivityNotifTitle = "⚠️ Dhikr Reminder",
            hapticTitle = "Haptic Vibration Feedback",
            hapticOff = "Disable Haptics",
            roundCompletedTitle = "Round {0} Completed!",
            roundCompletedDesc = "You have successfully completed all 15 dhikr targets. May Allah accept your devotion.",
            startNewRoundBtn = "🔄 Start New Round",
            nextZikirBtn = "Proceed to Next Dhikr",
            mashaallah = "Masha'Allah!",
            congratsDesc = "You have successfully completed this sacred dhikr target.",
            alhamdulillah = "Alhamdulillah",
            resetConfirm = "Reset counter for this dhikr?",
            resetAllConfirm = "Reset all dhikr counters?",
            cancel = "Cancel",
            reset = "Reset",
            save = "Save",
            zikirInfoModalTitle = "Spiritual Manifestations & Virtues",
            shareCardBtn = "Share Spiritual Card",
            closeBtn = "Close",
            zenMode = "Zen / Focus Mode",
            exitZenMode = "Exit Zen Mode",
            fontScaleTitle = "Font Size (Scale)",
            fontScaleSmall = "Small",
            fontScaleNormal = "Normal",
            fontScaleLarge = "Large",
            fontScaleHuge = "Extra Large",
            hapticTapLight = "Light Click",
            hapticTapMedium = "Medium",
            hapticTapStrong = "Strong",
        ),
        p3 = UiTranslationsPart3(
            levelFinished = "Finished Dhikrs",
            badgeTitle = "Sequence & Consistency Badges",
            recordCount = "Records",
            streakDay = "Days",
            perDayShort = "/ d",
            heatmapTitle = "Activity Heatmap",
            heatmapLess = "Less",
            heatmapMore = "More",
            badgeStatusUnlocked = "Unlocked",
            badgeStatusLocked = "Locked",
            badgeRequirement = "Requirement",
            badgeCategoryTerkip = "Dhikr Sequence",
            badgeCategoryStreak = "Consistency & Fortitude",
            statusActive = "In Progress",
            statusWaiting = "Waiting",
            statusDone = "Completed",
            terkipLevelTitle = "Sequence",
            badgesEarnedCount = "{0} / {1} Earned",
            bestStreakLabel = "Best streak: {0} days",
            overallSpeed = "Overall Pace",
            totalRemaining = "Remaining:",
            allTerkipFinish = "All Dhikrs Completion",
            streakCardTitle = "Streak",
            range7Days = "7 Days",
            range30Days = "30 Days",
            range6Months = "6 Months",
            chartTitle = "Progress Chart",
            sequenceWarningTitle = "Sequence Order Required",
            sequenceWarningDesc = "Before reciting Dhikr {0}, previous dhikrs must be completed.",
            goToRequiredZikir = "Go to Active Dhikr ({0})",
            currentActiveDhikrLabel = "Current Active Dhikr:",
            understandClose = "Dismiss",
            badgeCelebrationHeader = "New Badge Unlocked",
            badgeCelebrationBlessing = "Congratulations on your spiritual fortitude and consistency. May your journey be blessed.",
            celebrationContinueBtn = "Alhamdulillah (Continue)",
            selectZikirPrompt = "Switch to this Dhikr",
            lockReasonMsg = "You must complete previous dhikrs to unlock this dhikr.",
            exportStatsBackupBtn = "📤 Export Statistics Backup",
            exportStatsBackupTitle = "Statistics Backup",
            exportStatsBackupDesc = "Export and share your dhikr counts and statistics as a file.",
            exportStatsFileHeader = "Nafs Dhikr - Recitation Counts & Statistics Report",
            exportStatsChooserTitle = "Share Dhikr Statistics",
            importStatsBackupBtn = "📥 Restore from Backup",
            importStatsBackupError = "Failed to read backup file or invalid format!",
            importStatsConfirmTitle = "Restore Backup",
            importStatsConfirmMsg = "Backup data will overwrite current progress. Continue?",
            confirmAction = "Restore",
            cancelAction = "Cancel",
            fastJumpTitle = "Fast Forward (Direct Jump)",
            fastJumpConfirmMsg = "Do you want to continue from this dhikr ({0})? If confirmed, all previous dhikrs ({1}) will be marked as completed and {0} will be unlocked.",
            fastJumpBtn = "Complete Previous & Start Here",
            logoutText = "Sign Out",
            signOutDesc = "Sign Out",
            cloudWelcomeMessage = "Welcome, {0}!",
            cloudSignOutMessage = "Signed out successfully.",
            cloudSignInRequiredForBackup = "Please sign in with Google first to back up to cloud.",
            cloudSignInRequiredForRestore = "Please sign in with Google first to restore from cloud.",
            cloudBackupSuccess = "Your dhikrs and settings have been backed up to the cloud!",
            cloudRestoreSuccess = "Your cloud dhikrs and settings have been restored successfully!",
            cloudGenericSignInError = "Google sign-in failed.",
            cloudGenericBackupError = "An error occurred during cloud backup.",
            cloudGenericRestoreError = "An error occurred during cloud restore.",
            cloudSyncTitle = "Cloud Sync",
            collapse = "Collapse",
            expand = "Expand",
            deleteBtn = "Delete",
            appLogo = "Nefs Dhikr Logo",
            backBtn = "Back",
            nextBtn = "Next",
            selected = "Selected",
            adaptiveReminderNotifTitle = "You've decreased your dhikr lately",
            spiritualVerses = listOf(
                SpiritualVerse(
                    surah = "Surah Ta-Ha, Verse 124",
                    verseText = "And whoever turns away from My remembrance - indeed, he will have a depressed life, and We will gather him on the Day of Resurrection blind.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Surah Az-Zukhruf, Verse 36",
                    verseText = "And whoever is blinded from remembrance of the Most Merciful - We appoint for him a devil, and he is to him a companion.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Surah Al-Jinn, Verse 17",
                    verseText = "And whoever turns away from the remembrance of his Lord - He will put him into arduous punishment.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Surah Al-An'am, Verse 44",
                    verseText = "So when they forgot that by which they had been reminded, We opened to them the doors of every [good] thing until, when they rejoiced in that which they were given, We seized them suddenly, and at once they were in despair.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Surah Al-Munafiqun, Verse 9",
                    verseText = "O you who have believed, let not your wealth and your children divert you from remembrance of Allah. And whoever does that - then those are the losers.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Surah Al-Baqarah, Verse 152",
                    verseText = "So remember Me; I will remember you. And be grateful to Me and do not deny Me.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Surah Al-Ahzab, Verses 41-42",
                    verseText = "O you who have believed, remember Allah with much remembrance and glorify Him morning and afternoon.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Surah Ar-Ra'd, Verse 28",
                    verseText = "Those who have believed and whose hearts are assured by the remembrance of Allah. Unquestionably, by the remembrance of Allah hearts are assured.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Surah Al-Ahzab, Verse 35",
                    verseText = "...and the men who remember Allah often and the women who do so - for them Allah has prepared forgiveness and a great reward.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Surah Al-Ankabut, Verse 45",
                    verseText = "...and the remembrance of Allah is greater. And Allah knows that which you do.",
                    type = "glad_tidings"
                )
            )
        )
    )

    private val DE = UiTranslations(
        p1 = UiTranslationsPart1(
            title = "Nafs Dhikr",
            subtitle = "Spirituelle Dhikr-App",
            tabZikir = "Dhikr",
            tabListe = "Liste",
            tabIstatistik = "Statistik",
            tabBilgi = "Info",
            tabAyarlar = "Optionen",
            target = "Ziel",
            totalDone = "Gesamt Gelesen",
            remainingZikir = "Verbleibend",
            perDay = "/Tag",
            undo = "Rückgängig",
            manualTitle = "Manuell Hinzufügen",
            manualDesc = "Fügen Sie außerhalb gezählte Dhikr hinzu oder korrigieren Sie Zahlen.",
            manualPlaceholder = "Menge eingeben...",
            addBtn = "+ Hinzufügen",
            removeBtn = "− Entfernen",
            resetThis = "Diesen Dhikr Zurücksetzen",
            resetAll = "Alles Zurücksetzen",
            completedBadge = "Fertig",
            listTitle = "Dhikr-Liste",
            statTotalRecited = "Gesamt Gelesen",
            recentActivityTitle = "Letzte Aktivitäten",
            noActivityYet = "Noch keine Einträge vorhanden.",
            dayUnit = "Tage",
            calculating = "Wird berechnet",
            sheikhTitle = "Diener der Qadiri-Derwische",
            sheikhName = "Scheich Sayyid Muhammad Ruhi Al-Qadiri Al-Husayni",
            notesTitle = "Wichtige Hinweise",
            note1 = "1- Jeder dieser Dhikr wird je nach Kapazität innerhalb weniger Tage abgeschlossen.",
            note2 = "2- Die täglichen Pflichten sollten parallel weitergeführt werden.",
            note3 = "3- Bei spiritueller Erschöpfung vor Beginn um Segen und Beistand bitten.",
            note4 = "4- Es wird empfohlen, diese Praxis einmal jährlich zu vollziehen.",
            language = "Sprachauswahl",
            themeTitle = "Farbthema",
            counterPrefsTitle = "Zähler, Vird & Erinnerungen",
            countdownMode = "Countdown-Modus",
            countdownModeDesc = "Zählt vom Zielwert herunter auf Null",
            dailyTargetTitle = "Tagesziel",
            dailyTargetLabel = "Tägliche Dhikr-Menge",
            keepAwake = "Bildschirm anlassen",
            keepAwakeDesc = "Verhindert die Bildschirmsperre",
        ),
        p2 = UiTranslationsPart2(
            roundsTitle = "Abgeschlossene Runden",
            roundsDesc = "Anzahl der komplett durchlaufenen 15 Dhikr-Runden.",
            inactivityNotifTitle = "⚠️ Dhikr-Zeit verstreicht",
            hapticTitle = "Haptisches Feedback",
            hapticOff = "Vibration Deaktivieren",
            roundCompletedTitle = "Runde {0} Abgeschlossen!",
            roundCompletedDesc = "Sie haben alle 15 Dhikr-Ziele erreicht. Möge Allah Ihr Gebet annehmen.",
            startNewRoundBtn = "🔄 Neue Runde Starten",
            nextZikirBtn = "Zum nächsten Dhikr",
            mashaallah = "Maschallaha!",
            congratsDesc = "Sie haben das Ziel dieses gesegneten Dhikr erreicht.",
            alhamdulillah = "Alhamdulillah",
            resetConfirm = "Zähler zurücksetzen?",
            resetAllConfirm = "Alle Zähler zurücksetzen?",
            cancel = "Abbrechen",
            reset = "Zurücksetzen",
            save = "Speichern",
            zikirInfoModalTitle = "Spirituelle Wirkungen & Tugenden",
            shareCardBtn = "Karte Teilen",
            closeBtn = "Schließen",
            zenMode = "Zen- / Fokusmodus",
            exitZenMode = "Zen-Modus verlassen",
            fontScaleTitle = "Schriftgröße (Skalierung)",
            fontScaleSmall = "Klein",
            fontScaleNormal = "Normal",
            fontScaleLarge = "Groß",
            fontScaleHuge = "Sehr Groß",
            hapticTapLight = "Leichter Klick",
            hapticTapMedium = "Mittel",
            hapticTapStrong = "Stark",
        ),
        p3 = UiTranslationsPart3(
            levelFinished = "Beendete Dhikrs",
            badgeTitle = "Sequenz- & Beständigkeitsabzeichen",
            recordCount = "Einträge",
            streakDay = "Tage",
            perDayShort = "/ T",
            heatmapTitle = "Aktivitätskalender",
            heatmapLess = "Weniger",
            heatmapMore = "Mehr",
            badgeStatusUnlocked = "Freigeschaltet",
            badgeStatusLocked = "Gesperrt",
            badgeRequirement = "Voraussetzung",
            badgeCategoryTerkip = "Terkip-Stufe",
            badgeCategoryStreak = "Beständigkeit & Ausdauer",
            statusActive = "In Bearbeitung",
            statusWaiting = "Wartend",
            statusDone = "Abgeschlossen",
            terkipLevelTitle = "Reihenfolge",
            badgesEarnedCount = "{0} / {1} Verdient",
            bestStreakLabel = "Längste Serie: {0} Tage",
            overallSpeed = "Gesamttempo",
            totalRemaining = "Verbleibend:",
            allTerkipFinish = "Abschluss aller Dhikrs",
            streakCardTitle = "Serie",
            range7Days = "7 Tage",
            range30Days = "30 Tage",
            range6Months = "6 Monate",
            chartTitle = "Fortschrittsdiagramm",
            sequenceWarningTitle = "Reihenfolge-Warnung",
            sequenceWarningDesc = "In der spirituellen Disziplin baut jede Stufe auf der vorherigen auf. Bevor Sie Stufe {0} rezitieren, müssen vorherige Stufen abgeschlossen sein.",
            goToRequiredZikir = "Zu aktiver Stufe gehen ({0})",
            currentActiveDhikrLabel = "Aktuelle Stufe:",
            understandClose = "Verstanden, Schließen",
            badgeCelebrationHeader = "Neues Abzeichen Freigeschaltet",
            badgeCelebrationBlessing = "Herzlichen Glückwunsch zu Ihrer Ausdauer und Hingabe auf dem spirituellen Weg.",
            celebrationContinueBtn = "Alhamdulillah (Weiter)",
            selectZikirPrompt = "Zu diesem Dhikr wechseln",
            lockReasonMsg = "Sie müssen vorherige Stufen abschließen, um diese freizuschalten.",
            exportStatsBackupBtn = "📤 Statistik-Sicherung exportieren",
            exportStatsBackupTitle = "Statistik-Sicherung",
            exportStatsBackupDesc = "Dhikr-Zählerstände und Statistiken als Datei exportieren und teilen.",
            exportStatsFileHeader = "Nafs Dhikr - Zählerstände & Statistikbericht",
            exportStatsChooserTitle = "Statistik-Sicherung teilen",
            importStatsBackupBtn = "📥 Aus Sicherung wiederherstellen",
            importStatsBackupError = "Sicherungsdatei konnte nicht gelesen werden oder Format ungültig!",
            importStatsConfirmTitle = "Sicherung wiederherstellen",
            importStatsConfirmMsg = "Sicherungsdaten überschreiben den aktuellen Stand. Fortfahren?",
            confirmAction = "Wiederherstellen",
            cancelAction = "Abbrechen",
            fastJumpTitle = "Schneller Übergang (Sprung)",
            fastJumpConfirmMsg = "Möchten Sie mit dieser Stufe ({0}) fortfahren? Bei Bestätigung werden alle vorherigen Dhikr ({1}) als abgeschlossen markiert und {0} wird freigeschaltet.",
            fastJumpBtn = "Vorherige abschließen & hier beginnen",
            logoutText = "Abmelden",
            signOutDesc = "Abmelden",
            cloudWelcomeMessage = "Willkommen, {0}!",
            cloudSignOutMessage = "Erfolgreich abgemeldet.",
            cloudSignInRequiredForBackup = "Bitte melden Sie sich zuerst mit Google an, um in der Cloud zu sichern.",
            cloudSignInRequiredForRestore = "Bitte melden Sie sich zuerst mit Google an, um aus der Cloud wiederherzustellen.",
            cloudBackupSuccess = "Ihre Dhikrs und Einstellungen wurden erfolgreich in der Cloud gesichert!",
            cloudRestoreSuccess = "Ihre Dhikrs und Einstellungen aus der Cloud wurden erfolgreich wiederhergestellt!",
            cloudGenericSignInError = "Google-Anmeldung fehlgeschlagen.",
            cloudGenericBackupError = "Fehler bei der Cloud-Sicherung aufgetreten.",
            cloudGenericRestoreError = "Fehler bei der Cloud-Wiederherstellung aufgetreten.",
            cloudSyncTitle = "Cloud-Synchronisierung",
            collapse = "Einklappen",
            expand = "Ausklappen",
            deleteBtn = "Löschen",
            appLogo = "Nefs Dhikr Logo",
            backBtn = "Zurück",
            nextBtn = "Weiter",
            selected = "Ausgewählt",
            adaptiveReminderNotifTitle = "Sie haben Ihren Zikr in letzter Zeit reduziert",
            spiritualVerses = listOf(
                SpiritualVerse(
                    surah = "Sure Ta-Ha, Vers 124",
                    verseText = "Wer sich aber von Meiner Ermahnung abwendet, der wird ein enges Leben führen, und Wir werden ihn am Tag der Auferstehung blind versammeln.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Sure Az-Zukhruf, Vers 36",
                    verseText = "Wer für die Ermahnung des Allerbarmers blind ist, dem bestimmen Wir einen Satan, der sein ständiger Begleiter sein wird.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Sure Al-Dschinn, Vers 17",
                    verseText = "Und wer sich von der Ermahnung seines Herrn abwendet, den wird Er in eine immer schwerere Strafe stecken.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Sure Al-An'am, Vers 44",
                    verseText = "Als sie dann vergaßen, woran sie erinnert worden waren, öffneten Wir ihnen die Tore aller Dinge. Als sie sich schließlich über das freuten, was ihnen gegeben worden war, ergriffen Wir sie plötzlich, und da waren sie verzweifelt.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Sure Al-Munafiqun, Vers 9",
                    verseText = "O die ihr glaubt, weder euer Besitz noch eure Kinder sollen euch vom Gedenken an Allah ablenken. Wer das tut, das sind die Verlierer.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Sure Al-Baqarah, Vers 152",
                    verseText = "Gedenkt Meiner, so gedenke Ich eurer. Seid Mir dankbar und seid nicht undankbar.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Sure Al-Ahzab, Verse 41-42",
                    verseText = "O die ihr glaubt, gedenkt Allahs in häufigem Gedenken und preist Ihn morgens und abends.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Sure Ar-Ra'd, Vers 28",
                    verseText = "Diejenigen, die glauben und deren Herzen im Gedenken Allahs Ruhe finden. Wahrlich, im Gedenken Allahs finden die Herzen Ruhe.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Sure Al-Ahzab, Vers 35",
                    verseText = "...und die Männer, die Allahs viel gedenken, und die Frauen, die (Seiner) gedenken - für sie alle hat Allah Vergebung und gewaltigen Lohn bereitet.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Sure Al-Ankabut, Vers 45",
                    verseText = "...und das Gedenken Allahs ist gewiss das Größte. Und Allah weiß, was ihr tut.",
                    type = "glad_tidings"
                )
            )
        )
    )

    private val FR = UiTranslations(
        p1 = UiTranslationsPart1(
            title = "Nafs Dhikr",
            subtitle = "Application de Suivi Spirituel",
            tabZikir = "Dhikr",
            tabListe = "Liste",
            tabIstatistik = "Stats",
            tabBilgi = "Info",
            tabAyarlar = "Réglages",
            target = "Objectif",
            totalDone = "Total Récité",
            remainingZikir = "Dhikr Restant",
            perDay = "/jour",
            undo = "Annuler",
            manualTitle = "Ajout Manuel",
            manualDesc = "Ajoutez les récitations faites hors application ou corrigez les erreurs.",
            manualPlaceholder = "Entrez le nombre...",
            addBtn = "+ Ajouter",
            removeBtn = "− Enlever",
            resetThis = "Réinitialiser ce Dhikr",
            resetAll = "Tout Réinitialiser",
            completedBadge = "Terminé",
            listTitle = "Liste des Dhikrs",
            statTotalRecited = "Total Récité",
            recentActivityTitle = "Historique Récent",
            noActivityYet = "Aucune activité pour le moment.",
            dayUnit = "Jours",
            calculating = "En calcul",
            sheikhTitle = "Serviteur des Derviches Qadiri",
            sheikhName = "Cheikh Sayyid Muhammad Ruhi Al-Qadiri Al-Husayni",
            notesTitle = "Notes Importantes",
            note1 = "1- Chaque dhikr est accompli en quelques jours selon la capacité personnelle.",
            note2 = "2- Les invocations quotidiennes obligatoires doivent être maintenues.",
            note3 = "3- En cas de difficultés spirituelles, solliciter l'assistance spirituelle avant de commencer.",
            note4 = "4- Il est vivement recommandé d'accomplir cette pratique une fois par an.",
            language = "Choix de la Langue",
            themeTitle = "Choix du thème",
            counterPrefsTitle = "Compteur, Vird & Rappels",
            countdownMode = "Mode Décompte",
            countdownModeDesc = "Compte à rebours de l'objectif vers zéro",
            dailyTargetTitle = "Objectif Quotidien",
            dailyTargetLabel = "Objectif Quotidien de Dhikr",
            keepAwake = "Garder l'écran allumé",
            keepAwakeDesc = "Empêche l'extinction de l'écran pendant le dhikr",
        ),
        p2 = UiTranslationsPart2(
            roundsTitle = "Tours Complétés",
            roundsDesc = "Augmente chaque fois que vous complétez les 15 dhikrs.",
            inactivityNotifTitle = "⚠️ Rappel de Vird",
            hapticTitle = "Vibration Haptique",
            hapticOff = "Désactiver la Vibration",
            roundCompletedTitle = "Tour {0} Terminé !",
            roundCompletedDesc = "Vous avez complété avec succès les 15 objectifs. Qu'Allah accepte vos invocations.",
            startNewRoundBtn = "🔄 Commencer un Nouveau Tour",
            nextZikirBtn = "Passer au dhikr suivant",
            mashaallah = "Macha'Allah !",
            congratsDesc = "Vous avez complété avec succès l'objectif de ce noble dhikr.",
            alhamdulillah = "Alhamdoulillah",
            resetConfirm = "Réinitialiser ce compteur ?",
            resetAllConfirm = "Réinitialiser tous les compteurs ?",
            cancel = "Annuler",
            reset = "Réinitialiser",
            save = "Enregistrer",
            zikirInfoModalTitle = "Manifestations Spirituelles & Vertus",
            shareCardBtn = "Partager la Fiche",
            closeBtn = "Fermer",
            zenMode = "Mode Zen / Focus",
            exitZenMode = "Quitter le mode Zen",
            fontScaleTitle = "Taille de Police",
            fontScaleSmall = "Petit",
            fontScaleNormal = "Normal",
            fontScaleLarge = "Grand",
            fontScaleHuge = "Très Grand",
            hapticTapLight = "Clic Léger",
            hapticTapMedium = "Moyen",
            hapticTapStrong = "Fort",
        ),
        p3 = UiTranslationsPart3(
            levelFinished = "Dhikrs terminés",
            badgeTitle = "Badges de Séquence et de Régularité",
            recordCount = "Enregistrements",
            streakDay = "Jours",
            perDayShort = "/ j",
            heatmapTitle = "Calendrier d'Activité",
            heatmapLess = "Moins",
            heatmapMore = "Plus",
            badgeStatusUnlocked = "Débloqué",
            badgeStatusLocked = "Verrouillé",
            badgeRequirement = "Condition",
            badgeCategoryTerkip = "Niveau Terkip",
            badgeCategoryStreak = "Régularité & Persévérance",
            statusActive = "En cours",
            statusWaiting = "En attente",
            statusDone = "Terminé",
            terkipLevelTitle = "Séquence",
            badgesEarnedCount = "{0} / {1} Obtenus",
            bestStreakLabel = "Meilleure série : {0} jours",
            overallSpeed = "Rythme global",
            totalRemaining = "Restant :",
            allTerkipFinish = "Fin de tous les Dhikrs",
            streakCardTitle = "Série",
            range7Days = "7 Jours",
            range30Days = "30 Jours",
            range6Months = "6 Mois",
            chartTitle = "Graphique de progression",
            sequenceWarningTitle = "Avertissement d'Ordre de Séquence",
            sequenceWarningDesc = "Selon la convenance spirituelle, chaque étape prépare à la suivante. Veuillez compléter les niveaux précédents avant le niveau {0}.",
            goToRequiredZikir = "Aller au Dhikr requis ({0})",
            currentActiveDhikrLabel = "Dhikr actif actuel :",
            understandClose = "Compris, Fermer",
            badgeCelebrationHeader = "Nouveau Badge Débloqué",
            badgeCelebrationBlessing = "Félicitations pour votre persévérance et votre dévouement dans votre cheminement spirituel.",
            celebrationContinueBtn = "Alhamdoulillah (Continuer)",
            selectZikirPrompt = "Passer à ce Dhikr",
            lockReasonMsg = "Vous devez terminer les niveaux précédents pour débloquer celui-ci.",
            exportStatsBackupBtn = "📤 Exporter les statistiques",
            exportStatsBackupTitle = "Sauvegarde des Statistiques",
            exportStatsBackupDesc = "Exportez et partagez vos comptes de dhikrs et statistiques sous forme de fichier.",
            exportStatsFileHeader = "Nafs Dhikr - Rapport des statistiques et récitations",
            exportStatsChooserTitle = "Partager les statistiques",
            importStatsBackupBtn = "📥 Restaurer la sauvegarde",
            importStatsBackupError = "Échec de lecture du fichier de sauvegarde ou format non valide !",
            importStatsConfirmTitle = "Restaurer la sauvegarde",
            importStatsConfirmMsg = "Les données de sauvegarde écraseront la progression actuelle. Continuer ?",
            confirmAction = "Restaurer",
            cancelAction = "Annuler",
            fastJumpTitle = "Saut Rapide (Passage Direct)",
            fastJumpConfirmMsg = "Souhaitez-vous continuer à partir de ce niveau ({0}) ? Si vous confirmez, tous les dhikrs précédents ({1}) seront considérés comme terminés et {0} sera débloqué.",
            fastJumpBtn = "Compléter les précédents & Commencer ici",
            logoutText = "Se déconnecter",
            signOutDesc = "Se déconnecter",
            cloudWelcomeMessage = "Bienvenue, {0} !",
            cloudSignOutMessage = "Déconnexion réussie.",
            cloudSignInRequiredForBackup = "Veuillez d'abord vous connecter avec Google pour sauvegarder sur le cloud.",
            cloudSignInRequiredForRestore = "Veuillez d'abord vous connecter avec Google pour restaurer depuis le cloud.",
            cloudBackupSuccess = "Vos dhikrs et paramètres ont été sauvegardés sur le cloud avec succès !",
            cloudRestoreSuccess = "Vos dhikrs et paramètres cloud ont été restaurés avec succès !",
            cloudGenericSignInError = "Échec de la connexion Google.",
            cloudGenericBackupError = "Une erreur est survenue lors de la sauvegarde cloud.",
            cloudGenericRestoreError = "Une erreur est survenue lors de la restauration cloud.",
            cloudSyncTitle = "Synchronisation Cloud",
            collapse = "Réduire",
            expand = "Développer",
            deleteBtn = "Supprimer",
            appLogo = "Logo Nefs Dhikr",
            backBtn = "Retour",
            nextBtn = "Suivant",
            selected = "Sélectionné",
            adaptiveReminderNotifTitle = "Vous avez réduit votre dhikr ces derniers temps",
            spiritualVerses = listOf(
                SpiritualVerse(
                    surah = "Sourate Ta-Ha, Verset 124",
                    verseText = "Et quiconque se détourne de Mon Rappel, mènera certes une vie pleine de gêne, et le Jour de la Résurrection Nous l'amènerons aveugle au rassemblement.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Sourate Az-Zukhruf, Verset 36",
                    verseText = "Et quiconque s'aveugle (et s'écarte) du rappel du Tout Miséricordieux, Nous lui désignons un diable qui devient son compagnon inséparable.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Sourate Al-Jinn, Verset 17",
                    verseText = "Et quiconque se détourne du rappel de son Seigneur, Il l'achemine vers un châtiment sans cesse croissant.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Sourate Al-An'am, Verset 44",
                    verseText = "Puis, lorsqu'ils eurent oublié ce qu'on leur avait rappelé, Nous leur ouvrîmes les portes de toute chose; puis, lorsqu'ils eurent exulté de joie en raison de ce qui leur avait été donné, Nous les saisîmes soudain, et les voilà désespérés.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Sourate Al-Munafiqun, Verset 9",
                    verseText = "Ô vous qui avez cru ! Que ni vos biens ni vos enfants ne vous distraient du rappel d'Allah. Et quiconque fait cela... alors ceux-là sont les perdants.",
                    type = "warning"
                ),
                SpiritualVerse(
                    surah = "Sourate Al-Baqarah, Verset 152",
                    verseText = "Souvenez-vous de Moi donc, Je vous récompenserai. Remerciez-Moi et ne soyez pas ingrats envers Moi.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Sourate Al-Ahzab, Versets 41-42",
                    verseText = "Ô vous qui croyez ! Évoquez Allah d'une façon abondante et glorifiez-Le à la pointe et au déclin du jour.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Sourate Ar-Ra'd, Verset 28",
                    verseText = "Ceux qui ont cru, et dont les cœurs s'apaisent à l'évocation d'Allah. N'est-ce point par l'évocation d'Allah que se tranquillisent les cœurs ?",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Sourate Al-Ahzab, Verset 35",
                    verseText = "...les invocateurs fréquents d'Allah et les invocatrices : Allah a préparé pour eux un pardon et une énorme récompense.",
                    type = "glad_tidings"
                ),
                SpiritualVerse(
                    surah = "Sourate Al-Ankabut, Verset 45",
                    verseText = "...et le rappel d'Allah est certes ce qu'il y a de plus grand. Et Allah sait ce que vous faites.",
                    type = "glad_tidings"
                )
            )
        )
    )
    fun get(lang: String): UiTranslations {
        return when (lang.lowercase()) {
            "ar" -> AR
            "en" -> EN
            "de" -> DE
            "fr" -> FR
            else -> TR
        }
    }
}
