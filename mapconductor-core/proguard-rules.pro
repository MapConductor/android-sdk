# MapConductor Core ProGuard Rules

# Keep line number information for debugging
-keepattributes SourceFile,LineNumberTable

# Keep all public API classes and interfaces
-keep public class com.mapconductor.core.** { public *; }

# Keep all controller interfaces and their implementations
-keep interface com.mapconductor.core.controller.** { *; }
-keep class * implements com.mapconductor.core.controller.** { *; }

# Keep all marker, circle, polyline, and overlay classes
-keep class com.mapconductor.core.marker.** { *; }
-keep class com.mapconductor.core.circle.** { *; }
-keep class com.mapconductor.core.polyline.** { *; }
-keep class com.mapconductor.core.polygon.** { *; }
-keep class com.mapconductor.core.groundimage.** { *; }

# Keep projection and geocell utilities
-keep class com.mapconductor.core.projection.** { *; }
-keep class com.mapconductor.core.geocell.** { *; }
-keep class com.mapconductor.core.spherical.** { *; }

# Keep map view components
-keep class com.mapconductor.core.map.** { *; }

# Keep features and state classes
-keep class com.mapconductor.core.features.** { *; }
-keep class com.mapconductor.core.state.** { *; }

# Keep Compose-related classes
-keep class * extends androidx.compose.runtime.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Fix for Java 11+ StringConcatFactory issue
-dontwarn java.lang.invoke.StringConcatFactory
-keep class java.lang.invoke.StringConcatFactory { *; }