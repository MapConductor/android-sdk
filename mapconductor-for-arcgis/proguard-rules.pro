# MapConductor ArcGIS ProGuard Rules

# Keep line number information for debugging
-keepattributes SourceFile,LineNumberTable

# Keep all public API classes
-keep public class com.mapconductor.arcgis.** { public *; }

# Keep ArcGIS specific implementations
-keep class com.mapconductor.arcgis.ArcGISMapViewController { *; }
-keep class com.mapconductor.arcgis.ArcGISMapView { *; }

# Keep marker, circle, polyline implementations
-keep class com.mapconductor.arcgis.marker.** { *; }
-keep class com.mapconductor.arcgis.circle.** { *; }
-keep class com.mapconductor.arcgis.polyline.** { *; }
-keep class com.mapconductor.arcgis.polygon.** { *; }

# Keep ArcGIS SDK classes
-keep class com.esri.arcgisruntime.** { *; }

# Compose integration
-keep class * extends androidx.compose.runtime.** { *; }

# Fix for Java 11+ StringConcatFactory issue
-dontwarn java.lang.invoke.StringConcatFactory
-keep class java.lang.invoke.StringConcatFactory { *; }
