# HereGroundImageController

## Signature

```kotlin
class HereGroundImageController(
    groundImageManager: GroundImageManagerInterface<HereActualGroundImage> = GroundImageManager(),
    renderer: HereGroundImageOverlayRenderer,
) : GroundImageController<HereActualGroundImage>(groundImageManager, renderer)
```

## Description

The `HereGroundImageController` is a specialized controller for managing and rendering ground image overlays on a HERE map. It serves as a crucial link between the abstract ground image management logic in `GroundImageController` and the concrete rendering implementation required by the HERE SDK for Android.

This class takes a `HereGroundImageOverlayRenderer` to handle the actual drawing of images on the map canvas. It orchestrates the entire lifecycle of ground images, including their creation, addition, removal, and updates, ensuring they are correctly displayed on the HERE map.

## Parameters

This section describes the parameters for the `HereGroundImageController` constructor.

| Parameter            | Type                                                  | Description                                                                                             | Default                |
| -------------------- | ----------------------------------------------------- | ------------------------------------------------------------------------------------------------------- | ---------------------- |
| `groundImageManager` | `GroundImageManagerInterface<HereActualGroundImage>`  | The manager responsible for handling the state, data, and lifecycle of all ground images.               | `GroundImageManager()` |
| `renderer`           | `HereGroundImageOverlayRenderer`                      | The platform-specific renderer that draws the ground image overlays onto the associated HERE `MapView`. | -                      |

## Example

The following example demonstrates how to initialize and use the `HereGroundImageController` to add a ground image to a HERE map.

```kotlin
import com.here.sdk.mapview.MapView
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.Anchor2D
import com.mapconductor.core.graphics.BitmapDescriptorFactory
import com.mapconductor.here.groundimage.HereActualGroundImage
import com.mapconductor.here.groundimage.HereGroundImageController
import com.mapconductor.here.groundimage.HereGroundImageOverlayRenderer

// Assume 'mapView' is an initialized instance of com.here.sdk.mapview.MapView
// and 'imageBitmap' is a valid android.graphics.Bitmap object.

// 1. Create a renderer instance associated with your HERE MapView.
val groundImageRenderer = HereGroundImageOverlayRenderer(mapView)

// 2. Instantiate the controller, passing the renderer.
val groundImageController = HereGroundImageController(renderer = groundImageRenderer)

// 3. Define the properties for the new ground image.
val imageDescriptor = BitmapDescriptorFactory.fromBitmap(imageBitmap)
val location = GeoCoordinates(52.530932, 13.384915) // Center coordinates for the image
val dimensions = Anchor2D(250.0, 125.0) // Width and height in meters

// 4. Create a HereActualGroundImage object.
val groundImage = HereActualGroundImage(
    id = "unique-image-id-1",
    image = imageDescriptor,
    coordinates = location,
    dimensions = dimensions,
    bearing = 45f // Rotation in degrees
)

// 5. Add the ground image to the map using the controller.
// The controller will delegate the rendering task to the HereGroundImageOverlayRenderer.
groundImageController.add(groundImage)

// To remove the image from the map later:
// groundImageController.remove("unique-image-id-1")
```