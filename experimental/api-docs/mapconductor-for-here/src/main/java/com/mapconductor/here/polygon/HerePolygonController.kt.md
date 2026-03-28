# HerePolygonController

## Signature

```kotlin
class HerePolygonController(
    polygonManager: PolygonManagerInterface<HereActualPolygon> = PolygonManager(),
    renderer: HerePolygonOverlayRenderer,
) : PolygonController<HereActualPolygon>(polygonManager, renderer)
```

## Description

The `HerePolygonController` is the primary class for managing and displaying polygon overlays on a HERE map. It serves as a specialized implementation of the generic `PolygonController`, tailored specifically for the HERE Maps SDK environment.

This controller orchestrates the entire lifecycle of polygons, from data management to visual rendering. It connects the core `PolygonManager`, which handles the state of `HereActualPolygon` objects, with the `HerePolygonOverlayRenderer`, which is responsible for drawing the polygons onto the map canvas.

Use this controller to add, remove, update, and interact with polygons on your map.

## Parameters

This section describes the parameters for the `HerePolygonController` constructor.

| Parameter | Type | Description | Default |
| :--- | :--- | :--- | :--- |
| `polygonManager` | `PolygonManagerInterface<HereActualPolygon>` | The manager responsible for handling the state and lifecycle of polygon data. | `PolygonManager()` |
| `renderer` | `HerePolygonOverlayRenderer` | The HERE-specific renderer that draws the polygons on the map view. This parameter is required. | *none* |

## Example

The following example demonstrates how to initialize the `HerePolygonController`. You typically create it once per map instance.

```kotlin
// Assuming 'mapView' is an instance of a HERE MapView
// 1. Initialize the HERE-specific renderer for polygons.
val polygonRenderer = HerePolygonOverlayRenderer(mapView)

// 2. (Optional) Initialize a custom polygon manager if needed.
//    If not provided, a default PolygonManager will be used.
val polygonManager = PolygonManager<HereActualPolygon>()

// 3. Create an instance of the HerePolygonController.
val herePolygonController = HerePolygonController(
    polygonManager = polygonManager,
    renderer = polygonRenderer
)

// The controller is now ready to manage polygons.
// For example, to add a polygon (assuming an 'addPolygon' method exists
// on the parent PolygonController):
//
// val myPolygon = createHerePolygon() // Your polygon creation logic
// herePolygonController.addPolygon(myPolygon)
```