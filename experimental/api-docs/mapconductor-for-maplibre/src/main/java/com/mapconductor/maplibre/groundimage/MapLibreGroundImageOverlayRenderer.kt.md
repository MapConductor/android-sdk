# MapLibreGroundImageOverlayRenderer

The `MapLibreGroundImageOverlayRenderer` class is responsible for rendering and managing ground image overlays on a MapLibre map. It implements the `AbstractGroundImageOverlayRenderer` and handles the complete lifecycle of a ground image, including its creation, updates to its properties, and its removal.

This renderer operates by generating image tiles from a source image and its geographical bounds. It uses a `LocalTileServer` to serve these tiles to the MapLibre map, which then displays them as a `RasterLayer`.

## Signature

```kotlin
class MapLibreGroundImageOverlayRenderer(
    override val holder: MapLibreMapViewHolderInterface,
    private val tileServer: LocalTileServer,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractGroundImageOverlayRenderer<MapLibreActualGroundImage>()
```

## Constructor

Initializes a new instance of `MapLibreGroundImageOverlayRenderer`.

### Parameters

| Parameter   | Type                           | Description                                                                                             |
| :---------- | :----------------------------- | :------------------------------------------------------------------------------------------------------ |
| `holder`    | `MapLibreMapViewHolderInterface` | The view holder that provides access to the MapLibre map instance and its style.                        |
| `tileServer`| `LocalTileServer`              | A local server instance used to serve the generated image tiles to the map.                             |
| `coroutine` | `CoroutineScope`               | The coroutine scope for running asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

---

## Methods

### createGroundImage

Asynchronously creates a new ground image overlay on the map based on the provided state. It sets up a tile provider, registers it with the `LocalTileServer`, and adds the necessary `RasterSource` and `RasterLayer` to the map style.

#### Signature

```kotlin
override suspend fun createGroundImage(state: GroundImageState): MapLibreActualGroundImage?
```

#### Parameters

| Parameter | Type             | Description                                                                                             |
| :-------- | :--------------- | :------------------------------------------------------------------------------------------------------ |
| `state`   | `GroundImageState` | An object containing the configuration for the ground image, such as the image, geographical bounds, and tile size. |

#### Returns

| Type                        | Description                                                                                                                            |
| :-------------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `MapLibreActualGroundImage?`| A handle to the created ground image (`MapLibreActualGroundImage`) if successful, or `null` if the map style is not available or creation fails. This handle is used for subsequent updates and removal. |

---

### updateGroundImageProperties

Asynchronously updates an existing ground image overlay. It efficiently checks for changes in properties like opacity, bounds, or the image itself. If the tile content needs to be regenerated (e.g., bounds or image changed), it creates a new source and layer. If only opacity changes, it updates the existing layer's properties.

#### Signature

```kotlin
override suspend fun updateGroundImageProperties(
    groundImage: MapLibreActualGroundImage,
    current: GroundImageEntityInterface<MapLibreActualGroundImage>,
    prev: GroundImageEntityInterface<MapLibreActualGroundImage>,
): MapLibreActualGroundImage?
```

#### Parameters

| Parameter     | Type                                                  | Description                                                                    |
| :------------ | :---------------------------------------------------- | :----------------------------------------------------------------------------- |
| `groundImage` | `MapLibreActualGroundImage`                           | The handle of the ground image to update, returned from `createGroundImage`.   |
| `current`     | `GroundImageEntityInterface<MapLibreActualGroundImage>` | The entity representing the new state of the ground image.                     |
| `prev`        | `GroundImageEntityInterface<MapLibreActualGroundImage>` | The entity representing the previous state of the ground image, used for diffing. |

#### Returns

| Type                        | Description                                                                                                                                                                                                                         |
| :-------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `MapLibreActualGroundImage?`| A new handle to the updated ground image if changes required recreating the source/layer. Returns the original handle if only properties were updated, or `null` on failure (e.g., map style not available). |

---

### removeGroundImage

Asynchronously removes a ground image overlay from the map. It removes the associated `RasterLayer` and `RasterSource` from the map style and unregisters the tile provider from the `LocalTileServer`.

#### Signature

```kotlin
override suspend fun removeGroundImage(entity: GroundImageEntityInterface<MapLibreActualGroundImage>)
```

#### Parameters

| Parameter | Type                                                  | Description                                                              |
| :-------- | :---------------------------------------------------- | :----------------------------------------------------------------------- |
| `entity`  | `GroundImageEntityInterface<MapLibreActualGroundImage>` | The entity whose associated ground image overlay should be removed from the map. |

#### Returns

This function does not return a value.

---

## Example

The following example demonstrates the basic lifecycle of managing a ground image overlay using `MapLibreGroundImageOverlayRenderer`.

```kotlin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Assume these are initialized elsewhere in your application
val mapViewHolder: MapLibreMapViewHolderInterface = getMapViewHolder()
val localTileServer: LocalTileServer = getLocalTileServer()
val coroutineScope = CoroutineScope(Dispatchers.Main)

// 1. Initialize the renderer
val groundImageRenderer = MapLibreGroundImageOverlayRenderer(
    holder = mapViewHolder,
    tileServer = localTileServer,
    coroutine = coroutineScope
)

// 2. Define the initial state for the ground image
val initialImage = getYourBitmap() // Your image source
val initialBounds = getYourLatLngBounds() // The geographic area for the image
val initialState = GroundImageState(
    id = "historic-map-1890",
    image = initialImage,
    bounds = initialBounds,
    opacity = 0.75f,
    tileSize = 512
)

// A placeholder for the entity that holds the ground image state and handle
var groundImageEntity: GroundImageEntityInterface<MapLibreActualGroundImage>? = null

runBlocking {
    // 3. Create the ground image on the map
    val groundImageHandle = groundImageRenderer.createGroundImage(initialState)
    if (groundImageHandle != null) {
        // Store the handle and state in an entity for future reference
        groundImageEntity = createEntity(initialState, groundImageHandle)
        println("Ground image created successfully.")
    } else {
        println("Failed to create ground image.")
        return@runBlocking
    }

    // 4. Update the ground image (e.g., change opacity)
    val updatedState = initialState.copy(opacity = 1.0f)
    val previousEntity = groundImageEntity!!
    val updatedEntity = createEntity(updatedState, previousEntity.groundImage)

    val updatedHandle = groundImageRenderer.updateGroundImageProperties(
        groundImage = previousEntity.groundImage,
        current = updatedEntity,
        prev = previousEntity
    )

    if (updatedHandle != null) {
        // Update the entity with the new handle if it changed
        groundImageEntity = updatedEntity.copy(groundImage = updatedHandle)
        println("Ground image updated successfully.")
    } else {
        println("Failed to update ground image.")
    }

    // 5. Remove the ground image from the map
    groundImageRenderer.removeGroundImage(groundImageEntity!!)
    println("Ground image removed.")
}
```