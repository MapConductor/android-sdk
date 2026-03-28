Of course! Here is the high-quality SDK documentation for the provided `GoogleMapViewController` class.

---

# GoogleMapViewController

The `GoogleMapViewController` is the primary controller for managing and interacting with a `GoogleMap` instance. It serves as the main bridge between the `MapConductor` core logic and the Google Maps SDK for Android.

This class is responsible for:
- Managing the lifecycle and rendering of map overlays, including markers, polylines, polygons, circles, ground images, and raster layers.
- Handling user interactions such as map clicks and marker drags.
- Controlling camera movements, both immediate and animated.
- Listening to and broadcasting map state changes, like camera position and map load status.

## Class Signature

```kotlin
class GoogleMapViewController(
    override val holder: GoogleMapViewHolder,
    private val markerController: GoogleMapMarkerController,
    // ... other controllers
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    val backCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : BaseMapViewController(), GoogleMapViewControllerInterface, /* ... other listeners */
```

## Constructor

Initializes a new instance of the `GoogleMapViewController`. This typically occurs during the setup of your map view.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `holder` | `GoogleMapViewHolder` | The view holder containing the `GoogleMap` and `MapView` instances. |
| `markerController` | `GoogleMapMarkerController` | The controller responsible for managing markers. |
| `polylineController` | `GoogleMapPolylineController` | The controller responsible for managing polylines. |
| `polygonController` | `GoogleMapPolygonController` | The controller responsible for managing polygons. |
| `groundImageController` | `GoogleMapGroundImageController` | The controller responsible for managing ground images. |
| `circleController` | `GoogleMapCircleController` | The controller responsible for managing circles. |
| `rasterLayerController` | `GoogleMapRasterLayerController` | The controller responsible for managing raster tile layers. |
| `coroutine` | `CoroutineScope` | The coroutine scope for main thread operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |
| `backCoroutine` | `CoroutineScope` | The coroutine scope for background thread operations. Defaults to `CoroutineScope(Dispatchers.Default)`. |

## Properties

### mapLoadedState

A `StateFlow` that emits the loading status of the map. It emits `true` once the map has been fully loaded and is ready for interaction. You can collect this flow to perform actions after the map is ready.

**Signature**
```kotlin
val mapLoadedState: StateFlow<Boolean>
```

**Example**
```kotlin
coroutineScope.launch {
    mapViewController.mapLoadedState.collect { isLoaded ->
        if (isLoaded) {
            println("Map is now loaded and ready!")
            // Perform actions that require the map to be loaded
        }
    }
}
```

## Camera Methods

### moveCamera

Instantly moves the map camera to a specified position without animation.

**Signature**
```kotlin
override fun moveCamera(position: MapCameraPosition)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `MapCameraPosition` | The target camera position, including location, zoom, tilt, and bearing. |

### animateCamera

Animates the camera's movement from its current position to a new specified position over a given duration.

**Signature**
```kotlin
override fun animateCamera(position: MapCameraPosition, duration: Long)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `MapCameraPosition` | The target camera position to animate to. |
| `duration` | `Long` | The duration of the animation in milliseconds. |

## Overlay Management

### clearOverlays

Removes all overlays (markers, polylines, polygons, etc.) from the map.

**Signature**
```kotlin
override suspend fun clearOverlays()
```

### compositionMarkers

Adds a list of markers to the map. If a marker with the same ID already exists, it will be updated.

**Signature**
```kotlin
override suspend fun compositionMarkers(data: List<MarkerState>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<MarkerState>` | A list of `MarkerState` objects to be rendered on the map. |

### updateMarker

Updates a single existing marker on the map based on its state.

**Signature**
```kotlin
override suspend fun updateMarker(state: MarkerState)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `MarkerState` | The updated state for the marker. The marker is identified by `state.id`. |

### hasMarker

Checks if a marker with the given state's ID exists on the map.

**Signature**
```kotlin
override fun hasMarker(state: MarkerState): Boolean
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `MarkerState` | The state of the marker to check for. The check is based on `state.id`. |

**Returns**

`Boolean` - `true` if the marker exists, `false` otherwise.

---
*Note: The `composition`, `update`, and `has` methods for `Circles`, `Polygons`, `Polylines`, `GroundImages`, and `RasterLayers` follow the same pattern as the `Marker` methods shown above.*
---

## Event Listeners (Deprecated)

The following methods for setting global event listeners are deprecated. It is recommended to set event handlers directly on the individual `State` objects (e.g., `MarkerState.onClick`, `CircleState.onClick`).

### setOnMarkerClickListener

**[Deprecated]** Sets a global click listener for all markers.

**Signature**
```kotlin
@Deprecated("Use MarkerState.onClick instead.")
override fun setOnMarkerClickListener(listener: OnMarkerEventHandler?)
```

**Recommendation**: Assign a lambda to the `onClick` property of the `MarkerState` object when creating it.

**Example (Recommended)**
```kotlin
val marker = MarkerState(
    id = "marker1",
    position = GeoPoint(34.05, -118.24),
    onClick = { markerState ->
        println("Marker ${markerState.id} was clicked!")
    }
)
compositionMarkers(listOf(marker))
```

### setOnMarkerDragStart

**[Deprecated]** Sets a global listener for the start of a marker drag event.

**Signature**
```kotlin
@Deprecated("Use MarkerState.onDragStart instead.")
override fun setOnMarkerDragStart(listener: OnMarkerEventHandler?)
```

**Recommendation**: Use the `onDragStart` property on `MarkerState`.

### setOnMarkerDrag

**[Deprecated]** Sets a global listener for marker drag events.

**Signature**
```kotlin
@Deprecated("Use MarkerState.onDrag instead.")
override fun setOnMarkerDrag(listener: OnMarkerEventHandler?)
```

**Recommendation**: Use the `onDrag` property on `MarkerState`.

### setOnMarkerDragEnd

**[Deprecated]** Sets a global listener for the end of a marker drag event.

**Signature**
```kotlin
@Deprecated("Use MarkerState.onDragEnd instead.")
override fun setOnMarkerDragEnd(listener: OnMarkerEventHandler?)
```

**Recommendation**: Use the `onDragEnd` property on `MarkerState`.

### setOnCircleClickListener

**[Deprecated]** Sets a global click listener for all circles.

**Signature**
```kotlin
@Deprecated("Use CircleState.onClick instead.")
override fun setOnCircleClickListener(listener: OnCircleEventHandler?)
```

**Recommendation**: Use the `onClick` property on `CircleState`.

### setOnPolygonClickListener

**[Deprecated]** Sets a global click listener for all polygons.

**Signature**
```kotlin
@Deprecated("Use PolygonState.onClick instead.")
override fun setOnPolygonClickListener(listener: OnPolygonEventHandler?)
```

**Recommendation**: Use the `onClick` property on `PolygonState`.

### setOnPolylineClickListener

**[Deprecated]** Sets a global click listener for all polylines.

**Signature**
```kotlin
@Deprecated("Use PolylineState.onClick instead.")
override fun setOnPolylineClickListener(listener: OnPolylineEventHandler?)
```

**Recommendation**: Use the `onClick` property on `PolylineState`.

### setOnGroundImageClickListener

**[Deprecated]** Sets a global click listener for all ground images.

**Signature**
```kotlin
@Deprecated("Use GroundImageState.onClick instead.")
override fun setOnGroundImageClickListener(listener: OnGroundImageEventHandler?)
```

**Recommendation**: Use the `onClick` property on `GroundImageState`.

## Map Configuration

### setMapDesignType

Sets the visual design or type of the map (e.g., Normal, Satellite).

**Signature**
```kotlin
override fun setMapDesignType(value: GoogleMapDesignType)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `value` | `GoogleMapDesignType` | The desired map design, such as `GoogleMapDesign.Normal` or `GoogleMapDesign.Satellite`. |

### setMapDesignTypeChangeListener

Registers a listener that is invoked whenever the map's design type changes. The listener is also immediately called with the current map design type upon registration.

**Signature**
```kotlin
override fun setMapDesignTypeChangeListener(listener: GoogleMapDesignTypeChangeHandler)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `listener` | `GoogleMapDesignTypeChangeHandler` | A lambda or function that will be called with the new `GoogleMapDesignType`. |

## Advanced Marker Rendering

### createMarkerRenderer

Creates a renderer for markers that use a custom rendering strategy.

**Signature**
```kotlin
fun createMarkerRenderer(
    strategy: MarkerRenderingStrategyInterface<GoogleMapActualMarker>
): MarkerOverlayRendererInterface<GoogleMapActualMarker>
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `strategy` | `MarkerRenderingStrategyInterface<GoogleMapActualMarker>` | The custom strategy for rendering markers. |

**Returns**

`MarkerOverlayRendererInterface<GoogleMapActualMarker>` - A new marker renderer instance.

### createMarkerEventController

Creates an event controller for markers managed by a custom strategy.

**Signature**
```kotlin
fun createMarkerEventController(
    controller: StrategyMarkerController<GoogleMapActualMarker>,
    renderer: MarkerOverlayRendererInterface<GoogleMapActualMarker>
): MarkerEventControllerInterface<GoogleMapActualMarker>
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `controller` | `StrategyMarkerController<GoogleMapActualMarker>` | The strategy-based controller for the markers. |
| `renderer` | `MarkerOverlayRendererInterface<GoogleMapActualMarker>` | The renderer associated with the markers. |

**Returns**

`MarkerEventControllerInterface<GoogleMapActualMarker>` - A new marker event controller.

### registerMarkerEventController

Registers a custom marker event controller to handle marker interactions. This allows for extending or overriding the default marker event handling.

**Signature**
```kotlin
fun registerMarkerEventController(controller: MarkerEventControllerInterface<GoogleMapActualMarker>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `controller` | `MarkerEventControllerInterface<GoogleMapActualMarker>` | The custom event controller to register. |