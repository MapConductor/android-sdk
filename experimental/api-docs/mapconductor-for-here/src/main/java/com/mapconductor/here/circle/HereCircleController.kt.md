# HereCircleController

### Signature

```kotlin
class HereCircleController(
    circleManager: CircleManager<HereActualCircle> = CircleManager(),
    renderer: HereCircleOverlayRenderer,
) : CircleController<HereActualCircle>(circleManager, renderer)
```

### Description

The `HereCircleController` is a specialized controller responsible for managing and rendering circle overlays on a HERE map. It serves as a concrete implementation of the abstract `CircleController`, bridging the generic circle management logic with the platform-specific rendering capabilities of the HERE Maps SDK.

This controller orchestrates the `CircleManager`, which handles the data and state of circle objects, and the `HereCircleOverlayRenderer`, which performs the actual drawing on the map canvas. By inheriting from `CircleController<HereActualCircle>`, it exposes a consistent API for adding, updating, and removing circles, while handling the underlying HERE-specific implementation details.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `circleManager` | `CircleManager<HereActualCircle>` | The manager responsible for handling the collection and state of all circle data models. It defaults to a new `CircleManager()` instance if not provided. |
| `renderer` | `HereCircleOverlayRenderer` | The renderer that draws the `HereActualCircle` objects onto the HERE map view. This parameter is required. |

### Example

The following example demonstrates how to initialize the `HereCircleController` and use it to add a circle to a HERE map.

```kotlin
import android.graphics.Color
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapview.MapView
import com.mapconductor.core.circle.Circle

// Assume 'mapView' is an initialized instance of a HERE MapView.
// val mapView: MapView = ...

// 1. Create the platform-specific renderer for HERE maps.
val circleRenderer = HereCircleOverlayRenderer(mapView)

// 2. Instantiate the controller with the renderer.
// The CircleManager is created with its default value.
val circleController = HereCircleController(renderer = circleRenderer)

// 3. Define the properties of the circle you want to draw.
val circleCenter = GeoCoordinates(52.5200, 13.4050) // Berlin, Germany
val myCircle = Circle(
    id = "berlin-circle-1",
    center = circleCenter,
    radiusInMeters = 1000.0,
    fillColor = Color.argb(120, 255, 165, 0), // Semi-transparent orange
    strokeWidthInPixels = 8f,
    strokeColor = Color.rgb(200, 80, 0) // Dark orange
)

// 4. Add the circle to the controller to render it on the map.
circleController.add(listOf(myCircle))

// To remove the circle later using its ID:
// circleController.remove(listOf("berlin-circle-1"))
```