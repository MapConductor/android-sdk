# SDK Documentation

## `CirclePolygonHelper`

A helper object for generating polygon vertices that approximate a circle on a map. This is useful for drawing circular shapes using polygon-based map APIs, such as Google Maps Polygons.

The helper supports two types of circle calculations:
-   **Geodesic**: A true circle on the Earth's spherical surface (a great circle path). This is accurate for all distances and latitudes.
-   **Non-geodesic**: A circle drawn on a flat (Mercator) projection. This is computationally simpler but can appear distorted, especially over large distances or at high latitudes.

---

### `generateCirclePoints`

Generates a list of `LatLng` points that form the vertices of a polygon approximating a circle.

#### Signature

```kotlin
fun generateCirclePoints(
    center: LatLng,
    radiusMeters: Double,
    geodesic: Boolean
): List<LatLng>
```

#### Description

This function calculates a series of geographical coordinates (`LatLng` points) that, when connected, form a polygon that approximates a circle. You can specify the center, radius, and whether the circle should be calculated as a true geodesic circle or a simpler non-geodesic projection. The resulting list of points is ready to be used with map APIs to draw a polygon.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `center` | `LatLng` | The geographical center point of the circle. |
| `radiusMeters` | `Double` | The radius of the circle, specified in meters. |
| `geodesic` | `Boolean` | Determines the calculation method.<br/>- `true`: Generates a geodesic circle. Recommended for accuracy over large distances and at high latitudes.<br/>- `false`: Generates a non-geodesic circle using a simpler planar projection. Suitable for small radii where the Earth's curvature is negligible. |

#### Returns

| Type | Description |
| :--- | :--- |
| `List<LatLng>` | A list of `LatLng` points representing the vertices of the polygon that approximates the circle. The polygon is formed by connecting these points in order. |

#### Example

The following example demonstrates how to generate points for both a geodesic and a non-geodesic circle.

```kotlin
import com.google.android.gms.maps.model.LatLng
import com.mapconductor.googlemaps.circle.CirclePolygonHelper

fun main() {
    // Define the center and radius for our circle
    val centerPoint = LatLng(34.0522, -118.2437) // Los Angeles
    val radiusInMeters = 10000.0 // 10 kilometers

    // 1. Generate points for a geodesic circle (more accurate)
    // This is the recommended approach for most use cases.
    val geodesicCirclePoints: List<LatLng> = CirclePolygonHelper.generateCirclePoints(
        center = centerPoint,
        radiusMeters = radiusInMeters,
        geodesic = true
    )

    println("Generated ${geodesicCirclePoints.size} points for a geodesic circle.")
    // These points can now be used to add a Polygon to a Google Map.
    // e.g., googleMap.addPolygon(PolygonOptions().addAll(geodesicCirclePoints))


    // 2. Generate points for a non-geodesic circle
    // Suitable for small circles where performance is critical and accuracy is less of a concern.
    val nonGeodesicCirclePoints: List<LatLng> = CirclePolygonHelper.generateCirclePoints(
        center = centerPoint,
        radiusMeters = radiusInMeters,
        geodesic = false
    )

    println("Generated ${nonGeodesicCirclePoints.size} points for a non-geodesic circle.")
    // These points can also be used to add a Polygon to a map.
}
```