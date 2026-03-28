# `splitByMeridian`

## Signature

```kotlin
fun splitByMeridian(
    points: List<GeoPointInterface>,
    geodesic: Boolean,
): List<List<GeoPointInterface>>
```

## Description

This function processes a list of geographic points (representing a polyline or polygon boundary) and splits it into multiple segments wherever it crosses the 180°/-180° longitude line (the antimeridian).

When a crossing is detected between two consecutive points, the function interpolates a new point precisely on the meridian. The original segment is terminated with this new point. A new segment is then started with a corresponding point on the opposite side of the meridian (e.g., if the first segment ends at +180°, the new one starts at -180°), followed by the next point from the input list.

This process is essential for correctly rendering geometries that wrap around the globe on a 2D map, preventing visual artifacts like long horizontal lines stretching across the map.

## Parameters

| Parameter  | Type                      | Description                                                                                                                            |
| :--------- | :------------------------ | :------------------------------------------------------------------------------------------------------------------------------------- |
| `points`   | `List<GeoPointInterface>` | The list of geographic points to process.                                                                                              |
| `geodesic` | `Boolean`                 | A flag to determine the interpolation method. If `true`, geodesic (great-circle) interpolation is used. If `false`, linear interpolation is used. |

## Returns

**Type:** `List<List<GeoPointInterface>>`

Returns a list of point lists. Each inner list represents a continuous segment of the original geometry that does not cross the antimeridian. If the input list is empty, an empty list is returned. If no meridian crossings occur, a list containing a single list with all the original points is returned.

## Example

Here is an example of splitting a polyline that crosses the 180° meridian.

```kotlin
// For demonstration, define the necessary data classes.
interface GeoPointInterface {
    val latitude: Double
    val longitude: Double
}

data class GeoPoint(
    override val latitude: Double,
    override val longitude: Double
) : GeoPointInterface {
    override fun toString(): String {
        return "GeoPoint(lat=%.2f, lon=%.2f)".format(latitude, longitude)
    }
}

// Assume the existence of the splitByMeridian function.

fun main() {
    // Define a polyline that crosses the 180° meridian.
    val polyline = listOf(
        GeoPoint(latitude = 50.0, longitude = 175.0),   // Point A in the Eastern Hemisphere
        GeoPoint(latitude = 52.0, longitude = -170.0),  // Point B in the Western Hemisphere
        GeoPoint(latitude = 54.0, longitude = -165.0)   // Point C in the Western Hemisphere
    )

    println("Original Polyline: $polyline\n")

    // Split the polyline using geodesic interpolation.
    val splitPolylines = splitByMeridian(polyline, geodesic = true)

    // The result is a list containing two separate polylines.
    println("Resulting Segments:")
    splitPolylines.forEachIndexed { index, segment ->
        println("Segment ${index + 1}: $segment")
    }
}

/*
Expected Output:

Original Polyline: [GeoPoint(lat=50.00, lon=175.00), GeoPoint(lat=52.00, lon=-170.00), GeoPoint(lat=54.00, lon=-165.00)]

Resulting Segments:
Segment 1: [GeoPoint(lat=50.00, lon=175.00), GeoPoint(lat=50.83, lon=180.00)]
Segment 2: [GeoPoint(lat=50.83, lon=-180.00), GeoPoint(lat=52.00, lon=-170.00), GeoPoint(lat=54.00, lon=-165.00)]

*/
```

### Explanation

1.  The original polyline starts at `175.0°E` and crosses the antimeridian to `-170.0°W`.
2.  `splitByMeridian` detects this crossing between the first and second points.
3.  **Segment 1** is created. It contains the starting point (`175.0°E`) and a new, interpolated point on the meridian (`180.0°E`).
4.  **Segment 2** is created to continue the line on the other side of the map. It starts with a new point at `-180.0°W` (with the same interpolated latitude) and includes the remaining points of the original polyline.