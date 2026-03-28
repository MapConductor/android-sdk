Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# MapboxMapViewController

## Description

The `MapboxMapViewController` is the primary class for programmatic interaction with the map. It serves as a central controller for managing map overlays (like markers, polygons, and polylines), handling user gestures and map events, and controlling the map's camera. An instance of this class orchestrates various specialized controllers for different feature types, providing a unified API for map manipulation.

Since the constructor is `internal`, you will typically obtain an instance of this controller through a map view setup or a factory, rather than creating it directly.

---

## Methods

### clearOverlays

**Signature**
```kotlin
suspend fun clearOverlays()
```

**Description**
Asynchronously removes all overlays (markers, polylines, polygons, ground images, circles, and raster layers) from the map.

**Example**
```kotlin
coroutineScope.launch {
    mapViewController.clearOverlays()
}
```

---

### Overlay Composition

These methods efficiently add a list of new overlays to the map.

#### compositionMarkers

**Signature**
```kotlin
suspend fun compositionMarkers(data: List<MarkerState>)
```
**Description**
Adds a collection of markers to the map.

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `data` | `List<MarkerState>` | A list of `MarkerState` objects to be added to the map. |

#### Other Composition Methods
Similar methods are available for other overlay types:
- `suspend fun compositionGroundImages(data: List<GroundImageState>)`
- `suspend fun compositionPolylines(data: List<PolylineState>)`
- `suspend fun compositionPolygons(data: List<PolygonState>)`
- `suspend fun compositionCircles(data: List<CircleState>)`
- `suspend fun compositionRasterLayers(data: List<RasterLayerState>)`

---

### Overlay Updates

These methods update a single existing overlay on the map. The overlay is identified by the `id` within its `State` object.

#### updateMarker

**Signature**
```kotlin
suspend fun updateMarker(state: MarkerState)
```
**Description**
Updates an existing marker on the map based on its `id`. If a marker with the same `id` exists, its properties are updated.

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `state` | `MarkerState` | The `MarkerState` object containing the updated properties. |

#### Other Update Methods
Similar methods are available for other overlay types:
- `suspend fun updateGroundImage(state: GroundImageState)`
- `suspend fun updatePolyline(state: PolylineState)`
- `suspend fun updatePolygon(state: PolygonState)`
- `suspend fun updateCircle(state: CircleState)`
- `suspend fun updateRasterLayer(state: RasterLayerState)`

---

### Overlay Existence Checks

These methods check if a specific overlay exists on the map.

#### hasMarker

**Signature**
```kotlin
fun hasMarker(state: MarkerState): Boolean
```
**Description**
Checks if a marker with the same ID as the provided `MarkerState` exists on the map.

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `state` | `MarkerState` | The `MarkerState` object whose ID is used for the check. |

**Returns**
| Type | Description |
|---|---|
| `Boolean` | Returns `true` if the marker exists, `false` otherwise. |

#### Other Existence Check Methods
Similar methods are available for other overlay types:
- `fun hasPolyline(state: PolylineState): Boolean`
- `fun hasPolygon(state: PolygonState): Boolean`
- `fun hasCircle(state: CircleState): Boolean`
- `fun hasGroundImage(state: GroundImageState): Boolean`
- `fun hasRasterLayer(state: RasterLayerState): Boolean`

---

### Camera Control

#### moveCamera

**Signature**
```kotlin
fun moveCamera(position: MapCameraPosition)
```
**Description**
Instantly moves the map's camera to the specified position without animation.

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `position` | `MapCameraPosition` | The target camera position, including target, zoom, bearing, and tilt. |

**Example**
```kotlin
val newPosition = MapCameraPosition(
    target = GeoPoint(40.7128, -74.0060), // New York City
    zoom = 12.0
)
mapViewController.moveCamera(newPosition)
```

#### animateCamera

**Signature**
```kotlin
fun animateCamera(position: MapCameraPosition, duration: Long)
```
**Description**
Animates the map's camera from its current position to the specified position over a given duration. This uses a "fly-to" animation that provides a smooth, cinematic transition.

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `position` | `MapCameraPosition` | The target camera position. |
| `duration` | `Long` | The duration of the animation in milliseconds. |

**Example**
```kotlin
val targetPosition = MapCameraPosition(
    target = GeoPoint(34.0522, -118.2437), // Los Angeles
    zoom = 14.0,
    tilt = 30.0
)
mapViewController.animateCamera(targetPosition, duration = 2000L) // 2-second animation
```

---

### Map Design and Style

#### setMapDesignType

**Signature**
```kotlin
fun setMapDesignType(value: MapboxDesignType)
```
**Description**
Sets the map's style (e.g., Standard, Satellite, Streets). This operation is asynchronous and will cause the map style to reload, which may re-trigger style-loaded listeners.

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `value` | `MapboxDesignType` | The desired map design type to apply. |

#### setMapDesignTypeChangeListener

**Signature**
```kotlin
fun setMapDesignTypeChangeListener(listener: MapboxMapDesignTypeChangeHandler)
```
**Description**
Registers a listener that gets notified whenever the map's design type changes, for example, after a new style has been loaded.

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `listener` | `MapboxMapDesignTypeChangeHandler` | A callback function that receives the new `MapboxDesignType` after a style change. |

---

### Advanced Marker Customization

These methods are for advanced use cases, such as implementing a custom marker rendering or interaction strategy.

#### createMarkerRenderer

**Signature**
```kotlin
fun createMarkerRenderer(strategy: MarkerRenderingStrategyInterface<MapboxActualMarker>): MarkerOverlayRendererInterface<MapboxActualMarker>
```
**Description**
Creates a new marker overlay renderer based on a custom rendering strategy. This allows for complete control over how markers are drawn on the map.

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `strategy` | `MarkerRenderingStrategyInterface<MapboxActualMarker>` | The custom strategy defining marker management and rendering logic. |

**Returns**
| Type | Description |
|---|---|
| `MarkerOverlayRendererInterface<MapboxActualMarker>` | A new renderer instance configured with the provided strategy. |

#### createMarkerEventController

**Signature**
```kotlin
fun createMarkerEventController(controller: StrategyMarkerController<MapboxActualMarker>, renderer: MarkerOverlayRendererInterface<MapboxActualMarker>): MarkerEventControllerInterface<MapboxActualMarker>
```
**Description**
Creates a new marker event controller that links a strategy controller with a renderer. This is used to define custom marker interaction handling (clicks, drags, etc.).

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `controller` | `StrategyMarkerController<MapboxActualMarker>` | The strategy controller that manages the marker logic. |
| `renderer` | `MarkerOverlayRendererInterface<MapboxActualMarker>` | The renderer responsible for drawing the markers managed by the controller. |

**Returns**
| Type | Description |
|---|---|
| `MarkerEventControllerInterface<MapboxActualMarker>` | A new event controller instance. |

#### registerMarkerEventController

**Signature**
```kotlin
fun registerMarkerEventController(controller: MarkerEventControllerInterface<MapboxActualMarker>)
```
**Description**
Registers a custom marker event controller with the map view. Once registered, the map will delegate marker-related events to this controller, enabling custom interaction behaviors.

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `controller` | `MarkerEventControllerInterface<MapboxActualMarker>` | The custom event controller to register. |

---

### Deprecated Event Listeners

The following methods for setting global event listeners are deprecated. It is recommended to set event handlers directly on the state object for each individual overlay (e.g., `MarkerState.onClick`, `PolygonState.onClick`).

- `setOnCircleClickListener(listener: OnCircleEventHandler?)`
  - **Deprecated:** Use `CircleState.onClick` instead.
- `setOnGroundImageClickListener(listener: OnGroundImageEventHandler?)`
  - **Deprecated:** Use `GroundImageState.onClick` instead.
- `setOnMarkerDragStart(listener: OnMarkerEventHandler?)`
  - **Deprecated:** Use `MarkerState.onDragStart` instead.
- `setOnMarkerDrag(listener: OnMarkerEventHandler?)`
  - **Deprecated:** Use `MarkerState.onDrag` instead.
- `setOnMarkerDragEnd(listener: OnMarkerEventHandler?)`
  - **Deprecated:** Use `MarkerState.onDragEnd` instead.
- `setOnMarkerAnimateStart(listener: OnMarkerEventHandler?)`
  - **Deprecated:** Use `MarkerState.onAnimateStart` instead.
- `setOnMarkerAnimateEnd(listener: OnMarkerEventHandler?)`
  - **Deprecated:** Use `MarkerState.onAnimateEnd` instead.
- `setOnMarkerClickListener(listener: OnMarkerEventHandler?)`
  - **Deprecated:** Use `MarkerState.onClick` instead.
- `setOnPolylineClickListener(listener: OnPolylineEventHandler?)`
  - **Deprecated:** Use `PolylineState.onClick` instead.
- `setOnPolygonClickListener(listener: OnPolygonEventHandler?)`
  - **Deprecated:** Use `PolygonState.onClick` instead.