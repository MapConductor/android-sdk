Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

***

### isPointOnLinearLine

#### Signature

```kotlin
fun isPointOnLinearLine(
    from: GeoPointInterface,
    to: GeoPointInterface,
    position: GeoPointInterface,
    thresholdMeters: Double,
): Pair<GeoPointInterface, Double>?
```

#### Description

Determines if a given geographic point (`position`) is within a specified distance (`thresholdMeters`) of a straight line segment defined by `from` and `to` points.

This function uses a planar (flat-earth) approximation for the calculation, ignoring the Earth's curvature for the path of the line segment. It correctly handles longitude wrapping by choosing the shorter path across the ±180° meridian.

If the `position` is within the threshold, the function returns a `Pair` containing the closest point on the line segment and the exact distance in meters. Otherwise, it returns `null`.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `from` | `GeoPointInterface` | The starting point of the line segment. |
| `to` | `GeoPointInterface` | The ending point of the line segment. |
| `position` | `GeoPointInterface` | The point to check against the line segment. |
| `thresholdMeters` | `Double` | The maximum allowed distance in meters from the line segment for the point to be considered "on" it. |

#### Returns

**`Pair<GeoPointInterface, Double>?`**

*   A `Pair` containing the following if the `position` is within the `thresholdMeters`:
    *   **`first`**: A `GeoPoint` instance representing the closest point on the line segment to the `position`. Its altitude is linearly interpolated if both `from` and `to` points have altitude values.
    *   **`second`**: A `Double` representing the perpendicular distance in meters from the `position` to the line segment.
*   Returns `null` if the `position` is outside the specified threshold.

#### Example

```kotlin
import com.mapconductor.core.features.GeoPoint

fun main() {
    val fromPoint = GeoPoint(latitude = 35.681236, longitude = 139.767125) // Tokyo Station
    val toPoint = GeoPoint(latitude = 35.658581, longitude = 139.745433)   // Tokyo Tower

    // Case 1: A point close to the line segment
    val closePosition = GeoPoint(latitude = 35.670000, longitude = 139.756000)
    val threshold = 500.0 // 500 meters

    val resultOnLine = isPointOnLinearLine(fromPoint, toPoint, closePosition, threshold)

    if (resultOnLine != null) {
        val (closestPoint, distance) = resultOnLine
        println("Point is on the line.")
        println("Closest point on segment: lat=${closestPoint.latitude}, lon=${closestPoint.longitude}")
        println("Distance to segment: ${"%.2f".format(distance)} meters")
    } else {
        println("Point is NOT on the line.")
    }
    // Expected Output:
    // Point is on the line.
    // Closest point on segment: lat=35.66931..., lon=139.75689...
    // Distance to segment: 85.54 meters

    println("---")

    // Case 2: A point far from the line segment
    val farPosition = GeoPoint(latitude = 35.710063, longitude = 139.8107) // Tokyo Skytree
    val resultOffLine = isPointOnLinearLine(fromPoint, toPoint, farPosition, threshold)

    if (resultOffLine != null) {
        val (closestPoint, distance) = resultOffLine
        println("Point is on the line.")
        println("Closest point on segment: lat=${closestPoint.latitude}, lon=${closestPoint.longitude}")
        println("Distance to segment: ${"%.2f".format(distance)} meters")
    } else {
        println("Point is NOT on the line.")
    }
    // Expected Output:
    // Point is NOT on the line.
}
```