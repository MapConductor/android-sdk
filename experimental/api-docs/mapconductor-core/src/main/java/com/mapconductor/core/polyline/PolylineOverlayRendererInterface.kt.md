Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# PolylineOverlayRendererInterface

The `PolylineOverlayRendererInterface` defines a contract for rendering and managing polyline overlays on a map. It is designed to be implemented by platform-specific renderers, abstracting the underlying map SDK's polyline handling (e.g., Google Maps, Mapbox).

This interface handles the lifecycle of polylines, including their creation, modification, and removal, through a set of asynchronous methods.

**Signature**
```kotlin
interface PolylineOverlayRendererInterface<ActualPolyline>
```

### Type Parameters

| Name | Description |
| :--- | :--- |
| `ActualPolyline` | The generic type representing the platform-specific polyline object (e.g., `com.google.android.gms.maps.model.Polyline`). |

---

## Nested Interfaces

### AddParamsInterface

An interface that encapsulates the parameters required to add a new polyline to the map.

**Signature**
```kotlin
interface AddParamsInterface
```

**Properties**

| Name | Type | Description |
| :--- | :--- | :--- |
| `state` | `PolylineState` | The state object defining the properties of the new polyline, such as its points, color, width, and z-index. |

### ChangeParamsInterface

An interface that holds the data required to update an existing polyline.

**Signature**
```kotlin
interface ChangeParamsInterface<ActualPolyline>
```

**Properties**

| Name | Type | Description |
| :--- | :--- | :--- |
| `current` | `PolylineEntityInterface<ActualPolyline>` | The entity representing the new, updated state of the polyline. |
| `prev` | `PolylineEntityInterface<ActualPolyline>` | The entity representing the previous state of the polyline before the change. |

---

## Functions

### onAdd

Asynchronously adds a batch of new polylines to the map based on the provided state data.

**Signature**
```kotlin
suspend fun onAdd(data: List<AddParamsInterface>): List<ActualPolyline?>
```

**Parameters**

| Name | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<AddParamsInterface>` | A list of parameter objects, where each object defines a polyline to be added. |

**Returns**

`List<ActualPolyline?>` - A list containing the newly created, platform-specific `ActualPolyline` objects. The order of this list corresponds to the input `data` list. An element will be `null` if the creation of a specific polyline failed.

### onChange

Asynchronously updates a batch of existing polylines on the map.

**Signature**
```kotlin
suspend fun onChange(data: List<ChangeParamsInterface<ActualPolyline>>): List<ActualPolyline?>
```

**Parameters**

| Name | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<ChangeParamsInterface<ActualPolyline>>` | A list of change objects, each containing the previous and current state of a polyline to be updated. |

**Returns**

`List<ActualPolyline?>` - A list containing the updated `ActualPolyline` objects. An element can be `null` if an update operation failed.

### onRemove

Asynchronously removes a batch of polylines from the map.

**Signature**
```kotlin
suspend fun onRemove(data: List<PolylineEntityInterface<ActualPolyline>>)
```

**Parameters**

| Name | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<PolylineEntityInterface<ActualPolyline>>` | A list of polyline entities to be removed from the map. |

### onPostProcess

A lifecycle callback executed after all `onAdd`, `onChange`, and `onRemove` operations in a single update cycle are complete. This can be used for final cleanup, batch updates, or triggering a map refresh.

**Signature**
```kotlin
suspend fun onPostProcess()
```

---

## Example

Here is an example of a simplified, hypothetical implementation of `PolylineOverlayRendererInterface` for a fictional map framework.

```kotlin
// Define dummy classes for the example
data class MyMapPolyline(var id: String, var points: List<Any>, var color: Int)
data class PolylineState(val id: String, val points: List<Any>, val color: Int)
interface PolylineEntityInterface<T> {
    val nativePolyline: T?
    val state: PolylineState
}

// Example implementation of the renderer
class MyMapPolylineRenderer : PolylineOverlayRendererInterface<MyMapPolyline> {

    private val managedPolylines = mutableMapOf<String, MyMapPolyline>()

    override suspend fun onAdd(data: List<PolylineOverlayRendererInterface.AddParamsInterface>): List<MyMapPolyline?> {
        println("Adding ${data.size} polylines...")
        return data.map { params ->
            val state = params.state
            // In a real implementation, you would call the map SDK here
            val newPolyline = MyMapPolyline(
                id = state.id,
                points = state.points,
                color = state.color
            )
            managedPolylines[state.id] = newPolyline
            println("Added polyline with id: ${state.id}")
            newPolyline
        }
    }

    override suspend fun onChange(data: List<PolylineOverlayRendererInterface.ChangeParamsInterface<MyMapPolyline>>): List<MyMapPolyline?> {
        println("Changing ${data.size} polylines...")
        return data.map { params ->
            val currentEntity = params.current
            val nativePolyline = currentEntity.nativePolyline
            
            if (nativePolyline != null) {
                // Apply changes to the native polyline object
                nativePolyline.points = currentEntity.state.points
                nativePolyline.color = currentEntity.state.color
                println("Changed polyline with id: ${nativePolyline.id}")
                nativePolyline
            } else {
                println("Change failed: native polyline not found for id: ${currentEntity.state.id}")
                null
            }
        }
    }

    override suspend fun onRemove(data: List<PolylineEntityInterface<MyMapPolyline>>) {
        println("Removing ${data.size} polylines...")
        data.forEach { entity ->
            val polylineId = entity.state.id
            // In a real implementation, you would remove the polyline from the map
            if (managedPolylines.remove(polylineId) != null) {
                println("Removed polyline with id: $polylineId")
            }
        }
    }

    override suspend fun onPostProcess() {
        // For example, trigger a redraw of the map view if needed
        println("Post-processing: All polyline operations are complete for this cycle.")
        // mapView.invalidate()
    }
}
```