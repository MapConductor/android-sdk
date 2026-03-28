# Interface `OverlayControllerInterface<StateType, EntityType, EventType>`

## Description

The `OverlayControllerInterface` defines a standardized contract for managing a layer of visual elements (overlays) on a map. It provides a generic, abstract way to add, update, find, and clear entities, regardless of the underlying map provider.

This interface is designed to be implemented for specific types of map objects, such as markers, polylines, or polygons.

-   **`StateType`**: Represents the data model or state of an individual entity. This is the provider-agnostic data you work with.
-   **`EntityType`**: Represents the actual, provider-specific object rendered on the map (e.g., a Google Maps `Marker` or a Mapbox `Symbol`).
-   **`EventType`**: Represents the data object passed to the click listener when an entity is clicked. This often contains identifying information from the original `StateType`.

---

## Properties

### zIndex

Controls the vertical stacking order of the entire overlay layer. Overlays with a higher `zIndex` are drawn on top of those with a lower `zIndex`.

**Signature**

```kotlin
val zIndex: Int
```

---

### clickListener

A callback lambda that is invoked when a user clicks on an entity managed by this controller. Set this property to handle user interactions with the overlay items.

**Signature**

```kotlin
var clickListener: ((EventType) -> Unit)?
```

**Example**

```kotlin
// Assuming 'markerController' is an implementation of OverlayControllerInterface
markerController.clickListener = { clickedMarkerEvent ->
    // clickedMarkerEvent is the EventType object
    println("Marker clicked with ID: ${clickedMarkerEvent.id}")
    // Show an info window, navigate to a new screen, etc.
}
```

---

## Functions

### add

Asynchronously adds a list of new entities to the map overlay. Each entity is defined by its corresponding state object.

**Signature**

```kotlin
suspend fun add(data: List<StateType>)
```

**Parameters**

| Parameter | Type                  | Description                                          |
| :-------- | :-------------------- | :--------------------------------------------------- |
| `data`    | `List<StateType>` | A list of state objects representing the items to add. |

---

### update

Asynchronously updates an existing entity on the map. The controller identifies the entity to update based on the provided state object (usually via a unique ID within the state).

**Signature**

```kotlin
suspend fun update(state: StateType)
```

**Parameters**

| Parameter | Type          | Description                                                |
| :-------- | :------------ | :--------------------------------------------------------- |
| `state`   | `StateType` | The new state object for the entity that needs to be updated. |

---

### clear

Asynchronously removes all entities from this overlay layer.

**Signature**

```kotlin
suspend fun clear()
```

---

### find

Finds the topmost entity at a specific geographical position on the map. This is useful for hit-testing, such as determining which entity was tapped by the user.

**Signature**

```kotlin
fun find(position: GeoPointInterface): EntityType?
```

**Parameters**

| Parameter  | Type                | Description                               |
| :--------- | :------------------ | :---------------------------------------- |
| `position` | `GeoPointInterface` | The geographical coordinate to search at. |

**Returns**

| Type         | Description                                                              |
| :----------- | :----------------------------------------------------------------------- |
| `EntityType?` | The provider-specific entity (`EntityType`) found at the position, or `null` if no entity exists there. |

---

### onCameraChanged

A lifecycle method called by the map framework whenever the map's camera view changes (e.g., pan, zoom, tilt). Implementations can use this to optimize performance, such as by clustering markers or adjusting the level of detail.

**Signature**

```kotlin
suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

**Parameters**

| Parameter           | Type                | Description                                         |
| :------------------ | :------------------ | :-------------------------------------------------- |
| `mapCameraPosition` | `MapCameraPosition` | An object containing the new state of the map camera. |

---

### destroy

Cleans up and releases all resources used by the controller. This includes removing entities from the map, detaching listeners, and freeing up memory.

> **IMPORTANT**: This method must be called when the controller is no longer needed, such as when switching between map providers or when the map view is being destroyed. Failure to do so can lead to memory leaks.

**Signature**

```kotlin
fun destroy()
```