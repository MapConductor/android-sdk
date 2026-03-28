Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# MapboxRasterLayerOverlayRenderer

## Signature

```kotlin
class MapboxRasterLayerOverlayRenderer(
    private val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : RasterLayerOverlayRendererInterface<MapboxRasterLayerHandle>
```

## Description

The `MapboxRasterLayerOverlayRenderer` is responsible for rendering and managing raster tile overlays on a Mapbox map. It acts as a bridge between the abstract `RasterLayerEntityInterface` and the concrete Mapbox SDK, handling the lifecycle of raster layers, including their creation, updates, and removal.

This renderer translates different types of raster data sources (`UrlTemplate`, `TileJson`, `ArcGisService`) into Mapbox-compatible sources and layers. It also manages the visual properties of layers, such as opacity and visibility, and ensures they are stacked correctly according to their `zIndex`.

A key feature is its special handling for "marker tile" layers (identified by the `marker-tile-` prefix in their ID), which are strategically placed below map markers to avoid obscuring them.

## Constructor

### Signature

```kotlin
MapboxRasterLayerOverlayRenderer(
    holder: MapboxMapViewHolder, 
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main)
)
```

### Description

Initializes a new instance of the `MapboxRasterLayerOverlayRenderer`.

### Parameters

| Parameter   | Type                | Description                                                                                             |
| :---------- | :------------------ | :------------------------------------------------------------------------------------------------------ |
| `holder`    | `MapboxMapViewHolder` | The view holder that contains the `MapboxMap` instance where layers will be rendered.                   |
| `coroutine` | `CoroutineScope`    | The coroutine scope used for executing asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

---

# Methods

## onAdd

### Signature

```kotlin
override suspend fun onAdd(
    data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>,
): List<MapboxRasterLayerHandle?>
```

### Description

Adds a list of new raster layers to the map. For each item in the `data` list, this method creates a corresponding Mapbox `rasterSource` and `rasterLayer`. After adding the layers, it rebuilds the stacking order of all non-marker raster layers to respect their `zIndex` properties.

### Parameters

| Parameter | Type                                                              | Description                                                              |
| :-------- | :---------------------------------------------------------------- | :----------------------------------------------------------------------- |
| `data`    | `List<RasterLayerOverlayRendererInterface.AddParamsInterface>` | A list of `AddParamsInterface` objects, each defining a raster layer to add. |

### Returns

**Type:** `List<MapboxRasterLayerHandle?>`

A list of `MapboxRasterLayerHandle` objects corresponding to the newly created layers. If a layer fails to be created, its entry in the list will be `null`.

## onChange

### Signature

```kotlin
override suspend fun onChange(
    data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<MapboxRasterLayerHandle>>,
): List<MapboxRasterLayerHandle?>
```

### Description

Processes updates for a list of existing raster layers.

If a layer's `source` has changed, the old Mapbox source and layer are removed and new ones are created. If only properties like `opacity` or `visible` have changed, the existing Mapbox layer is updated in place for better performance. After processing all changes, it rebuilds the stacking order of non-marker raster layers.

### Parameters

| Parameter | Type                                                                                    | Description                                                                                             |
| :-------- | :-------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `data`    | `List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<MapboxRasterLayerHandle>>` | A list of `ChangeParamsInterface` objects, each containing the previous and current state of a layer. |

### Returns

**Type:** `List<MapboxRasterLayerHandle?>`

A list of `MapboxRasterLayerHandle` objects for the updated layers.

## onRemove

### Signature

```kotlin
override suspend fun onRemove(data: List<RasterLayerEntityInterface<MapboxRasterLayerHandle>>)
```

### Description

Removes a list of raster layers from the map. For each layer entity provided, this method removes its associated Mapbox layer and source from the map style. After removal, it rebuilds the stacking order of the remaining non-marker raster layers.

### Parameters

| Parameter | Type                                                      | Description                               |
| :-------- | :-------------------------------------------------------- | :---------------------------------------- |
| `data`    | `List<RasterLayerEntityInterface<MapboxRasterLayerHandle>>` | A list of layer entities to be removed. |

### Returns

This function does not return a value.

## onPostProcess

### Signature

```kotlin
override suspend fun onPostProcess()
```

### Description

A lifecycle method from the `RasterLayerOverlayRendererInterface`. In this implementation, it is a no-op and performs no actions.

### Parameters

This function does not take any parameters.

### Returns

This function does not return a value.

---

# Data Classes

## MapboxRasterLayerHandle

### Signature

```kotlin
data class MapboxRasterLayerHandle(
    val sourceId: String,
    val layerId: String,
)
```

### Description

A data class that serves as a handle for a raster layer managed by the `MapboxRasterLayerOverlayRenderer`. It encapsulates the unique identifiers for the underlying Mapbox `rasterSource` and `rasterLayer`, which are necessary for interacting with the layer via the Mapbox SDK.

### Parameters

| Parameter  | Type     | Description                               |
| :--------- | :------- | :---------------------------------------- |
| `sourceId` | `String` | The unique ID for the Mapbox `rasterSource`. |
| `layerId`  | `String` | The unique ID for the Mapbox `rasterLayer`.  |

---

# Example

The following example demonstrates how to instantiate the `MapboxRasterLayerOverlayRenderer` and use it to add a new raster layer from a URL template.

```kotlin
import com.mapconductor.core.raster.*
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.raster.MapboxRasterLayerOverlayRenderer
import kotlinx.coroutines.runBlocking

// Assume 'mapboxMapViewHolder' is an initialized instance of MapboxMapViewHolder
val mapboxMapViewHolder: MapboxMapViewHolder = /* ... */

// 1. Instantiate the renderer
val rasterRenderer = MapboxRasterLayerOverlayRenderer(mapboxMapViewHolder)

// 2. Define the raster layer to be added
val layerState = RasterLayerState(
    id = "open-street-map",
    source = RasterLayerSource.UrlTemplate(
        template = "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
        tileSize = 256,
        attribution = "© OpenStreetMap contributors"
    ),
    visible = true,
    opacity = 0.8f,
    zIndex = 1
)

// A simple implementation of the AddParamsInterface for the example
data class AddParams(override val state: RasterLayerState) :
    RasterLayerOverlayRendererInterface.AddParamsInterface

val addParams = listOf(AddParams(layerState))

// 3. Call onAdd to render the layer on the map
runBlocking {
    val handles = rasterRenderer.onAdd(addParams)
    if (handles.isNotEmpty() && handles[0] != null) {
        println("Successfully added raster layer. Handle: ${handles[0]}")
    } else {
        println("Failed to add raster layer.")
    }
}
```