Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# Interface `OverlayRendererInterface<ActualType, StateType, EntityType>`

## Description

The `OverlayRendererInterface` defines a contract for rendering and managing a collection of overlay entities on a map. It provides a set of lifecycle methods that are invoked by a controller to handle the addition, modification, and removal of visual objects. This interface is designed to be implemented by a class that translates abstract data states into actual, platform-specific map objects (e.g., markers, polylines, polygons).

The generic types are used as follows:
-   `ActualType`: The concrete, platform-specific object rendered on the map (e.g., `GoogleMap.Marker`, `Mapbox.Annotation`).
-   `StateType`: The initial, minimal state required to create a new entity. This is typically used for the `onAdd` operation.
-   `EntityType`: The full, managed representation of an entity, containing all its properties. This is used for change detection and removal.

All methods are `suspend` functions, indicating they are designed for asynchronous execution within a coroutine context, allowing for non-blocking I/O or computation.

---

## Nested Interface `ChangeParamsInterface<EntityType>`

Represents the data required to process a change in an entity. It encapsulates the entity's state before and after the modification.

### Signature
```kotlin
interface ChangeParamsInterface<EntityType>
```

### Properties

| Property  | Type         | Description                                  |
| :-------- | :----------- | :------------------------------------------- |
| `current` | `EntityType` | The new, updated state of the entity.        |
| `prev`    | `EntityType` | The previous state of the entity before the change. |

---

## Methods

### onAdd

Adds a new batch of entities to the map. This method takes a list of initial states and is responsible for creating the corresponding visual objects.

#### Signature
```kotlin
suspend fun onAdd(data: List<StateType>): List<ActualType?>
```

#### Parameters

| Parameter | Type                | Description                                                                                             |
| :-------- | :------------------ | :------------------------------------------------------------------------------------------------------ |
| `data`    | `List<StateType>` | A list of initial state objects for the new entities to be created.                                     |

#### Returns
`List<ActualType?>` - A list of the newly created `ActualType` objects. The list should correspond in order to the input `data` list. An element can be `null` if the creation of a specific object failed.

---

### onChange

Updates a batch of existing entities on the map. This method is called when properties of one or more entities have changed.

#### Signature
```kotlin
suspend fun onChange(data: List<ChangeParamsInterface<EntityType>>): List<ActualType?>
```

#### Parameters

| Parameter | Type                                       | Description                                                                                             |
| :-------- | :----------------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `data`    | `List<ChangeParamsInterface<EntityType>>` | A list of `ChangeParamsInterface` objects, each containing the previous and current state of an entity. |

#### Returns
`List<ActualType?>` - A list of the updated `ActualType` objects. The list should correspond in order to the input `data` list. An element can be `null` if the update failed.

---

### onRemove

Removes a batch of entities from the map.

#### Signature
```kotlin
suspend fun onRemove(data: List<EntityType>)
```

#### Parameters

| Parameter | Type                | Description                                      |
| :-------- | :------------------ | :----------------------------------------------- |
| `data`    | `List<EntityType>` | A list of the entity objects to be removed.      |

---

### onPostProcess

A lifecycle callback that is invoked after all `onAdd`, `onChange`, and `onRemove` operations for a given update cycle have been completed. This can be used for cleanup, final adjustments, or triggering a map redraw.

#### Signature
```kotlin
suspend fun onPostProcess()
```

---

## Example

Here is an example of a `MarkerRenderer` that implements the `OverlayRendererInterface` to manage custom markers on a map.

```kotlin
// Define the data models for the generic types
data class MarkerState(val id: String, val position: LatLng) // StateType
data class MarkerEntity(val id: String, val position: LatLng, val title: String) // EntityType
class MapMarker { /* Platform-specific marker object */ } // ActualType

// Define the ChangeParams implementation
data class MarkerChangeParams(
    override val current: MarkerEntity,
    override val prev: MarkerEntity
) : OverlayRendererInterface.ChangeParamsInterface<MarkerEntity>

// Implement the renderer
class MarkerRenderer(private val map: MapSDK) : // Assuming a hypothetical MapSDK
    OverlayRendererInterface<MapMarker, MarkerState, MarkerEntity> {

    // A map to track rendered markers by their ID
    private val renderedMarkers = mutableMapOf<String, MapMarker>()

    override suspend fun onAdd(data: List<MarkerState>): List<MapMarker?> {
        val newMarkers = mutableListOf<MapMarker?>()
        for (state in data) {
            // Create a new platform-specific marker
            val newMarker = map.addMarker(position = state.position)
            renderedMarkers[state.id] = newMarker
            newMarkers.add(newMarker)
            println("Added marker: ${state.id}")
        }
        return newMarkers
    }

    override suspend fun onChange(data: List<OverlayRendererInterface.ChangeParamsInterface<MarkerEntity>>): List<MapMarker?> {
        val updatedMarkers = mutableListOf<MapMarker?>()
        for (change in data) {
            val marker = renderedMarkers[change.current.id]
            if (marker != null) {
                // Update marker properties if they have changed
                if (change.current.position != change.prev.position) {
                    marker.position = change.current.position
                }
                if (change.current.title != change.prev.title) {
                    marker.title = change.current.title
                }
                println("Changed marker: ${change.current.id}")
            }
            updatedMarkers.add(marker)
        }
        return updatedMarkers
    }

    override suspend fun onRemove(data: List<MarkerEntity>) {
        for (entity in data) {
            renderedMarkers[entity.id]?.let { marker ->
                map.removeMarker(marker)
                renderedMarkers.remove(entity.id)
                println("Removed marker: ${entity.id}")
            }
        }
    }

    override suspend fun onPostProcess() {
        // For example, force the map to redraw or refresh its camera view
        println("Post-processing all marker changes.")
        map.invalidate()
    }
}
```