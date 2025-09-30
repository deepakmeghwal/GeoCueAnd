# GeoCueAnd ProGuard rules
# Keep Hilt generated components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories$AbstractFactory { *; }

# Keep any classes referenced from Google Play Services dynamically
-keep class com.google.android.gms.** { *; }

# Compose keeps
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
