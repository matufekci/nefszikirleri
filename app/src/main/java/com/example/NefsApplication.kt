package com.example

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.DailyEvaluationWorker
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NefsApplication : Application(), Configuration.Provider {

    companion object {
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun scheduleDailyEvaluation(context: android.content.Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .setRequiresDeviceIdle(false)
                    .build()

                // Align to next 08:00 to have consistent daily evaluation time
                val now = java.util.Calendar.getInstance()
                val nextRun = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 8)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                    if (before(now)) add(java.util.Calendar.DAY_OF_YEAR, 1)
                }
                val initialDelay = nextRun.timeInMillis - now.timeInMillis

                val periodicWorkRequest = PeriodicWorkRequestBuilder<DailyEvaluationWorker>(1, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "DailyEvaluationWork",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    periodicWorkRequest
                )
            } catch (e: Exception) {
                if (com.example.BuildConfig.DEBUG) {
                    android.util.Log.e("NefsApplication", "Failed to schedule DailyEvaluationWorker", e)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        initializeAppCheck()
        scheduleDailyEvaluation(this)
    }

    private fun initializeAppCheck() {
        try {
            // Skip AppCheck initialization in unit tests (Robolectric)
            // Detect Robolectric by checking if we're in test environment
            try {
                Class.forName("org.robolectric.RobolectricTestRunner")
                // We're in a Robolectric test, skip AppCheck
                return
            } catch (_: ClassNotFoundException) {
                // Not in test, continue
            }

            // Also skip if running in instrumentation test with test application
            if (packageName.contains(".test") || packageName.endsWith(".test")) {
                return
            }

            // Ensure Firebase is initialized - may fail with dummy google-services.json in CI, that's ok
            val firebaseApp = try {
                FirebaseApp.initializeApp(this) ?: FirebaseApp.getInstance()
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.w("NefsApplication", "Firebase init failed (likely dummy google-services.json in CI), skipping AppCheck", e)
                }
                return
            }

            // If FirebaseApp is null or has no options, skip
            if (firebaseApp.options.projectId.isBlank() || firebaseApp.options.projectId == "nefs-zikirleri" && firebaseApp.options.applicationId.contains("REDACTED")) {
                // Check if it's dummy config
                try {
                    val appId = firebaseApp.options.applicationId
                    if (appId.contains("REDACTED")) {
                        if (BuildConfig.DEBUG) {
                            Log.d("NefsApplication", "Dummy Firebase config detected (REDACTED), skipping AppCheck for CI")
                        }
                        return
                    }
                } catch (_: Exception) {}
            }

            val firebaseAppCheck = try {
                FirebaseAppCheck.getInstance(firebaseApp)
            } catch (e: Exception) {
                FirebaseAppCheck.getInstance()
            }

            // Debug token from .env / BuildConfig if provided
            val debugToken = try {
                val field = BuildConfig::class.java.getField("FIREBASE_APPCHECK_DEBUG_TOKEN")
                field.get(null) as? String
            } catch (_: Exception) {
                null
            }

            if (BuildConfig.DEBUG) {
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                if (BuildConfig.DEBUG) {
                    Log.d("NefsApplication", "AppCheck: Debug provider installed. Token from env: ${if (!debugToken.isNullOrBlank()) "present" else "auto-generated, check logcat for debug token"}")
                }
            } else {
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                if (BuildConfig.DEBUG) {
                    Log.d("NefsApplication", "AppCheck: Play Integrity provider installed")
                }
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.w("NefsApplication", "AppCheck initialization failed (non-fatal)", e)
            }
        }
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    private fun setupWorkManager() {
        scheduleDailyEvaluation(this)
    }
}
