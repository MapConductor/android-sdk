Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# Polyline

The `Polyline` composable adds a line or a series of connected line segments to the map. It is a flexible overlay that can be used to represent paths, routes, or boundaries.

This function must be called within the scope of a `MapView` composable.

There are three overloads for creating a `Polyline`:
1.  [From a list of points](#polyline-from-a-list-of-points): The primary method for creating a polyline from an ordered list of geographical coordinates.
2.  [From a bounding box](#polyline-from-a-bounding-box): A convenience method to draw a rectangular outline from a `GeoRectBounds` object.
3.  [From a state object](#polyline-from-a-state-object): A lower-level method that uses a `PolylineState` object for more advanced state management.

---

## Polyline (from a list of points)

This composable draws a polyline on the map by connecting an ordered list of `GeoPointInterface` vertices.

### Signature
```kotlin
@Composable
fun MapViewScope.Polyline(
    points: List<GeoPointInterface>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    geodesic: Boolean = false,
    zIndex: Int = 0,
    extra: Serializable? = null,
    onClick: OnPolylineEventHandler? = null,
)
```

### Description
This is the primary function for creating a polyline. You provide a list of geographical points, and it renders a line connecting them in the specified order. You can customize the appearance (color, width), behavior (geodesic), and interactivity (click handler) of the polyline.

### Parameters
| Parameter     | Type                      | Description                                                                                                                            |
|---------------|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `points`      | `List<GeoPointInterface>` | **Required.** An ordered list of `GeoPointInterface` objects that define the vertices of the polyline.                                  |
| `id`          | `String?`                 | An optional unique identifier for the polyline. This can be useful for finding or managing the polyline later. Defaults to `null`.        |
| `strokeColor` | `Color`                   | The color of the polyline. Defaults to `Color.Black`.                                                                                  |
| `strokeWidth` | `Dp`                      | The width of the polyline stroke in density-independent pixels (`Dp`). Defaults to `1.dp`.                                             |
| `geodesic`    | `Boolean`                 | If `true`, the segments of the polyline are drawn as geodesic curves that follow the curvature of the Earth. Defaults to `false`.        |
| `zIndex`      | `Int`                     | The stacking order of this polyline relative to other map overlays. Polylines with higher `zIndex` values are drawn on top. Defaults to `0`. |
| `extra`       | `Serializable?`           | Optional, extra serializable data to associate with the polyline. This can be retrieved in event handlers. Defaults to `null`.          |
| `onClick`     | `OnPolylineEventHandler?` | A callback lambda that is invoked when the user clicks on the polyline. Defaults to `null`.                                            |

### Example
```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapView
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polyline.Polyline

// Inside a Composable function
MapView {
    val routePoints = listOf(
        GeoPoint(40.7128, -74.0060), // New York
        GeoPoint(34.0522, -118.2437), // Los Angeles
        GeoPoint(41.8781, -87.6298)  // Chicago
    )

    Polyline(
        points = routePoints,
        id = "us-trip-route",
        strokeColor = Color.Blue,
        strokeWidth = 4.dp,
        geodesic = true,
        onClick = { polylineState ->
            println("Clicked on polyline with ID: ${polylineState.id}")
        }
    )
}
```

---

## Polyline (from a bounding box)

This composable is a convenience function that draws a rectangular polyline outlining a given `GeoRectBounds`.

### Signature
```kotlin
@Composable
fun MapViewScope.Polyline(
    bounds: GeoRectBounds,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    geodesic: Boolean = false,
    zIndex: Int = 0,
    extra: Serializable? = null,
    onClick: OnPolylineEventHandler? = null,
)
```

### Description
This function simplifies the process of drawing a rectangle on the map. It takes a `GeoRectBounds` object and automatically generates the five points (four corners and a closing point) needed to draw a closed rectangular polyline.

### Parameters
| Parameter     | Type                      | Description                                                                                                                            |
|---------------|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `bounds`      | `GeoRectBounds`           | **Required.** The geographical bounding box to be outlined by the polyline.                                                            |
| `id`          | `String?`                 | An optional unique identifier for the polyline. Defaults to `null`.                                                                    |
| `strokeColor` | `Color`                   | The color of the polyline. Defaults to `Color.Black`.                                                                                  |
| `strokeWidth` | `Dp`                      | The width of the polyline stroke in density-independent pixels (`Dp`). Defaults to `1.dp`.                                             |
| `geodesic`    | `Boolean`                 | If `true`, the segments of the polyline are drawn as geodesic curves. Defaults to `false`.                                             |
| `zIndex`      | `Int`                     | The stacking order of this polyline relative to other map overlays. Polylines with higher `zIndex` values are drawn on top. Defaults to `0`. |
| `extra`       | `Serializable?`           | Optional, extra serializable data to associate with the polyline. Defaults to `null`.                                                  |
| `onClick`     | `OnPolylineEventHandler?` | A callback lambda that is invoked when the user clicks on the polyline. Defaults to `null`.                                            |

### Example
```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapView
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.polyline.Polyline

// Inside a Composable function
MapView {
    val coloradoBounds = GeoRectBounds(
        northEast = GeoPoint(41.0, -102.05),
        southWest = GeoPoint(37.0, -109.05)
    )

    Polyline(
        bounds = coloradoBounds,
        id = "colorado-border",
        strokeColor = Color.Red,
        strokeWidth = 3.dp
    )
}
```

---

## Polyline (from a state object)

This is a lower-level composable that draws a polyline on the map using a `PolylineState` object.

### Signature
```kotlin
@Composable
fun MapViewScope.Polyline(state: PolylineState)
```

### Description
This function is intended for more advanced use cases where you need to manage the entire state of a polyline as a single object. It handles the lifecycle of the polyline on the map, adding it when the composable enters the composition and removing it upon disposal. For most common scenarios, the other `Polyline` overloads are recommended.

### Parameters
| Parameter | Type          | Description                                                                                             |
|-----------|---------------|---------------------------------------------------------------------------------------------------------|
| `state`   | `PolylineState` | **Required.** A state object that encapsulates all properties of the polyline, including its geometry, appearance, and event handlers. |

### Example
```kotlin
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapView
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.core.polyline.PolylineState

// Inside a Composable function
MapView {
    val polylineState = remember {
        PolylineState(
            points = listOf(
                GeoPoint(48.8566, 2.3522), // Paris
                GeoPoint(51.5074, -0.1278) // London
            ),
            id = "paris-london-route",
            strokeColor = Color.Magenta,
            strokeWidth = 5.dp
        )
    }

    Polyline(state = polylineState)
}
```