# MapConductor Marker Clustering ProGuard Rules

# Keep line number information for debugging
-keepattributes SourceFile,LineNumberTable

# Keep all public clustering classes
-keep public class com.mapconductor.marker.clustering.** { public *; }

# Coroutines support
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Fix for Java 11+ StringConcatFactory issue
-dontwarn java.lang.invoke.StringConcatFactory
-keep class java.lang.invoke.StringConcatFactory { *; }
