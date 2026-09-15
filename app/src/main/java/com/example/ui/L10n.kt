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
    val themePink = L10n("Pembe Lüks", "وردي فاخر", "Pink Luxury", "Rosa Luxe", "Rose Luxe")

    // ------------------------------------------------- çocuk kilidi
    val childLock = L10n("Çocuk Kilidi", "قفل الأطفال", "Child Lock", "Kindersicherung", "Verrou enfant")
    val childLockDesc = L10n(
        "Açıkken sayaç dokunuşlara kapanır; çocuklar zikirleri değiştiremez.",
        "عند التفعيل يُغلق العداد عن اللمس؛ لا يمكن للأطفال تغيير الأذكار.",
        "When on, the counter ignores taps; children cannot change counts.",
        "Wenn aktiv, ignoriert der Zähler Berührungen; Kinder können nichts ändern.",
        "Activé : le compteur ignore les appuis ; les enfants ne peuvent rien modifier."
    )
    val lockHoldHint = L10n("Kilidi açmak için 3 sn basılı tut", "اضغط مطولاً ٣ ثوانٍ لفتح القفل", "Hold 3 s to unlock", "3 s halten zum Entsperren", "Maintenir 3 s pour déverrouiller")
    val adultCheckTitle = L10n("Yetişkin doğrulaması", "تحقق البالغين", "Adult verification", "Erwachsenen-Verifikation", "Vérification adulte")
    val adultCheckWrong = L10n("Yanlış cevap — kilit kalıyor.", "إجابة خاطئة — يبقى القفل.", "Wrong answer — still locked.", "Falsche Antwort — bleibt gesperrt.", "Réponse fausse — reste verrouillé.")


    // ------------------------------------------------- tarihler
    val dateStart = L10n("Başlangıç", "البداية", "Start", "Beginn", "Début")
    val dateEnd = L10n("Bitiş", "النهاية", "End", "Ende", "Fin")
    val dateOngoing = L10n("devam ediyor", "مستمر", "ongoing", "läuft", "en cours")

    // ------------------------------------------------- aksam hatirlatmalari
    val eveningNotifTitle = L10n("Akşam Hatırlatması", "تذكير المساء", "Evening Reminder", "Abenderinnerung", "Rappel du soir")
    val finishNotifTitle = L10n("Son Viraj", "المنعطف الأخير", "Final Stretch", "Zielgerade", "Dernière ligne droite")

    /**
     * Palet kimligini secili dildeki tema adina cevirir.
     * Color.kt paletleri sabit Turkce ad tasidigi icin kart icerigi her dilde
     * Turkce gorunuyordu; kimlik->metin eslemesi burada, 5 dil zorunlu.
     */
    fun themeName(paletteId: String, lang: String): String = when (paletteId) {
        "hadra_gunduz" -> themeWhite
        "hadra_gece" -> themeGreen
        "pembe_lux" -> themePink
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
    val wrongPassword = L10n(
        "Yanlış parola",
        "كلمة مرور خاطئة",
        "Wrong password",
        "Falsches Passwort",
        "Mot de passe incorrect"
    )

    // ------------------------------------------- bulut / yedek hata siniflari
    // Neden var: bulut ve yedek hatalarinda `e.localizedMessage` dogrudan
    // kullaniciya gosteriliyordu; Firestore/Firebase mesajlari Ingilizce ve
    // teknik (orn. "UNAVAILABLE: Unable to resolve host"). CloudErrorMapper
    // hatayi siniflandirip asagidaki 5 dilli metinlerden birini secer.
    val cloudErrorNetwork = L10n(
        "İnternet bağlantısı kurulamadı. Bağlantını kontrol edip tekrar dene.",
        "تعذّر الاتصال بالإنترنت. تحقّق من الاتصال ثم أعد المحاولة.",
        "Could not connect to the Internet. Check your connection and try again.",
        "Internetverbindung konnte nicht hergestellt werden. Prüfe deine Verbindung und versuche es erneut.",
        "Connexion Internet impossible. Vérifie ta connexion puis réessaie."
    )
    val cloudErrorPermission = L10n(
        "Bulut yedeğine erişim izni verilmedi. Google hesabınla giriş yapıp tekrar dene.",
        "لم يُسمح بالوصول إلى النسخة الاحتياطية في السحابة. سجّل الدخول بحساب Google ثم أعد المحاولة.",
        "Access to the cloud backup was denied. Sign in with your Google account and try again.",
        "Zugriff auf die Cloud-Sicherung nicht erlaubt. Melde dich mit deinem Google-Konto an und versuche es erneut.",
        "Accès à la sauvegarde cloud refusé. Connecte-toi avec ton compte Google puis réessaie."
    )
    val cloudErrorNoBackup = L10n(
        "Bulutta kayıtlı bir yedek bulunamadı.",
        "لا توجد نسخة احتياطية محفوظة في السحابة.",
        "No backup is stored in the cloud.",
        "In der Cloud ist keine Sicherung gespeichert.",
        "Aucune sauvegarde enregistrée dans le cloud."
    )
    val cloudErrorCorrupt = L10n(
        "Buluttaki yedek okunamadı (veri bütünlüğü doğrulanamadı). Yerel verilerin güvende.",
        "تعذّرت قراءة النسخة الاحتياطية في السحابة (تعذّر التحقق من سلامة البيانات). بياناتك المحلية بأمان.",
        "The cloud backup could not be read (data integrity could not be verified). Your local data is safe.",
        "Die Cloud-Sicherung konnte nicht gelesen werden (Datenintegrität nicht bestätigt). Deine lokalen Daten sind sicher.",
        "La sauvegarde cloud n'a pas pu être lue (intégrité des données non vérifiée). Tes données locales sont en sécurité."
    )
    val cloudErrorCancelled = L10n(
        "Bulut işlemi yarım kaldı; verilerin cihazında duruyor. Tekrar deneyebilirsin.",
        "توقّفت عملية السحابة في منتصفها؛ بياناتك ما زالت على جهازك. يمكنك المحاولة مرة أخرى.",
        "The cloud operation was interrupted; your data is still on this device. You can try again.",
        "Der Cloud-Vorgang wurde unterbrochen; deine Daten sind weiterhin auf diesem Gerät. Versuche es erneut.",
        "L'opération cloud a été interrompue ; tes données sont toujours sur cet appareil. Réessaie."
    )
    val exportStatsError = L10n(
        "Yedek dosyası oluşturulamadı veya paylaşılamadı.",
        "تعذّر إنشاء ملف النسخة الاحتياطية أو مشاركته.",
        "The backup file could not be created or shared.",
        "Sicherungsdatei konnte nicht erstellt oder geteilt werden.",
        "Le fichier de sauvegarde n'a pas pu être créé ou partagé."
    )
    // --------------------------------------------- Google ile giris hatalari
    // Neden var: AuthManager 13 ayri yerde sabit Turkce metin uretiyordu;
    // Arapca/Almanca/Fransizca kullanici Turkce mesaj goruyordu ve SHA-1 /
    // project id gibi teknik ayrintilar ekrana dusuyordu. AuthManager artik
    // SignInErrorKind donduruyor, metni buradan CloudErrorMapper seciyor.
    val signInConfigBroken = L10n(
        "Bu sürümde Google ile giriş yapılandırılmamış. Lütfen uygulamanın güncel sürümünü kur.",
        "لم تُهيّأ ميزة الدخول عبر Google في هذا الإصدار. يُرجى تثبيت أحدث إصدار من التطبيق.",
        "Google sign-in is not configured in this build. Please install the latest version of the app.",
        "Google-Anmeldung ist in dieser Version nicht eingerichtet. Bitte installiere die neueste Version der App.",
        "La connexion Google n'est pas configurée dans cette version. Installe la dernière version de l'application."
    )
    val signInNoAccount = L10n(
        "Bu uygulama için uygun bir Google hesabı bulunamadı. Cihaz ayarlarından bir Google hesabı ekleyip tekrar dene.",
        "لم يُعثر على حساب Google مناسب لهذا التطبيق. أضف حساب Google من إعدادات الجهاز ثم أعد المحاولة.",
        "No suitable Google account was found for this app. Add a Google account in device settings and try again.",
        "Für diese App wurde kein passendes Google-Konto gefunden. Füge in den Geräteeinstellungen ein Google-Konto hinzu und versuche es erneut.",
        "Aucun compte Google compatible n'a été trouvé pour cette application. Ajoute un compte Google dans les paramètres de l'appareil puis réessaie."
    )
    val signInSetupError = L10n(
        "Google ile giriş doğrulanamadı; bu sürümün imzası veya yapılandırması Firebase'de kayıtlı olmayabilir. Güncel sürümü kurmayı dene.",
        "تعذّر التحقق من الدخول عبر Google؛ قد لا يكون توقيع هذا الإصدار أو إعداده مسجّلًا في Firebase. جرّب تثبيت أحدث إصدار.",
        "Google sign-in could not be verified; this build's signature or configuration may not be registered in Firebase. Try installing the latest version.",
        "Google-Anmeldung konnte nicht verifiziert werden; Signatur oder Konfiguration dieser Version ist möglicherweise nicht in Firebase registriert. Installiere die neueste Version.",
        "La connexion Google n'a pas pu être vérifiée ; la signature ou la configuration de cette version n'est peut-être pas enregistrée dans Firebase. Essaie d'installer la dernière version."
    )
    val signInRetry = L10n(
        "Giriş tamamlanamadı. Lütfen tekrar dene.",
        "تعذّر إكمال تسجيل الدخول. يُرجى المحاولة مرة أخرى.",
        "Sign-in could not be completed. Please try again.",
        "Anmeldung konnte nicht abgeschlossen werden. Bitte versuche es erneut.",
        "La connexion n'a pas pu aboutir. Réessaie."
    )
    val signInAccountCollision = L10n(
        "Bu e-posta adresi başka bir giriş yöntemiyle zaten kayıtlı.",
        "هذا البريد الإلكتروني مسجّل بالفعل بطريقة دخول أخرى.",
        "This email address is already registered with another sign-in method.",
        "Diese E-Mail-Adresse ist bereits mit einer anderen Anmeldemethode registriert.",
        "Cette adresse e-mail est déjà enregistrée avec une autre méthode de connexion."
    )
    val signInUserDisabled = L10n(
        "Bu Google hesabı devre dışı bırakılmış veya silinmiş.",
        "حساب Google هذا معطّل أو محذوف.",
        "This Google account has been disabled or deleted.",
        "Dieses Google-Konto wurde deaktiviert oder gelöscht.",
        "Ce compte Google a été désactivé ou supprimé."
    )
    // Parola alaninin etiketi: BackupPasswordDialog'da sabit "Parola" yaziyordu,
    // Arapca/Almanca/Fransizca kullanici Turkce etiket goruyordu.
    val passwordFieldLabel = L10n(
        "Parola",
        "كلمة المرور",
        "Password",
        "Passwort",
        "Mot de passe"
    )
    val passwordRequired = L10n(
        "Bu yedek parola ile korunuyor; parolayı gir.",
        "هذه النسخة الاحتياطية محمية بكلمة مرور؛ أدخل كلمة المرور.",
        "This backup is password-protected; enter the password.",
        "Diese Sicherung ist passwortgeschützt; bitte Passwort eingeben.",
        "Cette sauvegarde est protégée par mot de passe ; saisis le mot de passe."
    )
    val syncConflictDetected = L10n(
        "Senkronizasyon çakışması algılandı.",
        "تم اكتشاف تعارض في المزامنة.",
        "Sync conflict detected.",
        "Synchronisationskonflikt erkannt.",
        "Conflit de synchronisation détecté."
    )

    // --------------------------------------------------- giris sonrasi bulut
    // Google ile giris yapildiginda bulutta yedek varsa kullaniciya SORULUR.
    // (Eskiden sorulmuyor, hatta yerel veri sessizce buluta yukleniyordu.)
    val cloudBackupFoundTitle = L10n(
        "Bulutta Yedek Bulundu",
        "تم العثور على نسخة في السحابة",
        "Cloud Backup Found",
        "Cloud-Backup gefunden",
        "Sauvegarde cloud trouvée"
    )
    val cloudBackupFoundMessage = L10n(
        "Bu hesaba ait bulutta {0} tarihli bir zikir yedeği var. Ne yapmak istersiniz?",
        "توجد نسخة احتياطية من الأذكار على السحابة لهذا الحساب بتاريخ {0}. ماذا تريد أن تفعل؟",
        "There is a dhikr backup from {0} in the cloud for this account. What would you like to do?",
        "Für dieses Konto gibt es ein Dhikr-Backup vom {0} in der Cloud. Was möchtest du tun?",
        "Une sauvegarde de dhikr du {0} existe dans le cloud pour ce compte. Que veux-tu faire ?"
    )
    val cloudChoiceMerge = L10n(
        "Birleştir (en güvenli – en yüksek sayaçlar korunur)",
        "دمج (الأكثر أمانًا – يُحافظ على أكبر العدادات)",
        "Merge (safest – keeps the highest counts)",
        "Zusammenführen (am sichersten – höchste Zähler bleiben)",
        "Fusionner (le plus sûr – garde les compteurs les plus élevés)"
    )
    val cloudChoiceUseRemote = L10n(
        "Buluttakini yükle (bu cihazın verisinin üzerine yazar)",
        "تحميل نسخة السحابة (يكتب فوق بيانات هذا الجهاز)",
        "Load cloud backup (overwrites this device's data)",
        "Cloud-Backup laden (überschreibt die Daten dieses Geräts)",
        "Charger la sauvegarde cloud (écrase les données de cet appareil)"
    )
    val cloudChoiceKeepLocal = L10n(
        "Bu cihazda kal (bulutu bu cihazla güncelle)",
        "إبقاء بيانات هذا الجهاز (تحديث السحابة بها)",
        "Stay on this device (update the cloud with it)",
        "Auf diesem Gerät bleiben (Cloud damit aktualisieren)",
        "Rester sur cet appareil (mettre le cloud à jour)"
    )
    val cloudNoBackupUploadedLocal = L10n(
        "Bulutta kayıtlı yedek bulunamadı; bu cihazın verisi buluta yüklendi.",
        "لا توجد نسخة محفوظة في السحابة؛ تم رفع بيانات هذا الجهاز إليها.",
        "No backup was found in the cloud; this device's data was uploaded.",
        "Kein Backup in der Cloud gefunden; die Daten dieses Geräts wurden hochgeladen.",
        "Aucune sauvegarde trouvée dans le cloud ; les données de cet appareil ont été téléversées."
    )
    val cloudRestoredOnSignIn = L10n(
        "Buluttaki yedeğiniz geri yüklendi.",
        "تمت استعادة نسختك الاحتياطية من السحابة.",
        "Your cloud backup has been restored.",
        "Dein Cloud-Backup wurde wiederhergestellt.",
        "Ta sauvegarde cloud a été restaurée."
    )

    // --- Play uyumlulugu: uygulama ici gizlilik politikasi erisimi ---
    val privacyPolicy = L10n(
        "Gizlilik Politikası",
        "سياسة الخصوصية",
        "Privacy Policy",
        "Datenschutzerklärung",
        "Politique de confidentialité"
    )
    val privacyPolicyUnavailable = L10n(
        "Gizlilik politikası adresi bu sürümde henüz tanımlı değil. Lütfen uygulama mağazası sayfasındaki bağlantıyı kullan.",
        "لم يتم تعريف رابط سياسة الخصوصية في هذا الإصدار بعد. يرجى استخدام الرابط في صفحة التطبيق بالمتجر.",
        "The privacy policy address is not configured in this build yet. Please use the link on the app's store listing.",
        "Die Adresse der Datenschutzerklärung ist in dieser Version noch nicht hinterlegt. Bitte nutze den Link im Store-Eintrag der App.",
        "L'adresse de la politique de confidentialité n'est pas encore configurée dans cette version. Utilisez le lien sur la fiche de l'application dans le store."
    )
    val openLinkFailed = L10n(
        "Bağlantı açılamadı; cihazda uygun bir tarayıcı bulunamadı.",
        "تعذّر فتح الرابط؛ لا يوجد متصفح مناسب على الجهاز.",
        "The link could not be opened; no suitable browser was found on the device.",
        "Der Link konnte nicht geöffnet werden; es wurde kein passender Browser gefunden.",
        "Le lien n'a pas pu être ouvert ; aucun navigateur adapté n'a été trouvé sur l'appareil."
    )

    // --- Play uyumlulugu: hesap + iliskili veri silme ---
    val deleteAccount = L10n(
        "Hesabı Sil",
        "حذف الحساب",
        "Delete Account",
        "Konto löschen",
        "Supprimer le compte"
    )
    val deleteAccountConfirmTitle = L10n(
        "Hesap kalıcı olarak silinsin mi?",
        "هل تريد حذف الحساب نهائيًا؟",
        "Delete account permanently?",
        "Konto endgültig löschen?",
        "Supprimer définitivement le compte ?"
    )
    val deleteAccountConfirmMsg = L10n(
        "Google hesabın ve buluttaki tüm yedeklerin (zikirler, geçmiş, ayarlar, hatırlatıcılar) kalıcı olarak silinir. Bu işlem geri alınamaz. Bu cihazdaki kayıtlı veriler korunur.",
        "سيتم حذف حسابك على Google وجميع نسخك الاحتياطية في السحابة (الأذكار، السجل، الإعدادات، المنبهات) نهائيًا. لا يمكن التراجع عن هذا الإجراء. تبقى بيانات هذا الجهاز محفوظة.",
        "Your Google account and all of its cloud backups (dhikrs, history, settings, reminders) will be permanently deleted. This action cannot be undone. Data stored on this device is kept.",
        "Dein Google-Konto und alle zugehörigen Cloud-Backups (Dhikr, Verlauf, Einstellungen, Erinnerungen) werden endgültig gelöscht. Dieser Schritt lässt sich nicht rückgängig machen. Die Daten auf diesem Gerät bleiben erhalten.",
        "Votre compte Google et toutes ses sauvegardes cloud (dhikr, historique, paramètres, rappels) seront définitivement supprimés. Cette action est irréversible. Les données enregistrées sur cet appareil sont conservées."
    )
    val deleteAccountConfirmBtn = L10n(
        "Kalıcı olarak sil",
        "حذف نهائي",
        "Delete permanently",
        "Endgültig löschen",
        "Supprimer définitivement"
    )
    val deleteAccountSuccess = L10n(
        "Hesabın ve buluttaki verilerin kalıcı olarak silindi.",
        "تم حذف حسابك وبياناتك في السحابة نهائيًا.",
        "Your account and its cloud data have been permanently deleted.",
        "Dein Konto und die zugehörigen Cloud-Daten wurden endgültig gelöscht.",
        "Votre compte et ses données cloud ont été définitivement supprimés."
    )
    val deleteAccountFailed = L10n(
        "Hesap silinemedi; hiçbir veri silinmedi. Lütfen daha sonra tekrar dene.",
        "تعذّر حذف الحساب؛ لم يتم حذف أي بيانات. يرجى المحاولة لاحقًا.",
        "The account could not be deleted; no data was removed. Please try again later.",
        "Das Konto konnte nicht gelöscht werden; es wurden keine Daten entfernt. Bitte später erneut versuchen.",
        "Le compte n'a pas pu être supprimé ; aucune donnée n'a été effacée. Veuillez réessayer plus tard."
    )
    /** Bulut verisi silindi ama Firebase Auth hesabi silinemedi -> yarim durumu ASLA basari gosterme. */
    val deleteAccountCloudOnlyDeleted = L10n(
        "Bulut verilerin silindi ancak Google hesabı silinemedi. Hesabı silmek için tekrar dene.",
        "تم حذف بياناتك في السحابة، لكن تعذّر حذف حساب Google. أعد المحاولة لحذف الحساب.",
        "Your cloud data was deleted, but the Google account could not be deleted. Please try again to delete the account.",
        "Deine Cloud-Daten wurden gelöscht, aber das Google-Konto konnte nicht gelöscht werden. Bitte versuche erneut, das Konto zu löschen.",
        "Vos données cloud ont été supprimées, mais le compte Google n'a pas pu être supprimé. Veuillez réessayer pour supprimer le compte."
    )
    val accountDeletionReauthRequired = L10n(
        "Google hesabın yeniden doğrulanamadı; hesap silinmedi. Bağlantını kontrol edip tekrar dene.",
        "تعذّرت إعادة التحقق من حساب Google؛ لم يتم حذف الحساب. تحقق من اتصالك ثم أعد المحاولة.",
        "Your Google account could not be re-verified, so the account was not deleted. Check your connection and try again.",
        "Dein Google-Konto konnte nicht erneut bestätigt werden; das Konto wurde nicht gelöscht. Prüfe die Verbindung und versuche es erneut.",
        "Votre compte Google n'a pas pu être revalidé ; le compte n'a donc pas été supprimé. Vérifiez votre connexion puis réessayez."
    )
}
