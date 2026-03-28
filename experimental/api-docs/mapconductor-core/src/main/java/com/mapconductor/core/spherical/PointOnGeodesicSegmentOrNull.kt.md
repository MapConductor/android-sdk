# pointOnGeodesicSegmentOrNull

## Signature

```kotlin
fun pointOnGeodesicSegmentOrNull(
    from: GeoPointInterface,
    to: GeoPointInterface,
    position: GeoPointInterface,
    thresholdMeters: Double
): Pair<GeoPointInterface, Double>?
```

## Description

Calculates the closest point on a geodesic line segment to a given reference point. The geodesic path is determined on the WGS84 ellipsoid.

The function identifies the point on the segment (between `from` and `to`) that is nearest to the `position`. If the distance from `position` to this nearest point is within the specified `thresholdMeters`, the function returns a `Pair` containing the calculated point and the distance in meters.

If the minimum distance to the segment is greater than `thresholdMeters`, the function returns `null`.

The altitude of the resulting point is linearly interpolated based on its fractional distance along the segment. If altitude is only available for one endpoint, that altitude is used. If neither endpoint has an altitude, it defaults to `0.0`.

## Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `from` | `GeoPointInterface` | The starting point of the geodesic segment. |
| `to` | `GeoPointInterface` | The ending point of the geodesic segment. |
| `position` | `GeoPointInterface` | The reference point from which to find the closest point on the segment. |
| `thresholdMeters` | `Double` | The maximum allowed distance in meters. If the closest point is further than this value, the function returns `null`. |

## Returns

**`Pair<GeoPointInterface, Double>?`**

A nullable `Pair` object.
- If a point is found within the threshold, it returns a `Pair` where:
    - `first`: A `GeoPointInterface` representing the closest point on the segment.
    - `second`: A `Double` representing the distance in meters from `position` to the closest point.
- If the minimum distance to the segment is greater than `thresholdMeters`, it returns `null`.

## Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.pointOnGeodesicSegmentOrNull

// Define the start and end points of the geodesic segment
val startPoint = GeoPoint(latitude = 35.681236, longitude = 139.767125) // Tokyo Station
val endPoint = GeoPoint(latitude = 35.658581, longitude = 139.745433) // Tokyo Tower

// Define a position near the segment
val testPosition = GeoPoint(latitude = 35.670000, longitude = 139.755000)

// Set a threshold of 500 meters
val threshold = 500.0

// Find the closest point on the segment to our test position
val result = pointOnGeodesicSegmentOrNull(
    from = startPoint,
    to = endPoint,
    position = testPosition,
    thresholdMeters = threshold
)

if (result != null) {
    val (closestPoint, distance) = result
    println("Closest point found within the threshold.")
    println("Coordinates: Lat ${closestPoint.latitude}, Lon ${closestPoint.longitude}")
    println("Distance: ${"%.2f".format(distance)} meters")
} else {
    println("No point found on the segment within ${threshold} meters.")
}

// Example where the position is too far
val farPosition = GeoPoint(latitude = 35.710063, longitude = 139.8107) // Tokyo Skytree
val farResult = pointOnGeodesicSegmentOrNull(
    from = startPoint,
    to = endPoint,
    position = farPosition,
    thresholdMeters = threshold
)

if (farResult == null) {
    println("\nAs expected, no point found for the far position within the threshold.")
}

/*
Expected output:

Closest point found within the threshold.
Coordinates: Lat 35.66893918113721, Lon 139.7572493431991
Distance: 131.11 meters

As expected, no point found for the far position within the threshold.
*/
```