Excellent! Here is the high-quality SDK documentation for the provided Kotlin code snippets.

***

This document provides detailed information about utility functions used to create MapLibre-compatible `Feature` objects for polylines and polygons from a list of geographical points.

### A Note on Visibility

The functions `createMapLibreLines` and `createMapLibrePolygons` are marked as `internal`. This means they are designed for use within the `com.mapconductor.maplibre` module and are not part of the public-facing API.

---

## `createMapLibreLines`

This internal function constructs a list of MapLibre `Feature` objects that represent one or more polylines. It intelligently handles interpolation and splits the line if it crosses the antimeridian to ensure correct rendering on the map.

### Signature

```kotlin
internal fun createMapLibreLines(
    id: String,
    points: List<GeoPointInterface>,
    geodesic: Boolean,
    strokeColor: Color,
    strokeWidth: Dp,
    zIndex: Int = 0,
): List<Feature>
```

### Description

The `createMapLibreLines` function takes a list of geographic points and generates the corresponding MapLibre `LineString` features.

Key features include:
*   **Interpolation**: It can create either geodesic (great circle) paths or simple linear paths based on the `geodesic` flag.
*   **Antimeridian Splitting**: To prevent rendering artifacts, it automatically splits a line that crosses the 180° meridian into multiple `Feature` objects.
*   **Styling**: It attaches properties for styling, such as stroke color and width, directly to the generated features.

### Parameters

| Name          | Type                    | Description                                                                                                                            |
|---------------|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `id`          | `String`                | A unique base identifier for the polyline. An index will be appended to this ID for each feature created (e.g., `polyline-myline-0`).     |
| `points`      | `List<GeoPointInterface>` | A list of `GeoPointInterface` objects that define the vertices of the polyline.                                                        |
| `geodesic`    | `Boolean`               | If `true`, the line follows a great circle path. If `false`, it's a straight line on the Mercator projection.                           |
| `strokeColor` | `Color`                 | The color of the polyline stroke, provided as a `androidx.compose.ui.graphics.Color`.                                                  |
| `strokeWidth` | `Dp`                    | The width of the polyline stroke, specified in `Dp` (density-independent pixels).                                                      |
| `zIndex`      | `Int`                   | The drawing order of the line. Higher values are drawn on top. Defaults to `0`.                                                        |

### Returns

**`List<Feature>`**

A list of MapLibre `Feature` objects. This list will contain a single feature for a simple line or multiple features if the line was split at the antimeridian.

### Example

```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint

// Define the points for the polyline
val points = listOf(
    GeoPoint(latitude = 34.0522, longitude = -118.2437), // Los Angeles
    GeoPoint(latitude = 40.7128, longitude = -74.0060)   // New York
)

// Create the MapLibre line features
val lineFeatures = createMapLibreLines(
    id = "la-to-ny-flight",
    points = points,
    geodesic = true,
    strokeColor = Color.Blue,
    strokeWidth = 3.dp,
    zIndex = 1
)

// The 'lineFeatures' list can now be added to a MapLibre GeoJsonSource.
```

---

## `createMapLibrePolygons`

This internal function constructs a list of MapLibre `Feature` objects representing one or more polygons. It supports geodesic boundaries, holes, and antimeridian splitting.

### Signature

```kotlin
internal fun createMapLibrePolygons(
    id: String,
    points: List<GeoPointInterface>,
    holes: List<List<GeoPointInterface>> = emptyList(),
    geodesic: Boolean,
    fillColor: Color,
    zIndex: Int,
): List<Feature>
```

### Description

The `createMapLibrePolygons` function generates MapLibre `Polygon` features from a set of boundary points and optional holes.

Key features include:
*   **Interpolation**: Creates polygon edges that are either geodesic (great circle) or linear.
*   **Holes**: Supports the creation of polygons with inner boundaries (holes). Note that holes are only applied if the outer boundary does not cross the antimeridian.
*   **Antimeridian Splitting**: Automatically splits a polygon that crosses the 180° meridian into multiple valid polygon features.
*   **Styling**: Attaches properties for styling, such as fill color, to the generated features.

### Parameters

| Name        | Type                            | Description                                                                                                                            |
|-------------|---------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `id`        | `String`                        | A unique base identifier for the polygon. An index will be appended for each feature created (e.g., `polygon-area51-0`).                 |
| `points`    | `List<GeoPointInterface>`       | A list of `GeoPointInterface` objects defining the outer boundary (ring) of the polygon. The function automatically closes the ring.     |
| `holes`     | `List<List<GeoPointInterface>>` | A list of inner rings, where each inner ring is a list of points defining a hole. Defaults to an empty list.                           |
| `geodesic`  | `Boolean`                       | If `true`, the polygon edges follow a great circle path. If `false`, they are straight lines on the Mercator projection.               |
| `fillColor` | `Color`                         | The fill color of the polygon, provided as a `androidx.compose.ui.graphics.Color`.                                                     |
| `zIndex`    | `Int`                           | The drawing order of the polygon. Higher values are drawn on top.                                                                      |

### Returns

**`List<Feature>`**

A list of MapLibre `Feature` objects. This list will contain a single feature for a simple polygon or multiple features if the polygon was split at the antimeridian.

### Example

```kotlin
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.features.GeoPoint

// Define the outer boundary of a polygon
val outerRing = listOf(
    GeoPoint(37.0, -116.0),
    GeoPoint(37.0, -115.0),
    GeoPoint(38.0, -115.0),
    GeoPoint(38.0, -116.0)
)

// Define an inner hole
val innerHole = listOf(
    GeoPoint(37.2, -115.8),
    GeoPoint(37.2, -115.2),
    GeoPoint(37.8, -115.2),
    GeoPoint(37.8, -115.8)
)

// Create the MapLibre polygon features
val polygonFeatures = createMapLibrePolygons(
    id = "restricted-area",
    points = outerRing,
    holes = listOf(innerHole),
    geodesic = false,
    fillColor = Color.Red.copy(alpha = 0.4f),
    zIndex = 0
)

// The 'polygonFeatures' list can now be added to a MapLibre GeoJsonSource.
```

---

## `Color.toMapLibreColorString`

An extension function that converts a Jetpack Compose `Color` into a MapLibre-compatible RGBA string.

### Signature

```kotlin
fun Color.toMapLibreColorString(): String
```

### Description

This utility function takes a `androidx.compose.ui.graphics.Color` object and converts its red, green, blue, and alpha components into the `rgba(r, g, b, a)` string format required by MapLibre style expressions. The color components are scaled from the `[0.0, 1.0]` float range to the `[0, 255]` integer range.

### Returns

**`String`**

The RGBA string representation of the color (e.g., `"rgba(255, 0, 0, 0.5)"`).

### Example

```kotlin
import androidx.compose.ui.graphics.Color

val composeColor = Color.Red.copy(alpha = 0.5f)

// Convert the color to a MapLibre-compatible string
val maplibreColorString = composeColor.toMapLibreColorString()

// The value of maplibreColorString will be "rgba(255, 0, 0, 0.5)"
println(maplibreColorString)
```