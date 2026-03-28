Of course! Here is the high-quality SDK documentation for the provided `MarkerCapableInterface` code snippet.

# Interface `MarkerCapableInterface`

Defines the contract for a component capable of managing and displaying markers on a map. This interface provides methods for adding, updating, and interacting with markers, as well as checking their existence.

---

## `compositionMarkers`

Adds or updates a collection of markers on the map. This function is designed for efficiently managing multiple markers at once, recomposing the map's markers to match the provided list of `MarkerState` objects. This is a suspend function and should be called from a coroutine.

### Signature

```kotlin
suspend fun compositionMarkers(data: List<MarkerState>)
```

### Parameters

| Parameter | Type                | Description                                                              |
| :-------- | :------------------ | :----------------------------------------------------------------------- |
| `data`    | `List<MarkerState>` | A list of `MarkerState` objects, each representing a marker to be drawn. |

### Example

```kotlin
import kotlinx.coroutines.launch

// Assuming 'mapController' is an instance that implements MarkerCapableInterface
// and we are in a CoroutineScope

val marker1 = MarkerState(id = "marker-1", position = LatLng(34.0522, -118.2437))
val marker2 = MarkerState(id = "marker-2", position = LatLng(40.7128, -74.0060))

// Add two markers to the map
coroutineScope.launch {
    mapController.compositionMarkers(listOf(marker1, marker2))
}
```

---

## `updateMarker`

Updates a single existing marker on the map based on its `MarkerState`. If a marker with the same ID as the provided state exists, it will be updated. If it does not exist, it may be created. This is a suspend function and should be called from a coroutine.

### Signature

```kotlin
suspend fun updateMarker(state: MarkerState)
```

### Parameters

| Parameter | Type          | Description                                                |
| :-------- | :------------ | :--------------------------------------------------------- |
| `state`   | `MarkerState` | The state object representing the marker to be updated. |

### Example

```kotlin
import kotlinx.coroutines.launch

// Assuming 'mapController' is an instance that implements MarkerCapableInterface
// and we are in a CoroutineScope

val updatedMarkerState = MarkerState(
    id = "marker-1",
    position = LatLng(34.0522, -118.2437),
    alpha = 0.5f // Update the marker's alpha
)

// Update the marker on the map
coroutineScope.launch {
    mapController.updateMarker(updatedMarkerState)
}
```

---

## `setOnMarkerDragStart`

> **Deprecated:** Use the `onDragStart` lambda property on an individual `MarkerState` object for more granular, per-marker control.

Sets a global listener that is invoked when a user begins dragging any marker on the map.

### Signature

```kotlin
@Deprecated("Use MarkerState.onDragStart instead.")
fun setOnMarkerDragStart(listener: OnMarkerEventHandler?)
```

### Parameters

| Parameter  | Type                   | Description                                                              |
| :--------- | :--------------------- | :----------------------------------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when a marker drag starts. Pass `null` to clear the listener. |

---

## `setOnMarkerDrag`

> **Deprecated:** Use the `onDrag` lambda property on an individual `MarkerState` object for more granular, per-marker control.

Sets a global listener that is invoked repeatedly while a user is dragging any marker on the map.

### Signature

```kotlin
@Deprecated("Use MarkerState.onDrag instead.")
fun setOnMarkerDrag(listener: OnMarkerEventHandler?)
```

### Parameters

| Parameter  | Type                   | Description                                                              |
| :--------- | :--------------------- | :----------------------------------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke while a marker is being dragged. Pass `null` to clear the listener. |

---

## `setOnMarkerDragEnd`

> **Deprecated:** Use the `onDragEnd` lambda property on an individual `MarkerState` object for more granular, per-marker control.

Sets a global listener that is invoked when a user finishes dragging any marker on the map.

### Signature

```kotlin
@Deprecated("Use MarkerState.onDragEnd instead.")
fun setOnMarkerDragEnd(listener: OnMarkerEventHandler?)
```

### Parameters

| Parameter  | Type                   | Description                                                              |
| :--------- | :--------------------- | :----------------------------------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when a marker drag operation ends. Pass `null` to clear the listener. |

---

## `setOnMarkerAnimateStart`

> **Deprecated:** Use the `onAnimateStart` lambda property on an individual `MarkerState` object for more granular, per-marker control.

Sets a global listener that is invoked when a marker animation begins.

### Signature

```kotlin
@Deprecated("Use MarkerState.onAnimateStart instead.")
fun setOnMarkerAnimateStart(listener: OnMarkerEventHandler?)
```

### Parameters

| Parameter  | Type                   | Description                                                              |
| :--------- | :--------------------- | :----------------------------------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when a marker animation starts. Pass `null` to clear the listener. |

---

## `setOnMarkerAnimateEnd`

> **Deprecated:** Use the `onAnimateEnd` lambda property on an individual `MarkerState` object for more granular, per-marker control.

Sets a global listener that is invoked when a marker animation completes.

### Signature

```kotlin
@Deprecated("Use MarkerState.onAnimateEnd instead.")
fun setOnMarkerAnimateEnd(listener: OnMarkerEventHandler?)
```

### Parameters

| Parameter  | Type                   | Description                                                              |
| :--------- | :--------------------- | :----------------------------------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when a marker animation finishes. Pass `null` to clear the listener. |

---

## `setOnMarkerClickListener`

> **Deprecated:** Use the `onClick` lambda property on an individual `MarkerState` object for more granular, per-marker control.

Sets a global listener that is invoked when a user clicks on any marker.

### Signature

```kotlin
@Deprecated("Use MarkerState.onClick instead.")
fun setOnMarkerClickListener(listener: OnMarkerEventHandler?)
```

### Parameters

| Parameter  | Type                   | Description                                                              |
| :--------- | :--------------------- | :----------------------------------------------------------------------- |
| `listener` | `OnMarkerEventHandler?` | The callback to invoke when a marker is clicked. Pass `null` to clear the listener. |

---

## `hasMarker`

Checks if a specific marker, identified by its `MarkerState`, currently exists on the map. The check is typically based on the marker's unique ID.

### Signature

```kotlin
fun hasMarker(state: MarkerState): Boolean
```

### Parameters

| Parameter | Type          | Description                                             |
| :-------- | :------------ | :------------------------------------------------------ |
| `state`   | `MarkerState` | The state object of the marker to check for. |

### Returns

`Boolean` - Returns `true` if a marker with the same ID as the provided state exists, `false` otherwise.

### Example

```kotlin
// Assuming 'mapController' is an instance that implements MarkerCapableInterface

val markerToCheck = MarkerState(id = "marker-1")

if (mapController.hasMarker(markerToCheck)) {
    println("Marker with ID 'marker-1' exists on the map.")
} else {
    println("Marker with ID 'marker-1' does not exist.")
}
```