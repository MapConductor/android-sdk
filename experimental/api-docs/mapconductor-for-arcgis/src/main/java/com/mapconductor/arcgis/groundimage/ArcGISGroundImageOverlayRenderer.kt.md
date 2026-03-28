# ArcGISGroundImageOverlayRenderer

Manages the lifecycle of ground image overlays on an ArcGIS map. This class is a concrete implementation of `AbstractGroundImageOverlayRenderer` tailored for the ArcGIS Maps SDK for Kotlin. It translates abstract `GroundImageState` objects into tangible `WebTiledLayer` instances on the map, handling their creation, updates, and removal by interacting with a `LocalTileServer`.

## Constructor

### Signature

```kotlin
class ArcGISGroundImageOverlayRenderer(
    override val holder: ArcGISMapViewHolder,
    private val tileServer: LocalTileServer,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractGroundImageOverlayRenderer<ArcGISGroundImageHandle>()
```

### Description

Initializes a new instance of the `ArcGISGroundImageOverlayRenderer`.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `holder` | `ArcGISMapViewHolder` | The view holder that manages the ArcGIS map instance where overlays will be rendered. |
| `tileServer` | `LocalTileServer` | The local tile server responsible for generating and serving image tiles for the ground overlays. |
| `coroutine` | `CoroutineScope` | The coroutine scope for executing asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Default)`. |

---

## Methods

### createGroundImage

#### Signature

```kotlin
override suspend fun createGroundImage(state: GroundImageState): ArcGISGroundImageHandle?
```

#### Description

Asynchronously creates and displays a new ground image overlay on the map based on the provided state.

This function generates tiles for the image, registers them with the `LocalTileServer`, creates an ArcGIS `WebTiledLayer`, and adds it to the map's operational layers. Opacity is handled by the native `WebTiledLayer.opacity` property for efficient updates, rather than being baked into the image tiles.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `GroundImageState` | An object defining the properties of the ground image, such as the image bitmap, geographical bounds, tile size, and opacity. |

#### Returns

| Type | Description |
| :--- | :--- |
| `ArcGISGroundImageHandle?` | A handle to the newly created ground image overlay, or `null` if creation fails (e.g., the map scene is not available or the layer fails to load). |

---

### updateGroundImageProperties

#### Signature

```kotlin
override suspend fun updateGroundImageProperties(
    groundImage: ArcGISGroundImageHandle,
    current: GroundImageEntityInterface<ArcGISGroundImageHandle>,
    prev: GroundImageEntityInterface<ArcGISGroundImageHandle>,
): ArcGISGroundImageHandle?
```

#### Description

Asynchronously updates an existing ground image overlay by comparing the `current` and `prev` states.

The method performs efficient updates:
- If only the opacity has changed, it updates the `opacity` property on the existing `WebTiledLayer` directly.
- If the image, bounds, or tile size have changed, it regenerates the underlying tile set, creates a new `WebTiledLayer` to ensure the cache is invalidated, and seamlessly swaps it with the old layer on the map.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `groundImage` | `ArcGISGroundImageHandle` | The handle for the existing ground image overlay to be updated. |
| `current` | `GroundImageEntityInterface<ArcGISGroundImageHandle>` | An entity wrapper for the new, desired state of the ground image. |
| `prev` | `GroundImageEntityInterface<ArcGISGroundImageHandle>` | An entity wrapper for the previous state, used to determine which properties have changed. |

#### Returns

| Type | Description |
| :--- | :--- |
| `ArcGISGroundImageHandle?` | A handle representing the updated overlay. This may be a new instance if the underlying tiles were regenerated. Returns `null` if the update fails. |

---

### removeGroundImage

#### Signature

```kotlin
override suspend fun removeGroundImage(entity: GroundImageEntityInterface<ArcGISGroundImageHandle>)
```

#### Description

Asynchronously removes a ground image overlay from the map. It removes the corresponding `WebTiledLayer` from the scene's operational layers and unregisters its tile provider from the `LocalTileServer` to free up resources.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `entity` | `GroundImageEntityInterface<ArcGISGroundImageHandle>` | The entity containing the handle of the ground image to remove. |

#### Returns

This function does not return a value.

---

## Example

The following example demonstrates the full lifecycle (create, update, remove) of a ground image using `ArcGISGroundImageOverlayRenderer`.

```kotlin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Assume these instances are already configured and available in your application
val mapViewHolder: ArcGISMapViewHolder = getMapViewHolder()
val localTileServer: LocalTileServer = getLocalTileServer()
val coroutineScope = CoroutineScope(Dispatchers.Main)

// A placeholder entity class for the example
data class GroundImageEntity<T>(
    override val state: GroundImageState,
    override var groundImage: T
) : GroundImageEntityInterface<T> {
    override val fingerPrint: GroundImageFingerprint
        get() = state.fingerPrint
}

suspend fun manageGroundImageLifecycle() {
    // 1. Initialize the renderer
    val groundImageRenderer = ArcGISGroundImageOverlayRenderer(
        holder = mapViewHolder,
        tileServer = localTileServer,
        coroutine = coroutineScope
    )

    // 2. Define the state for a new ground image
    val imageBitmap: Bitmap = loadBitmapFromAssets("campus_map.png")
    val imageBounds = Envelope(
        -122.4194, 37.7749, -122.4150, 37.7780,
        SpatialReference.wgs84()
    )
    val initialState = GroundImageState(
        id = "campus-overlay",
        image = imageBitmap,
        bounds = imageBounds,
        opacity = 0.9f,
        tileSize = 256
    )

    // 3. Create the ground image on the map
    var groundImageHandle = groundImageRenderer.createGroundImage(initialState)
    if (groundImageHandle != null) {
        println("Ground image created successfully.")
    } else {
        println("Failed to create ground image.")
        return
    }

    // --- Later, to update the image's opacity ---

    // 4. Define the updated state
    val updatedState = initialState.copy(opacity = 0.5f)

    // Create entities required by the update method
    val currentEntity = GroundImageEntity(state = updatedState, groundImage = groundImageHandle)
    val prevEntity = GroundImageEntity(state = initialState, groundImage = groundImageHandle)

    // 5. Update the ground image
    val updatedHandle = groundImageRenderer.updateGroundImageProperties(
        groundImage = groundImageHandle,
        current = currentEntity,
        prev = prevEntity
    )
    if (updatedHandle != null) {
        groundImageHandle = updatedHandle // Store the new handle
        println("Ground image updated successfully.")
    } else {
        println("Failed to update ground image.")
    }

    // --- Later, to remove the image ---

    // 6. Create an entity to identify the image to be removed
    val entityToRemove = GroundImageEntity(state = updatedState, groundImage = groundImageHandle)

    // 7. Remove the ground image
    groundImageRenderer.removeGroundImage(entityToRemove)
    println("Ground image removed.")
}

// Execute the function within a coroutine
coroutineScope.launch {
    manageGroundImageLifecycle()
}
```