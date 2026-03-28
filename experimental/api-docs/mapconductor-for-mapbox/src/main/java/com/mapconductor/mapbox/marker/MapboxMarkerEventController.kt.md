Of course! Here is the high-quality SDK documentation for the provided code snippet.

# Mapbox Marker Event Controllers

This document provides a detailed reference for the `MapboxMarkerEventControllerInterface` and its implementations, which are responsible for managing marker interactions and events on a Mapbox map.

## `MapboxMarkerEventControllerInterface`

An interface that defines the contract for controlling marker events and interactions within the Mapbox map environment. It provides a unified API for finding markers, handling selection, dispatching user interactions (clicks, drags), and setting up event listeners.

### Properties

#### `renderer`
The renderer responsible for drawing markers and overlays on the map.

**Signature**
```kotlin
val renderer: MapboxMarkerOverlayRenderer
```

---

### Methods

#### `find()`
Finds the topmost marker entity at a given geographic position.

**Signature**
```kotlin
fun find(position: GeoPointInterface): MarkerEntityInterface<MapboxActualMarker>?
```

**Parameters**

| Parameter  | Type                | Description                                  |
|------------|---------------------|----------------------------------------------|
| `position` | `GeoPointInterface` | The geographic coordinate to search for a marker at. |

**Returns**

`MarkerEntityInterface<MapboxActualMarker>?`: The found marker entity or `null` if no marker exists at the specified position.

---

#### `getSelectedMarker()`
Retrieves the currently selected marker entity.

**Signature**
```kotlin
fun getSelectedMarker(): MarkerEntityInterface<MapboxActualMarker>?
```

**Returns**

`MarkerEntityInterface<MapboxActualMarker>?`: The currently selected marker entity or `null` if no marker is selected.

---

#### `setSelectedMarker()`
Sets the specified marker entity as the currently selected one. To clear the current selection, pass `null`.

**Signature**
```kotlin
fun setSelectedMarker(entity: MarkerEntityInterface<MapboxActualMarker>?)
```

**Parameters**

| Parameter | Type                                         | Description                                        |
|-----------|----------------------------------------------|----------------------------------------------------|
| `entity`  | `MarkerEntityInterface<MapboxActualMarker>?` | The marker entity to select, or `null` to deselect. |

---

#### `dispatchClick()`
Dispatches a marker click event. This is typically invoked by the map's internal gesture handlers when a marker is tapped.

**Signature**
```kotlin
fun dispatchClick(state: MarkerState)
```

**Parameters**

| Parameter | Type          | Description                               |
|-----------|---------------|-------------------------------------------|
| `state`   | `MarkerState` | The state of the marker at the time of the event. |

---

#### `dispatchDragStart()`
Dispatches a marker drag start event.

**Signature**
```kotlin
fun dispatchDragStart(state: MarkerState)
```

**Parameters**

| Parameter | Type          | Description                               |
|-----------|---------------|-------------------------------------------|
| `state`   | `MarkerState` | The state of the marker at the time of the event. |

---

#### `dispatchDrag()`
Dispatches a marker drag event, which is fired continuously as the user drags a marker.

**Signature**
```kotlin
fun dispatchDrag(state: MarkerState)
```

**Parameters**

| Parameter | Type          | Description                               |
|-----------|---------------|-------------------------------------------|
| `state`   | `MarkerState` | The state of the marker at the time of the event. |

---

#### `dispatchDragEnd()`
Dispatches a marker drag end event, fired when the user releases a marker after dragging it.

**Signature**
```kotlin
fun dispatchDragEnd(state: MarkerState)
```

**Parameters**

| Parameter | Type          | Description                               |
|-----------|---------------|-------------------------------------------|
| `state`   | `MarkerState` | The state of the marker at the time of the event. |

---

#### `setClickListener()`
Sets a listener to be invoked when a marker is clicked.

**Signature**
```kotlin
fun setClickListener(listener: OnMarkerEventHandler?)
```

**Parameters**

| Parameter  | Type                   | Description                                                        |
|------------|------------------------|--------------------------------------------------------------------|
| `listener` | `OnMarkerEventHandler?` | The handler to be called on a marker click, or `null` to remove it. |

---

#### `setDragStartListener()`
Sets a listener to be invoked when a marker drag gesture begins.

**Signature**
```kotlin
fun setDragStartListener(listener: OnMarkerEventHandler?)
```

**Parameters**

| Parameter  | Type                   | Description                                                              |
|------------|------------------------|--------------------------------------------------------------------------|
| `listener` | `OnMarkerEventHandler?` | The handler to be called when a marker drag starts, or `null` to remove it. |

---

#### `setDragListener()`
Sets a listener to be invoked repeatedly while a marker is being dragged.

**Signature**
```kotlin
fun setDragListener(listener: OnMarkerEventHandler?)
```

**Parameters**

| Parameter  | Type                   | Description                                                              |
|------------|------------------------|--------------------------------------------------------------------------|
| `listener` | `OnMarkerEventHandler?` | The handler to be called during a marker drag, or `null` to remove it. |

---

#### `setDragEndListener()`
Sets a listener to be invoked when a marker drag gesture ends.

**Signature**
```kotlin
fun setDragEndListener(listener: OnMarkerEventHandler?)
```

**Parameters**

| Parameter  | Type                   | Description                                                            |
|------------|------------------------|------------------------------------------------------------------------|
| `listener` | `OnMarkerEventHandler?` | The handler to be called when a marker drag ends, or `null` to remove it. |

---

#### `setAnimateStartListener()`
Sets a listener to be invoked when a marker animation begins.

**Signature**
```kotlin
fun setAnimateStartListener(listener: OnMarkerEventHandler?)
```

**Parameters**

| Parameter  | Type                   | Description                                                                  |
|------------|------------------------|------------------------------------------------------------------------------|
| `listener` | `OnMarkerEventHandler?` | The handler to be called when a marker animation starts, or `null` to remove it. |

---

#### `setAnimateEndListener()`
Sets a listener to be invoked when a marker animation completes.

**Signature**
```kotlin
fun setAnimateEndListener(listener: OnMarkerEventHandler?)
```

**Parameters**

| Parameter  | Type                   | Description                                                                |
|------------|------------------------|----------------------------------------------------------------------------|
| `listener` | `OnMarkerEventHandler?` | The handler to be called when a marker animation ends, or `null` to remove it. |

---

## Implementations

### `DefaultMapboxMarkerEventController`
A default implementation of `MapboxMarkerEventControllerInterface`. It acts as a simple proxy, delegating all event handling and state management calls to an underlying `MapboxMarkerController`. This is the standard controller for basic marker management.

### `StrategyMapboxMarkerEventController`
An advanced implementation of `MapboxMarkerEventControllerInterface` designed to work with a `StrategyMarkerController`. This controller provides specialized behavior for marker selection and dragging, such as moving the selected marker to a dedicated "drag layer" for smoother interaction and visual separation. This is typically used in scenarios with complex rendering strategies like marker clustering.

---

## Example

The following example demonstrates how to use the `MapboxMarkerEventControllerInterface` to set listeners for marker click and drag events.

```kotlin
// Assume 'mapController' is an instance of your main map controller
// and 'eventController' is obtained from it.
val eventController: MapboxMarkerEventControllerInterface = mapController.markerEventController

// Set a listener for marker click events
eventController.setClickListener { markerState ->
    println("Marker clicked! ID: ${markerState.id}")
    // You can now select the marker
    val markerEntity = eventController.find(markerState.position)
    eventController.setSelectedMarker(markerEntity)
    true // Return true to indicate the event was handled
}

// Set a listener for marker drag end events
eventController.setDragEndListener { markerState ->
    println("Marker drag ended at position: ${markerState.position.latitude}, ${markerState.position.longitude}")
    true // Event handled
}

// To remove a listener, pass null
eventController.setClickListener(null)
```