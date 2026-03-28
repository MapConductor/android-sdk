# `calculatePositionAtDistance`

Calculates a destination `GeoPoint` on the Earth's surface given a starting point, a distance, and an initial bearing.

This function uses a spherical model of the Earth for its calculations, which provides a good approximation for most applications.

### Signature
```kotlin
fun calculatePositionAtDistance(
    center: GeoPoint,
    distanceMeters: Double,
    bearingDegrees: Double,
): GeoPoint
```

### Description
This function determines the geographic coordinates of a point that is a specified distance and direction away from a given starting point. The calculation is based on spherical trigonometry, treating the Earth as a perfect sphere.

### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `center` | `GeoPoint` | The starting geographical point, containing latitude and longitude. |
| `distanceMeters` | `Double` | The distance to travel from the `center` point, specified in meters. |
| `bearingDegrees` | `Double` | The initial bearing (azimuth) from the `center` point, specified in degrees. North is 0°, East is 90°, South is 180°, and West is 270°. |

### Returns
| Type | Description |
| :--- | :--- |
| `GeoPoint` | A new `GeoPoint` object representing the calculated destination coordinates. |

### Example
Here's how to calculate a position 100 kilometers due east (90°) of a starting point in San Francisco.

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.calculatePositionAtDistance

fun main() {
    // Define the starting point (e.g., somewhere in San Francisco)
    val sanFrancisco = GeoPoint.fromLatLong(latitude = 37.7749, longitude = -122.4194)

    // Define the distance and bearing
    val distance = 100_000.0 // 100 kilometers in meters
    val bearing = 90.0      // 90 degrees (due East)

    // Calculate the destination point
    val destinationPoint = calculatePositionAtDistance(
        center = sanFrancisco,
        distanceMeters = distance,
        bearingDegrees = bearing
    )

    // The result is a new GeoPoint approximately 100km east of San Francisco
    println("Starting point: $sanFrancisco")
    println("Destination point: $destinationPoint")
    // Expected output might be something like:
    // Starting point: GeoPoint(latitude=37.7749, longitude=-122.4194)
    // Destination point: GeoPoint(latitude=37.7733, longitude=-121.2833)
}
```