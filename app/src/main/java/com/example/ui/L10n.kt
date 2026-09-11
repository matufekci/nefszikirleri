package com.example.ui

/**
 * Derleme zamaninda 5 dilin TAMAMINI zorunlu kilan metin sarmalayici.
 *
 * Neden var: Ayarlar'da Arapca seciliyken "Yedekleme / Temalar" kartlarindaki
 * bazi metinler Turkce gorunuyordu. Sebep, satir ici `when(lang)` bloklarinda
 * "ar" dalinin unutulmasi ve kodun sessizce Turkce `else`'e dusmesiydi.
 * Bu sinif 5 dili de constructor parametresi olarak zorunlu tuttugu icin bir
 * dili unutmak DERLEME HATASI verir; kullaniciya secmedigi bir dilin
 * gosterilmesi imkansizlasir.
 *
 * Yeni bir kullanici metni eklerken AppStrings'e anahtar acmak yerine buraya
 * eklemek de gecerlidir; onemli olan 5 dilin birden zorunlu olmasi.
 */
class L10n(
    private val tr: String,
    private val ar: String,
    private val en: String,
    private val de: String,
    private val fr: String
) {
    fun get(lang: String): String = when (lang.lowercase()) {
        "ar" -> ar
        "en" -> en
        "de" -> de
        "fr" -> fr
        else -> tr
    }

    /** {0}, {1} ... yer tutucularini sirayla doldurur. */
    fun format(lang: String, vararg args: Any?): String {
        var out = get(lang)
        args.forEachIndexed { index, value -> out = out.replace("{$index}", value.toString()) }
        return out
    }
}

/**
 * AppStrings disinda kalmis, kullaniciya gorunen metinler.
 * Her girdi 5 dili de icermek zorunda (L10n constructor'i bunu garanti eder).
 */
object UiText {

    // ---------------------------------------------------------- tema adlari
    // Color.kt icindeki paletler sabit Turkce ad tasiyordu; kart icerigi bu
    // yuzden her dilde Turkce gorunuyordu.
    val themeWhite = L10n("Beyaz", "أبيض", "White", "Weiß", "Blanc")
    val themeGreen = L10n("Yeşil", "أخضر", "Green", "Grün", "Vert")
    val themeBlack = L10n("Siyah", "أسود", "Black", "Schwarz", "Noir")

    /**
     * Palet kimligini secili dildeki tema adina cevirir.
     * Color.kt paletleri sabit Turkce ad tasidigi icin kart icerigi her dilde
     * Turkce gorunuyordu; kimlik->metin eslemesi burada, 5 dil zorunlu.
     */
    fun themeName(paletteId: String, lang: String): String = when (paletteId) {
        "hadra_gunduz" -> themeWhite
        "hadra_gece" -> themeGreen
        else -> themeBlack
    }.get(lang)

    // ------------------------------------------------- bulut / yedekleme karti
    val googleAccount = L10n("Google Hesabı", "حساب Google", "Google Account", "Google-Konto", "Compte Google")
    val connected = L10n("Bağlandı", "متصل", "Connected", "Verbunden", "Connecté")
    val signInWithGoogle = L10n(
        "Google ile Giriş Yap",
        "تسجيل الدخول عبر Google",
        "Sign in with Google",
        "Mit Google anmelden",
        "Se connecter avec Google"
    )
    val lastSyncAt = L10n(
        "Son Eşitleme: {0}",
        "آخر مزامنة: {0}",
        "Last sync: {0}",
        "Letzte Synchronisierung: {0}",
        "Dernière synchro : {0}"
    )
    val autoSyncActive = L10n(
        "Otomatik Eşitleme Aktif",
        "المزامنة التلقائية مفعّلة",
        "Auto-sync active",
        "Automatische Synchronisierung aktiv",
        "Synchronisation auto active"
    )
    val backupToCloud = L10n(
        "Buluta Yedekle",
        "نسخ احتياطي إلى السحابة",
        "Back up to cloud",
        "In die Cloud sichern",
        "Sauvegarder dans le cloud"
    )
    val restore = L10n("Geri Yükle", "استعادة", "Restore", "Wiederherstellen", "Restaurer")

    // ------------------------------------------------- yedek parola diyaloglari
    val backupEncryptionTitle = L10n(
        "Yedek Şifreleme",
        "تشفير النسخة الاحتياطية",
        "Backup Encryption",
        "Backup-Verschlüsselung",
        "Chiffrement de la sauvegarde"
    )
    val backupEncryptionMessage = L10n(
        "Yedeğinizi AES-256 ile korumak için bir parola belirleyin:",
        "عيّن كلمة مرور لحماية نسختك الاحتياطية بتشفير AES-256:",
        "Set a password to protect your backup with AES-256:",
        "Lege ein Passwort fest, um dein Backup mit AES-256 zu schützen:",
        "Définis un mot de passe pour protéger ta sauvegarde avec AES-256 :"
    )
    val encryptAndShare = L10n(
        "Şifrele ve Paylaş",
        "تشفير ومشاركة",
        "Encrypt & Share",
        "Verschlüsseln & teilen",
        "Chiffrer et partager"
    )
    val backupPasswordTitle = L10n(
        "Yedek Parolası",
        "كلمة مرور النسخة الاحتياطية",
        "Backup Password",
        "Backup-Passwort",
        "Mot de passe de sauvegarde"
    )
    val backupPasswordMessage = L10n(
        "Şifrelenmiş yedeği açmak için parolayı girin (eski şifresiz yedekler için boş bırakabilirsiniz):",
        "أدخل كلمة المرور لفتح النسخة المشفّرة (يمكن تركها فارغة للنسخ القديمة غير المشفّرة):",
        "Enter the password to open the encrypted backup (leave empty for older unencrypted backups):",
        "Gib das Passwort ein, um das verschlüsselte Backup zu öffnen (bei alten unverschlüsselten Backups leer lassen):",
        "Saisis le mot de passe pour ouvrir la sauvegarde chiffrée (laisse vide pour les anciennes sauvegardes non chiffrées) :"
    )
    val openBackup = L10n("Yedeği Aç", "فتح النسخة", "Open Backup", "Backup öffnen", "Ouvrir la sauvegarde")
    val backupRestoredToast = L10n(
        "Yedek başarıyla geri yüklendi ({0} zikir)",
        "تمت استعادة النسخة الاحتياطية بنجاح ({0} ذكر)",
        "Backup restored successfully ({0} dhikr)",
        "Backup erfolgreich wiederhergestellt ({0} Dhikr)",
        "Sauvegarde restaurée avec succès ({0} dhikr)"
    )
    val cancel = L10n("İptal", "إلغاء", "Cancel", "Abbrechen", "Annuler")

    // ------------------------------------------------------- bildirim / hata
    val notificationPermissionNeeded = L10n(
        "Hatırlatıcıları alabilmek için bildirim iznine ihtiyacımız var",
        "نحتاج إذن الإشعارات لتصلك التذكيرات",
        "We need notification permission to send you reminders",
        "Wir benötigen die Benachrichtigungsberechtigung für Erinnerungen",
        "Nous avons besoin de l'autorisation de notifications pour les rappels"
    )
    val wrongPassword = L10n(
        "Yanlış parola",
        "كلمة مرور خاطئة",
        "Wrong password",
        "Falsches Passwort",
        "Mot de passe incorrect"
    )
    val syncConflictDetected = L10n(
        "Senkronizasyon çakışması algılandı.",
        "تم اكتشاف تعارض في المزامنة.",
        "Sync conflict detected.",
        "Synchronisationskonflikt erkannt.",
        "Conflit de synchronisation détecté."
    )
}
