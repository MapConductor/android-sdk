# SDK Documentation: HereMarkerEventControllerInterface

The `HereMarkerEventControllerInterface` provides a standardized way to manage and interact with map markers, specifically for the HERE Maps SDK integration. It handles marker selection, event dispatching (clicks, drags), and listener registration.

This interface is implemented by `DefaultHereMarkerEventController` and `StrategyHereMarkerEventController`, which provide the concrete logic for different marker management strategies.

## find

### Signature
```kotlin
fun find(position: GeoPoint): MarkerEntityInterface<HereActualMarker>?
```

### Description
Searches for a marker entity at a specific geographical coordinate.

### Parameters
| Name | Type | Description |
| :--- | :--- | :---------- |
| `position` | `GeoPoint` | The geographical coordinate to search for a marker. |

### Returns
`MarkerEntityInterface<HereActualMarker>?` - The found marker entity, or `null` if no marker exists at the specified position.

---

## getSelectedMarker

### Signature
```kotlin
fun getSelectedMarker(): MarkerEntityInterface<HereActualMarker>?
```

### Description
Retrieves the currently selected marker entity.

### Returns
`MarkerEntityInterface<HereActualMarker>?` - The currently selected marker entity, or `null` if no marker is selected.

---

## setSelectedMarker

### Signature
```kotlin
fun setSelectedMarker(entity: MarkerEntityInterface<HereActualMarker>?)
```

### Description
Sets the specified marker entity as the currently selected one. To clear the current selection, pass `null`.

### Parameters
| Name | Type | Description |
| :--- | :--- | :---------- |
| `entity` | `MarkerEntityInterface<HereActualMarker>?` | The marker entity to select, or `null` to deselect the current marker. |

---

## dispatchClick

### Signature
```kotlin
fun dispatchClick(state: MarkerState)
```

### Description
Dispatches a click event to the registered click listener. This is typically called by the underlying map implementation when a user taps a marker.

### Parameters
| Name | Type | Description |
| :--- | :--- | :---------- |
| `state` | `MarkerState` | The state of the marker at the time of the click event. |

---

## dispatchDragStart

### Signature
```kotlin
fun dispatchDragStart(state: MarkerState)
```

### Description
Dispatches a drag start event to the registered drag start listener.

### Parameters
| Name | Type | Description |
| :--- | :--- | :---------- |
| `state` | `MarkerState` | The state of the marker when the drag operation begins. |

---

## dispatchDrag

### Signature
```kotlin
fun dispatchDrag(state: MarkerState)
```

### Description
Dispatches a drag event to the registered drag listener as the marker is being dragged.

### Parameters
| Name | Type | Description |
| :--- | :--- | :---------- |
| `state` | `MarkerState` | The current state of the marker during the drag operation. |

---

## dispatchDragEnd

### Signature
```kotlin
fun dispatchDragEnd(state: MarkerState)
```

### Description
Dispatches a drag end event to the registered drag end listener when the user finishes dragging the marker.

### Parameters
| Name | Type | Description |
| :--- | :--- | :---------- |
| `state` | `MarkerState` | The final state of the marker after the drag operation has completed. |

---

## setClickListener

### Signature
```kotlin
fun setClickListener(listener: OnMarkerEventHandler?)
```

### Description
Registers a listener to be invoked when a marker is clicked. The listener receives the state of the marker that was clicked.

### Parameters
| Name | Type | Description |
| :--- | :--- | :---------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke on a marker click. Pass `null` to remove the existing listener. |

---

## setDragStartListener

### Signature
```kotlin
fun setDragStartListener(listener: OnMarkerEventHandler?)
```

### Description
Registers a listener to be invoked when a marker drag operation begins.

### Parameters
| Name | Type | Description |
| :--- | :--- | :---------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when a marker drag starts. Pass `null` to remove the existing listener. |

---

## setDragListener

### Signature
```kotlin
fun setDragListener(listener: OnMarkerEventHandler?)
```

### Description
Registers a listener to be invoked repeatedly as a marker is being dragged.

### Parameters
| Name | Type | Description |
| :--- | :--- | :---------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke during a marker drag. Pass `null` to remove the existing listener. |

---

## setDragEndListener

### Signature
```kotlin
fun setDragEndListener(listener: OnMarkerEventHandler?)
```

### Description
Registers a listener to be invoked when a marker drag operation ends.

### Parameters
| Name | Type | Description |
| :--- | :--- | :---------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when a marker drag ends. Pass `null` to remove the existing listener. |

---

## setAnimateStartListener

### Signature
```kotlin
fun setAnimateStartListener(listener: OnMarkerEventHandler?)
```

### Description
Registers a listener to be invoked when a marker animation starts.

### Parameters
| Name | Type | Description |
| :--- | :--- | :---------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when an animation starts. Pass `null` to remove the existing listener. |

---

## setAnimateEndListener

### Signature
```kotlin
fun setAnimateEndListener(listener: OnMarkerEventHandler?)
```

### Description
Registers a listener to be invoked when a marker animation ends.

### Parameters
| Name | Type | Description |
| :--- | :--- | :---------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when an animation ends. Pass `null` to remove the existing listener. |

---

## Example

Here's an example of how to use the `HereMarkerEventControllerInterface` to manage marker interactions.

```kotlin
// Assume 'markerEventController' is an instance of a class
// that implements HereMarkerEventControllerInterface.

// 1. Set a click listener to handle marker taps
markerEventController.setClickListener { markerState ->
    println("Marker clicked! ID: ${markerState.id}, Position: ${markerState.position}")
    // You can now show an info window or perform another action.
}

// 2. Set a drag end listener to get the new position of a marker
markerEventController.setDragEndListener { markerState ->
    println("Marker drag finished. New position: ${markerState.position}")
    // Update your application's state with the marker's new coordinates.
}

// 3. Programmatically select a marker
// First, find a marker by its geographical position
val position = GeoPoint(48.8584, 2.2945) // e.g., Eiffel Tower
val markerToSelect = markerEventController.find(position)

// Then, set it as the selected marker
markerEventController.setSelectedMarker(markerToSelect)

// 4. Retrieve the currently selected marker
val currentSelection = markerEventController.getSelectedMarker()
if (currentSelection != null) {
    println("Currently selected marker ID: ${currentSelection.id}")
} else {
    println("No marker is currently selected.")
}
```