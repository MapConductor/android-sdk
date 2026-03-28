# MapboxPolylineController

### Signature

```kotlin
class MapboxPolylineController(
    override val renderer: MapboxPolylineOverlayRenderer,
    polylineManager: PolylineManagerInterface<MapboxActualPolyline> = renderer.polylineManager,
) : PolylineController<MapboxActualPolyline>(polylineManager, renderer)
```

### Description

The `MapboxPolylineController` is a specialized controller class responsible for managing and rendering polylines on a Mapbox map. It acts as a bridge between the data-handling logic of a `PolylineManagerInterface` and the visual rendering provided by `MapboxPolylineOverlayRenderer`.

By inheriting from the generic `PolylineController`, it provides a consistent API for polyline manipulation while being specifically tailored for the Mapbox environment. This class simplifies the process of adding, updating, and removing polylines by coordinating the underlying data manager and the renderer.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `renderer` | `MapboxPolylineOverlayRenderer` | The Mapbox-specific renderer instance responsible for drawing the polylines onto the map's style layer. |
| `polylineManager` | `PolylineManagerInterface<MapboxActualPolyline>` | The manager responsible for the underlying data and state of the polylines. If not provided, it defaults to the manager instance associated with the `renderer`, ensuring both components operate on the same data source. |

### Example

Here is an example of how to set up and use the `MapboxPolylineController` to draw a polyline between two coordinates on a Mapbox map.

```kotlin
import android.graphics.Color
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.Style
import com.mapconductor.core.polyline.PolylineOptions
import com.mapconductor.mapbox.polyline.MapboxPolylineOverlayRenderer
import com.mapconductor.mapbox.polyline.MapboxPolylineController

// Assume 'mapboxStyle' is an instance of com.mapbox.mapboxsdk.maps.Style
// and 'context' is an Android Context.

// 1. Initialize the Mapbox-specific renderer with the map's style
val polylineRenderer = MapboxPolylineOverlayRenderer(mapboxStyle, context)

// 2. Create the controller, passing in the renderer.
// The controller will automatically use the renderer's polyline manager by default.
val polylineController = MapboxPolylineController(renderer = polylineRenderer)

// 3. Define the properties of the polyline you want to add
val polylineOptions = PolylineOptions(
    points = listOf(
        LatLng(37.7749, -122.4194), // San Francisco
        LatLng(34.0522, -118.2437)  // Los Angeles
    ),
    color = Color.BLUE,
    width = 5f
)

// 4. Use the controller to add the polyline to the map.
// The controller delegates this task to its manager and renderer.
val addedPolyline = polylineController.addPolyline(polylineOptions)

// The polyline is now visible on the map. You can continue to interact
// with it through the controller.
// For example, to remove the polyline:
// polylineController.removePolyline(addedPolyline)
```