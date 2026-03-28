# SDK Documentation: GeographicLibCalculator

## `GeographicLibCalculator`

### Description

A utility object that provides geodesic calculations using the GeographicLib library, based on the WGS84 ellipsoid. It offers methods for computing distances and interpolating points along a geodesic path.

---

## Methods

### `computeDistanceBetween`

#### Signature

```kotlin
fun computeDistanceBetween(
    from: GeoPointInterface,
    to: GeoPointInterface
): Double
```

#### Description

Calculates the shortest distance (geodesic) between two geographical points on the WGS84 ellipsoid.

#### Parameters

| Parameter | Type                | Description                  |
| :-------- | :------------------ | :--------------------------- |
| `from`    | `GeoPointInterface` | The starting geographical point. |
| `to`      | `GeoPointInterface` | The ending geographical point.   |

#### Returns

**`Double`**

The geodesic distance between the two points in meters.

#### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.GeographicLibCalculator

// Define two points (e.g., San Francisco and Los Angeles)
val sf = GeoPoint(37.7749, -122.4194)
val la = GeoPoint(34.0522, -118.2437)

// Calculate the distance between them
val distanceInMeters = GeographicLibCalculator.computeDistanceBetween(sf, la)

println("Distance: $distanceInMeters meters")
// Expected output might be around: Distance: 559120.539653327 meters
```

---

### `interpolate`

#### Signature

```kotlin
fun interpolate(
    from: GeoPointInterface,
    to: GeoPointInterface,
    fraction: Double
): GeoPoint
```

#### Description

Calculates an intermediate point along the geodesic line between two given points. The position of the intermediate point is determined by a fraction of the total distance.

The altitude of the resulting point is interpolated as follows:
- If both `from` and `to` have an altitude, the new altitude is linearly interpolated.
- If only one point has an altitude, its altitude is used for the result.
- If neither point has an altitude, the resulting altitude is `0.0`.

#### Parameters

| Parameter  | Type                | Description                                                                                             |
| :--------- | :------------------ | :------------------------------------------------------------------------------------------------------ |
| `from`     | `GeoPointInterface` | The starting geographical point.                                                                        |
| `to`       | `GeoPointInterface` | The ending geographical point.                                                                          |
| `fraction` | `Double`            | The fractional distance from the `from` point towards the `to` point. A value of `0.0` returns the `from` point, and `1.0` returns the `to` point. |

#### Returns

**`GeoPoint`**

A new `GeoPoint` object representing the interpolated point, including the calculated latitude, longitude, and altitude.

#### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.GeographicLibCalculator

// Define two points with altitude
val startPoint = GeoPoint(latitude = 37.7749, longitude = -122.4194, altitude = 10.0)
val endPoint = GeoPoint(latitude = 34.0522, longitude = -118.2437, altitude = 110.0)

// Find the midpoint (fraction = 0.5)
val midPoint = GeographicLibCalculator.interpolate(startPoint, endPoint, 0.5)

println("Midpoint Latitude: ${midPoint.latitude}")
println("Midpoint Longitude: ${midPoint.longitude}")
println("Midpoint Altitude: ${midPoint.altitude}")
// Expected output:
// Midpoint Latitude: 35.9185...
// Midpoint Longitude: -120.3219...
// Midpoint Altitude: 60.0
```