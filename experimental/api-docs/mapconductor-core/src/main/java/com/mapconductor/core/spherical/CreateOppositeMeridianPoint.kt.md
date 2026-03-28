Of course! Here is the high-quality SDK documentation for the given Kotlin code snippet.

***

### `createOppositeMeridianPoint`

Creates a point on the opposite meridian (the 180° / -180° longitude line) while preserving the original point's latitude and altitude.

#### Signature

```kotlin
fun createOppositeMeridianPoint(point: GeoPointInterface): GeoPoint
```

#### Description

This function calculates and returns a new `GeoPoint` located on the antimeridian (the 180° or -180° longitude line). The new point will have the same latitude and altitude as the source `point`.

The longitude for the new point is determined based on the hemisphere of the input `point`:
- If the input longitude is in the Eastern Hemisphere (>= 0°), the new longitude is set to -180.0°.
- If the input longitude is in the Western Hemisphere (< 0°), the new longitude is set to 180.0°.

This is useful for calculations and visualizations that involve wrapping around the globe at the International Date Line. If the source point's altitude is `null`, the resulting point's altitude will be `0.0`.

#### Parameters

| Parameter | Type                | Description                                                                            |
|-----------|---------------------|----------------------------------------------------------------------------------------|
| `point`   | `GeoPointInterface` | The original geographical point from which to create the opposite meridian equivalent. |

#### Returns

| Type       | Description                                                                                                                            |
|------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `GeoPoint` | A new `GeoPoint` instance on the opposite meridian with the same latitude and altitude as the input. Defaults to an altitude of `0.0` if the original is `null`. |

#### Example

The following example demonstrates how to use `createOppositeMeridianPoint` for points in both the Eastern and Western Hemispheres.

```kotlin
// Assume GeoPoint and GeoPointInterface are defined as follows:
// interface GeoPointInterface {
//     val latitude: Double
//     val longitude: Double
//     val altitude: Double?
// }
// data class GeoPoint(...) : GeoPointInterface

fun main() {
    // Case 1: A point in the Eastern Hemisphere (Tokyo)
    val tokyo = GeoPoint(latitude = 35.6895, longitude = 139.6917, altitude = 40.0)
    val oppositeOfTokyo = createOppositeMeridianPoint(tokyo)

    println("Original Point (Tokyo): $tokyo")
    // > Original Point (Tokyo): GeoPoint(latitude=35.6895, longitude=139.6917, altitude=40.0)
    println("Opposite Meridian Point: $oppositeOfTokyo")
    // > Opposite Meridian Point: GeoPoint(latitude=35.6895, longitude=-180.0, altitude=40.0)

    println("---")

    // Case 2: A point in the Western Hemisphere (New York) with null altitude
    val newYork = GeoPoint(latitude = 40.7128, longitude = -74.0060, altitude = null)
    val oppositeOfNewYork = createOppositeMeridianPoint(newYork)

    println("Original Point (New York): $newYork")
    // > Original Point (New York): GeoPoint(latitude=40.7128, longitude=-74.006, altitude=null)
    println("Opposite Meridian Point: $oppositeOfNewYork")
    // > Opposite Meridian Point: GeoPoint(latitude=40.7128, longitude=180.0, altitude=0.0)
}
```