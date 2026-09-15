package com.example.data.cloud

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val credentialManager by lazy { CredentialManager.create(context) }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
    }

    init {
        try {
            _currentUser.value = auth.currentUser
            auth.addAuthStateListener(authStateListener)
        } catch (e: Exception) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Firebase Auth init failed", e)
            }
        }
    }

    fun cleanup() {
        try {
            auth.removeAuthStateListener(authStateListener)
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Bu APK'nin hangi Firebase projesiyle derlendigini kontrol eder.
     *
     * Gecmiste CI, gercek app/google-services.json dosyasini sahte
     * (ci-dummy) surumle eziyordu. O APK'da Google ile giris daima
     * "hesap bulunamadi" gibi anlasilmaz bir hatayla patliyordu; cunku
     * web istemci kimligi uydurma bir projeye aitti. Boyle bir build'i
     * kullanici butona basar basmaz, net bir mesajla yakaliyoruz.
     *
     * @return sorun varsa LOG icin teknik detay, yoksa null.
     *         Kullaniciya gosterilen metin `SignInErrorKind.CONFIG_BROKEN`
     *         uzerinden 5 dilde uretilir (CloudErrorMapper.resolveSignIn).
     */
    private fun detectBrokenFirebaseConfig(): String? {
        val projectId = try {
            context.getString(com.example.R.string.project_id)
        } catch (e: Exception) {
            null
        }
        return when {
            projectId.isNullOrBlank() ->
                "Firebase yapılandırması bu APK'da bulunamadı (project_id yok). Google ile giriş bu build'de çalışmaz."
            projectId.contains("dummy", ignoreCase = true) ->
                "Bu APK sahte (test) Firebase yapılandırmasıyla derlenmiş (proje: $projectId). " +
                    "Google ile giriş çalışmaz; gerçek google-services.json ile derlenmiş bir APK kurman gerekiyor."
            else -> null
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
        return try {
            detectBrokenFirebaseConfig()?.let { problem ->
                if (com.example.BuildConfig.DEBUG) {
                    Log.e("AuthManager", problem)
                }
                return Result.failure(
                    SignInFailedException(SignInErrorKind.CONFIG_BROKEN, problem)
                )
            }

            val webClientId = try {
                context.getString(com.example.R.string.default_web_client_id)
            } catch (e: Exception) {
                if (com.example.BuildConfig.DEBUG) {
                    Log.e("AuthManager", "default_web_client_id not found in strings.xml", e)
                }
                return Result.failure(
                    SignInFailedException(
                        SignInErrorKind.CONFIG_BROKEN,
                        "default_web_client_id is missing from google-services.json"
                    )
                )
            }

            if (webClientId.isBlank() || webClientId == "REDACTED" || webClientId.contains("REDACTED")) {
                return Result.failure(
                    SignInFailedException(
                        SignInErrorKind.CONFIG_BROKEN,
                        "default_web_client_id is blank or a placeholder (REDACTED)"
                    )
                )
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                    ?: throw SignInFailedException(
                        SignInErrorKind.SIGNIN_RETRY,
                        "authResult.user was null after signInWithCredential"
                    )
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(
                    SignInFailedException(
                        SignInErrorKind.SIGNIN_RETRY,
                        "Unexpected credential type: ${credential.type}"
                    )
                )
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(SignInCancelledException())
        } catch (e: NoCredentialException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.w("AuthManager", "No credentials available on device/emulator", e)
            }
            Result.failure(
                SignInFailedException(
                    SignInErrorKind.NO_ACCOUNT,
                    "NoCredentialException: no usable Google account on this device",
                    e
                )
            )
        } catch (e: GetCredentialProviderConfigurationException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Credential provider configuration error", e)
            }
            Result.failure(
                SignInFailedException(
                    SignInErrorKind.SIGNIN_SETUP,
                    "GetCredentialProviderConfigurationException",
                    e
                )
            )
        } catch (e: GetCredentialCustomException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Credential custom error: ${e.type}", e)
            }
            Result.failure(
                SignInFailedException(
                    SignInErrorKind.SIGNIN_SETUP,
                    "GetCredentialCustomException: ${e.type}",
                    e
                )
            )
        } catch (e: GetCredentialException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "GetCredentialException", e)
            }
            Result.failure(
                SignInFailedException(
                    SignInErrorKind.UNKNOWN,
                    "GetCredentialException: ${e.message ?: "unknown"}",
                    e
                )
            )
        } catch (e: FirebaseNetworkException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.w("AuthManager", "Network error during Google sign-in", e)
            }
            Result.failure(
                SignInFailedException(SignInErrorKind.NETWORK, "FirebaseNetworkException during sign-in", e)
            )
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Invalid credential", e)
            }
            Result.failure(
                SignInFailedException(
                    SignInErrorKind.SIGNIN_SETUP,
                    "FirebaseAuthInvalidCredentialsException",
                    e
                )
            )
        } catch (e: FirebaseAuthUserCollisionException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.w("AuthManager", "Account collision", e)
            }
            Result.failure(
                SignInFailedException(SignInErrorKind.ACCOUNT_COLLISION, "FirebaseAuthUserCollisionException", e)
            )
        } catch (e: FirebaseAuthInvalidUserException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.w("AuthManager", "Invalid user", e)
            }
            Result.failure(
                SignInFailedException(SignInErrorKind.USER_DISABLED, "FirebaseAuthInvalidUserException", e)
            )
        } catch (e: FirebaseAuthException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Firebase auth error: ${e.javaClass.simpleName}", e)
            }
            Result.failure(
                SignInFailedException(
                    SignInErrorKind.UNKNOWN,
                    "FirebaseAuthException: ${e.message ?: e.javaClass.simpleName}",
                    e
                )
            )
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Google Sign-In failed", e)
            }
            if (e is SignInFailedException) {
                // Yukarida firlatilan sinifli hata: sarmalanmadan gecirilir,
                // yoksa kind bilgisi kaybolur ve kullaniciya genel mesaj duser.
                return Result.failure(e)
            }
            Result.failure(
                SignInFailedException(
                    SignInErrorKind.UNKNOWN,
                    e.message ?: "Google sign-in failed: ${e.javaClass.simpleName}",
                    e
                )
            )
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Sign out error", e)
            }
            Result.failure(e)
        }
    }

    /**
     * Hesabi GERCEKTEN siler (sign-out / disable degil).
     *
     * Neden var: Google Play, hesap olusturan uygulamalarda kullaniciya hesap
     * silme yolu sunmayi sart kosuyor. Cagri tarafi (ViewModel) once
     * `SyncManager.deleteAllUserData` ile Firestore verisini siler; o adim
     * basarili olmadan buraya gelmez. Boylece "hesap silindi" denildiginde
     * geride kullanici verisi kalmaz.
     *
     * Re-auth: Firebase hassas islemlerde (delete) yakin zamanli giris ister
     * (`ERROR_RECENT_LOGIN_REQUIRED`). Bu durumda kullaniciya parola SORMAYIZ
     * (uygulamada parola yok, giris Google SSO); mevcut Credential Manager +
     * Google akisiyla taze ID token alinip `reauthenticate` cagrilir ve silme
     * tekrar denenir. Basarisiz/iptal olursa `REAUTH_REQUIRED` doner ve hesap
     * SILINMIS GIBI GOSTERILMEZ.
     *
     * Guvenlik: log yalnizca exception sinif adini yazar; token/credential/
     * e-posta/UID loglanmaz.
     */
    suspend fun deleteAccount(activityContext: Context): Result<Unit> {
        val user = auth.currentUser
            ?: return Result.failure(
                SignInFailedException(
                    SignInErrorKind.NO_ACCOUNT,
                    "deleteAccount: no signed-in Firebase user"
                )
            )

        return try {
            try {
                user.delete().await()
            } catch (e: FirebaseAuthException) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                if (!isRecentLoginRequired(e)) throw e
                val credential = requestGoogleAuthCredential(activityContext)
                user.reauthenticate(credential).await()
                user.delete().await()
            }
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "deleteAccount failed: ${e.javaClass.simpleName}")
            }
            if (e is SignInFailedException) return Result.failure(e)
            val kind = when {
                isRecentLoginRequired(e) -> SignInErrorKind.REAUTH_REQUIRED
                e is FirebaseNetworkException -> SignInErrorKind.NETWORK
                else -> SignInErrorKind.UNKNOWN
            }
            Result.failure(
                SignInFailedException(kind, "deleteAccount: ${e.javaClass.simpleName}", e)
            )
        }
    }

    /**
     * Firebase'in "recent login required" hatasi mi?
     *
     * Sinif adina VE errorCode'a bakiliyor: boylece Firebase sinifini derleme
     * zamaninda sabit bagimliliga cevirmeden de taninir (projenin
     * `CloudErrorMapper.classifyByShape` yaklasimiyla ayni).
     */
    private fun isRecentLoginRequired(error: Throwable?): Boolean {
        var current: Throwable? = error
        var hops = 0
        while (current != null && hops < 8) {
            hops++
            if (current.javaClass.simpleName == "FirebaseAuthRecentLoginRequiredException") return true
            if (current is FirebaseAuthException &&
                current.errorCode == "ERROR_RECENT_LOGIN_REQUIRED"
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }

    /**
     * Yalnizca RE-AUTH icin: cihazdaki MEVCUT Google hesabindan taze ID token
     * alir. `signInWithGoogle` bilerek DEGISTIRILMEDI (calisan giris akisina
     * dokunmamak icin); burada hesap secici kapali ve otomatik secim acik,
     * yani kullaniciya yeni hesap sectirilmez.
     */
    private suspend fun requestGoogleAuthCredential(
        activityContext: Context
    ): com.google.firebase.auth.AuthCredential {
        val webClientId = context.getString(com.example.R.string.default_web_client_id)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val response = credentialManager.getCredential(
            request = request,
            context = activityContext
        )
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            return GoogleAuthProvider.getCredential(idToken, null)
        }
        throw SignInFailedException(
            SignInErrorKind.REAUTH_REQUIRED,
            "Re-auth returned unexpected credential type: ${credential.type}"
        )
    }
}

/**
 * Kullanici Google hesap seciciyi KENDI ISTEGIYLE kapatti.
 *
 * Neden ayri tip: bu bir hata degildir. Eskiden duz `Exception(...)` ile
 * dondugu icin cagri tarafi bunu gercek bir giris hatasindan ayirt edemiyor
 * ve ekranda kirmizi bir "Giris islemi iptal edildi" mesaji cikiyordu.
 * Artik ViewModel bu tipi gorunce mesaj GOSTERMEZ.
 */
class SignInCancelledException(
    message: String = "Sign-in cancelled by the user."
) : Exception(message)
