# Preserve line numbers and source file names for crash deobfuscation
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile
-printmapping mapping.txt

# Disable multi-pass optimization loops to prevent 504 Gateway Time-out during bundling,
# while keeping R8 code shrinking and symbol obfuscation fully active.
-dontoptimize

# ====================================================================
# Room Database & Entities
# ====================================================================
-keep class com.example.data.db.** { *; }
-keep class com.example.data.dao.** { *; }
-keep class com.example.data.model.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# ====================================================================
# Moshi & JSON Models
# ====================================================================
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-keep class com.example.ai.AiModelsKt** { *; }
-keep class com.example.ai.** { *; }

# ====================================================================
# Retrofit & OkHttp
# ====================================================================
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# ====================================================================
# Google Mobile Ads (AdMob)
# ====================================================================
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }
-keep class com.example.ads.** { *; }
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.ads.**

# ====================================================================
# ML Kit Vision & Text Recognition
# ====================================================================
-keep class com.google.mlkit.vision.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.vision.**

# ====================================================================
# Jetpack Lifecycle & ViewModel
# ====================================================================
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.ViewModel

# ====================================================================
# Kotlin Coroutines
# ====================================================================
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ====================================================================
# Jetpack Compose
# ====================================================================
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ====================================================================
# CameraX & Coil
# ====================================================================
-dontwarn androidx.camera.**
-dontwarn coil.**

