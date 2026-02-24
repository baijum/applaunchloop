# Firebase Firestore
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# DataStore
-keep class androidx.datastore.** { *; }

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**

# Keep Compose runtime
-dontwarn androidx.compose.**
