# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve line numbers, source file names, and reflection annotations
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Room Database Entities, DAOs, and TypeConverters
-keep class com.example.data.model.** { *; }
-keep class com.example.data.local.** { *; }
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Moshi JSON Reflection & Data Classes
-keepclasseswithmembers class * {
    @com.squareup.moshi.JsonClass <methods>;
}
-keep class com.example.data.backup.** { *; }
-keep class com.squareup.moshi.** { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-dontwarn com.squareup.moshi.**
-dontwarn kotlin.reflect.**

# Google Identity & Credential Manager
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Firebase Firestore, Auth, and Models
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
}
-keep class com.example.data.cloud.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}


