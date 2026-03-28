Excellent. Here is the high-quality SDK documentation for the provided Kotlin code snippet.

***

# AbstractMarkerController&lt;ActualMarker&gt;

## Description

An abstract base class for managing and rendering a collection of markers on a map. It handles the complete lifecycle of markers, including adding, updating, and removing them in an efficient, state-driven manner.

This controller is designed to be platform-agnostic. It must be subclassed to implement provider-specific rendering logic for a particular map SDK (e.g., Google Maps, Mapbox).

**Key Features:**

*   **State-Driven Rendering**: The UI is a direct function of the provided `MarkerState` list. The controller intelligently diffs the new state against the current state to determine what to add, update, or remove.
*   **Efficient Batch Processing**: To maintain UI responsiveness, especially with a large number of markers, operations are batched and executed with `yield()` calls to avoid blocking the main thread.
*   **Thread Safety**: Employs a `Semaphore` to ensure that concurrent modifications to the marker collection are handled safely and sequentially.
*   **Comprehensive Event Handling**: Provides a robust system for handling user interactions like clicks and drags, with both global and per-marker listeners.

## Generic Parameters

| Parameter | Description |
| :--- | :--- |
| `<ActualMarker>` | The platform-specific native marker object type (e.g., `com.google.android.gms.maps.model.Marker`). |

## Signature

```kotlin
abstract class AbstractMarkerController<ActualMarker>(
    val markerManager: MarkerManager<ActualMarker>,
    renderer: MarkerOverlayRendererInterface<ActualMarker>,
    override var clickListener: OnMarkerEventHandler? = null,
) : OverlayControllerInterface<
        MarkerState,
        MarkerEntityInterface<ActualMarker>,
        MarkerState,
    >
```

### Constructor

Creates a new instance of the marker controller.

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `markerManager` | `MarkerManager<ActualMarker>` | The manager responsible for storing and tracking the state of all markers. |
| `renderer` | `MarkerOverlayRendererInterface<ActualMarker>` | The renderer responsible for drawing and updating the native marker objects on the map. |
| `clickListener` | `OnMarkerEventHandler?` | (Optional) A global listener for marker click events. Defaults to `null`. |

## Properties

| Property | Type | Description |
| :--- | :--- | :--- |
| `renderer` | `MarkerOverlayRendererInterface<ActualMarker>` | The renderer instance used for drawing markers on the map. |
| `zIndex` | `Int` | The z-index for the marker overlay, used to control rendering order. Hardcoded to `10`. |
| `clickListener` | `OnMarkerEventHandler?` | A global event handler invoked when any marker is clicked. |
| `dragStartListener` | `OnMarkerEventHandler?` | A global event handler invoked when a drag gesture starts on any marker. |
| `dragListener` | `OnMarkerEventHandler?` | A global event handler invoked continuously while any marker is being dragged. |
| `dragEndListener` | `OnMarkerEventHandler?` | A global event handler invoked when a drag gesture ends on any marker. |
| `animateStartListener` | `OnMarkerEventHandler?` | A global event handler invoked when a marker animation starts. |
| `animateEndListener` | `OnMarkerEventHandler?` | A global event handler invoked when a marker animation ends. |

## Methods

### add

Adds, updates, or removes markers to match the provided list of `MarkerState` objects. This method performs a diff against the current set of markers and efficiently applies the necessary changes (add, update, remove). Operations are batched to maintain UI responsiveness.

**Signature**
```kotlin
override suspend fun add(data: List<MarkerState>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<MarkerState>` | The complete list of marker states that should be displayed on the map. |

### update

Updates a single existing marker with a new state. If a marker with the given `state.id` does not exist, the operation is ignored. This method is an optimized path for updating a single marker and is more efficient than calling `add` with a full list. It also triggers animations if the animation property has changed.

**Signature**
```kotlin
override suspend fun update(state: MarkerState)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `MarkerState` | The new state for the marker to be updated. |

### clear

Removes all markers managed by this controller from the map.

**Signature**
```kotlin
override suspend fun clear()
```

### destroy

Cleans up all resources used by the controller and its associated `MarkerManager`. This method is crucial to call when the map is being destroyed or when switching map providers to prevent memory leaks.

**Signature**
```kotlin
override fun destroy()
```

### Event Dispatchers

These methods are typically called by the platform-specific implementation to propagate events from the map's native markers to the controller's event handling system.

| Method | Description |
| :--- | :--- |
| `dispatchClick(state: MarkerState)` | Dispatches a click event. This triggers the `onClick` callback on the specific `MarkerState` and the global `clickListener` on the controller. |
| `dispatchDragStart(state: MarkerState)` | Dispatches a drag start event, triggering `onDragStart` on the `MarkerState` and the global `dragStartListener`. |
| `dispatchDrag(state: MarkerState)` | Dispatches a drag event, triggering `onDrag` on the `MarkerState` and the global `dragListener`. |
| `dispatchDragEnd(state: MarkerState)` | Dispatches a drag end event, triggering `onDragEnd` on the `MarkerState` and the global `dragEndListener`. |
| `dispatchAnimateStart(state: MarkerState)` | Dispatches an animation start event, triggering `onAnimateStart` on the `MarkerState` and the global `animateStartListener`. |
| `dispatchAnimateEnd(state: MarkerState)` | Dispatches an animation end event, triggering `onAnimateEnd` on the `MarkerState` and the global `animateEndListener`. |

## Protected Methods

### setDraggingState

Sets the internal dragging state for a marker. This method should be called by subclasses to manage the dragging lifecycle, as the `isDragging` property is not directly accessible.

**Signature**
```kotlin
protected fun setDraggingState(markerState: MarkerState, dragging: Boolean)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `markerState` | `MarkerState` | The state object of the marker whose dragging status is being changed. |
| `dragging` | `Boolean` | `true` if the marker is being dragged, `false` otherwise. |

## Example

Below is a conceptual example of how a developer would use a concrete implementation of `AbstractMarkerController`.

```kotlin
// Assume 'myMarkerController' is a concrete implementation of AbstractMarkerController
// and has been initialized.

// 1. Set a global click listener
myMarkerController.clickListener = { markerState ->
    Log.d("MapApp", "Marker clicked: ${markerState.id}")
}

// 2. Define the markers to be displayed
val markerStates = listOf(
    MarkerState(
        id = "marker-1",
        position = LatLng(34.0522, -118.2437),
        title = "Los Angeles",
        onClick = { state -> Log.d("MapApp", "Clicked on LA marker!") }
    ),
    MarkerState(
        id = "marker-2",
        position = LatLng(40.7128, -74.0060),
        title = "New York City"
    )
)

// 3. Add the markers to the map using the controller
// This will add both markers to the map.
viewModelScope.launch {
    myMarkerController.add(markerStates)
}

// 4. Later, update a single marker
val updatedNYState = MarkerState(
    id = "marker-2",
    position = LatLng(40.7128, -74.0060),
    title = "The Big Apple", // Title changed
    icon = CustomMapIcon("new_icon") // Icon changed
)

// This will efficiently update only the NYC marker.
viewModelScope.launch {
    myMarkerController.update(updatedNYState)
}

// 5. Clear all markers from the map
viewModelScope.launch {
    myMarkerController.clear()
}

// 6. Clean up resources when the map is destroyed
override fun onCleared() {
    myMarkerController.destroy()
    super.onCleared()
}
```