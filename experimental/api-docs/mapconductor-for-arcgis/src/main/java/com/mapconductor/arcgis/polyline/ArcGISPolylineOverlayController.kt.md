# ArcGISPolylineOverlayController

The `ArcGISPolylineOverlayController` is a specialized controller that manages the display and lifecycle of polyline overlays on an ArcGIS map.

## Signature

```kotlin
class ArcGISPolylineOverlayController(
    polylineManager: PolylineManagerInterface<ArcGISActualPolyline> = PolylineManager(),
    override val renderer: ArcGISPolylineOverlayRenderer,
) : PolylineController<ArcGISActualPolyline>(polylineManager, renderer)
```

## Description

This class acts as the primary component for managing polyline overlays within an ArcGIS environment. It connects the core polyline management logic (from `PolylineManager`) with the specific rendering implementation required for ArcGIS maps (`ArcGISPolylineOverlayRenderer`).

The controller orchestrates the state of polyline data and delegates the responsibility of drawing the polylines on the map to the provided `renderer`. It inherits from the generic `PolylineController`, specializing it for `ArcGISActualPolyline` objects.

## Parameters

| Parameter | Type | Description | Default |
| :--- | :--- | :--- | :--- |
| `polylineManager` | `PolylineManagerInterface<ArcGISActualPolyline>` | The manager responsible for handling the state and lifecycle of polyline data. It tracks all added, removed, and updated polylines. | `PolylineManager()` |
| `renderer` | `ArcGISPolylineOverlayRenderer` | The renderer responsible for drawing the polylines onto the ArcGIS map view. This object handles the platform-specific drawing operations. | - |

## Example

The following example demonstrates how to initialize the `ArcGISPolylineOverlayController` to manage polylines on an ArcGIS map.

```kotlin
import com.esri.arcgisruntime.mapping.view.MapView
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayController
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayRenderer

// 1. Assume you have an instance of an ArcGIS MapView
val mapView: MapView = getMyArcGISMapView()

// 2. Create the specific renderer for ArcGIS polyline overlays,
//    passing the MapView instance to it.
val polylineRenderer = ArcGISPolylineOverlayRenderer(mapView)

// 3. Instantiate the controller with the renderer.
//    The default PolylineManager will be used automatically.
val polylineController = ArcGISPolylineOverlayController(renderer = polylineRenderer)

// 4. The controller is now set up. You can use it to manage polylines
//    on the map. For example, adding a new polyline:
//
// val polylineOptions = PolylineOptions(...)
// val newPolyline = polylineController.addPolyline(polylineOptions)
```