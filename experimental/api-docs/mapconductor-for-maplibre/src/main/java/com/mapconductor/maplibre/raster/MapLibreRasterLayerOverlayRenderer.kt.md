Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# MapLibreRasterLayerOverlayRenderer

## `MapLibreRasterLayerOverlayRenderer`

### Signature

```kotlin
class MapLibreRasterLayerOverlayRenderer(
    private val holder: MapLibreMapViewHolderInterface,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : RasterLayerOverlayRendererInterface<MapLibreRasterLayerHandle>
```

### Description

The `MapLibreRasterLayerOverlayRenderer` is a concrete implementation of `RasterLayerOverlayRendererInterface` designed for the MapLibre map SDK. It manages the entire lifecycle of raster layers on the map, including adding, updating, and removing them.

This renderer translates abstract `RasterLayerState` objects into tangible MapLibre `RasterSource` and `RasterLayer` objects on the map. It handles various source types, such as URL templates, TileJSON, and ArcGIS services.

Key responsibilities include:
- Creating and managing MapLibre sources and layers for each raster overlay.
- Applying properties like opacity and visibility.
- Handling z-ordering for non-marker raster layers by re-adding them in the correct sequence based on their `zIndex`.
- Special handling for raster layers with an ID prefixed by `marker-tile-` to ensure they are rendered below map markers.

### Constructor

| Parameter   | Type                               | Description                                                                                             |
| :---------- | :--------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `holder`    | `MapLibreMapViewHolderInterface`   | An interface providing access to the MapLibre map instance and its style.                               |
| `coroutine` | `CoroutineScope`                   | The coroutine scope used for executing asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

---

## Methods

### `onAdd`

#### Signature

```kotlin
override suspend fun onAdd(
    data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>,
): List<MapLibreRasterLayerHandle?>
```

#### Description

Adds a list of new raster layers to the map. For each item in the `data` list, it creates a corresponding `RasterSource` and `RasterLayer`, adds them to the map's style, and applies the initial properties (e.g., opacity, visibility). After adding the layers, it rebuilds the z-order of all non-marker raster layers.

#### Parameters

| Parameter | Type                                                              | Description                                                              |
| :-------- | :---------------------------------------------------------------- | :----------------------------------------------------------------------- |
| `data`    | `List<RasterLayerOverlayRendererInterface.AddParamsInterface>`    | A list of parameters, each containing the `RasterLayerState` for a new layer to be added. |

#### Returns

A `List` of `MapLibreRasterLayerHandle?` objects. Each handle corresponds to a newly created layer. A `null` value indicates that the layer could not be added.

---

### `onChange`

#### Signature

```kotlin
override suspend fun onChange(
    data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<MapLibreRasterLayerHandle>>,
): List<MapLibreRasterLayerHandle?>
```

#### Description

Processes a list of changes to existing raster layers.

- If a layer's `source` has changed, the old layer and source are removed, and a new set is created.
- If only properties like `opacity` or `visible` have changed, the existing MapLibre `RasterLayer` is updated in place.

After processing all changes, it rebuilds the z-order of all non-marker raster layers.

#### Parameters

| Parameter | Type                                                                                     | Description                                                                                             |
| :-------- | :--------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `data`    | `List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<MapLibreRasterLayerHandle>>` | A list of change parameters, each containing the previous and current state of a layer. |

#### Returns

A `List` of `MapLibreRasterLayerHandle?` objects. Each handle corresponds to the layer after the update. A `null` value indicates that the layer could not be updated or re-created.

---

### `onRemove`

#### Signature

```kotlin
override suspend fun onRemove(data: List<RasterLayerEntityInterface<MapLibreRasterLayerHandle>>)
```

#### Description

Removes a list of raster layers from the map. For each layer entity provided, it removes both the `RasterLayer` and its associated `RasterSource` from the map's style. After removing the layers, it rebuilds the z-order of the remaining non-marker raster layers.

#### Parameters

| Parameter | Type                                                              | Description                                                              |
| :-------- | :---------------------------------------------------------------- | :----------------------------------------------------------------------- |
| `data`    | `List<RasterLayerEntityInterface<MapLibreRasterLayerHandle>>`     | A list of layer entities to be removed from the map.                     |

---

### `onPostProcess`

#### Signature

```kotlin
override suspend fun onPostProcess()
```

#### Description

A lifecycle method called after all add, change, and remove operations in a batch are complete. In this implementation, the method is empty and performs no action.

---

# `MapLibreRasterLayerHandle`

### Signature

```kotlin
data class MapLibreRasterLayerHandle(
    val sourceId: String,
    val layerId: String,
)
```

### Description

A data class that serves as a handle for a raster layer rendered on the map. It holds the unique identifiers for the MapLibre `RasterSource` and `RasterLayer` that constitute the overlay. This handle is used internally by the renderer to reference and manage specific layers on the map.

### Properties

| Property   | Type     | Description                                               |
| :--------- | :------- | :-------------------------------------------------------- |
| `sourceId` | `String` | The unique ID of the `RasterSource` in the MapLibre style. |
| `layerId`  | `String` | The unique ID of the `RasterLayer` in the MapLibre style.  |

### Example

```kotlin
// Example of a handle instance
val handle = MapLibreRasterLayerHandle(
    sourceId = "raster-source-osm-tiles",
    layerId = "raster-layer-osm-tiles"
)
```