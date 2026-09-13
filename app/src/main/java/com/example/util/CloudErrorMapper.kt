package com.example.util

import com.example.data.backup.PasswordRequiredException
import com.example.data.backup.WrongPasswordException
import com.example.data.cloud.CloudDataCorruptionException
import com.example.data.cloud.NoCloudBackupException
import com.example.data.cloud.SignInCancelledException
import com.example.ui.L10n
import com.example.ui.UiText

/**
 * Bulut/backup hatasinin kullaniciya GOSTERILEBILECEK sinifi.
 *
 * Neden var: cagri taraflari `e.localizedMessage ?: "genel mesaj"` kalibini
 * kullaniyordu. Sonuc iki yonde de bozuktu:
 *  1) Firestore/Firebase mesajlari Ingilizce ve teknik ("UNAVAILABLE: Unable
 *     to resolve host ...") -> Arapca/Almanca/Fransizca kullaniciya Ingilizce
 *     teknik metin gosteriliyordu.
 *  2) Kimi yerlerde sabit tek dilli metin vardi ("Parola gerekli",
 *     "Export Error").
 * Bu sinif hatayi sinifa ayirir; metni `UiText` (5 dil zorunlu) uretir.
 */
enum class BackupErrorKind {
    NETWORK,
    PERMISSION,
    NO_BACKUP,
    CORRUPT,
    PASSWORD_REQUIRED,
    PASSWORD_WRONG,
    CANCELLED,
    UNKNOWN
}

object CloudErrorMapper {

    /** Sonsuz sebep zincirine karsi ust sinir. */
    private const val MAX_CAUSE_HOPS = 16

    /**
     * Hatayi siniflandirir. Once TIP'e bakar (kendi tanimli tiplerimiz kesin
     * bilgi verir), sonra sinif adi + mesaj icerigine (Firebase siniflarini
     * derleme zamaninda bagimliliga cevirmeden tanimak icin).
     */
    fun classify(error: Throwable?): BackupErrorKind {
        var current: Throwable? = error
        var hops = 0
        while (current != null && hops < MAX_CAUSE_HOPS) {
            hops++
            when (current) {
                is SignInCancelledException -> return BackupErrorKind.CANCELLED
                is NoCloudBackupException -> return BackupErrorKind.NO_BACKUP
                is CloudDataCorruptionException -> return BackupErrorKind.CORRUPT
                is PasswordRequiredException -> return BackupErrorKind.PASSWORD_REQUIRED
                is WrongPasswordException -> return BackupErrorKind.PASSWORD_WRONG
            }
            val byShape = classifyByShape(current)
            if (byShape != null) return byShape
            current = current.cause
        }
        return BackupErrorKind.UNKNOWN
    }

    /** Kullanicinin Google seciciyi kendi istegiyle kapatmasi -> hata degil. */
    fun isCancelled(error: Throwable?): Boolean = classify(error) == BackupErrorKind.CANCELLED

    /**
     * Sinifa ait 5 dilli metni dondurur; `fallback` yalnizca UNKNOWN icin
     * kullanilir (cagri tarafi kendi genel mesajini gecirmeye devam eder).
     */
    fun resolve(error: Throwable?, lang: String, fallback: String): String =
        textFor(classify(error))?.get(lang) ?: fallback

    /** Ayni sey, fallback zaten 5 dilli bir `L10n` ise. */
    fun resolve(error: Throwable?, lang: String, fallback: L10n): String =
        resolve(error, lang, fallback.get(lang))

    fun textFor(kind: BackupErrorKind): L10n? = when (kind) {
        BackupErrorKind.NETWORK -> UiText.cloudErrorNetwork
        BackupErrorKind.PERMISSION -> UiText.cloudErrorPermission
        BackupErrorKind.NO_BACKUP -> UiText.cloudErrorNoBackup
        BackupErrorKind.CORRUPT -> UiText.cloudErrorCorrupt
        BackupErrorKind.PASSWORD_REQUIRED -> UiText.passwordRequired
        BackupErrorKind.PASSWORD_WRONG -> UiText.wrongPassword
        BackupErrorKind.CANCELLED -> UiText.cloudErrorCancelled
        BackupErrorKind.UNKNOWN -> null
    }

    private fun classifyByShape(t: Throwable): BackupErrorKind? {
        val name = t.javaClass.name
        val message = t.message.orEmpty()
        val upper = message.uppercase()

        if (name.contains("CancellationException")) return BackupErrorKind.CANCELLED

        // --- yetki ---
        if (upper.contains("PERMISSION_DENIED") ||
            upper.contains("MISSING OR INSUFFICIENT PERMISSIONS") ||
            name.contains("FirebaseAuthInvalidUserException") ||
            name.contains("FirebaseAuthInvalidCredentialsException") ||
            name.contains("FirebaseAuthRecentLoginRequired")
        ) {
            return BackupErrorKind.PERMISSION
        }

        // --- baglanti / zaman asimi ---
        // NOT: duz IOException BILEREK burada degil; yerel dosya yaziminda da
        // firlatiliyor ve "internet yok" mesaji yanlis olurdu.
        if (name.contains("FirebaseNetworkException") ||
            name.contains("UnknownHostException") ||
            name.contains("SocketTimeoutException") ||
            name.contains("ConnectException") ||
            name.contains("SSLHandshakeException") ||
            name.contains("InterruptedIOException") ||
            upper.startsWith("UNAVAILABLE") ||
            upper.startsWith("DEADLINE_EXCEEDED") ||
            upper.contains("UNABLE TO RESOLVE HOST") ||
            upper.contains("FAILED TO CONNECT") ||
            upper.contains("NETWORK_ERROR")
        ) {
            return BackupErrorKind.NETWORK
        }

        return null
    }
}
