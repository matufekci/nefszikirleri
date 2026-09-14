package com.example.data.cloud

/**
 * Google ile giris basarisizliginin KULLANICIYA ANLATILABILIR sinifi.
 *
 * Neden var: AuthManager 13 ayri yerde sabit TURKCE metin uretiyordu
 * ("Bu APK sahte Firebase yapilandirmasiyla derlenmis...", "Google kimlik
 * bilgisi Firebase tarafindan reddedildi..." vb.). Arapca/Almanca/Fransizca
 * kullanici Turkce mesaj goruyordu; ayrica teknik ayrintilar (SHA-1, project
 * id, exception tipi) kullanicinin onune dusuyordu.
 *
 * Yeni kural: AuthManager yalnizca SINIF + log icin Ingilizce detay dondurur;
 * kullanici metnini `CloudErrorMapper.resolveSignIn` 5 dilde uretir. Teknik
 * detay DEBUG log'da kalir.
 */
enum class SignInErrorKind {
    /** APK eksik/sahte Firebase yapilandirmasiyla derlenmis (project_id, web client id). */
    CONFIG_BROKEN,

    /** Cihazda bu uygulama icin kullanilabilir Google hesabi yok. */
    NO_ACCOUNT,

    /** Imza (SHA-1) / web istemci kimligi Firebase'de kayitli degil. */
    SIGNIN_SETUP,

    /** Beklenmeyen yanit / hesap bilgisi alinamadi: tekrar denenebilir. */
    SIGNIN_RETRY,

    /** Ayni e-posta baska bir giris yontemiyle zaten kayitli. */
    ACCOUNT_COLLISION,

    /** Google hesabi devre disi veya silinmis. */
    USER_DISABLED,

    /** Baglanti yok / zaman asimi. */
    NETWORK,

    /** Siniflandirilamayan giris hatasi -> cagri tarafi kendi genel mesajini kullanir. */
    UNKNOWN
}

/**
 * Giris hatasi. `message` yalnizca LOG icindir (Ingilizce); kullaniciya
 * gosterilecek metin `SignInErrorKind`'dan uretilir.
 */
class SignInFailedException(
    val kind: SignInErrorKind,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
