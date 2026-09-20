# ==============================================================================
# KAAVAL - Accessibility-First Emergency Response Ecosystem
# Production Proguard / R8 Optimization & Obfuscation Rules
# ==============================================================================

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Room Database ---
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.kaaval.app.data.entity.** { *; }
-keep class com.kaaval.app.data.dao.** { *; }
-keep class com.kaaval.app.data.KaavalDatabase_Impl { *; }

# --- Firebase Firestore ---
-keep class com.google.firebase.firestore.** { *; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
}

# --- Domain Models & Data Transfer Objects ---
-keep class com.kaaval.app.domain.model.** { *; }
-keep class com.kaaval.app.data.model.** { *; }

# --- Google Play Services Location ---
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**

# --- Android Accessibility & Foreground Services ---
-keep class com.kaaval.app.service.KaavalAccessibilityService { *; }
-keep class com.kaaval.app.service.EmergencyForegroundService { *; }
-keep class com.kaaval.app.service.KaavalBleManager { *; }

# --- Jetpack Compose ---
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }

# --- Suppress innocuous warnings ---
-dontwarn okio.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
