# MapLibrePolylineController

The `MapLibrePolylineController` is the primary controller for managing and displaying polylines on a MapLibre map.

## Signature

```kotlin
class MapLibrePolylineController(
    override val renderer: MapLibrePolylineOverlayRenderer,
    polylineManager: PolylineManagerInterface<MapLibreActualPolyline> = renderer.polylineManager,
) : PolylineController<MapLibreActualPolyline>(polylineManager, renderer)
```

## Description

This class serves as the main entry point for developers to interact with polylines within a MapLibre environment. It integrates the generic polyline management logic from the base `PolylineController` with the MapLibre-specific rendering capabilities provided by `MapLibrePolylineOverlayRenderer`. Use this controller to add, remove, update, and manage the lifecycle of polylines on the map.

## Parameters

This section describes the parameters for the `MapLibrePolylineController` constructor.

| Parameter | Type | Description |
|---|---|---|
| `renderer` | `MapLibrePolylineOverlayRenderer` | **Required.** The renderer responsible for drawing and managing polyline overlays on the MapLibre map. |
| `polylineManager` | `PolylineManagerInterface<MapLibreActualPolyline>` | *Optional.* The manager for the underlying polyline data structures. By default, it uses the `polylineManager` instance from the provided `renderer`. |

## Example

The following example demonstrates how to initialize the `MapLibrePolylineController` and use it to add a polyline to the map.

```kotlin
import android.graphics.Color
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapconductor.maplibre.polyline.MapLibrePolylineController
import com.mapconductor.maplibre.polyline.MapLibrePolylineOverlayRenderer
import com.mapconductor.core.polyline.PolylineOptions // Assuming this class exists

// Prerequisites: A MapLibre MapView instance and a loaded Style object
// val mapView: MapView = ...
// val style: Style = ...
// val context: Context = ...

// 1. Initialize the MapLibre-specific renderer
val polylineRenderer = MapLibrePolylineOverlayRenderer(context, mapView, style)

// 2. Create the polyline controller instance, passing in the renderer
val polylineController = MapLibrePolylineController(renderer = polylineRenderer)

// 3. Define the properties for a new polyline
val polylineOptions = PolylineOptions(
    points = listOf(
        LatLng(37.7749, -122.4194), // San Francisco
        LatLng(34.0522, -118.2437)  // Los Angeles
    ),
    color = Color.RED,
    width = 8f
)

// 4. Add the polyline to the map using the controller
val newPolyline = polylineController.addPolyline(polylineOptions)

// The newPolyline is now visible on the map.
// You can further manage it through the controller or the returned object.
// For example, to remove the polyline:
// polylineController.removePolyline(newPolyline)
```