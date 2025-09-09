# MapConductor Mapbox ProGuard Rules

# Keep line number information for debugging
-keepattributes SourceFile,LineNumberTable

# Keep all public API classes
-keep public class com.mapconductor.mapbox.** { public *; }

# Keep Mapbox specific implementations
-keep class com.mapconductor.mapbox.MapboxMapViewController { *; }
-keep class com.mapconductor.mapbox.MapboxMapViewControllerImpl { *; }
-keep class com.mapconductor.mapbox.MapboxMapView { *; }

# Keep marker, circle, polyline implementations
-keep class com.mapconductor.mapbox.marker.** { *; }
-keep class com.mapconductor.mapbox.circle.** { *; }
-keep class com.mapconductor.mapbox.polyline.** { *; }
-keep class com.mapconductor.mapbox.polygon.** { *; }

# Keep Mapbox SDK classes
-keep class com.mapbox.maps.** { *; }
-keep class com.mapbox.geojson.** { *; }
-keep class com.mapbox.android.** { *; }

# Keep Mapbox style classes
-keep class com.mapbox.maps.extension.style.** { *; }

# Compose integration
-keep class * extends androidx.compose.runtime.** { *; }

# Fix for Java 11+ StringConcatFactory issue
-dontwarn java.lang.invoke.StringConcatFactory
-keep class java.lang.invoke.StringConcatFactory { *; }