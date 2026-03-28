Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

### `ArcGISGroundImageController`

#### Signature

```kotlin
class ArcGISGroundImageController(
    renderer: ArcGISGroundImageOverlayRenderer
) : GroundImageController<ArcGISGroundImageHandle>
```

#### Description

The `ArcGISGroundImageController` is the primary class for managing and displaying ground-level imagery on an ArcGIS map. It serves as the main entry point for developers to add, remove, and control ground images within an ArcGIS environment.

This controller acts as a bridge, connecting the generic `GroundImageManager` (which handles the core logic and state of all ground images) with the platform-specific `ArcGISGroundImageOverlayRenderer` (which handles the actual drawing of images on the map). It is specialized to work with `ArcGISGroundImageHandle` objects, which represent individual ground images added to the map.

#### Parameters

This section describes the parameters for the `ArcGISGroundImageController` constructor.

| Parameter  | Type                               | Description                                                                                             |
| :--------- | :--------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `renderer` | `ArcGISGroundImageOverlayRenderer` | The renderer instance responsible for drawing the ground image overlays onto the ArcGIS map. This object handles all platform-specific rendering tasks. |

#### Example

The following example demonstrates how to initialize the `ArcGISGroundImageController` and use it to add a ground image to an ArcGIS map.

```kotlin
import android.graphics.Bitmap
import com.mapconductor.arcgis.groundimage.ArcGISGroundImageController
import com.mapconductor.arcgis.groundimage.ArcGISGroundImageHandle
import com.mapconductor.arcgis.groundimage.ArcGISGroundImageOverlayRenderer
import com.mapconductor.core.groundimage.GroundImage
import com.mapconductor.core.types.LatLng

// 1. Assume you have an ArcGISMapView instance from your layout
val mapView: ArcGISMapView = findViewById(R.id.mapView)

// 2. Create the specific renderer for the ArcGIS map
val groundImageRenderer = ArcGISGroundImageOverlayRenderer(mapView)

// 3. Instantiate the controller with the renderer
val groundImageController = ArcGISGroundImageController(renderer = groundImageRenderer)

// 4. Define the properties for the ground image you want to display
// (This assumes you have a Bitmap loaded and a LatLng class for coordinates)
val imageBitmap: Bitmap = // ... load your image bitmap from assets or network
val imageLocation = LatLng(34.0522, -118.2437) // Example: Los Angeles, CA
val imageWidthInMeters = 75.0
val imageBearing = 45.0 // Orientation in degrees from North

val myGroundImage = GroundImage(
    bitmap = imageBitmap,
    position = imageLocation,
    width = imageWidthInMeters,
    bearing = imageBearing
)

// 5. Add the ground image to the map via the controller.
// The controller returns a handle that can be used to manage this specific image later.
val imageHandle: ArcGISGroundImageHandle = groundImageController.add(myGroundImage)

// You can now use the handle to update or remove the image
// For example, to remove the image from the map:
// groundImageController.remove(imageHandle)
```