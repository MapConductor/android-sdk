Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

# WGS84Geodesic

The `WGS84Geodesic` object provides a collection of utility functions for performing geodesic calculations on the WGS84 ellipsoid, which is the reference coordinate system used by the Global Positioning System (GPS). These functions are designed for high accuracy and are compatible with standard geodesic computations, such as those used by Google Maps.

This utility handles calculations for distance, heading (azimuth), and path interpolation between geographic coordinates.

---

## `computeDistanceBetween`

Calculates the shortest distance (geodesic) between two points on the surface of the WGS84 ellipsoid. This method implements Vincenty's inverse formula for high accuracy.

### Signature

```kotlin
fun computeDistanceBetween(
    from: GeoPointInterface,
    to: GeoPointInterface
): Double
```

### Description

This function computes the geodesic distance in meters between a starting point (`from`) and an ending point (`to`). The calculation is based on Vincenty's inverse formula, which is an iterative method that is highly accurate for all distances on an ellipsoid. It is a robust alternative to the Haversine formula, which assumes a perfect sphere.

### Parameters

| Parameter | Type                | Description                      |
| :-------- | :------------------ | :------------------------------- |
| `from`    | `GeoPointInterface` | The starting geographical point. |
| `to`      | `GeoPointInterface` | The destination geographical point. |

### Returns

`Double` - The geodesic distance between the two points in meters. Returns `0.0` if the points are identical or if the algorithm fails to converge.

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.WGS84Geodesic

fun main() {
    val newYork = GeoPoint(latitude = 40.7128, longitude = -74.0060)
    val london = GeoPoint(latitude = 51.5074, longitude = -0.1278)

    val distance = WGS84Geodesic.computeDistanceBetween(newYork, london)

    // The distance is approximately 5,570,224.5 meters
    println("Distance between New York and London: $distance meters")
}
```

---

## `computeHeading`

Calculates the initial bearing (forward azimuth) from a starting point to a destination point on the WGS84 ellipsoid.

### Signature

```kotlin
fun computeHeading(
    from: GeoPointInterface,
    to: GeoPointInterface
): Double
```

### Description

This function determines the initial heading, in degrees, for the shortest path (geodesic) from the `from` point to the `to` point. The heading is measured clockwise from true north.

### Parameters

| Parameter | Type                | Description                      |
| :-------- | :------------------ | :------------------------------- |
| `from`    | `GeoPointInterface` | The starting geographical point. |
| `to`      | `GeoPointInterface` | The destination geographical point. |

### Returns

`Double` - The initial heading in degrees, normalized to a range of `-180` to `180`.
- `0°` is North
- `90°` is East
- `180°` or `-180°` is South
- `-90°` is West

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.WGS84Geodesic

fun main() {
    val startPoint = GeoPoint(latitude = 40.7128, longitude = -74.0060) // New York
    val endPoint = GeoPoint(latitude = 51.5074, longitude = -0.1278)   // London

    val heading = WGS84Geodesic.computeHeading(startPoint, endPoint)

    // The initial heading is approximately 51.2 degrees
    println("Initial heading from New York to London: $heading degrees")
}
```

---

## `interpolate`

Calculates an intermediate geographical point along the great-circle path between two points using spherical linear interpolation (Slerp).

### Signature

```kotlin
fun interpolate(
    from: GeoPointInterface,
    to: GeoPointInterface,
    fraction: Double
): GeoPoint
```

### Description

This function finds a `GeoPoint` that lies at a specified fraction of the distance along the path from a starting point to a destination point. It uses Spherical Linear Interpolation (Slerp), which provides a good approximation for short to medium distances by treating the Earth as a sphere. For the highest precision over long distances, Vincenty's direct formula would be required.

Altitude is interpolated linearly. If only one of the points has an altitude, that altitude is used for the interpolated point.

### Parameters

| Parameter  | Type                | Description                                                                                                                            |
| :--------- | :------------------ | :------------------------------------------------------------------------------------------------------------------------------------- |
| `from`     | `GeoPointInterface` | The starting geographical point.                                                                                                       |
| `to`       | `GeoPointInterface` | The destination geographical point.                                                                                                    |
| `fraction` | `Double`            | The fraction of the distance from the `from` point to the `to` point. Must be between `0.0` (returns `from`) and `1.0` (returns `to`). |

### Returns

`GeoPoint` - The new `GeoPoint` at the specified fractional distance along the path.

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.WGS84Geodesic

fun main() {
    val startPoint = GeoPoint(latitude = 40.7128, longitude = -74.0060, altitude = 10.0)
    val endPoint = GeoPoint(latitude = 51.5074, longitude = -0.1278, altitude = 20.0)

    // Find the midpoint (fraction = 0.5)
    val midPoint = WGS84Geodesic.interpolate(startPoint, endPoint, 0.5)

    println("Midpoint:")
    println("  Latitude: ${midPoint.latitude}")
    println("  Longitude: ${midPoint.longitude}")
    println("  Altitude: ${midPoint.altitude}") // Expected altitude: 15.0
}
```