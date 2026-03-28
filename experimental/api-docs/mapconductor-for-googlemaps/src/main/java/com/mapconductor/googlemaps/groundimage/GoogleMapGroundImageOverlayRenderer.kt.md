# GoogleMapGroundImageOverlayRenderer

The `GoogleMapGroundImageOverlayRenderer` is a concrete implementation of `AbstractGroundImageOverlayRenderer` designed for the Google Maps SDK. It manages the lifecycle of ground image overlays on a `GoogleMap` instance, including their creation, removal, and property updates. This class translates abstract `GroundImageState` objects into tangible `GroundOverlay` objects on the map.

`GoogleMapActualGroundImage` is a type alias for the Google Maps SDK's `GroundOverlay`.

## Signature

```kotlin
class GoogleMapGroundImageOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractGroundImageOverlayRenderer<GoogleMapActualGroundImage>()
```

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `holder` | `GoogleMapViewHolder` | The view holder containing the `GoogleMap` instance where overlays will be rendered. |
| `coroutine` | `CoroutineScope` | The coroutine scope for executing asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

---

## Methods

### createGroundImage

Asynchronously creates and adds a new ground overlay to the map based on the provided state. It configures the overlay's image, bounds, and opacity.

#### Signature

```kotlin
override suspend fun createGroundImage(state: GroundImageState): GoogleMapActualGroundImage?
```

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `state` | `GroundImageState` | An object containing the desired properties for the new ground overlay, such as the image, geographic bounds, and opacity. |

#### Returns

| Type | Description |
|------|-------------|
| `GoogleMapActualGroundImage?` | The newly created `GroundOverlay` instance if successful, or `null` if the creation fails (e.g., due to invalid bounds). |

---

### removeGroundImage

Asynchronously removes a specified ground overlay from the map.

#### Signature

```kotlin
override suspend fun removeGroundImage(entity: GroundImageEntityInterface<GoogleMapActualGroundImage>)
```

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `entity` | `GroundImageEntityInterface<GoogleMapActualGroundImage>` | The entity wrapper containing the `GroundOverlay` instance to be removed. |

#### Returns

This method does not return a value.

---

### updateGroundImageProperties

Asynchronously updates the properties of an existing ground overlay. It efficiently compares the previous and current states and applies only the changed properties (bounds, image, or opacity) to the `GroundOverlay` on the map.

#### Signature

```kotlin
override suspend fun updateGroundImageProperties(
    groundImage: GoogleMapActualGroundImage,
    current: GroundImageEntityInterface<GoogleMapActualGroundImage>,
    prev: GroundImageEntityInterface<GoogleMapActualGroundImage>,
): GoogleMapActualGroundImage?
```

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `groundImage` | `GoogleMapActualGroundImage` | The actual `GroundOverlay` object on the map to be updated. |
| `current` | `GroundImageEntityInterface<GoogleMapActualGroundImage>` | The entity representing the new, desired state of the ground overlay. |
| `prev` | `GroundImageEntityInterface<GoogleMapActualGroundImage>` | The entity representing the previous state of the ground overlay, used for comparison. |

#### Returns

| Type | Description |
|------|-------------|
| `GoogleMapActualGroundImage?` | The updated `GroundOverlay` instance. |

---

## Example

Here is an example of how to instantiate the renderer and use it to create a ground overlay.

```kotlin
import com.google.android.gms.maps.GoogleMap
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.math.LatLonBounds
import com.mapconductor.googlemaps.GoogleMapViewHolder
import kotlinx.coroutines.runBlocking

// Assume you have a GoogleMap instance and a Drawable resource
// val googleMap: GoogleMap = ...
// val imageDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.newark_nj_1922)!!

// 1. Set up the GoogleMapViewHolder
val mapViewHolder = GoogleMapViewHolder(googleMap)

// 2. Instantiate the renderer
val groundImageRenderer = GoogleMapGroundImageOverlayRenderer(mapViewHolder)

// 3. Define the state for the ground overlay
val imageState = GroundImageState(
    id = "newark-1922-map",
    image = imageDrawable,
    bounds = LatLonBounds(
        north = 40.785,
        south = 40.685,
        east = -74.118,
        west = -74.258
    ),
    opacity = 0.75f
)

// 4. Use the renderer to create the ground overlay on the map
runBlocking {
    val groundOverlay = groundImageRenderer.createGroundImage(imageState)
    if (groundOverlay != null) {
        println("Ground overlay created with tag: ${groundOverlay.tag}")
    } else {
        println("Failed to create ground overlay.")
    }
}
```