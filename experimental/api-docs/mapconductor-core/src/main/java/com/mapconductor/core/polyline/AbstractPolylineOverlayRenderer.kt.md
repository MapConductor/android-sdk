# AbstractPolylineOverlayRenderer<ActualPolyline>

### Description

An abstract base class that provides a foundational implementation for rendering and managing polyline overlays on a map. This class implements the core logic for handling add, change, and remove operations by delegating the platform-specific rendering details to its subclasses.

Developers should extend this class to create a concrete renderer for a specific map provider (e.g., Google Maps, Mapbox). Subclasses are required to implement the abstract methods for creating, updating, and removing the actual polyline objects on the map.

### Signature

```kotlin
abstract class AbstractPolylineOverlayRenderer<ActualPolyline> : PolylineOverlayRendererInterface<ActualPolyline>
```

### Abstract Properties

Subclasses must provide implementations for the following properties.

| Property | Type | Description |
|---|---|---|
| `holder` | `MapViewHolderInterface<*, *>` | The map view holder instance that this renderer is associated with. |
| `coroutine` | `CoroutineScope` | The coroutine scope used for launching suspend functions and managing their lifecycle. |

---

### Abstract Methods

Subclasses must implement the following methods to handle platform-specific polyline rendering.

#### createPolyline

Creates a new, platform-specific polyline object based on the provided state.

**Signature**
```kotlin
abstract suspend fun createPolyline(state: PolylineState): ActualPolyline?
```

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `state` | `PolylineState` | The initial state (e.g., points, color, width) for the new polyline. |

**Returns**
| Type | Description |
|---|---|
| `ActualPolyline?` | The newly created platform-specific polyline object, or `null` if creation fails. |

---

#### updatePolylineProperties

Updates the properties of an existing polyline on the map.

**Signature**
```kotlin
abstract suspend fun updatePolylineProperties(
    polyline: ActualPolyline,
    current: PolylineEntityInterface<ActualPolyline>,
    prev: PolylineEntityInterface<ActualPolyline>,
): ActualPolyline?
```

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `polyline` | `ActualPolyline` | The native polyline object to be updated. |
| `current` | `PolylineEntityInterface<ActualPolyline>` | The entity containing the new, updated state of the polyline. |
| `prev` | `PolylineEntityInterface<ActualPolyline>` | The entity containing the previous state of the polyline, for comparison. |

**Returns**
| Type | Description |
|---|---|
| `ActualPolyline?` | The updated polyline object, or `null` if the update fails. |

---

#### removePolyline

Removes a polyline from the map.

**Signature**
```kotlin
abstract suspend fun removePolyline(entity: PolylineEntityInterface<ActualPolyline>)
```

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `entity` | `PolylineEntityInterface<ActualPolyline>` | The polyline entity to be removed. Its `polyline` property contains the native object to remove. |

---

### Methods

#### onPostProcess

A lifecycle hook called after a batch of add, change, or remove operations has been processed. This method can be overridden to perform custom actions, such as refreshing the map view. The default implementation is empty.

**Signature**
```kotlin
override suspend fun onPostProcess()
```

---

#### onAdd

Handles the addition of new polylines. This method iterates through the list of `AddParamsInterface` and calls the abstract `createPolyline` method for each item.

**Signature**
```kotlin
override suspend fun onAdd(
    data: List<PolylineOverlayRendererInterface.AddParamsInterface>
): List<ActualPolyline?>
```

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `data` | `List<PolylineOverlayRendererInterface.AddParamsInterface>` | A list of parameters for the polylines to be added. |

**Returns**
| Type | Description |
|---|---|
| `List<ActualPolyline?>` | A list containing the newly created `ActualPolyline` objects, with `null` for any that failed to be created. |

---

#### onChange

Handles changes to existing polylines. This method iterates through the list of `ChangeParamsInterface` and calls the abstract `updatePolylineProperties` method for each item.

**Signature**
```kotlin
override suspend fun onChange(
    data: List<PolylineOverlayRendererInterface.ChangeParamsInterface<ActualPolyline>>
): List<ActualPolyline?>
```

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `data` | `List<PolylineOverlayRendererInterface.ChangeParamsInterface<ActualPolyline>>` | A list of parameters describing the changes for each polyline. |

**Returns**
| Type | Description |
|---|---|
| `List<ActualPolyline?>` | A list containing the updated `ActualPolyline` objects, with `null` for any that failed to be updated. |

---

#### onRemove

Handles the removal of polylines. This method iterates through the list of `PolylineEntityInterface` and calls the abstract `removePolyline` method for each item.

**Signature**
```kotlin
override suspend fun onRemove(data: List<PolylineEntityInterface<ActualPolyline>>)
```

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `data` | `List<PolylineEntityInterface<ActualPolyline>>` | A list of polyline entities to be removed from the map. |

---

### Example

Here is an example of how to create a concrete renderer for a hypothetical `CustomMapPolyline` object.

```kotlin
// Assume CustomMapPolyline is the native polyline class for a specific map SDK
class CustomMapPolyline {
    // Platform-specific properties and methods
}

// Concrete implementation of the renderer
class CustomMapPolylineRenderer(
    override val holder: MapViewHolderInterface<*, *>,
    override val coroutine: CoroutineScope
) : AbstractPolylineOverlayRenderer<CustomMapPolyline>() {

    // Get the native map object from the holder
    private val map = holder.getMap() 

    override suspend fun createPolyline(state: PolylineState): CustomMapPolyline? {
        // Logic to create a new CustomMapPolyline on the map
        val newPolyline = CustomMapPolyline()
        // ... set properties on newPolyline from state (points, color, etc.)
        // ... add newPolyline to the map
        println("Creating polyline with ${state.points.size} points.")
        return newPolyline
    }

    override suspend fun updatePolylineProperties(
        polyline: CustomMapPolyline,
        current: PolylineEntityInterface<CustomMapPolyline>,
        prev: PolylineEntityInterface<CustomMapPolyline>
    ): CustomMapPolyline? {
        // Logic to update an existing CustomMapPolyline
        val currentState = current.state
        val prevState = prev.state
        
        // Example: only update points if they have changed
        if (currentState.points != prevState.points) {
            // ... polyline.setPoints(currentState.points)
        }
        
        // Example: only update color if it has changed
        if (currentState.color != prevState.color) {
            // ... polyline.setColor(currentState.color)
        }
        
        println("Updating polyline.")
        return polyline
    }

    override suspend fun removePolyline(entity: PolylineEntityInterface<CustomMapPolyline>) {
        // Logic to remove the CustomMapPolyline from the map
        val polylineToRemove = entity.polyline
        // ... map.remove(polylineToRemove)
        println("Removing polyline.")
    }
}
```