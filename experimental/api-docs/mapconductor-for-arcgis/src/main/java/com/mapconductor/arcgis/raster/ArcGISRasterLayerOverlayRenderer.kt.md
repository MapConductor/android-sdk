# ArcGISRasterLayerOverlayRenderer

The `ArcGISRasterLayerOverlayRenderer` class is responsible for managing and rendering raster layer overlays on an ArcGIS map. It implements the `RasterLayerOverlayRendererInterface` and handles the lifecycle of raster layers, including adding, updating, and removing them from the map scene.

This renderer supports raster data from `ArcGisService` and `UrlTemplate` sources. It does not support `TileJson` sources.

## Constructor

### Signature

```kotlin
ArcGISRasterLayerOverlayRenderer(
    holder: ArcGISMapViewHolder,
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main)
)
```

### Description

Creates a new instance of the `ArcGISRasterLayerOverlayRenderer`. This renderer will add, update, and remove layers on the map provided by the `ArcGISMapViewHolder`.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `holder` | `ArcGISMapViewHolder` | The view holder that contains the ArcGIS map instance where layers will be rendered. |
| `coroutine` | `CoroutineScope` | The coroutine scope for running asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

## Methods

### onAdd

#### Signature

```kotlin
override suspend fun onAdd(
    data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>
): List<Layer?>
```

#### Description

Adds new raster layers to the map based on the provided state data. For each item in the input list, it creates a corresponding ArcGIS `Layer` and adds it to the map's operational layers.

Supported sources are `RasterLayerSource.ArcGisService` and `RasterLayerSource.UrlTemplate`. `RasterLayerSource.TileJson` is not supported and will result in a `null` entry in the returned list. The renderer waits for each layer to load successfully before adding it to the map to ensure its properties are fully initialized.

#### Parameters

| Parameter | Type | Description |
|---|---|---|
| `data` | `List<AddParamsInterface>` | A list of parameters, each containing the `RasterLayerState` for a new layer to be added. |

#### Returns

A `List<Layer?>` containing the newly created ArcGIS `Layer` instances, corresponding in order to the input data. An element will be `null` if the layer could not be created (e.g., unsupported source type or a loading error).

---

### onChange

#### Signature

```kotlin
override suspend fun onChange(
    data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<Layer>>
): List<Layer?>
```

#### Description

Processes changes to existing raster layers.

- If a layer's source (`RasterLayerState.source`) has changed, the old layer is removed, and a new one is created and added to the map to reflect the new source.
- If only other properties like `opacity` or `visible` have changed, the existing layer is updated in place for better performance.

#### Parameters

| Parameter | Type | Description |
|---|---|---|
| `data` | `List<ChangeParamsInterface<Layer>>` | A list of change parameters, each containing the previous and current state of a layer. |

#### Returns

A `List<Layer?>` containing the updated or newly created ArcGIS `Layer` instances corresponding to the input data.

---

### onRemove

#### Signature

```kotlin
override suspend fun onRemove(data: List<RasterLayerEntityInterface<Layer>>)
```

#### Description

Removes the specified raster layers from the map's operational layers.

#### Parameters

| Parameter | Type | Description |
|---|---|---|
| `data` | `List<RasterLayerEntityInterface<Layer>>` | A list of layer entities to be removed from the map. |

#### Returns

This method does not return a value.

---

### onPostProcess

#### Signature

```kotlin
override suspend fun onPostProcess()
```

#### Description

A lifecycle method called after all add, change, and remove operations in a batch are processed. In this implementation, this method is empty and performs no action.

#### Parameters

This method has no parameters.

#### Returns

This method does not return a value.

## Example

The following example demonstrates how to initialize the `ArcGISRasterLayerOverlayRenderer` and use it to add a new raster layer from a URL template (OpenStreetMap) to the map.

```kotlin
import com.mapconductor.arcgis.raster.ArcGISRasterLayerOverlayRenderer
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.raster.RasterLayerOverlayRendererInterface
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

// Assume the following setup exists:
// - An `ArcGISMapViewHolder` instance named `mapViewHolder` is available.
// - A `CoroutineScope` instance named `coroutineScope` is available (e.g., viewModelScope).

// Define a simple implementation of AddParamsInterface for the example
data class AddParams(
    override val state: RasterLayerState
) : RasterLayerOverlayRendererInterface.AddParamsInterface

// 1. Initialize the renderer with the map holder
val renderer = ArcGISRasterLayerOverlayRenderer(mapViewHolder)

// 2. Define the state for a new raster layer from a URL template
val openStreetMapState = RasterLayerState(
    id = "osm-layer",
    source = RasterLayerSource.UrlTemplate(
        template = "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
        tileSize = 256,
        scheme = TileScheme.XYZ
    ),
    opacity = 0.8f,
    visible = true
)

// 3. Create the parameters for the onAdd call
val addData = listOf(
    AddParams(state = openStreetMapState)
)

// 4. Add the layer to the map asynchronously
coroutineScope.launch {
    val addedLayers = renderer.onAdd(addData)
    if (addedLayers.isNotEmpty() && addedLayers[0] != null) {
        println("Successfully added OpenStreetMap layer to the map.")
    } else {
        println("Failed to add OpenStreetMap layer.")
    }
}
```