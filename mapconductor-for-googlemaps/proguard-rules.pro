# MapConductor Google Maps ProGuard Rules

# Keep line number information for debugging
-keepattributes SourceFile,LineNumberTable

# Keep all public API classes
-keep public class com.mapconductor.googlemaps.** { public *; }

# Keep Google Maps specific implementations
-keep class com.mapconductor.googlemaps.GoogleMapViewController { *; }
-keep class com.mapconductor.googlemaps.GoogleMapView { *; }

# Keep marker, circle, polyline implementations
-keep class com.mapconductor.googlemaps.marker.** { *; }
-keep class com.mapconductor.googlemaps.circle.** { *; }
-keep class com.mapconductor.googlemaps.polyline.** { *; }
-keep class com.mapconductor.googlemaps.polygon.** { *; }
-keep class com.mapconductor.googlemaps.groundimage.** { *; }

# Keep Google Maps SDK classes
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.maps.android.** { *; }

# Keep Google Maps model classes
-keep class com.google.android.gms.maps.model.** { *; }

# Compose integration
-keep class * extends androidx.compose.runtime.** { *; }

# Fix for Java 11+ StringConcatFactory issue
-dontwarn java.lang.invoke.StringConcatFactory
-keep class java.lang.invoke.StringConcatFactory { *; }
