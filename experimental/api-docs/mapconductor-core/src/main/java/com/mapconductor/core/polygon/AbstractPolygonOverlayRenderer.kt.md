Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# AbstractPolygonOverlayRenderer<ActualPolygon>

## Signature

```kotlin
abstract class AbstractPolygonOverlayRenderer<ActualPolygon> : PolygonOverlayRendererInterface<ActualPolygon>
```

## Description

`AbstractPolygonOverlayRenderer` provides a foundational implementation for rendering and managing polygon overlays on a map. It serves as a base class that handles the core logic for processing add, change, and remove operations on a set of polygons.

This class separates the generic lifecycle management of overlays from the platform-specific rendering details. Subclasses are required to implement the abstract methods (`createPolygon`, `updatePolygonProperties`, `removePolygon`) to interact with the native map SDK's polygon objects.

## Generic Parameters

| Parameter | Description |
|-----------|-------------|
| `ActualPolygon` | The native polygon object type from the specific map provider's SDK (e.g., `com.google.android.gms.maps.model.Polygon` for Google Maps). |

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `holder` | `MapViewHolderInterface<*, *>` | An abstract property that must be implemented to provide access to the map view holder, which manages the map instance. |
| `coroutine` | `CoroutineScope` | An abstract property that must be implemented to provide a coroutine scope for managing asynchronous operations related to map rendering. |

## Methods

### Abstract Methods

These methods must be implemented by any subclass to provide platform-specific rendering logic.

#### createPolygon

Creates a new native polygon on the map based on the provided state.

**Signature**
```kotlin
abstract suspend fun createPolygon(state: PolygonState): ActualPolygon?
```

**Description**
This function is called when a new polygon needs to be added to the map. The implementation should use the properties within the `state` object to construct and configure a native polygon object and add it to the map.

**Parameters**
| Parameter | Type | Description |
|-----------|--------------|-------------|
| `state` | `PolygonState` | An object containing all the properties (e.g., points, fill color, stroke width) for the new polygon. |

**Returns**
`ActualPolygon?`: The newly created native polygon object or `null` if the creation failed.

---

#### updatePolygonProperties

Updates the properties of an existing native polygon on the map.

**Signature**
```kotlin
abstract suspend fun updatePolygonProperties(
    polygon: ActualPolygon,
    current: PolygonEntityInterface<ActualPolygon>,
    prev: PolygonEntityInterface<ActualPolygon>,
): ActualPolygon?
```

**Description**
This function is called when a polygon's properties have changed. The implementation should efficiently update the visual properties of the given `polygon` object by comparing the `current` and `prev` states.

**Parameters**
| Parameter | Type | Description |
|-----------|---------------------------------------|-------------|
| `polygon` | `ActualPolygon` | The native polygon object to be updated. |
| `current` | `PolygonEntityInterface<ActualPolygon>` | The entity wrapper containing the new state of the polygon. |
| `prev` | `PolygonEntityInterface<ActualPolygon>` | The entity wrapper containing the previous state of the polygon. |

**Returns**
`ActualPolygon?`: The updated native polygon object, or a new instance if the underlying map SDK requires replacement. Returns `null` if the update fails.

---

#### removePolygon

Removes a native polygon from the map.

**Signature**
```kotlin
abstract suspend fun removePolygon(entity: PolygonEntityInterface<ActualPolygon>)
```

**Description**
This function is called when a polygon needs to be removed from the map. The implementation should find the corresponding native polygon object within the `entity` and remove it from the map view.

**Parameters**
| Parameter | Type | Description |
|-----------|---------------------------------------|-------------|
| `entity` | `PolygonEntityInterface<ActualPolygon>` | The entity wrapper for the polygon to be removed. It contains the native polygon object. |

**Returns**
`Unit`

---

### Implemented Methods

These methods are part of the `PolygonOverlayRendererInterface` and are implemented by this abstract class. They orchestrate the calls to the abstract methods defined above.

#### onAdd

Handles the addition of a batch of new polygons.

**Signature**
```kotlin
override suspend fun onAdd(data: List<PolygonOverlayRendererInterface.AddParamsInterface>): List<ActualPolygon?>
```

**Description**
This method iterates through a list of polygon creation parameters and calls `createPolygon` for each one, effectively adding a batch of polygons to the map.

**Parameters**
| Parameter | Type | Description |
|-----------|----------------------------------------------------------|-------------|
| `data` | `List<PolygonOverlayRendererInterface.AddParamsInterface>` | A list of parameters, where each element contains the state for a new polygon. |

**Returns**
`List<ActualPolygon?>`: A list containing the newly created native polygon objects. Each element corresponds to an item in the input `data` list.

---

#### onChange

Handles property changes for a batch of existing polygons.

**Signature**
```kotlin
override suspend fun onChange(
    data: List<PolygonOverlayRendererInterface.ChangeParamsInterface<ActualPolygon>>,
): List<ActualPolygon?>
```

**Description**
This method iterates through a list of changed polygons and calls `updatePolygonProperties` for each one, applying the new properties.

**Parameters**
| Parameter | Type | Description |
|-----------|-----------------------------------------------------------------------|-------------|
| `data` | `List<PolygonOverlayRendererInterface.ChangeParamsInterface<ActualPolygon>>` | A list of parameters, where each element contains the previous and current state of a polygon. |

**Returns**
`List<ActualPolygon?>`: A list of the updated native polygon objects.

---

#### onRemove

Handles the removal of a batch of polygons.

**Signature**
```kotlin
override suspend fun onRemove(data: List<PolygonEntityInterface<ActualPolygon>>)
```

**Description**
This method iterates through a list of polygon entities and calls `removePolygon` for each one to remove them from the map.

**Parameters**
| Parameter | Type | Description |
|-----------|---------------------------------------------|-------------|
| `data` | `List<PolygonEntityInterface<ActualPolygon>>` | A list of polygon entities to be removed. |

**Returns**
`Unit`

---

#### onPostProcess

A lifecycle hook called after a batch of add, change, and remove operations is complete.

**Signature**
```kotlin
override suspend fun onPostProcess()
```

**Description**
The default implementation is empty. Subclasses can override this method to perform any final processing, cleanup, or view refresh actions that are needed after a batch update.

**Returns**
`Unit`

## Example

The following example demonstrates how to create a concrete renderer for Google Maps by extending `AbstractPolygonOverlayRenderer`.

```kotlin
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.mapconductor.core.map.MapViewHolderInterface
import com.mapconductor.core.polygon.*
import kotlinx.coroutines.CoroutineScope

// Assume GoogleMapViewHolder and other related classes are defined elsewhere
class GoogleMapPolygonOverlayRenderer(
    override val holder: MapViewHolderInterface<GoogleMap, *>,
    override val coroutine: CoroutineScope
) : AbstractPolygonOverlayRenderer<Polygon>() {

    private val googleMap: GoogleMap?
        get() = holder.getMap()

    override suspend fun createPolygon(state: PolygonState): Polygon? {
        val map = googleMap ?: return null
        
        val polygonOptions = PolygonOptions().apply {
            addAll(state.points)
            fillColor(state.fillColor)
            strokeColor(state.strokeColor)
            strokeWidth(state.strokeWidth)
            zIndex(state.zIndex)
            clickable(state.isClickable)
        }
        
        // Add the polygon to the Google Map
        return map.addPolygon(polygonOptions)
    }

    override suspend fun updatePolygonProperties(
        polygon: Polygon,
        current: PolygonEntityInterface<Polygon>,
        prev: PolygonEntityInterface<Polygon>
    ): Polygon? {
        val currentState = current.state
        val prevState = prev.state

        // Efficiently update only the properties that have changed
        if (currentState.points != prevState.points) {
            polygon.points = currentState.points
        }
        if (currentState.fillColor != prevState.fillColor) {
            polygon.fillColor = currentState.fillColor
        }
        if (currentState.strokeColor != prevState.strokeColor) {
            polygon.strokeColor = currentState.strokeColor
        }
        // ... update other properties as needed ...
        
        return polygon
    }

    override suspend fun removePolygon(entity: PolygonEntityInterface<Polygon>) {
        // Remove the polygon from the map
        entity.polygon.remove()
    }

    override suspend fun onPostProcess() {
        // Optional: Perform any final actions after a batch update,
        // e.g., refreshing a cluster manager if it's affected.
    }
}
```