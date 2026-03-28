Of course! Here is the high-quality SDK documentation for the provided `HereMapViewController` code snippet.

---

# HereMapViewController

## Class: HereMapViewController

The `HereMapViewController` is a central controller for the HERE SDK map view. It manages the lifecycle and interactions of various map overlays such as markers, polylines, polygons, circles, and ground images. It also handles camera movements, user gestures (taps, long presses), and map styling. This class serves as the primary interface for developers to interact with the map.

### Signature

```kotlin
class HereMapViewController(
    private val markerController: HereMarkerController,
    private val polylineController: HerePolylineController,
    private val polygonController: HerePolygonController,
    private val groundImageController: HereGroundImageController,
    private val circleController: HereCircleController,
    private val rasterLayerController: HereRasterLayerController,
    override val holder: HereViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    val backCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(),
    CircleCapableInterface,
    HereMapViewControllerInterface,
    MapCameraListener,
    TapListener,
    LongPressListener
```

### Constructor

Initializes a new instance of the `HereMapViewController`.

#### Parameters

| Name                  | Type                      | Description                                                                    |
| --------------------- | ------------------------- | ------------------------------------------------------------------------------ |
| `markerController`    | `HereMarkerController`    | The controller responsible for managing marker overlays.                       |
| `polylineController`  | `HerePolylineController`  | The controller responsible for managing polyline overlays.                     |
| `polygonController`   | `HerePolygonController`   | The controller responsible for managing polygon overlays.                      |
| `groundImageController`| `HereGroundImageController`| The controller responsible for managing ground image overlays.                 |
| `circleController`    | `HereCircleController`    | The controller responsible for managing circle overlays.                       |
| `rasterLayerController`| `HereRasterLayerController`| The controller responsible for managing raster layer overlays.                 |
| `holder`              | `HereViewHolder`          | The view holder that contains the `MapView` instance.                          |
| `coroutine`           | `CoroutineScope`          | The coroutine scope for main-thread operations. Defaults to `Dispatchers.Main`.|
| `backCoroutine`       | `CoroutineScope`          | The coroutine scope for background operations. Defaults to `Dispatchers.Default`.|

---

## Overlay Management

### clearOverlays

Clears all overlays (markers, polylines, polygons, etc.) from the map.

#### Signature

```kotlin
override suspend fun clearOverlays()
```

---

### compositionMarkers

Adds a list of markers to the map based on the provided `MarkerState` data.

#### Signature

```kotlin
override suspend fun compositionMarkers(data: List<MarkerState>)
```

#### Parameters

| Name   | Type                | Description                               |
| ------ | ------------------- | ----------------------------------------- |
| `data` | `List<MarkerState>` | A list of `MarkerState` objects to render. |

---

### updateMarker

Updates an existing marker on the map with new state information.

#### Signature

```kotlin
override suspend fun updateMarker(state: MarkerState)
```

#### Parameters

| Name    | Type          | Description                                  |
| ------- | ------------- | -------------------------------------------- |
| `state` | `MarkerState` | The new state for the marker to be updated. |

---

### hasMarker

Checks if a specific marker exists on the map.

#### Signature

```kotlin
override fun hasMarker(state: MarkerState): Boolean
```

#### Parameters

| Name    | Type          | Description                               |
| ------- | ------------- | ----------------------------------------- |
| `state` | `MarkerState` | The state of the marker to check for. |

#### Returns

`Boolean` - `true` if the marker exists, `false` otherwise.

---

### compositionPolylines

Adds a list of polylines to the map.

#### Signature

```kotlin
override suspend fun compositionPolylines(data: List<PolylineState>)
```

#### Parameters

| Name   | Type                  | Description                                 |
| ------ | --------------------- | ------------------------------------------- |
| `data` | `List<PolylineState>` | A list of `PolylineState` objects to render. |

---

### updatePolyline

Updates an existing polyline on the map.

#### Signature

```kotlin
override suspend fun updatePolyline(state: PolylineState)
```

#### Parameters

| Name    | Type            | Description                                    |
| ------- | --------------- | ---------------------------------------------- |
| `state` | `PolylineState` | The new state for the polyline to be updated. |

---

### hasPolyline

Checks if a specific polyline exists on the map.

#### Signature

```kotlin
override fun hasPolyline(state: PolylineState): Boolean
```

#### Parameters

| Name    | Type            | Description                                 |
| ------- | --------------- | ------------------------------------------- |
| `state` | `PolylineState` | The state of the polyline to check for. |

#### Returns

`Boolean` - `true` if the polyline exists, `false` otherwise.

---

### compositionPolygons

Adds a list of polygons to the map.

#### Signature

```kotlin
override suspend fun compositionPolygons(data: List<PolygonState>)
```

#### Parameters

| Name   | Type                 | Description                                |
| ------ | -------------------- | ------------------------------------------ |
| `data` | `List<PolygonState>` | A list of `PolygonState` objects to render. |

---

### updatePolygon

Updates an existing polygon on the map.

#### Signature

```kotlin
override suspend fun updatePolygon(state: PolygonState)
```

#### Parameters

| Name    | Type           | Description                                   |
| ------- | -------------- | --------------------------------------------- |
| `state` | `PolygonState` | The new state for the polygon to be updated. |

---

### hasPolygon

Checks if a specific polygon exists on the map.

#### Signature

```kotlin
override fun hasPolygon(state: PolygonState): Boolean
```

#### Parameters

| Name    | Type           | Description                                |
| ------- | -------------- | ------------------------------------------ |
| `state` | `PolygonState` | The state of the polygon to check for. |

#### Returns

`Boolean` - `true` if the polygon exists, `false` otherwise.

---

### compositionCircles

Adds a list of circles to the map.

#### Signature

```kotlin
override suspend fun compositionCircles(data: List<CircleState>)
```

#### Parameters

| Name   | Type                | Description                               |
| ------ | ------------------- | ----------------------------------------- |
| `data` | `List<CircleState>` | A list of `CircleState` objects to render. |

---

### updateCircle

Updates an existing circle on the map.

#### Signature

```kotlin
override suspend fun updateCircle(state: CircleState)
```

#### Parameters

| Name    | Type          | Description                                  |
| ------- | ------------- | -------------------------------------------- |
| `state` | `CircleState` | The new state for the circle to be updated. |

---

### hasCircle

Checks if a specific circle exists on the map.

#### Signature

```kotlin
override fun hasCircle(state: CircleState): Boolean
```

#### Parameters

| Name    | Type          | Description                               |
| ------- | ------------- | ----------------------------------------- |
| `state` | `CircleState` | The state of the circle to check for. |

#### Returns

`Boolean` - `true` if the circle exists, `false` otherwise.

---

### compositionGroundImages

Adds a list of ground images to the map.

#### Signature

```kotlin
override suspend fun compositionGroundImages(data: List<GroundImageState>)
```

#### Parameters

| Name   | Type                     | Description                                      |
| ------ | ------------------------ | ------------------------------------------------ |
| `data` | `List<GroundImageState>` | A list of `GroundImageState` objects to render. |

---

### updateGroundImage

Updates an existing ground image on the map.

#### Signature

```kotlin
override suspend fun updateGroundImage(state: GroundImageState)
```

#### Parameters

| Name    | Type               | Description                                        |
| ------- | ------------------ | -------------------------------------------------- |
| `state` | `GroundImageState` | The new state for the ground image to be updated. |

---

### hasGroundImage

Checks if a specific ground image exists on the map.

#### Signature

```kotlin
override fun hasGroundImage(state: GroundImageState): Boolean
```

#### Parameters

| Name    | Type               | Description                                    |
| ------- | ------------------ | ---------------------------------------------- |
| `state` | `GroundImageState` | The state of the ground image to check for. |

#### Returns

`Boolean` - `true` if the ground image exists, `false` otherwise.

---

### compositionRasterLayers

Adds a list of raster layers to the map.

#### Signature

```kotlin
override suspend fun compositionRasterLayers(data: List<RasterLayerState>)
```

#### Parameters

| Name   | Type                     | Description                                      |
| ------ | ------------------------ | ------------------------------------------------ |
| `data` | `List<RasterLayerState>` | A list of `RasterLayerState` objects to render. |

---

### updateRasterLayer

Updates an existing raster layer on the map.

#### Signature

```kotlin
override suspend fun updateRasterLayer(state: RasterLayerState)
```

#### Parameters

| Name    | Type               | Description                                        |
| ------- | ------------------ | -------------------------------------------------- |
| `state` | `RasterLayerState` | The new state for the raster layer to be updated. |

---

### hasRasterLayer

Checks if a specific raster layer exists on the map.

#### Signature

```kotlin
override fun hasRasterLayer(state: RasterLayerState): Boolean
```

#### Parameters

| Name    | Type               | Description                                    |
| ------- | ------------------ | ---------------------------------------------- |
| `state` | `RasterLayerState` | The state of the raster layer to check for. |

#### Returns

`Boolean` - `true` if the raster layer exists, `false` otherwise.

---

## Camera Control

### moveCamera

Moves the map camera to a specified position instantly.

#### Signature

```kotlin
override fun moveCamera(position: MapCameraPosition)
```

#### Parameters

| Name       | Type                | Description                               |
| ---------- | ------------------- | ----------------------------------------- |
| `position` | `MapCameraPosition` | The target position for the map camera. |

#### Example

```kotlin
val newPosition = MapCameraPosition(
    position = GeoPoint(35.681236, 139.767125), // Tokyo Station
    zoom = 15.0,
    bearing = 0.0,
    tilt = 0.0
)
mapViewController.moveCamera(newPosition)
```

---

### animateCamera

Animates the map camera to a new position over a specified duration. This creates a "fly-to" effect.

#### Signature

```kotlin
override fun animateCamera(
    position: MapCameraPosition,
    duration: Long,
)
```

#### Parameters

| Name       | Type                | Description                                                              |
| ---------- | ------------------- | ------------------------------------------------------------------------ |
| `position` | `MapCameraPosition` | The target position for the map camera.                                  |
| `duration` | `Long`              | The duration of the animation in milliseconds.                           |

#### Example

```kotlin
val targetPosition = MapCameraPosition(
    position = GeoPoint(40.7128, -74.0060), // New York City
    zoom = 14.0,
    bearing = 45.0,
    tilt = 25.0
)
mapViewController.animateCamera(targetPosition, duration = 2000L) // 2-second animation
```

---

## Event Listener Configuration

### setOnMarkerClickListener

Sets a global listener for marker click events.

> **Deprecated:** This method is deprecated. Use the `onClick` lambda on the `MarkerState` object for individual marker event handling.

#### Signature

```kotlin
@Deprecated("Use MarkerState.onClick instead.")
override fun setOnMarkerClickListener(listener: OnMarkerEventHandler?)
```

#### Parameters

| Name       | Type                   | Description                               |
| ---------- | ---------------------- | ----------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The listener to be invoked on marker click. |

---

### setOnMarkerDragStart

Sets a global listener for marker drag start events.

> **Deprecated:** This method is deprecated. Use the `onDragStart` lambda on the `MarkerState` object.

#### Signature

```kotlin
@Deprecated("Use MarkerState.onDragStart instead.")
override fun setOnMarkerDragStart(listener: OnMarkerEventHandler?)
```

#### Parameters

| Name       | Type                   | Description                                     |
| ---------- | ---------------------- | ----------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The listener to be invoked when a marker drag starts. |

---

### setOnMarkerDrag

Sets a global listener for marker drag events.

> **Deprecated:** This method is deprecated. Use the `onDrag` lambda on the `MarkerState` object.

#### Signature

```kotlin
@Deprecated("Use MarkerState.onDrag instead.")
override fun setOnMarkerDrag(listener: OnMarkerEventHandler?)
```

#### Parameters

| Name       | Type                   | Description                                     |
| ---------- | ---------------------- | ----------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The listener to be invoked during a marker drag. |

---

### setOnMarkerDragEnd

Sets a global listener for marker drag end events.

> **Deprecated:** This method is deprecated. Use the `onDragEnd` lambda on the `MarkerState` object.

#### Signature

```kotlin
@Deprecated("Use MarkerState.onDragEnd instead.")
override fun setOnMarkerDragEnd(listener: OnMarkerEventHandler?)
```

#### Parameters

| Name       | Type                   | Description                                   |
| ---------- | ---------------------- | --------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The listener to be invoked when a marker drag ends. |

---

### setOnCircleClickListener

Sets a global listener for circle click events.

> **Deprecated:** This method is deprecated. Use the `onClick` lambda on the `CircleState` object.

#### Signature

```kotlin
@Deprecated("Use CircleState.onClick instead.")
override fun setOnCircleClickListener(listener: OnCircleEventHandler?)
```

#### Parameters

| Name       | Type                  | Description                               |
| ---------- | --------------------- | ----------------------------------------- |
| `listener` | `OnCircleEventHandler?` | The listener to be invoked on circle click. |

---

### setOnGroundImageClickListener

Sets a global listener for ground image click events.

> **Deprecated:** This method is deprecated. Use the `onClick` lambda on the `GroundImageState` object.

#### Signature

```kotlin
@Deprecated("Use GroundImageState.onClick instead.")
override fun setOnGroundImageClickListener(listener: OnGroundImageEventHandler?)
```

#### Parameters

| Name       | Type                       | Description                                   |
| ---------- | -------------------------- | --------------------------------------------- |
| `listener` | `OnGroundImageEventHandler?` | The listener to be invoked on ground image click. |

---

### setOnPolylineClickListener

Sets a global listener for polyline click events.

> **Deprecated:** This method is deprecated. Use the `onClick` lambda on the `PolylineState` object.

#### Signature

```kotlin
@Deprecated("Use PolylineState.onClick instead.")
override fun setOnPolylineClickListener(listener: OnPolylineEventHandler?)
```

#### Parameters

| Name       | Type                    | Description                                 |
| ---------- | ----------------------- | ------------------------------------------- |
| `listener` | `OnPolylineEventHandler?` | The listener to be invoked on polyline click. |

---

### setOnPolygonClickListener

Sets a global listener for polygon click events.

> **Deprecated:** This method is deprecated. Use the `onClick` lambda on the `PolygonState` object.

#### Signature

```kotlin
@Deprecated("Use PolygonState.onClick instead.")
override fun setOnPolygonClickListener(listener: OnPolygonEventHandler?)
```

#### Parameters

| Name       | Type                   | Description                                |
| ---------- | ---------------------- | ------------------------------------------ |
| `listener` | `OnPolygonEventHandler?` | The listener to be invoked on polygon click. |

---

## Map Styling

### setMapDesignType

Sets the visual style (theme) of the map, such as normal day, satellite, or night mode. This operation is asynchronous.

#### Signature

```kotlin
override fun setMapDesignType(value: HereMapDesignType)
```

#### Parameters

| Name    | Type                | Description                                                              |
| ------- | ------------------- | ------------------------------------------------------------------------ |
| `value` | `HereMapDesignType` | The desired map design type (e.g., `HereMapDesign.NormalDay`). |

#### Example

```kotlin
// Switch to the satellite map style
mapViewController.setMapDesignType(HereMapDesign.Satellite)
```

---

### setMapDesignTypeChangeListener

Registers a listener that is notified whenever the map's design type changes. The listener is immediately invoked with the current map design type upon registration.

#### Signature

```kotlin
override fun setMapDesignTypeChangeListener(listener: HereMapDesignTypeChangeHandler)
```

#### Parameters

| Name       | Type                           | Description                                                              |
| ---------- | ------------------------------ | ------------------------------------------------------------------------ |
| `listener` | `HereMapDesignTypeChangeHandler` | A handler that receives the new `HereMapDesignType` when the style changes. |

#### Example

```kotlin
mapViewController.setMapDesignTypeChangeListener { newDesignType ->
    println("Map design changed to: ${newDesignType::class.simpleName}")
}
```

---

## Utility and Factory Methods

### setupListeners

Sets up or re-applies the internal listeners for camera updates and gesture handling on the `MapView`. This is typically called during initialization.

#### Signature

```kotlin
fun setupListeners()
```

---

### createMarkerRenderer

Creates a renderer for markers that use a specific rendering strategy.

#### Signature

```kotlin
fun createMarkerRenderer(
    strategy: MarkerRenderingStrategyInterface<HereActualMarker>,
): MarkerOverlayRendererInterface<HereActualMarker>
```

#### Parameters

| Name       | Type                                                   | Description                               |
| ---------- | ------------------------------------------------------ | ----------------------------------------- |
| `strategy` | `MarkerRenderingStrategyInterface<HereActualMarker>` | The strategy to use for rendering markers. |

#### Returns

`MarkerOverlayRendererInterface<HereActualMarker>` - A new marker renderer instance.

---

### createMarkerEventController

Creates an event controller for markers that use a specific rendering strategy.

#### Signature

```kotlin
fun createMarkerEventController(
    controller: StrategyMarkerController<HereActualMarker>,
    renderer: MarkerOverlayRendererInterface<HereActualMarker>,
): MarkerEventControllerInterface<HereActualMarker>
```

#### Parameters

| Name         | Type                                             | Description                               |
| ------------ | ------------------------------------------------ | ----------------------------------------- |
| `controller` | `StrategyMarkerController<HereActualMarker>`   | The strategy controller for the markers.  |
| `renderer`   | `MarkerOverlayRendererInterface<HereActualMarker>` | The renderer associated with the markers. |

#### Returns

`MarkerEventControllerInterface<HereActualMarker>` - A new marker event controller instance.

---

### registerMarkerEventController

Registers a custom marker event controller to handle user interactions like clicks and drags for a specific set of markers.

#### Signature

```kotlin
fun registerMarkerEventController(controller: MarkerEventControllerInterface<HereActualMarker>)
```

#### Parameters

| Name         | Type                                           | Description                                                              |
| ------------ | ---------------------------------------------- | ------------------------------------------------------------------------ |
| `controller` | `MarkerEventControllerInterface<HereActualMarker>` | The event controller to register. It will be configured with any global listeners. |