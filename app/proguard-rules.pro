# Nefs Zikirleri - ProGuard / R8 Rules
# Minimal, precise keeps - avoid broad wildcards

# Preserve line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# --- Room ---
-keep class com.example.data.model.Zikir { *; }
-keep class com.example.data.model.ZikirHistory { *; }
-keep class com.example.data.model.ReminderSlot { *; }
-keep class com.example.data.model.AppSettings { *; }
-keep class com.example.data.model.PendingOperation { *; }
-keep class com.example.data.local.*Dao { *; }
-keep class androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# --- Moshi ---
-keepclasseswithmembers class * {
    @com.squareup.moshi.JsonClass <methods>;
}
-keep class com.example.data.backup.BackupPayload { *; }
-keep class com.example.data.backup.ZikirBackupItem { *; }
-keep class com.example.data.backup.ZikirHistoryBackupItem { *; }
-keep class com.example.data.backup.ReminderSlotBackupItem { *; }
-keep class com.example.data.backup.AppSettingsBackupItem { *; }
-keep class com.squareup.moshi.** { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-dontwarn com.squareup.moshi.**
-dontwarn kotlin.reflect.**

# --- Firebase Firestore (reflection for POJO mapping) ---
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
}
-keep class com.example.data.cloud.SyncManager { *; }
-keep class com.example.data.cloud.CloudBackupData { *; }
-keep class com.example.data.cloud.SyncMetadata { *; }

# --- Credential Manager & Google Identity ---
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# --- Compose (keep Composable methods, but not entire androidx.compose) ---
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keep class androidx.compose.runtime.** { *; }
# Avoid keeping entire compose UI - R8 can optimize it
-dontwarn androidx.compose.**

# --- WorkManager ---
-keep class androidx.work.** { *; }
-keep class com.example.worker.DailyEvaluationWorker { *; }

# --- Security / Crypto ---
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }

# --- OkHttp / Retrofit (if used) ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# --- Remove logging in release ---
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# --- Optimization ---
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

