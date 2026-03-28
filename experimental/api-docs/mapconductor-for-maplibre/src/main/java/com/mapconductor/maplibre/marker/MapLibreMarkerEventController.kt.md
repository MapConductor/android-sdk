# MapLibreMarkerEventControllerInterface

## Description

The `MapLibreMarkerEventControllerInterface` defines the contract for managing and dispatching user interaction events for markers on a MapLibre map. It provides a unified API for handling marker selection, finding markers at specific coordinates, and setting up listeners for various events like clicks, drags, and animations.

This interface is implemented by two internal classes:
-   `DefaultMapLibreMarkerEventController`: A standard implementation that delegates event handling directly to a `MapLibreMarkerController`.
-   `StrategyMapLibreMarkerEventController`: An implementation designed to work with a `StrategyMarkerController`, providing more complex state management for selected markers, especially during drag operations.

## Properties

### renderer

Provides access to the `MapLibreMarkerOverlayRenderer` responsible for drawing markers and their overlays on the map.

**Signature**
```kotlin
val renderer: MapLibreMarkerOverlayRenderer
```

## Methods

### find

Finds the topmost marker entity at a given geographical position.

#### Signature

```kotlin
fun find(position: GeoPointInterface): MarkerEntityInterface<MapLibreActualMarker>?
```

#### Description

Searches for a marker on the map that intersects with the specified `position`. If multiple markers are at the same location, it returns the one with the highest z-index.

#### Parameters

| Parameter  | Type                | Description                               |
| :--------- | :------------------ | :---------------------------------------- |
| `position` | `GeoPointInterface` | The geographical coordinate to search at. |

#### Returns

| Type                                                 | Description                                                              |
| :--------------------------------------------------- | :----------------------------------------------------------------------- |
| `MarkerEntityInterface<MapLibreActualMarker>?` | The found marker entity, or `null` if no marker exists at that position. |

---

### getSelectedMarker

Retrieves the currently selected marker entity.

#### Signature

```kotlin
fun getSelectedMarker(): MarkerEntityInterface<MapLibreActualMarker>?
```

#### Description

Returns the marker entity that is currently in a "selected" state. A selected state is typically initiated by a user tap or can be set programmatically via `setSelectedMarker`.

#### Returns

| Type                                                 | Description                                         |
| :--------------------------------------------------- | :-------------------------------------------------- |
| `MarkerEntityInterface<MapLibreActualMarker>?` | The currently selected marker entity, or `null` if none is selected. |

---

### setSelectedMarker

Sets or clears the currently selected marker.

#### Signature

```kotlin
fun setSelectedMarker(entity: MarkerEntityInterface<MapLibreActualMarker>?)
```

#### Description

Programmatically sets the selection state for a marker. Passing a marker `entity` will select it. Passing `null` will deselect any currently selected marker. This can be used to highlight a marker or initiate a specific UI state.

#### Parameters

| Parameter | Type                                                 | Description                                                              |
| :-------- | :--------------------------------------------------- | :----------------------------------------------------------------------- |
| `entity`  | `MarkerEntityInterface<MapLibreActualMarker>?` | The marker entity to select, or `null` to clear the current selection. |

---

### dispatchClick

Dispatches a click event for a given marker state.

#### Signature

```kotlin
fun dispatchClick(state: MarkerState)
```

#### Description

This method is typically called internally by the map's gesture handler when a tap is detected on a marker. It triggers the `OnMarkerEventHandler` set by `setClickListener`.

#### Parameters

| Parameter | Type          | Description                               |
| :-------- | :------------ | :---------------------------------------- |
| `state`   | `MarkerState` | The state of the marker that was clicked. |

---

### dispatchDragStart

Dispatches a drag start event for a given marker state.

#### Signature

```kotlin
fun dispatchDragStart(state: MarkerState)
```

#### Description

This method is typically called internally when a drag gesture is initiated on a marker. It triggers the `OnMarkerEventHandler` set by `setDragStartListener`.

#### Parameters

| Parameter | Type          | Description                                      |
| :-------- | :------------ | :----------------------------------------------- |
| `state`   | `MarkerState` | The state of the marker when the drag started. |

---

### dispatchDrag

Dispatches a drag event for a given marker state.

#### Signature

```kotlin
fun dispatchDrag(state: MarkerState)
```

#### Description

This method is called internally during a drag gesture on a marker. It triggers the `OnMarkerEventHandler` set by `setDragListener`, providing continuous updates on the marker's state as it is being dragged.

#### Parameters

| Parameter | Type          | Description                                    |
| :-------- | :------------ | :--------------------------------------------- |
| `state`   | `MarkerState` | The current state of the marker during the drag. |

---

### dispatchDragEnd

Dispatches a drag end event for a given marker state.

#### Signature

```kotlin
fun dispatchDragEnd(state: MarkerState)
```

#### Description

This method is called internally when a drag gesture on a marker concludes. It triggers the `OnMarkerEventHandler` set by `setDragEndListener`.

#### Parameters

| Parameter | Type          | Description                                  |
| :-------- | :------------ | :------------------------------------------- |
| `state`   | `MarkerState` | The final state of the marker after the drag. |

---

### setClickListener

Sets a listener to handle marker click events.

#### Signature

```kotlin
fun setClickListener(listener: OnMarkerEventHandler?)
```

#### Description

Registers a callback that will be invoked whenever a marker is clicked. The listener receives the `MarkerState` of the clicked marker. Set the listener to `null` to remove it.

#### Parameters

| Parameter  | Type                   | Description                                                              |
| :--------- | :--------------------- | :----------------------------------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke on a marker click, or `null` to clear the listener. |

---

### setDragStartListener

Sets a listener to handle the start of a marker drag event.

#### Signature

```kotlin
fun setDragStartListener(listener: OnMarkerEventHandler?)
```

#### Description

Registers a callback that will be invoked when a user begins dragging a marker. The listener receives the `MarkerState` of the marker at the beginning of the drag operation. Set the listener to `null` to remove it.

#### Parameters

| Parameter  | Type                   | Description                                                                      |
| :--------- | :--------------------- | :------------------------------------------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when a marker drag starts, or `null` to clear the listener. |

---

### setDragListener

Sets a listener to handle continuous marker drag events.

#### Signature

```kotlin
fun setDragListener(listener: OnMarkerEventHandler?)
```

#### Description

Registers a callback that will be invoked repeatedly as a user drags a marker across the map. This is useful for tracking the marker's position in real-time during the drag. Set the listener to `null` to remove it.

#### Parameters

| Parameter  | Type                   | Description                                                                        |
| :--------- | :--------------------- | :--------------------------------------------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke as a marker is being dragged, or `null` to clear the listener. |

---

### setDragEndListener

Sets a listener to handle the end of a marker drag event.

#### Signature

```kotlin
fun setDragEndListener(listener: OnMarkerEventHandler?)
```

#### Description

Registers a callback that will be invoked when a user releases a marker after dragging it. The listener receives the final `MarkerState` of the marker. Set the listener to `null` to remove it.

#### Parameters

| Parameter  | Type                   | Description                                                                    |
| :--------- | :--------------------- | :----------------------------------------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when a marker drag ends, or `null` to clear the listener. |

---

### setAnimateStartListener

Sets a listener to handle the start of a marker animation.

#### Signature

```kotlin
fun setAnimateStartListener(listener: OnMarkerEventHandler?)
```

#### Description

Registers a callback that will be invoked when a marker's animation begins. The listener receives the `MarkerState` of the marker at the start of the animation. Set the listener to `null` to remove it.

#### Parameters

| Parameter  | Type                   | Description                                                                          |
| :--------- | :--------------------- | :----------------------------------------------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when a marker animation starts, or `null` to clear the listener. |

---

### setAnimateEndListener

Sets a listener to handle the end of a marker animation.

#### Signature

```kotlin
fun setAnimateEndListener(listener: OnMarkerEventHandler?)
```

#### Description

Registers a callback that will be invoked when a marker's animation completes. The listener receives the final `MarkerState` of the marker after the animation. Set the listener to `null` to remove it.

#### Parameters

| Parameter  | Type                   | Description                                                                        |
| :--------- | :--------------------- | :--------------------------------------------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when a marker animation ends, or `null` to clear the listener. |

## Example

The following example demonstrates how to use the `MapLibreMarkerEventControllerInterface` to set a click listener and programmatically select a marker.

```kotlin
import android.util.Log
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.maplibre.marker.MapLibreMarkerEventControllerInterface

// Assume 'markerEventController' is obtained from your map instance
val markerEventController: MapLibreMarkerEventControllerInterface = getMarkerEventController()

// 1. Set a click listener to log marker information
markerEventController.setClickListener { markerState ->
    Log.d("MarkerClick", "Marker with ID ${markerState.id} was clicked at ${markerState.position}")
    
    // You can also use the controller to make this clicked marker the selected one
    val clickedMarkerEntity = markerEventController.find(markerState.position)
    markerEventController.setSelectedMarker(clickedMarkerEntity)
    true // Return true to indicate the event was handled
}

// 2. Set a drag end listener
markerEventController.setDragEndListener { markerState ->
    Log.d("MarkerDragEnd", "Marker ${markerState.id} was dropped at ${markerState.position}")
    true // Event handled
}

// 3. Programmatically find and select a marker
val searchPosition = GeoPoint(40.7128, -74.0060) // New York City
val markerToSelect = markerEventController.find(searchPosition)

if (markerToSelect != null) {
    markerEventController.setSelectedMarker(markerToSelect)
    Log.d("MarkerSelection", "Marker ${markerToSelect.state.id} has been selected programmatically.")
}

// 4. To clear the selection
markerEventController.setSelectedMarker(null)
```