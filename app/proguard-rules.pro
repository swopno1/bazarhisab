# Preserve line numbers and source file names for crash deobfuscation
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile
-printmapping mapping.txt

# Data models and Room entities
-keep class com.example.data.db.** { *; }
-keep class com.example.data.model.** { *; }
-keep class com.example.ads.** { *; }
-keep class com.example.ai.** { *; }

# Moshi & Retrofit
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-dontwarn com.squareup.moshi.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Google Mobile Ads / AdMob
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }
-dontwarn com.google.android.gms.ads.**

# ViewModels and Lifecycle
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Coroutines
-dontwarn kotlinx.coroutines.**
