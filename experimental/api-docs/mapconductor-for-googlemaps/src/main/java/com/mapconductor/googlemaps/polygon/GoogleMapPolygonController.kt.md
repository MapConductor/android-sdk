# GoogleMapPolygonController

### Signature

```kotlin
class GoogleMapPolygonController(
    polygonManager: PolygonManagerInterface<GoogleMapActualPolygon> = PolygonManager(),
    renderer: GoogleMapPolygonOverlayRenderer,
) : PolygonController<GoogleMapActualPolygon>(polygonManager, renderer)
```

### Description

The `GoogleMapPolygonController` is a specialized controller responsible for managing and rendering polygon overlays on a Google Map. It extends the generic `PolygonController` to provide a concrete implementation tailored for the Google Maps platform.

This class acts as a bridge, connecting the abstract polygon data management logic (handled by `polygonManager`) with the platform-specific rendering logic (handled by `renderer`). It orchestrates the process of adding, updating, and removing polygons from the map view.

### Parameters

| Parameter | Type | Description | Optional |
| :--- | :--- | :--- | :--- |
| `polygonManager` | `PolygonManagerInterface<GoogleMapActualPolygon>` | The manager responsible for handling the lifecycle and state of polygon data. It defaults to a new `PolygonManager` instance. | Yes |
| `renderer` | `GoogleMapPolygonOverlayRenderer` | The renderer responsible for drawing and updating the polygon visuals on the Google Map instance. | No |

### Example

This example demonstrates how to set up and instantiate the `GoogleMapPolygonController`.

```kotlin
// Assuming 'googleMap' is an instance of the GoogleMap object from the Maps SDK
// and 'context' is an available Android Context.

// 1. Create a renderer for Google Maps polygons.
// This renderer needs the GoogleMap object to draw on.
val polygonRenderer = GoogleMapPolygonOverlayRenderer(googleMap, context)

// 2. Instantiate the controller with the specific renderer.
// This example uses the default PolygonManager.
val polygonController = GoogleMapPolygonController(
    renderer = polygonRenderer
)

// The controller is now ready to manage polygons on the map.
// You can use methods inherited from PolygonController to add, update, or remove polygons.

// For example, to add a new polygon:
val polygonToAdd = MapPolygon(
    id = "unique-polygon-id-1",
    options = PolygonOptions()
        .add(LatLng(40.7128, -74.0060)) // New York
        .add(LatLng(34.0522, -118.2437)) // Los Angeles
        .add(LatLng(41.8781, -87.6298)) // Chicago
        .strokeColor(Color.RED)
        .fillColor(Color.argb(100, 255, 0, 0))
)

polygonController.add(listOf(polygonToAdd))
```