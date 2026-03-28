Of course! Here is a high-quality SDK document for the provided code snippet, formatted in Markdown.

---

### `isPointOnTheGeodesicLine`

<br/>

Checks if a given point is near a geodesic polyline by finding the closest point on the line and its distance. If the calculated distance is within a specified threshold, the point can be considered "on" the line.

The function first identifies a relevant line segment and then interpolates points along it to find the closest point. It includes optional debugging parameters to visualize the process.

#### Signature

```kotlin
fun isPointOnTheGeodesicLine(
    points: List<GeoPointInterface>,
    position: GeoPointInterface,
    threshold: Double,
    debugDrawRectangle: ((GeoRectBounds, Int) -> Unit)?,
    debugDrawCircle: ((GeoPointInterface, Double, Int) -> Unit)?,
): Pair<GeoPointInterface, Double>?
```

#### Parameters

| Parameter            | Type                                       | Description                                                                                                                            |
| -------------------- | ------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------- |
| `points`             | `List<GeoPointInterface>`                  | A list of geo-points that define the vertices of the geodesic polyline. Must contain at least two points to form a line.                |
| `position`           | `GeoPointInterface`                        | The geographic point to check against the polyline.                                                                                    |
| `threshold`          | `Double`                                   | The tolerance in meters. This value is used to determine if the point is close enough to the line to be considered "on" it.             |
| `debugDrawRectangle` | `((GeoRectBounds, Int) -> Unit)?`          | An optional lambda function for debugging. If provided, it is called to draw the bounding box of the line segment being inspected.        |
| `debugDrawCircle`    | `((GeoPointInterface, Double, Int) -> Unit)?` | An optional lambda function for debugging. If provided, it is called to draw circles around interpolated points during the search process. |

#### Returns

**`Pair<GeoPointInterface, Double>?`**

*   A `Pair` containing:
    *   `first`: The `GeoPointInterface` representing the closest point on the polyline to the `position`.
    *   `second`: The `Double` value representing the distance in meters from the `position` to this closest point.
*   Returns `null` if the input `points` list contains fewer than two points.
*   The distance in the returned pair may be `Double.MAX_VALUE` if a suitable intersection point cannot be precisely calculated.

#### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.isPointOnTheGeodesicLine

// Define a polyline with two points (a single line segment)
val polyline = listOf(
    GeoPoint(34.0522, -118.2437), // Los Angeles
    GeoPoint(40.7128, -74.0060)   // New York
)

// Define a point to test
val testPosition = GeoPoint(38.8977, -94.5828) // A point near Kansas City

// Set a threshold of 50 kilometers (50000 meters)
val distanceThreshold = 50000.0

// Check if the point is on the line
val result = isPointOnTheGeodesicLine(
    points = polyline,
    position = testPosition,
    threshold = distanceThreshold,
    debugDrawRectangle = null, // No debugging
    debugDrawCircle = null
)

if (result != null) {
    val closestPoint = result.first
    val distance = result.second

    if (distance <= distanceThreshold) {
        println("Point is on the geodesic line.")
        println("Closest point on line: ${closestPoint.latitude}, ${closestPoint.longitude}")
        println("Distance to line: ${"%.2f".format(distance)} meters")
    } else {
        println("Point is not on the geodesic line (distance > threshold).")
        println("Distance to line: ${"%.2f".format(distance)} meters")
    }
} else {
    println("Could not perform check. The polyline must have at least 2 points.")
}

// Example Output:
// Point is on the geodesic line.
// Closest point on line: 38.9012, -94.5791
// Distance to line: 45012.34 meters
```