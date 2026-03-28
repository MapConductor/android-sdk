# MapboxCircleController

### Signature

```kotlin
class MapboxCircleController(
    override val renderer: MapboxCircleOverlayRenderer,
    circleManager: CircleManagerInterface<MapboxActualCircle> = renderer.circleManager,
) : CircleController<MapboxActualCircle>(circleManager, renderer)
```

### Description

The `MapboxCircleController` is a specialized controller for managing and rendering circle overlays on a Mapbox map. It serves as a concrete implementation of the generic `CircleController`, bridging the core circle management logic with the Mapbox-specific rendering engine.

This controller coordinates the state of circle objects (`MapboxActualCircle`) with their visual representation on the map, which is handled by the `MapboxCircleOverlayRenderer`.

### Parameters

This documentation describes the parameters for the `MapboxCircleController` constructor.

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `renderer` | `MapboxCircleOverlayRenderer` | The Mapbox-specific renderer responsible for drawing the circles on the map. |
| `circleManager` | `CircleManagerInterface<MapboxActualCircle>` | **Optional**. The manager responsible for the underlying data and state of the circles. If not provided, it defaults to the `circleManager` instance from the supplied `renderer`. |

### Example

The following example demonstrates how to instantiate and use the `MapboxCircleController`.

```kotlin
// Assume you have a Mapbox MapView and a loaded Style object
// val mapView: MapView = ...
// val mapboxMap: MapboxMap = ...
// val style: Style = ...

// 1. Create an instance of the Mapbox-specific renderer
val circleRenderer = MapboxCircleOverlayRenderer(mapView, mapboxMap, style)

// 2. Instantiate the controller with the renderer.
// The circleManager will be implicitly taken from the renderer.
val mapboxCircleController = MapboxCircleController(renderer = circleRenderer)

// 3. Now you can use the controller to add, remove, or update circles.
// (This example assumes the controller has an `addCircle` method that accepts CircleOptions)
val circleOptions = CircleOptions()
    .withLatLng(LatLng(34.0522, -118.2437)) // Los Angeles
    .withRadius(1000f) // in meters
    .withFillColor(Color.BLUE)
    .withFillOpacity(0.5f)

val circle = mapboxCircleController.addCircle(circleOptions)

// You can later update the circle using the controller
circle.radius = 1500f
mapboxCircleController.updateCircle(circle)
```