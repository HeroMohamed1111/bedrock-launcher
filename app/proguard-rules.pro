# Add project specific ProGuard rules here.
-keep class com.bedrocklaunch.model.** { *; }
-keep class com.bedrocklaunch.api.** { *; }
-keep class com.bedrocklaunch.data.entity.** { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okio.**

# Gson
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Hilt
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** { **[] $VALUES; public *; }

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker { public <init>(android.content.Context, androidx.work.WorkerParameters); }
