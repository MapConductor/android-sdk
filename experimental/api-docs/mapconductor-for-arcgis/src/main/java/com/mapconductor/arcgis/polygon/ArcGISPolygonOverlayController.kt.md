# ArcGISPolygonOverlayController

### Signature
```kotlin
class ArcGISPolygonOverlayController(
    polygonManager: PolygonManagerInterface<ArcGISActualPolygon> = PolygonManager(),
    override val renderer: ArcGISPolygonOverlayRenderer,
) : PolygonController<ArcGISActualPolygon>(polygonManager, renderer)
```

### Description
The `ArcGISPolygonOverlayController` is a specialized controller responsible for managing and rendering polygon overlays on an ArcGIS map. It acts as a bridge between the generic polygon management logic provided by `PolygonController` and the specific rendering implementation for ArcGIS maps, `ArcGISPolygonOverlayRenderer`.

This class coordinates the state of polygons (e.g., adding, removing, updating) with their visual representation on the map, leveraging a `PolygonManager` for state handling and an `ArcGISPolygonOverlayRenderer` for drawing.

### Parameters
| Parameter | Type | Description | Default |
|-----------|------|-------------|---------|
| `polygonManager` | `PolygonManagerInterface<ArcGISActualPolygon>` | The manager responsible for handling the lifecycle and state of `ArcGISActualPolygon` objects. | `PolygonManager()` |
| `renderer` | `ArcGISPolygonOverlayRenderer` | The renderer responsible for drawing the polygons onto the ArcGIS map view. This is a required parameter. | None |

### Example
Here is an example of how to instantiate and use the `ArcGISPolygonOverlayController`.

```kotlin
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayController
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonManager

// Assume you have an instance of your ArcGIS MapView and a renderer class.
// val arcgisMapView = getYourMapView() 
// class ArcGISPolygonOverlayRenderer(mapView: Any) { /* ... rendering logic ... */ }

// 1. Initialize the renderer for your ArcGIS map
val polygonRenderer = ArcGISPolygonOverlayRenderer(arcgisMapView)

// 2. Instantiate the controller with the specific renderer.
// The default PolygonManager will be used.
val polygonController = ArcGISPolygonOverlayController(
    renderer = polygonRenderer
)

// You can now use the controller to manage polygons on the map.
// For example:
// val newPolygon = createArcGisPolygon()
// polygonController.addPolygon(newPolygon)


// --- Optional: Using a custom Polygon Manager ---

// If you have a custom implementation of PolygonManagerInterface,
// you can provide it during instantiation.
val customManager = CustomPolygonManager<ArcGISActualPolygon>() // Your custom implementation

val customPolygonController = ArcGISPolygonOverlayController(
    polygonManager = customManager,
    renderer = polygonRenderer
)
```