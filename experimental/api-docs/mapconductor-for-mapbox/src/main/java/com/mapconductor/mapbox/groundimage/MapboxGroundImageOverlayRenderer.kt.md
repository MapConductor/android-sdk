Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# Class `MapboxGroundImageOverlayRenderer`

## Description

The `MapboxGroundImageOverlayRenderer` class is responsible for managing the lifecycle of ground image overlays on a Mapbox map. It handles the creation, updating, and removal of these overlays by interfacing with the Mapbox Maps SDK.

This renderer operates by creating a `RasterLayer` for each ground image. The image data is served as tiles through a `LocalTileServer`, which this class configures and manages. It efficiently handles updates by detecting changes and applying only the necessary modifications to the map style, such as replacing the tile source or simply adjusting layer opacity.

This class extends `AbstractGroundImageOverlayRenderer<MapboxActualGroundImage>`.

## Constructor

### Signature

```kotlin
class MapboxGroundImageOverlayRenderer(
    override val holder: MapboxMapViewHolder,
    private val tileServer: LocalTileServer,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
)
```

### Description

Creates a new instance of the `MapboxGroundImageOverlayRenderer`.

### Parameters

| Parameter  | Type                  | Description                                                                                             |
| :--------- | :-------------------- | :------------------------------------------------------------------------------------------------------ |
| `holder`   | `MapboxMapViewHolder` | The view holder that provides access to the Mapbox `Map` instance.                                      |
| `tileServer` | `LocalTileServer`     | The local server used to generate and serve image tiles for the ground overlay's raster source.         |
| `coroutine`  | `CoroutineScope`      | The coroutine scope for executing asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

## Methods

### `createGroundImage`

#### Signature

```kotlin
override suspend fun createGroundImage(state: GroundImageState): MapboxActualGroundImage?
```

#### Description

Asynchronously creates and displays a new ground image overlay on the map. This method sets up a `GroundImageTileProvider`, registers it with the `LocalTileServer`, and then adds a corresponding raster source and layer to the Mapbox map's style.

#### Parameters

| Parameter | Type             | Description                                                                                             |
| :-------- | :--------------- | :------------------------------------------------------------------------------------------------------ |
| `state`   | `GroundImageState` | An object containing the initial configuration for the ground image, including its ID, bounds, and image. |

#### Returns

A `MapboxActualGroundImage` handle for the newly created overlay, which contains identifiers for the map source and layer. Returns `null` if the creation fails.

---

### `updateGroundImageProperties`

#### Signature

```kotlin
override suspend fun updateGroundImageProperties(
    groundImage: MapboxActualGroundImage,
    current: GroundImageEntityInterface<MapboxActualGroundImage>,
    prev: GroundImageEntityInterface<MapboxActualGroundImage>,
): MapboxActualGroundImage?
```

#### Description

Asynchronously updates an existing ground image overlay based on changes between its previous and current states. The method performs an efficient diff to determine what changed:
- If the image content, bounds, or tile size have changed, the underlying tile provider and Mapbox source/layer are recreated.
- If only the opacity has changed, the `raster-opacity` property of the existing layer is updated, which is more performant.
- If no relevant properties have changed, no action is taken.

#### Parameters

| Parameter     | Type                                                  | Description                                                              |
| :------------ | :---------------------------------------------------- | :----------------------------------------------------------------------- |
| `groundImage` | `MapboxActualGroundImage`                             | The current handle for the ground image on the map.                      |
| `current`     | `GroundImageEntityInterface<MapboxActualGroundImage>` | The entity representing the new, updated state of the ground image.      |
| `prev`        | `GroundImageEntityInterface<MapboxActualGroundImage>` | The entity representing the previous state, used for comparison.         |

#### Returns

An updated `MapboxActualGroundImage` handle reflecting the changes. If no properties were changed, the original `groundImage` handle is returned.

---

### `removeGroundImage`

#### Signature

```kotlin
override suspend fun removeGroundImage(entity: GroundImageEntityInterface<MapboxActualGroundImage>)
```

#### Description

Asynchronously removes a ground image overlay from the map. This method cleans up all associated resources by removing the raster layer and source from the map style and unregistering the tile provider from the `LocalTileServer`.

#### Parameters

| Parameter | Type                                                  | Description                                                        |
| :-------- | :---------------------------------------------------- | :----------------------------------------------------------------- |
| `entity`  | `GroundImageEntityInterface<MapboxActualGroundImage>` | The entity containing the handle of the ground image to be removed. |

---

## Example

The following example demonstrates how to instantiate and use the `MapboxGroundImageOverlayRenderer` to manage a ground image on the map.

```kotlin
import com.mapbox.maps.MapboxMap
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.GroundImageEntity
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.core.tileserver.LocalTileServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import com.mapbox.geojson.BoundingBox

// Assume these are provided by your application's context
val mapboxMap: MapboxMap = // ... get your MapboxMap instance
val mapHolder = MapboxMapViewHolder(mapboxMap)
val tileServer = LocalTileServer()
val coroutineScope = CoroutineScope(Dispatchers.Main)

// 1. Initialize the renderer
val groundImageRenderer = MapboxGroundImageOverlayRenderer(
    holder = mapHolder,
    tileServer = tileServer,
    coroutine = coroutineScope
)

// 2. Define the state for a new ground image
val imageBitmap: Bitmap = // ... load your image bitmap
val imageBounds: BoundingBox = // ... define the geographic bounds
val initialState = GroundImageState(
    id = "unique-image-1",
    image = imageBitmap,
    bounds = imageBounds,
    opacity = 0.8f
)

// An entity to hold the state and the map-specific handle
var imageEntity: GroundImageEntity<MapboxActualGroundImage>? = null

coroutineScope.launch {
    // 3. Create the ground image on the map
    val groundImageHandle = groundImageRenderer.createGroundImage(initialState)
    if (groundImageHandle != null) {
        imageEntity = GroundImageEntity(initialState, groundImageHandle)
        println("Ground image created successfully.")
    }

    // ... later, you might want to update the image
    
    val currentEntity = imageEntity ?: return@launch
    val previousEntity = currentEntity.copy() // Keep a copy of the old state

    // 4. Define the new state (e.g., change opacity)
    val updatedState = currentEntity.state.copy(opacity = 0.5f)
    currentEntity.state = updatedState

    // 5. Update the ground image properties on the map
    val updatedHandle = groundImageRenderer.updateGroundImageProperties(
        groundImage = currentEntity.groundImage,
        current = currentEntity,
        prev = previousEntity
    )
    if (updatedHandle != null) {
        currentEntity.groundImage = updatedHandle
        println("Ground image updated successfully.")
    }

    // ... finally, when it's no longer needed

    // 6. Remove the ground image from the map
    groundImageRenderer.removeGroundImage(currentEntity)
    println("Ground image removed.")
}
```