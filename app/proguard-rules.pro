# ============================================================
# ArrowMaze - ProGuard / R8 Optimization Rules
# ============================================================

# ==================== Default Android ====================
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ==================== Kotlin ====================
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class **.WhenMappings {
    <fields>;
    **[] $VALUES;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class kotlin.coroutines.Continuation {
    ** fun cont **(**);
}
-keepclassmembers class * {
    ** Companion;
}
-keepclassmembers class * {
    ** INSTANCE;
}

# ==================== Coroutines ====================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ==================== Retrofit ====================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ==================== OkHttp ====================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }

# ==================== Gson ====================
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ==================== Kotlinx Serialization ====================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.zenox.arrowmaze.**$$serializer { *; }
-keepclassmembers class com.zenox.arrowmaze.** {
    *** Companion;
}
-keepclasseswithmembers class com.zenox.arrowmaze.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.zenox.arrowmaze.**$**$$serializer { *; }
-keepclassmembers class com.zenox.arrowmaze.**$** {
    *** Companion;
}
-keepclasseswithmembers class com.zenox.arrowmaze.**$** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==================== Firebase ====================
-keep class com.google.firebase.** { *; }
-keepclassmembers class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ==================== Google Play Services ====================
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ==================== Hilt ====================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper {
    <init>(android.content.Context);
}

# ==================== AndroidX / Compose ====================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.navigation.** { *; }
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }

# ==================== Room ====================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ==================== Coil ====================
-dontwarn coil.**
-keep class coil.** { *; }

# ==================== Lottie ====================
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# ==================== AdMob ====================
-keep class com.google.android.gms.ads.** { *; }
-keep public class com.google.android.gms.ads.** { public *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.internal.ads.**

# ==================== Play Billing ====================
-keep class com.android.vending.billing.** { *; }
-keep class com.google.android.gms.billing.** { *; }

# ==================== Model Classes (keep all) ====================
-keep class com.zenox.arrowmaze.data.model.** { *; }
-keep class com.zenox.arrowmaze.domain.model.** { *; }
-keep class com.zenox.arrowmaze.data.local.entity.** { *; }
-keep class com.zenox.arrowmaze.data.remote.dto.** { *; }

# ==================== Data Classes (Kotlin serialization) ====================
-keepclassmembers class com.zenox.arrowmaze.** {
    <init>(...);
}
-keep @kotlinx.serialization.Serializable class com.zenox.arrowmaze.** { *; }

# ==================== Enum ====================
-keepclassmembers enum * {
    **[] $VALUES;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ==================== WorkManager ====================
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# ==================== Remove Log Calls in Release ====================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
-assumenosideeffects class kotlin.io.ConsoleKt {
    public static void println(...);
}
