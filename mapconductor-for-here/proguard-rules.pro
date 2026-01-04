# MapConductor HERE Maps ProGuard Rules

# Keep line number information for debugging
-keepattributes SourceFile,LineNumberTable

# Keep all public API classes
-keep public class com.mapconductor.here.** { public *; }

# Keep HERE Maps specific implementations
-keep class com.mapconductor.here.HereMapViewController { *; }
-keep class com.mapconductor.here.HereMapView { *; }

# Keep marker, circle, polyline implementations
-keep class com.mapconductor.here.marker.** { *; }
-keep class com.mapconductor.here.circle.** { *; }
-keep class com.mapconductor.here.polyline.** { *; }
-keep class com.mapconductor.here.polygon.** { *; }

# Keep HERE SDK classes
-keep class com.here.sdk.** { *; }

# Compose integration
-keep class * extends androidx.compose.runtime.** { *; }

# Fix for Java 11+ StringConcatFactory issue
-dontwarn java.lang.invoke.StringConcatFactory
-keep class java.lang.invoke.StringConcatFactory { *; }
