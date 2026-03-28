Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

# GoogleMapPolylineController

### Signature
```kotlin
class GoogleMapPolylineController(
    polylineManager: PolylineManagerInterface<GoogleMapActualPolyline> = PolylineManager(),
    renderer: GoogleMapPolylineOverlayRenderer,
) : PolylineController<GoogleMapActualPolyline>(polylineManager, renderer)
```

### Description
The `GoogleMapPolylineController` is the primary controller for managing and rendering polylines on a Google Map. It serves as the main entry point for all polyline-related operations within the Google Maps implementation of the MapConductor framework.

This class orchestrates the core logic from a `PolylineManager` and the visual representation from a `GoogleMapPolylineOverlayRenderer`. It extends the base `PolylineController` to provide a concrete implementation tailored specifically for the Google Maps SDK, handling the lifecycle and drawing of `GoogleMapActualPolyline` objects.

### Parameters
The constructor accepts the following parameters:

| Parameter | Type | Description |
|---|---|---|
| `polylineManager` | `PolylineManagerInterface<GoogleMapActualPolyline>` | The manager responsible for the lifecycle and state of polyline data. It handles adding, removing, and updating polylines. **Optional**: If not provided, a default `PolylineManager` instance is created. |
| `renderer` | `GoogleMapPolylineOverlayRenderer` | The platform-specific renderer that draws the polylines onto the `GoogleMap` instance. This parameter is **required**. |

### Example
The following example demonstrates how to initialize the `GoogleMapPolylineController` to manage polylines on a `GoogleMap` instance.

```kotlin
import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.mapconductor.googlemaps.polyline.GoogleMapPolylineController
import com.mapconductor.googlemaps.polyline.GoogleMapPolylineOverlayRenderer

// Assuming you have a GoogleMap object from a MapFragment or MapView
lateinit var googleMap: GoogleMap
lateinit var context: Context

// 1. Create an instance of the renderer, which requires the GoogleMap object and a Context.
val polylineRenderer = GoogleMapPolylineOverlayRenderer(googleMap, context)

// 2. Instantiate the controller, passing the required renderer.
//    The polylineManager is optional and will be created with its default implementation.
val polylineController = GoogleMapPolylineController(renderer = polylineRenderer)

// Now you can use the polylineController to add, remove, and manage polylines.
// For example, to add a new polyline:
// val newPolyline = Polyline(...) 
// polylineController.add(listOf(newPolyline))
```