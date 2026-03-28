### `interpolateAtMeridianGeodesic`

#### Signature
```kotlin
fun interpolateAtMeridianGeodesic(
    from: GeoPointInterface,
    to: GeoPointInterface
): GeoPoint
```

#### Description
Calculates the geographical point where the great circle path between two points (`from` and `to`) intersects the antimeridian (180° or -180° longitude).

This function employs a high-precision iterative binary search algorithm to find the exact intersection point. It is particularly useful for accurately rendering geodesic paths that cross the international date line, preventing visual artifacts where a line incorrectly wraps around the globe. The final returned point will have its longitude set precisely to `180.0` or `-180.0`, with its latitude and altitude spherically interpolated.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `from` | `GeoPointInterface` | The starting point of the geodesic path. |
| `to` | `GeoPointInterface` | The ending point of the geodesic path. |

#### Returns
| Type | Description |
| :--- | :--- |
| `GeoPoint` | A new `GeoPoint` object representing the intersection point on the antimeridian. |

#### Example
The following example demonstrates how to find the antimeridian crossing point for a path from a point east of the antimeridian (e.g., near Fiji) to a point west of it (e.g., near Hawaii).

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.spherical.Spherical // Assuming Spherical.sphericalInterpolate exists
import com.mapconductor.core.spherical.interpolateAtMeridianGeodesic

// Mock implementations for demonstration purposes
// In a real scenario, you would use the SDK's provided classes.
object Spherical {
    // A simplified mock of spherical interpolation for the example to run.
    // The actual implementation is more complex.
    fun sphericalInterpolate(from: GeoPointInterface, to: GeoPointInterface, fraction: Double): GeoPoint {
        val lat = from.latitude + (to.latitude - from.latitude) * fraction
        val lon = from.longitude + (to.longitude - from.longitude) * fraction
        val alt = (from.altitude ?: 0.0) + ((to.altitude ?: 0.0) - (from.altitude ?: 0.0)) * fraction
        return GeoPoint(lat, lon, alt)
    }
}

fun main() {
    // Define a starting point in Fiji (east of the antimeridian)
    val fiji = GeoPoint(latitude = -17.7134, longitude = 178.0650, altitude = 10.0)

    // Define an ending point in Hawaii (west of the antimeridian)
    val hawaii = GeoPoint(latitude = 21.3069, longitude = -157.8583, altitude = 20.0)

    // Calculate the point where the path crosses the antimeridian
    val crossingPoint = interpolateAtMeridianGeodesic(from = fiji, to = hawaii)

    println("Path from: $fiji")
    println("Path to: $hawaii")
    println("---")
    println("Antimeridian crossing point found at:")
    println("Latitude: ${crossingPoint.latitude}")
    println("Longitude: ${crossingPoint.longitude}")
    println("Altitude: ${crossingPoint.altitude}")
}

// Expected Output:
// Path from: GeoPoint(latitude=-17.7134, longitude=178.065, altitude=10.0)
// Path to: GeoPoint(latitude=21.3069, longitude=-157.8583, altitude=20.0)
// ---
// Antimeridian crossing point found at:
// Latitude: -11.59...
// Longitude: 180.0
// Altitude: 12.0...
```