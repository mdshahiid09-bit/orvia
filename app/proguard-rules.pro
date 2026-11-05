# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
-keep class com.orvia.shahid.app.data.remote.models.** { *; }
-keep class com.orvia.shahid.app.domain.model.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
