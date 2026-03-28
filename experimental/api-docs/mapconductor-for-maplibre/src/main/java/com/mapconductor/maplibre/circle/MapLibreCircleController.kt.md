Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

### `MapLibreCircleController`

A controller class that manages the lifecycle and rendering of circle overlays on a MapLibre map.

#### Signature

```kotlin
class MapLibreCircleController(
    override val renderer: MapLibreCircleOverlayRenderer,
    circleManager: CircleManagerInterface<MapLibreActualCircle> = CircleManager(),
) : CircleController<MapLibreActualCircle>(circleManager, renderer)
```

#### Description

The `MapLibreCircleController` is the primary entry point for developers to manage and display circles on a MapLibre map. It orchestrates the interaction between the generic circle management logic (`CircleManager`) and the platform-specific rendering implementation (`MapLibreCircleOverlayRenderer`).

This controller simplifies the process of adding, removing, and updating circles by providing a high-level API. It delegates the state management of circle objects to the `circleManager` and the actual drawing on the map canvas to the `renderer`.

#### Parameters

| Parameter | Type | Description |
|---|---|---|
| `renderer` | `MapLibreCircleOverlayRenderer` | **Required.** The renderer instance responsible for drawing the circles onto the MapLibre map. |
| `circleManager` | `CircleManagerInterface<MapLibreActualCircle>` | *Optional.* The manager that handles the collection of circles, including their properties and state. If not provided, a default `CircleManager()` instance is created. |

#### Example

The following example demonstrates how to initialize the `MapLibreCircleController` and use it to add and manage a circle on the map.

```kotlin
// Assuming you have a MapLibre 'map' and 'style' object available.

// 1. Initialize the renderer required by the controller.
val circleRenderer = MapLibreCircleOverlayRenderer(map, style)

// 2. Create an instance of the MapLibreCircleController.
val circleController = MapLibreCircleController(renderer = circleRenderer)

// 3. Define the properties for a new circle using CircleOptions.
val circleOptions = CircleOptions(
    center = LatLng(40.7128, -74.0060), // New York City
    radius = 800.0, // Radius in meters
    fillColor = Color.argb(100, 33, 150, 243), // Semi-transparent blue
    strokeColor = Color.BLUE,
    strokeWidth = 3.0f
)

// 4. Add the circle to the map using the controller.
val myCircle = circleController.add(circleOptions)

// You can later update the circle's properties.
myCircle.radius = 1200.0
myCircle.fillColor = Color.argb(100, 255, 87, 34) // Change to orange

// To remove the circle from the map:
// circleController.remove(myCircle)
```