# createLinearInterpolatePoints

### Signature
```kotlin
fun createLinearInterpolatePoints(
    points: List<GeoPointInterface>,
    fractionStep: Double = 0.01
): List<GeoPointInterface>
```

### Description
Generates a new list of points by performing linear interpolation along the path defined by a given list of geographic points. This function effectively "densifies" a polyline by adding intermediate points between each pair of consecutive vertices. The original vertices from the input list are preserved and included in the output.

The density of the new points is controlled by the `fractionStep` parameter. For each segment between two consecutive points, the function adds new points at intervals of `fractionStep` along the great-circle path.

### Parameters
| Parameter      | Type                      | Description                                                                                                                                                           |
|----------------|---------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `points`       | `List<GeoPointInterface>` | A list of two or more `GeoPointInterface` objects representing the vertices of the path to be densified.                                                              |
| `fractionStep` | `Double`                  | The fractional increment used for interpolation between each pair of points. Must be > 0.0 and <= 1.0. A smaller value creates a denser path. Defaults to `0.01`. |

### Returns
| Type                      | Description                                                                                             |
|---------------------------|---------------------------------------------------------------------------------------------------------|
| `List<GeoPointInterface>` | A new list containing the original points and all the generated intermediate points in sequence.          |

### Example
Suppose you have a simple path defined by two points and you want to add a point at the halfway mark. You can achieve this by setting `fractionStep` to `0.5`.

```kotlin
// Assume GeoPoint is a data class implementing GeoPointInterface
// and Spherical.linearInterpolate is available.

// 1. Define the start and end points of the path
val startPoint = GeoPoint(latitude = 40.7128, longitude = -74.0060) // New York City
val endPoint = GeoPoint(latitude = 34.0522, longitude = -118.2437) // Los Angeles

val path = listOf(startPoint, endPoint)

// 2. Generate a densified path with a point at the 50% mark (midpoint)
val densifiedPath = createLinearInterpolatePoints(
    points = path,
    fractionStep = 0.5
)

// 3. The resulting list will contain 3 points:
//    - The original startPoint
//    - The new interpolated midpoint
//    - The original endPoint
println("Original path had ${path.size} points.")
println("Densified path now has ${densifiedPath.size} points.")

// Expected Output:
// Original path had 2 points.
// Densified path now has 3 points.
```