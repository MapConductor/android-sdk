Of course! Here is the high-quality SDK documentation for the provided code snippet.

# Polygon

The `Polygon` composable adds a closed shape to the map, defined by a series of coordinates. It can be used to highlight areas, define boundaries, or create other custom shapes. Polygons can have solid fill and stroke colors, and can also contain interior holes.

There are three overloads for the `Polygon` composable, allowing you to create a polygon from a list of points, from a rectangular bound, or from a pre-configured `PolygonState` object.

---

## Polygon (from points)

This is the primary composable for creating a custom-shaped polygon on the map. You define the polygon's outer boundary by providing a list of geographical points.

### Signature

```kotlin
@Composable
fun MapViewScope.Polygon(
    points: List<GeoPointInterface>,
    holes: List<List<GeoPointInterface>> = emptyList(),
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    zIndex: Int = 0,
    extra: Serializable? = null,
    onClick: OnPolygonEventHandler? = null,
)
```

### Description

This composable draws a polygon on the map defined by a list of vertices for its outer boundary and an optional list of holes. The polygon's appearance and behavior, such as colors, stroke width, and click handling, can be customized. The shape is automatically closed by connecting the last point to the first.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `points` | `List<GeoPointInterface>` | **Required.** A list of vertices for the polygon's outer boundary. The list must contain at least three points. |
| `holes` | `List<List<GeoPointInterface>>` | A list of hole definitions. Each hole is itself a list of vertices defining an inner boundary. Defaults to `emptyList()`. |
| `id` | `String?` | An optional unique identifier for the polygon. Defaults to `null`. |
| `strokeColor` | `Color` | The color of the polygon's outline. Defaults to `Color.Black`. |
| `strokeWidth` | `Dp` | The width of the polygon's outline. Defaults to `1.dp`. |
| `fillColor` | `Color` | The color inside the polygon. Defaults to `Color.Transparent`. |
| `geodesic` | `Boolean` | If `true`, the polygon's edges are drawn as the shortest path on the Earth's surface (a geodesic line). If `false`, edges are drawn as straight lines on the map's 2D projection. Defaults to `false`. |
| `zIndex` | `Int` | The stacking order of this polygon relative to other map overlays. Polygons with a higher `zIndex` are drawn on top of those with a lower `zIndex`. Defaults to `0`. |
| `extra` | `Serializable?` | Optional serializable data to associate with the polygon, which can be retrieved later (e.g., in the `onClick` handler). Defaults to `null`. |
| `onClick` | `OnPolygonEventHandler?` | A callback lambda that is invoked when the user clicks on the polygon. The handler receives the polygon's `id` and `extra` data. Defaults to `null`. |

### Example

Here is an example of creating a triangular polygon with a rectangular hole inside.

```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPoint

@Composable
fun MapViewScope.MyPolygonExample() {
    // Define the outer boundary of the polygon (a triangle)
    val outerPoints = listOf(
        GeoPoint(40.7128, -74.0060), // New York
        GeoPoint(34.0522, -118.2437), // Los Angeles
        GeoPoint(29.7604, -95.3698)  // Houston
    )

    // Define a hole inside the polygon
    val holePoints = listOf(
        GeoPoint(35.0, -98.0),
        GeoPoint(35.0, -99.0),
        GeoPoint(36.0, -99.0),
        GeoPoint(36.0, -98.0)
    )

    Polygon(
        points = outerPoints,
        holes = listOf(holePoints),
        strokeColor = Color.Blue,
        strokeWidth = 3.dp,
        fillColor = Color.Blue.copy(alpha = 0.3f),
        geodesic = true,
        zIndex = 1,
        onClick = { id, extra ->
            println("Polygon clicked! ID: $id")
        }
    )
}
```

---

## Polygon (from bounds)

A convenience composable for creating a rectangular polygon from a `GeoRectBounds` object.

### Signature

```kotlin
@Composable
fun MapViewScope.Polygon(
    bounds: GeoRectBounds,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    zIndex: Int = 0,
    extra: Serializable? = null,
    onClick: OnPolygonEventHandler? = null,
)
```

### Description

This composable simplifies the creation of a rectangular polygon. It takes a `GeoRectBounds` object and automatically generates the four corner points to draw the rectangle on the map.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `bounds` | `GeoRectBounds` | **Required.** The geographical rectangle that defines the polygon's shape. |
| `id` | `String?` | An optional unique identifier for the polygon. Defaults to `null`. |
| `strokeColor` | `Color` | The color of the polygon's outline. Defaults to `Color.Black`. |
| `strokeWidth` | `Dp` | The width of the polygon's outline. Defaults to `1.dp`. |
| `fillColor` | `Color` | The color inside the polygon. Defaults to `Color.Transparent`. |
| `geodesic` | `Boolean` | If `true`, the polygon's edges are drawn as geodesic lines. Defaults to `false`. |
| `zIndex` | `Int` | The stacking order of this polygon relative to other map overlays. Defaults to `0`. |
| `extra` | `Serializable?` | Optional serializable data to associate with the polygon. Defaults to `null`. |
| `onClick` | `OnPolygonEventHandler?` | A callback lambda that is invoked when the user clicks on the polygon. Defaults to `null`. |

### Example

```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds

@Composable
fun MapViewScope.MyBoundedPolygonExample() {
    val coloradoBounds = GeoRectBounds(
        northEast = GeoPoint(41.0, -102.05),
        southWest = GeoPoint(37.0, -109.05)
    )

    Polygon(
        bounds = coloradoBounds,
        strokeColor = Color.Red,
        strokeWidth = 2.dp,
        fillColor = Color.Red.copy(alpha = 0.2f),
        id = "colorado-boundary"
    )
}
```

---

## Polygon (from state)

An advanced composable that adds a polygon to the map using a pre-configured `PolygonState` object.

### Signature

```kotlin
@Composable
fun MapViewScope.Polygon(state: PolygonState)
```

### Description

This composable is typically used for advanced scenarios where the polygon's state is managed externally. It adds a polygon defined by the given `PolygonState` to the map and handles its lifecycle, adding it on composition and removing it on disposal.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `PolygonState` | **Required.** The state object that defines all properties of the polygon, including its points, holes, and styling. |

### Example

```kotlin
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.PolygonState

@Composable
fun MapViewScope.MyStatefulPolygonExample() {
    val polygonState = remember {
        PolygonState(
            points = listOf(
                GeoPoint(40.7128, -74.0060),
                GeoPoint(34.0522, -118.2437),
                GeoPoint(29.7604, -95.3698)
            ),
            fillColor = Color.Green.copy(alpha = 0.5f),
            id = "stateful-polygon"
        )
    }

    // The polygon is drawn on the map and its lifecycle is managed automatically.
    Polygon(state = polygonState)
}
```