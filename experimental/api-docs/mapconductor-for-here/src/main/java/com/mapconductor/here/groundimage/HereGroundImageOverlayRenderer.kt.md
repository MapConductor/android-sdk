Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# HereGroundImageOverlayRenderer

## Class: `HereGroundImageOverlayRenderer`

### Description

The `HereGroundImageOverlayRenderer` is a concrete implementation of `AbstractGroundImageOverlayRenderer` designed specifically for the HERE SDK. It manages the rendering and lifecycle of ground image overlays on a HERE map.

This class handles the creation, updating, and removal of ground images by interacting with a `LocalTileServer` to serve image tiles and with the HERE SDK's `MapLayer` and `RasterDataSource` components to display them. It is responsible for translating a platform-agnostic `GroundImageState` into a visible overlay on the map.

### Constructor

#### Signature

```kotlin
class HereGroundImageOverlayRenderer(
    override val holder: HereViewHolder,
    private val tileServer: LocalTileServer,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractGroundImageOverlayRenderer<HereActualGroundImage>()
```

#### Description

Initializes a new instance of the `HereGroundImageOverlayRenderer`.

#### Parameters

| Parameter   | Type              | Description                                                                                             |
| :---------- | :---------------- | :------------------------------------------------------------------------------------------------------ |
| `holder`    | `HereViewHolder`  | The view holder containing the HERE `MapView` and `MapContext` where the overlays will be rendered.       |
| `tileServer`| `LocalTileServer` | The local server instance responsible for generating and serving image tiles for the ground overlays.     |
| `coroutine` | `CoroutineScope`  | The coroutine scope used for managing background tasks, such as creating and updating map layers. Defaults to `CoroutineScope(Dispatchers.Default)`. |

---

## Methods

### createGroundImage

#### Signature

```kotlin
override suspend fun createGroundImage(state: GroundImageState): HereActualGroundImage?
```

#### Description

Creates and displays a new ground image overlay on the map based on the provided state. This method sets up a `GroundImageTileProvider`, registers it with the `LocalTileServer`, and then configures the necessary HERE SDK `RasterDataSource` and `MapLayer` to render the image.

#### Parameters

| Parameter | Type             | Description                                                                                             |
| :-------- | :--------------- | :------------------------------------------------------------------------------------------------------ |
| `state`   | `GroundImageState` | An object defining the properties of the ground image, such as the image bitmap, geographic bounds, and opacity. |

#### Returns

A `HereActualGroundImage` handle to the newly created map objects on success, or `null` if the layer could not be created.

---

### updateGroundImageProperties

#### Signature

```kotlin
override suspend fun updateGroundImageProperties(
    groundImage: HereActualGroundImage,
    current: GroundImageEntityInterface<HereActualGroundImage>,
    prev: GroundImageEntityInterface<HereActualGroundImage>,
): HereActualGroundImage?
```

#### Description

Updates an existing ground image overlay with new properties. The method efficiently determines if a visual refresh is needed by comparing the fingerprints of the current and previous states. If properties such as the image, bounds, opacity, or tile size have changed, it rebuilds the underlying map layer and data source to reflect the updates.

#### Parameters

| Parameter     | Type                                          | Description                                                              |
| :------------ | :-------------------------------------------- | :----------------------------------------------------------------------- |
| `groundImage` | `HereActualGroundImage`                       | The existing ground image handle to be updated.                          |
| `current`     | `GroundImageEntityInterface<HereActualGroundImage>` | The entity wrapper containing the new state for the ground image.        |
| `prev`        | `GroundImageEntityInterface<HereActualGroundImage>` | The entity wrapper containing the previous state for comparison purposes. |

#### Returns

The updated `HereActualGroundImage` handle. This may be a new handle if the underlying map objects were recreated, or the original handle if no visual update was necessary. Returns `null` if the update fails.

---

### removeGroundImage

#### Signature

```kotlin
override suspend fun removeGroundImage(entity: GroundImageEntityInterface<HereActualGroundImage>)
```

#### Description

Removes a ground image overlay from the map and cleans up all associated resources. This function destroys the HERE SDK `MapLayer` and `RasterDataSource` and unregisters the corresponding tile provider from the `LocalTileServer`.

#### Parameters

| Parameter | Type                                          | Description                                                              |
| :-------- | :-------------------------------------------- | :----------------------------------------------------------------------- |
| `entity`  | `GroundImageEntityInterface<HereActualGroundImage>` | The entity wrapper for the ground image to be removed from the map.      |

#### Returns

This is a suspending function and does not return a value.

---

### Example

The following example demonstrates how to initialize the `HereGroundImageOverlayRenderer` and use it to add, update, and remove a ground image overlay.

```kotlin
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoBox
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.GroundImageEntity
import com.mapconductor.here.groundimage.HereGroundImageOverlayRenderer
import com.mapconductor.core.tileserver.LocalTileServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Assume 'hereViewHolder' is an initialized HereViewHolder instance
// and 'myBitmap' is a valid Bitmap object.

// 1. Initialize the tile server and renderer
val tileServer = LocalTileServer()
val renderer = HereGroundImageOverlayRenderer(hereViewHolder, tileServer)

// 2. Define the initial state for the ground image
val initialBounds = GeoBox(
    GeoCoordinates(40.7128, -74.0060), // NYC South-West
    GeoCoordinates(40.7528, -73.9860)  // NYC North-East
)
val initialState = GroundImageState(
    id = "nyc-overlay",
    image = myBitmap,
    bounds = initialBounds,
    opacity = 0.8f,
    tileSize = 512
)

// 3. Create and add the ground image to the map
val groundImageEntity = GroundImageEntity(initialState)

CoroutineScope(Dispatchers.Main).launch {
    val groundImageHandle = renderer.createGroundImage(initialState)
    if (groundImageHandle != null) {
        groundImageEntity.groundImage = groundImageHandle
        println("Ground image created successfully.")

        // 4. (Optional) Update the ground image with new properties
        val updatedBounds = GeoBox(
            GeoCoordinates(40.7128, -74.0160), // Shift west
            GeoCoordinates(40.7528, -73.9960)
        )
        val updatedState = initialState.copy(bounds = updatedBounds, opacity = 1.0f)
        val updatedEntity = GroundImageEntity(updatedState)

        val updatedHandle = renderer.updateGroundImageProperties(
            groundImage = groundImageEntity.groundImage!!,
            current = updatedEntity,
            prev = groundImageEntity
        )
        
        if (updatedHandle != null) {
            updatedEntity.groundImage = updatedHandle
            println("Ground image updated successfully.")

            // 5. Remove the ground image from the map
            renderer.removeGroundImage(updatedEntity)
            println("Ground image removed.")
        }
    } else {
        println("Failed to create ground image.")
    }
}
```