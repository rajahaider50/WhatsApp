# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.jetbrains.annotations.* <fields>;
    @org.jetbrains.annotations.* <methods>;
}

# Firebase Realtime Database Models
-keepclassmembers class com.whatsapp.android.data.models.** {
    public <methods>;
    public <fields>;
}
-keep class com.whatsapp.android.data.models.** { *; }

# Coil
-keep class coil.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
