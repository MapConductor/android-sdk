# GoogleMapGroundImageController

### Signature

```kotlin
class GoogleMapGroundImageController(
    groundImageManager: GroundImageManagerInterface<GoogleMapActualGroundImage> = GroundImageManager(),
    renderer: GoogleMapGroundImageOverlayRenderer,
) : GroundImageController<GoogleMapActualGroundImage>(groundImageManager, renderer)
```

### Description

The `GoogleMapGroundImageController` is a specialized controller responsible for managing and displaying ground image overlays on a Google Map instance. It acts as a bridge between the generic `GroundImageController` logic and the platform-specific rendering provided by `GoogleMapGroundImageOverlayRenderer`.

This controller handles the entire lifecycle of ground images—including adding, updating, and removing them from the map—by coordinating the `GroundImageManager` and the `renderer`.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `groundImageManager` | `GroundImageManagerInterface<GoogleMapActualGroundImage>` | (Optional) The manager responsible for handling the state and lifecycle of ground images. Defaults to a new `GroundImageManager()` instance. |
| `renderer` | `GoogleMapGroundImageOverlayRenderer` | The renderer responsible for drawing and managing the ground image overlays on the `GoogleMap` object. |

### Example

The following example demonstrates how to initialize the `GoogleMapGroundImageController` and use it to add a ground image overlay to a map.

```kotlin
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.mapconductor.core.groundimage.GroundImage // Assuming this core data class exists

// Assume you have a GoogleMap instance from the onMapReady callback
// val googleMap: GoogleMap = ...

// 1. Create a renderer for the Google Map
val groundImageRenderer = GoogleMapGroundImageOverlayRenderer(googleMap)

// 2. Instantiate the controller with the renderer
val groundImageController = GoogleMapGroundImageController(renderer = groundImageRenderer)

// 3. Define the properties for a new ground image
val imageBounds = LatLngBounds(
    LatLng(40.7122, -74.2265), // Southwest corner
    LatLng(40.7739, -74.1254)  // Northeast corner
)
val overlayImage = BitmapDescriptorFactory.fromResource(R.drawable.overlay_image)

// 4. Create a GroundImage object
val groundImage = GroundImage(
    id = "unique-overlay-id-1",
    image = overlayImage,
    bounds = imageBounds,
    transparency = 0.5f,
    zIndex = 5f
)

// 5. Add the ground image to the map via the controller
// The controller inherits the `add` method from GroundImageController
groundImageController.add(listOf(groundImage))

// To remove the ground image later by its ID
// groundImageController.remove(listOf("unique-overlay-id-1"))
```