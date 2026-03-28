Excellent! Here is the high-quality SDK documentation for the provided Kotlin code snippet, formatted in Markdown.

# Mapbox Feature Creation Utilities

This document provides details on utility functions used to create Mapbox `Feature` objects for lines and polygons from `GeoPointInterface` data. These functions handle geodesic interpolation and antimeridian splitting.

---

## `createMapboxLines`

Creates a list of Mapbox `Feature` objects representing one or more polylines.

### Signature
```kotlin
internal fun createMapboxLines(
    id: String,
    points: List<GeoPointInterface>,
    geodesic: Boolean,
    strokeColor: Color,
    strokeWidth: Dp,
    zIndex: Int = 0,
): List<Feature>
```

### Description
This function generates a list of Mapbox `Feature` objects that represent a polyline defined by a sequence of geographical points. It can create either geodesic lines (which follow the curvature of the Earth) or linear rhumb lines.

The function automatically handles cases where the line crosses the antimeridian (180th meridian) by splitting it into multiple `Feature` objects. Each feature is assigned style properties such as stroke color, width, and z-index, which are used by the Mapbox style layer.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `id` | `String` | A unique base identifier for the polyline. This is used to generate unique IDs for each resulting `Feature`. |
| `points` | `List<GeoPointInterface>` | A list of geographical points that define the vertices of the polyline. |
| `geodesic` | `Boolean` | If `true`, the line is drawn as a geodesic path (the shortest path on the Earth's surface). If `false`, it's a straight rhumb line. |
| `strokeColor` | `Color` | The color of the polyline's stroke. |
| `strokeWidth` | `Dp` | The width of the polyline's stroke, specified in density-independent pixels (`Dp`). |
| `zIndex` | `Int` | *(Optional)* The drawing order of the line. Lines with a higher `zIndex` are drawn over those with a lower `zIndex`. Defaults to `0`. |

### Returns
**`List<Feature>`**

A list of Mapbox `Feature` objects. If the polyline crosses the antimeridian, this list will contain multiple features, one for each segment. Each feature includes the line geometry and its style properties.

### Example

```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint

// 1. Define the points for the polyline
val linePoints = listOf(
    GeoPoint(latitude = 34.0522, longitude = -118.2437), // Los Angeles
    GeoPoint(latitude = 40.7128, longitude = -74.0060)   // New York
)

// 2. Create the Mapbox line features
val lineFeatures = createMapboxLines(
    id = "my-trip-line",
    points = linePoints,
    geodesic = true,
    strokeColor = Color.Blue,
    strokeWidth = 4.dp,
    zIndex = 2
)

// 3. 'lineFeatures' can now be added to a Mapbox source to be displayed on the map.
```

---

## `createMapboxPolygons`

Creates a list of Mapbox `Feature` objects representing one or more polygons.

### Signature
```kotlin
internal fun createMapboxPolygons(
    id: String,
    points: List<GeoPointInterface>,
    holes: List<List<GeoPointInterface>> = emptyList(),
    geodesic: Boolean,
    fillColor: Color,
    zIndex: Int,
): List<Feature>
```

### Description
This function generates a list of Mapbox `Feature` objects for a polygon defined by an outer boundary and optional inner holes. It supports both geodesic and linear polygon edges.

The function automatically closes the polygon's outer ring if the first and last points are not identical. It also handles polygons that cross the antimeridian by splitting them into multiple `Feature` objects.

**Important:** Holes are only supported for polygons that do **not** cross the antimeridian. If the polygon is split, any provided holes will be ignored.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `id` | `String` | A unique base identifier for the polygon. This is used to generate unique IDs for each resulting `Feature`. |
| `points` | `List<GeoPointInterface>` | A list of geographical points defining the outer boundary of the polygon. |
| `holes` | `List<List<GeoPointInterface>>` | *(Optional)* A list of point lists, where each inner list defines a hole within the polygon. Ignored if the polygon crosses the antimeridian. Defaults to `emptyList()`. |
| `geodesic` | `Boolean` | If `true`, the polygon edges are drawn as geodesic paths. If `false`, they are straight rhumb lines. |
| `fillColor` | `Color` | The fill color of the polygon. |
| `zIndex` | `Int` | The drawing order of the polygon. Polygons with a higher `zIndex` are drawn over those with a lower `zIndex`. |

### Returns
**`List<Feature>`**

A list of Mapbox `Feature` objects. If the polygon crosses the antimeridian, this list will contain multiple features. Each feature includes the polygon geometry and its style properties.

### Example

```kotlin
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.features.GeoPoint

// 1. Define the outer boundary of a polygon
val outerRing = listOf(
    GeoPoint(37.8, -122.5),
    GeoPoint(37.8, -122.4),
    GeoPoint(37.7, -122.4),
    GeoPoint(37.7, -122.5)
)

// 2. Define a hole within the polygon
val innerHole = listOf(
    GeoPoint(37.78, -122.45),
    GeoPoint(37.78, -122.44),
    GeoPoint(37.77, -122.44),
    GeoPoint(37.77, -122.45)
)

// 3. Create the Mapbox polygon features
val polygonFeatures = createMapboxPolygons(
    id = "bay-area-zone",
    points = outerRing,
    holes = listOf(innerHole),
    geodesic = false,
    fillColor = Color.Red.copy(alpha = 0.5f),
    zIndex = 1
)

// 4. 'polygonFeatures' can now be added to a Mapbox source to be displayed on the map.
```