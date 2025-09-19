# MapConductor Marker Strategy ProGuard Rules

# Keep line number information for debugging
-keepattributes SourceFile,LineNumberTable

# Keep all public strategy classes
-keep public class com.mapconductor.marker.strategy.** { public *; }

# Keep all strategy implementations
-keep class com.mapconductor.marker.strategy.*Strategy { *; }

# Keep factory classes
-keep class com.mapconductor.marker.strategy.SpatialMarkerRenderingStrategies { *; }

# Coroutines support
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Fix for Java 11+ StringConcatFactory issue
-dontwarn java.lang.invoke.StringConcatFactory
-keep class java.lang.invoke.StringConcatFactory { *; }
