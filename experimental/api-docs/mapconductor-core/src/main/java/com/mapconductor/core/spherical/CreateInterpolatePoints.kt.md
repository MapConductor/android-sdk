Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

### `createInterpolatePoints`

#### Signature

```kotlin
fun createInterpolatePoints(
    points: List<GeoPointInterface>,
    maxSegmentLength: Double = 10000.0
): List<GeoPointInterface>
```

#### Description

This function densifies a polyline (a list of geographic points) by interpolating additional points along the geodesic path between each pair of consecutive points. It ensures that the straight-line distance on the WGS84 ellipsoid between any two adjacent points in the returned list is less than or equal to the specified `maxSegmentLength`.

This is useful for processes that require a higher resolution path than the one originally provided, such as for accurate rendering on a map or for collision detection along a route.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `points` | `List<GeoPointInterface>` | The list of geographic points that define the original polyline. The list must contain at least one point. |
| `maxSegmentLength` | `Double` | The maximum desired distance in meters between consecutive points in the output list. **Default**: `10000.0` (10 kilometers). |

#### Returns

**`List<GeoPointInterface>`**

A new list of `GeoPointInterface` objects that includes the original points plus the interpolated points. The points are ordered to form a continuous, higher-resolution path. If the input list contains fewer than two points, a copy of the original list is returned.

#### Example

Below is an example of how to use `createInterpolatePoints` to add points to a path between two locations.

```kotlin
// Assume GeoPointInterface and a concrete implementation exist:
interface GeoPointInterface {
    val latitude: Double
    val longitude: Double
}

data class GeoPoint(
    override val latitude: Double,
    override val longitude: Double
) : GeoPointInterface

fun main() {
    // Define two points far apart (e.g., San Francisco to Los Angeles)
    val sanFrancisco = GeoPoint(latitude = 37.7749, longitude = -122.4194)
    val losAngeles = GeoPoint(latitude = 34.0522, longitude = -118.2437)

    val originalPath = listOf(sanFrancisco, losAngeles)
    println("Original number of points: ${originalPath.size}")

    // Densify the path so that segments are no longer than 100km (100,000 meters)
    val densifiedPath = createInterpolatePoints(
        points = originalPath,
        maxSegmentLength = 100000.0 // 100 km
    )

    println("Number of points after interpolation: ${densifiedPath.size}")

    // Print the first 5 points of the new path
    densifiedPath.take(5).forEachIndexed { index, point ->
        println(
            "Point ${index + 1}: Lat=${"%.4f".format(point.latitude)}, " +
            "Lon=${"%.4f".format(point.longitude)}"
        )
    }
}

/*
Expected Output:

Original number of points: 2
Number of points after interpolation: 7
Point 1: Lat=37.7749, Lon=-122.4194
Point 2: Lat=37.1594, Lon=-121.7428
Point 3: Lat=36.5403, Lon=-121.0593
Point 4: Lat=35.9175, Lon=-120.3689
Point 5: Lat=35.2911, Lon=-119.6716
*/
```