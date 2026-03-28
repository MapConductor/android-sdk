# ProjectionInterface

The `ProjectionInterface` defines a contract for converting between geographical coordinates and screen pixel coordinates. This interface is essential for rendering geographical data on a 2D surface, such as a map view, and for translating screen interactions back into geographical locations.

---

## `project()`

Projects a geographical coordinate (`GeoPointInterface`) to a screen coordinate (`Offset`). This is used to determine where a specific latitude/longitude point should be drawn on the screen.

### Signature

```kotlin
fun project(position: GeoPointInterface): Offset
```

### Description

This method takes a geographical point, which typically contains latitude and longitude, and converts it into a 2D Cartesian coordinate (`Offset`) that represents a pixel position on the display.

### Parameters

| Parameter  | Type                | Description                               |
|------------|---------------------|-------------------------------------------|
| `position` | `GeoPointInterface` | The geographical coordinate to project.   |

### Returns

An `Offset` object representing the corresponding (x, y) pixel coordinate on the screen.

---

## `unproject()`

Performs the reverse projection, converting a screen coordinate (`Offset`) back into a geographical coordinate (`GeoPointInterface`). This is useful for handling user interactions like taps or clicks on the map to identify the corresponding geographical location.

### Signature

```kotlin
fun unproject(point: Offset): GeoPointInterface
```

### Description

This method takes a 2D screen coordinate (`Offset`) and converts it back into its corresponding geographical coordinate (`GeoPointInterface`), which typically contains latitude and longitude.

### Parameters

| Parameter | Type     | Description                                       |
|-----------|----------|---------------------------------------------------|
| `point`   | `Offset` | The screen (x, y) pixel coordinate to unproject.  |

### Returns

A `GeoPointInterface` object representing the geographical coordinate corresponding to the screen point.

---

### Example

The following conceptual example demonstrates how `ProjectionInterface` might be used within a map component to draw a marker and handle a tap event.

```kotlin
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.projection.ProjectionInterface

// A hypothetical map view that uses a projection
class MapView(val projection: ProjectionInterface) {

    // A point of interest to display on the map
    private val poi: GeoPointInterface = GeoPoint(latitude = 40.7128, longitude = -74.0060) // New York City

    // Function to draw the map content
    fun draw(canvas: Canvas) {
        // 1. Project the GeoPoint to a screen Offset
        val screenPosition: Offset = projection.project(poi)

        // Draw a circle at the projected position
        canvas.drawCircle(
            color = Color.Red,
            radius = 10f,
            center = screenPosition
        )
    }

    // Function to handle user taps
    fun handleTap(tapPosition: Offset) {
        // 2. Unproject the screen tap position to a GeoPoint
        val tappedGeoPoint: GeoPointInterface = projection.unproject(tapPosition)

        println("User tapped at: Lat=${tappedGeoPoint.latitude}, Lon=${tappedGeoPoint.longitude}")
    }
}

// A conceptual Composable using the MapView
@Composable
fun MapScreen(mapView: MapView) {
    Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        detectTapGestures { offset ->
            // Pass the tap offset to the handler
            mapView.handleTap(offset)
        }
    }) {
        // Draw the map content
        mapView.draw(this)
    }
}
```