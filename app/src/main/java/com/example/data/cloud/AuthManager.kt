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
import com.google.firebase.auth.FirebaseAuth
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

    init {
        try {
            _currentUser.value = auth.currentUser
            auth.addAuthStateListener { firebaseAuth ->
                _currentUser.value = firebaseAuth.currentUser
            }
        } catch (e: Exception) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Firebase Auth init failed", e)
            }
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
        return try {
            val serverClientId = try {
                context.getString(com.example.R.string.default_web_client_id)
            } catch (e: Exception) {
                "72481680894-51gufn22qa2ht863u3c118ledjv55dmu.apps.googleusercontent.com"
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
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
            Result.failure(Exception("Cihazda kayıtlı Google hesabı bulunamadı. Lütfen cihaz ayarlarından bir Google hesabı ekleyin."))
        } catch (e: GetCredentialProviderConfigurationException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Credential provider configuration error", e)
            }
            Result.failure(Exception("Google Hizmetleri yapılandırması doğrulanamadı."))
        } catch (e: GetCredentialCustomException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Credential custom error: ${e.type}", e)
            }
            Result.failure(Exception("Kimlik doğrulama hatası: ${e.type}"))
        } catch (e: GetCredentialException) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "GetCredentialException", e)
            }
            Result.failure(Exception("Giriş yapılamadı (${e.message ?: "Bilinmeyen hata"})."))
        } catch (e: Exception) {
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
            if (com.example.BuildConfig.DEBUG) {
                Log.e("AuthManager", "Sign out error", e)
            }
            Result.failure(e)
        }
    }
}
