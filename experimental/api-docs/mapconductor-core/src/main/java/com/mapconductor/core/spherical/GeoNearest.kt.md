# SDK Documentation

This document provides detailed information about the `GeoNearest` object and its associated data structures for performing geodesic calculations.

## `ClosestHit`

A data class that encapsulates the result of a nearest point calculation. It contains the closest point on a segment, the distance to it, and the calculation mode that was used.

**Properties**

| Property | Type | Description |
| :--- | :--- | :--- |
| `radiusMeters` | `Double` | The shortest distance in meters from the reference point to the line segment. |
| `hit` | `GeoPointInterface` | The geographic coordinates of the closest point on the line segment. |
| `mode` | `String` | The calculation mode used: `"planar"` for the local plane approximation or `"spherical"` for the great-circle method. |

---

## `GeoNearest`

A utility object for performing geodesic calculations related to finding the nearest points on the Earth's surface.

### `closestIntersection`

**Signature**

```kotlin
fun closestIntersection(
    P: GeoPointInterface,
    A: GeoPointInterface,
    B: GeoPointInterface
): ClosestHit
```

**Description**

Calculates the point on the geodesic line segment defined by points `A` and `B` that is closest to a given reference point `P`.

This function employs a dynamic calculation strategy for optimal performance and accuracy:
-   **Planar Mode**: If the maximum distance between any two of the three points (`P`, `A`, `B`) is less than or equal to 50 km, a local planar approximation (equirectangular projection) is used. This method is fast and sufficiently accurate for short distances.
-   **Spherical Mode**: For distances greater than 50 km, a more rigorous spherical model is used, treating the segment `AB` as a great-circle arc. This ensures high accuracy over long distances where the Earth's curvature is significant.

If the closest point on the great-circle containing the segment `AB` lies outside the arc `AB`, the function returns the closer of the two endpoints (`A` or `B`).

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `P` | `GeoPointInterface` | The reference point. |
| `A` | `GeoPointInterface` | The starting point of the line segment. |
| `B` | `GeoPointInterface` | The ending point of the line segment. |

**Returns**

A `ClosestHit` object containing the details of the closest point. See the `ClosestHit` class documentation for more details.

**Example**

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.GeoNearest

fun main() {
    // Define the reference point and the line segment endpoints
    val referencePoint = GeoPoint(35.6812, 139.7671) // Tokyo Station
    val segmentStart = GeoPoint(35.6895, 139.6917) // Shinjuku Station
    val segmentEnd = GeoPoint(35.7295, 139.7140)   // Ikebukuro Station

    // Find the closest point on the segment to the reference point
    val result = GeoNearest.closestIntersection(
        P = referencePoint,
        A = segmentStart,
        B = segmentEnd
    )

    println("Calculation Mode: ${result.mode}")
    println("Closest Point (Hit): Lat=${result.hit.latitude}, Lon=${result.hit.longitude}")
    println("Distance (Radius): ${"%.2f".format(result.radiusMeters)} meters")

    // Example Output (values are illustrative):
    // Calculation Mode: planar
    // Closest Point (Hit): Lat=35.7093, Lon=139.7029
    // Distance (Radius): 6054.32 meters
}
```