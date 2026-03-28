Excellent. Here is the high-quality SDK documentation for the provided code snippet.

### `interpolateAtMeridianLinear`

#### Signature
```kotlin
fun interpolateAtMeridianLinear(
    from: GeoPointInterface,
    to: GeoPointInterface
): GeoPoint
```

#### Description
Performs linear interpolation to find the point where a line segment between two geographical points crosses the anti-meridian (180° or -180° longitude).

This function calculates the intersection by treating the coordinates as if they are on a simple 2D Cartesian plane (an Equirectangular projection). It determines the latitude at the meridian crossing by interpolating linearly based on the longitude difference.

The altitude is also interpolated if both `from` and `to` points provide an altitude value. If only one point has an altitude, that value is used for the crossing point. If neither has an altitude, the altitude defaults to `0.0`.

**Note:** This function assumes that the longitudes are "unwrapped," meaning they can extend beyond the standard `[-180, 180]` degree range to represent a continuous path. For example, a path from 170° longitude to -170° longitude should be represented with `to.longitude = 190` for the interpolation to be calculated correctly across the shortest path.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `from` | `GeoPointInterface` | The starting point of the line segment. |
| `to` | `GeoPointInterface` | The ending point of the line segment, potentially with an "unwrapped" longitude. |

#### Returns
**Type:** `GeoPoint`

A new `GeoPoint` object representing the calculated intersection point on the anti-meridian. The longitude of the returned point will be either `180.0` or `-180.0`, depending on the direction of the crossing.

#### Example
The following example demonstrates how to find the meridian crossing point for a path that goes from 170°E to 190°E (which is equivalent to -170°W).

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface

// For the purpose of this example, we can assume the GeoPoint and GeoPointInterface
// from the problem description are available.

fun main() {
    // Define a starting point east of the anti-meridian.
    val startPoint = GeoPoint(latitude = 50.0, longitude = 170.0, altitude = 1000.0)

    // Define an ending point west of the anti-meridian.
    // Note the use of an "unwrapped" longitude (190.0) to represent a
    // continuous path crossing the 180° meridian. 190° is equivalent to -170°.
    val endPoint = GeoPoint(latitude = 60.0, longitude = 190.0, altitude = 2000.0)

    // Calculate the intersection point on the meridian.
    val meridianCrossingPoint = interpolateAtMeridianLinear(startPoint, endPoint)

    // The function finds the point exactly halfway between the two points
    // in terms of longitude, and interpolates the latitude and altitude accordingly.
    println(meridianCrossingPoint)
    // Expected output: GeoPoint(latitude=55.0, longitude=180.0, altitude=1500.0)
}
```