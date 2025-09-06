# MapConductor Example App ProGuard Rules

# Keep line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Optimization settings
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Obfuscation settings (remove -dontobfuscate for better compression)
-repackageclasses ''
-allowaccessmodification

# Keep essential Android classes
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep Compose essentials only
-keep class androidx.compose.runtime.Composable
-keep class * extends androidx.activity.ComponentActivity {
    public <init>(...);
}

# Keep ViewModel constructors
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep only essential MapConductor classes
-keep class com.mapconductor.example.MainActivity { *; }
-keep class com.mapconductor.example.MapConductorExampleApplication { *; }

# Keep data classes used in UI
-keepclassmembers class com.mapconductor.example.pages.stores.DemoData {
    <fields>;
    <methods>;
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep serialization
-keepattributes Signature
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}