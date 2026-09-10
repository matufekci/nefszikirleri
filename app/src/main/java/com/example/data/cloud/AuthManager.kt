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
import androidx.credentials.exceptions.GetCredentialInterruptedException
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
     * @return sorun varsa kullaniciya gosterilecek mesaj, yoksa null
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
                return Result.failure(Exception(problem))
            }

            val webClientId = try {
                context.getString(com.example.R.string.default_web_client_id)
            } catch (e: Exception) {
                if (com.example.BuildConfig.DEBUG) {
                    Log.e("AuthManager", "default_web_client_id not found in strings.xml", e)
                }
                return Result.failure(Exception("Google istemci kimliği yapılandırılamadı. google-services.json dosyasını kontrol edin."))
            }

            if (webClientId.isBlank() || webClientId == "REDACTED" || webClientId.contains("REDACTED")) {
                return Result.failure(Exception("Google istemci kimliği geçersiz veya örnek dosyadan geliyor. Gerçek google-services.json gerekli."))
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
                val user = authResult.user ?: throw IllegalStateException("Kullanıcı bilgisi alınamadı.")
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Beklenmeyen kimlik doğrulama yanıtı."))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Giriş işlemi iptal edildi."))
        } catch (e: NoCredentialException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.w("AuthManager", "No credentials available on device/emulator", e)
            }
            Result.failure(Exception("Bu uygulama için kullanılabilir bir Google hesabı bulunamadı. " +
                    "Önce cihaz ayarlarında bir Google hesabının ekli olduğundan emin ol; hesap ekliyse " +
                    "bu APK'nin imza sertifikası (SHA-1) Firebase'e kayıtlı değildir."))
        } catch (e: GetCredentialProviderConfigurationException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Credential provider configuration error", e)
            }
            Result.failure(Exception("Google Hizmetleri yapılandırması doğrulanamadı."))
        } catch (e: GetCredentialCustomException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Credential custom error: ${e.type}", e)
            }
            Result.failure(Exception("Google kimlik doğrulama hatası (${e.type}). " +
                    "Bu genelde APK'nin imza SHA-1 değerinin veya web istemci kimliğinin " +
                    "Firebase Console'da kayıtlı olmadığı anlamına gelir."))
        } catch (e: GetCredentialException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "GetCredentialException", e)
            }
            Result.failure(Exception("Giriş yapılamadı (${e.message ?: "Bilinmeyen hata"})."))
        } catch (e: FirebaseNetworkException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.w("AuthManager", "Network error during Google sign-in", e)
            }
            Result.failure(Exception("İnternet bağlantısı kurulamadı. Bağlantını kontrol edip tekrar dene."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Invalid credential", e)
            }
            Result.failure(Exception("Google kimlik bilgisi Firebase tarafından reddedildi. " +
                "Bu APK'nin imzası veya yapılandırması hesabınla eşleşmiyor."))
        } catch (e: FirebaseAuthUserCollisionException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.w("AuthManager", "Account collision", e)
            }
            Result.failure(Exception("Bu e-posta adresi başka bir giriş yöntemiyle zaten kayıtlı."))
        } catch (e: FirebaseAuthInvalidUserException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.w("AuthManager", "Invalid user", e)
            }
            Result.failure(Exception("Bu Google hesabı devre dışı bırakılmış veya silinmiş."))
        } catch (e: FirebaseAuthException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Firebase auth error: ${e.javaClass.simpleName}", e)
            }
            Result.failure(Exception("Google girişi başarısız: ${e.message ?: e.javaClass.simpleName}"))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Google Sign-In failed", e)
            }
            val msg = e.localizedMessage ?: "Google girişi sırasında bir hata oluştu."
            Result.failure(Exception(msg))
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
}
