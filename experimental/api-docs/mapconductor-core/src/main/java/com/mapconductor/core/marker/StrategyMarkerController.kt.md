# SDK Documentation: StrategyMarkerController

## `StrategyMarkerController<ActualMarker>`

### Description

The `StrategyMarkerController` is a generic controller responsible for managing the lifecycle and rendering of map markers. It acts as an overlay controller, implementing the `OverlayControllerInterface`.

This class employs a strategy pattern through the `MarkerRenderingStrategyInterface`. This design allows for flexible and interchangeable rendering logic, such as simple marker rendering, clustering, or other custom behaviors, based on the current map viewport. The controller coordinates the `strategy` and a `renderer` to efficiently add, update, and display markers. It also manages and dispatches user interaction events like clicks, drags, and animations.

The generic type `<ActualMarker>` allows this controller to work with any platform-specific marker object (e.g., a marker from Google Maps, Mapbox, or another map provider).

### Signature

```kotlin
class StrategyMarkerController<ActualMarker>(
    private val strategy: MarkerRenderingStrategyInterface<ActualMarker>,
    private val renderer: MarkerOverlayRendererInterface<ActualMarker>,
    override var clickListener: OnMarkerEventHandler? = null,
) : OverlayControllerInterface<
        MarkerState,
        MarkerEntityInterface<ActualMarker>,
        MarkerState,
    >
```

### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `strategy` | `MarkerRenderingStrategyInterface<ActualMarker>` | The rendering strategy that defines how markers are processed and displayed based on the map's state (e.g., clustering, culling). |
| `renderer` | `MarkerOverlayRendererInterface<ActualMarker>` | The renderer responsible for the actual drawing and animation of marker objects on the map canvas. |
| `clickListener` | `OnMarkerEventHandler?` | An optional global event handler that is invoked whenever any marker managed by this controller is clicked. Defaults to `null`. |

---

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `markerManager` | `MarkerManager<ActualMarker>` | Provides access to the underlying manager that tracks all marker entities. |
| `zIndex` | `Int` | The z-index of the overlay, which determines its stacking order on the map. Hardcoded to `10`. |
| `clickListener` | `OnMarkerEventHandler?` | A global listener for marker click events. |
| `dragStartListener` | `OnMarkerEventHandler?` | A global listener invoked when a marker drag operation begins. |
| `dragListener` | `OnMarkerEventHandler?` | A global listener invoked continuously while a marker is being dragged. |
| `dragEndListener` | `OnMarkerEventHandler?` | A global listener invoked when a marker drag operation ends. |
| `animateStartListener` | `OnMarkerEventHandler?` | A global listener invoked when a marker animation begins. |
| `animateEndListener` | `OnMarkerEventHandler?` | A global listener invoked when a marker animation completes. |

---

## Methods

### add

Adds a list of markers to be managed by the controller. The rendering logic is delegated to the configured `strategy`, which typically evaluates which markers are visible within the current map viewport. If the map camera is not yet initialized, the markers are queued and added once the camera position becomes available.

#### Signature

```kotlin
override suspend fun add(data: List<MarkerState>)
```

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `data` | `List<MarkerState>` | A list of `MarkerState` objects representing the markers to be added. |

---

### update

Updates the state of a single existing marker. This method can be used to change a marker's properties, such as its position or icon. The update logic is delegated to the configured `strategy`.

#### Signature

```kotlin
override suspend fun update(state: MarkerState)
```

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `state` | `MarkerState` | The new `MarkerState` for the marker to be updated. The marker is identified by its `id`. |

---

### clear

Removes all markers managed by this controller from the map.

#### Signature

```kotlin
override suspend fun clear()
```

---

### getEntity

Retrieves the underlying marker entity (the wrapper around the platform-specific marker object) by its unique identifier.

#### Signature

```kotlin
fun getEntity(id: String): MarkerEntityInterface<ActualMarker>?
```

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `String` | The unique identifier of the marker. |

#### Returns

| Type | Description |
|------|-------------|
| `MarkerEntityInterface<ActualMarker>?` | The `MarkerEntityInterface` if a marker with the specified `id` is found; otherwise, `null`. |

---

### find

Finds the nearest marker entity to a given geographical point. This is useful for implementing features like "tap to select nearest marker".

#### Signature

```kotlin
override fun find(position: GeoPointInterface): MarkerEntityInterface<ActualMarker>?
```

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `position` | `GeoPointInterface` | The geographical coordinates to search near. |

#### Returns

| Type | Description |
|------|-------------|
| `MarkerEntityInterface<ActualMarker>?` | The nearest `MarkerEntityInterface` if one exists within the search parameters of the strategy; otherwise, `null`. |

---

### onCameraChanged

A lifecycle method that should be called by the map framework whenever the map's camera position changes (e.g., due to panning, zooming, or tilting). The controller uses the new camera position to update the visibility of markers via the configured `strategy`.

#### Signature

```kotlin
override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `mapCameraPosition` | `MapCameraPosition` | An object containing the new camera position, zoom level, and visible region. |

---

### destroy

Cleans up all resources used by the controller. This method calls `clear()` to remove all markers from the map. It should be called when the controller is no longer needed to prevent memory leaks.

#### Signature

```kotlin
override fun destroy()
```

---

### Event Dispatchers

These methods are typically called by the `MarkerOverlayRendererInterface` implementation to propagate platform-specific events back to the controller, which then notifies the appropriate listeners.

| Method | Description |
|--------|-------------|
| `dispatchClick(state: MarkerState)` | Dispatches a click event. Invokes the marker's `onClick` handler and the controller's `clickListener`. |
| `dispatchDragStart(state: MarkerState)` | Dispatches a drag start event. Invokes the marker's `onDragStart` handler and the controller's `dragStartListener`. |
| `dispatchDrag(state: MarkerState)` | Dispatches a drag event. Invokes the marker's `onDrag` handler and the controller's `dragListener`. |
| `dispatchDragEnd(state: MarkerState)` | Dispatches a drag end event. Invokes the marker's `onDragEnd` handler and the controller's `dragEndListener`. |
| `dispatchAnimateStart(state: MarkerState)` | Dispatches an animation start event. Invokes the marker's `onAnimateStart` handler and the controller's `animateStartListener`. |
| `dispatchAnimateEnd(state: MarkerState)` | Dispatches an animation end event. Invokes the marker's `onAnimateEnd` handler and the controller's `animateEndListener`. |

---

## Example

The following conceptual example demonstrates how to instantiate and use the `StrategyMarkerController`.

```kotlin
// 1. Define dependencies (strategy and renderer are platform-specific)
val myMarkerRenderingStrategy: MarkerRenderingStrategyInterface<MyMapMarker> = MyClusteringStrategy()
val myMarkerRenderer: MarkerOverlayRendererInterface<MyMapMarker> = MyMapMarkerRenderer(map)

// 2. Instantiate the controller with a global click listener
val markerController = StrategyMarkerController(
    strategy = myMarkerRenderingStrategy,
    renderer = myMarkerRenderer,
    clickListener = { markerState ->
        println("Global click on marker: ${markerState.id}")
    }
)

// 3. Add markers to the controller
val markersToAdd = listOf(
    MarkerState(id = "marker1", position = GeoPoint(40.7128, -74.0060)),
    MarkerState(id = "marker2", position = GeoPoint(34.0522, -118.2437))
)

// Use a coroutine scope to call suspend functions
coroutineScope.launch {
    markerController.add(markersToAdd)
}

// 4. Update a marker's state
val updatedState = MarkerState(
    id = "marker1",
    position = GeoPoint(40.7130, -74.0062), // New position
    icon = "new_icon_resource"
)

coroutineScope.launch {
    markerController.update(updatedState)
}

// 5. Find the closest marker to a point
val searchPoint = GeoPoint(34.0520, -118.2435)
val nearestMarkerEntity = markerController.find(searchPoint)
println("Nearest marker ID: ${nearestMarkerEntity?.state?.id}")

// 6. Clean up when the controller is no longer needed
// Typically called in a lifecycle method like onDestroy() or onDispose()
markerController.destroy()
```