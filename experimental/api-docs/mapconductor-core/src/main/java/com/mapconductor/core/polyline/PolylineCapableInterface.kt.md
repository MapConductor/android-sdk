# PolylineCapableInterface

An interface for components that have the capability to display and manage polylines on a map. This interface provides a standard contract for adding, updating, and interacting with polylines.

---

## Methods

### compositionPolylines

Asynchronously composes a list of polylines on the map. This function synchronizes the polylines displayed on the map with the provided list of `PolylineState` objects. It will add new polylines, update existing ones, and remove any polylines that are not in the new list. This is the preferred method for managing a collection of polylines declaratively.

#### Signature

```kotlin
suspend fun compositionPolylines(data: List<PolylineState>)
```

#### Parameters

| Parameter | Type                  | Description                                                              |
| :-------- | :-------------------- | :----------------------------------------------------------------------- |
| `data`    | `List<PolylineState>` | The complete list of polyline states to be rendered on the map.          |

#### Example

```kotlin
// Assuming 'mapController' implements PolylineCapableInterface
val polylineStates = listOf(
    PolylineState(id = "route1", points = listOf(...)),
    PolylineState(id = "route2", points = listOf(...), color = Color.BLUE)
)

// Asynchronously update the map to show only the polylines in the list
coroutineScope.launch {
    mapController.compositionPolylines(polylineStates)
}
```

---

### updatePolyline

Asynchronously adds a new polyline or updates an existing one based on the provided `PolylineState`. If a polyline with the same ID already exists on the map, its properties will be updated. Otherwise, a new polyline will be created and added.

#### Signature

```kotlin
suspend fun updatePolyline(state: PolylineState)
```

#### Parameters

| Parameter | Type          | Description                                      |
| :-------- | :------------ | :----------------------------------------------- |
| `state`   | `PolylineState` | The state of the polyline to add or update.      |

#### Example

```kotlin
// Create a new polyline state with updated properties
val updatedRouteState = PolylineState(
    id = "route1",
    points = newPointList,
    width = 12f
)

// Asynchronously update the specific polyline on the map
coroutineScope.launch {
    mapController.updatePolyline(updatedRouteState)
}
```

---

### setOnPolylineClickListener

Sets a global click listener for all polylines managed by this component.

> **Deprecated:** This method is deprecated. Use the `onClick` lambda property within the `PolylineState` for each individual polyline instead. This provides more granular control and is aligned with modern, state-driven UI patterns.

#### Signature

```kotlin
@Deprecated("Use PolylineState.onClick instead.")
fun setOnPolylineClickListener(listener: OnPolylineEventHandler?)
```

#### Parameters

| Parameter  | Type                     | Description                                                              |
| :--------- | :----------------------- | :----------------------------------------------------------------------- |
| `listener` | `OnPolylineEventHandler?` | The event handler to be invoked when any polyline is clicked. Set to `null` to remove the listener. |

---

### hasPolyline

Checks if a polyline matching the given `PolylineState` exists on the map. The check is typically performed using the unique identifier from the `PolylineState`.

#### Signature

```kotlin
fun hasPolyline(state: PolylineState): Boolean
```

#### Parameters

| Parameter | Type          | Description                                                              |
| :-------- | :------------ | :----------------------------------------------------------------------- |
| `state`   | `PolylineState` | The polyline state to check for. The lookup is based on the polyline's ID. |

#### Returns

**Boolean** - Returns `true` if a polyline with the same ID exists, and `false` otherwise.

#### Example

```kotlin
val routeToCheck = PolylineState(id = "route_alpha")

if (mapController.hasPolyline(routeToCheck)) {
    println("Polyline 'route_alpha' is already on the map.")
} else {
    println("Polyline 'route_alpha' is not on the map.")
}
```