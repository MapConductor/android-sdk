# ArcGISRasterLayerController

### Signature

```kotlin
class ArcGISRasterLayerController(
    rasterLayerManager: RasterLayerManagerInterface<Layer> = RasterLayerManager(),
    renderer: ArcGISRasterLayerOverlayRenderer,
) : RasterLayerController<Layer>(rasterLayerManager, renderer)
```

### Description

The `ArcGISRasterLayerController` is a specialized controller responsible for managing and displaying raster data as layers within an ArcGIS map environment. It extends the generic `RasterLayerController`, bridging the core raster layer management logic with an ArcGIS-specific rendering implementation (`ArcGISRasterLayerOverlayRenderer`).

This controller coordinates the state of raster layers, managed by the `rasterLayerManager`, with their visual representation on the map, handled by the `renderer`. It is the primary component for integrating raster layer functionality into an ArcGIS map application.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `rasterLayerManager` | `RasterLayerManagerInterface<Layer>` | The manager responsible for handling the collection of raster layers. If not provided, a default `RasterLayerManager` instance is created. (Optional) |
| `renderer` | `ArcGISRasterLayerOverlayRenderer` | The ArcGIS-specific renderer responsible for drawing the raster layers onto the map view. (Required) |

### Example

The following example demonstrates how to create an instance of `ArcGISRasterLayerController` and associate it with an ArcGIS `MapView`.

```kotlin
import com.arcgismaps.mapping.view.MapView
import com.mapconductor.arcgis.raster.ArcGISRasterLayerController
import com.mapconductor.arcgis.raster.ArcGISRasterLayerOverlayRenderer
import com.mapconductor.core.raster.RasterLayerManager

// Assuming you have an ArcGIS MapView instance from your layout
val mapView: MapView = findViewById(R.id.mapView)

// 1. Create the ArcGIS-specific renderer.
// This renderer typically interacts with the map view's graphics overlays to display raster data.
val arcGisRenderer = ArcGISRasterLayerOverlayRenderer(mapView)

// 2. Instantiate the controller with the required renderer.
// This example uses the default RasterLayerManager.
val rasterController = ArcGISRasterLayerController(renderer = arcGisRenderer)

// Now you can use the rasterController to add, remove, and manage raster layers.
// For example:
// val myRaster = // ... create or load an ArcGIS Raster
// val myRasterLayer = RasterLayer(myRaster)
// rasterController.addLayer(myRasterLayer)


// --- Alternative Example with a custom manager ---

// You can also provide a custom or shared RasterLayerManager instance.
// This is useful if you need to share layer state between different components.
val customManager = RasterLayerManager<Layer>()
val rasterControllerWithCustomManager = ArcGISRasterLayerController(
    rasterLayerManager = customManager,
    renderer = arcGisRenderer
)
```