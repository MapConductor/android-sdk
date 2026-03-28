# AbstractCircleOverlayRenderer<ActualCircle>

An abstract base class for rendering circle overlays on a map. This class provides the core logic for processing add, change, and remove operations for circles, delegating the platform-specific rendering details to subclasses.

Developers should extend this class to create a concrete renderer for a specific map provider (e.g., Google Maps, Mapbox). Subclasses are required to implement the abstract methods for creating, updating, and removing the actual map circle objects.

The generic type `<ActualCircle>` represents the platform-specific circle object (e.g., `com.google.android.gms.maps.model.Circle`).

## Properties

| Name | Type | Description |
| :--- | :--- | :--- |
| `holder` | `MapViewHolderInterface<*, *>` | **[Abstract]** The map view holder that provides access to the map instance. |
| `coroutine` | `CoroutineScope` | **[Abstract]** The coroutine scope used to launch and manage suspend functions. |

## Methods

### onPostProcess
A lifecycle hook called after all add, change, and remove operations for a given update cycle are complete. Subclasses can override this to perform any final processing, such as triggering a screen redraw. The default implementation is empty.

**Signature**
```kotlin
suspend fun onPostProcess()
```

---

### removeCircle
**[Abstract]** Subclasses must implement this method to remove a platform-specific circle from the map.

**Signature**
```kotlin
abstract suspend fun removeCircle(entity: CircleEntityInterface<ActualCircle>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `entity` | `CircleEntityInterface<ActualCircle>` | The circle entity containing the platform-specific circle object to be removed from the map. |

---

### createCircle
**[Abstract]** Subclasses must implement this method to create a new platform-specific circle on the map based on the provided state.

**Signature**
```kotlin
abstract suspend fun createCircle(state: CircleState): ActualCircle?
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `CircleState` | An object containing the initial properties (e.g., center, radius, color) for the new circle. |

**Returns**

| Type | Description |
| :--- | :--- |
| `ActualCircle?` | The newly created platform-specific circle object, or `null` if creation failed. |

---

### updateCircleProperties
**[Abstract]** Subclasses must implement this method to update the properties of an existing platform-specific circle on the map.

**Signature**
```kotlin
abstract suspend fun updateCircleProperties(
    circle: ActualCircle,
    current: CircleEntityInterface<ActualCircle>,
    prev: CircleEntityInterface<ActualCircle>,
): ActualCircle?
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `circle` | `ActualCircle` | The existing platform-specific circle object to update. |
| `current` | `CircleEntityInterface<ActualCircle>` | The entity representing the new, updated state of the circle. |
| `prev` | `CircleEntityInterface<ActualCircle>` | The entity representing the previous state of the circle, for comparison. |

**Returns**

| Type | Description |
| :--- | :--- |
| `ActualCircle?` | The updated circle object. This can be the same instance or a new one. Returns `null` if the update resulted in the circle being removed or replaced. |

---

### onAdd
Handles the addition of new circles to the map. This method iterates through a list of circle data and calls the abstract `createCircle` method for each one.

**Signature**
```kotlin
override suspend fun onAdd(
    data: List<CircleOverlayRendererInterface.AddParamsInterface>
): List<ActualCircle?>
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<CircleOverlayRendererInterface.AddParamsInterface>` | A list of parameters, where each element contains the state for a new circle to be added. |

**Returns**

| Type | Description |
| :--- | :--- |
| `List<ActualCircle?>` | A list of the newly created platform-specific circle objects. An element will be `null` if the corresponding circle could not be created. |

---

### onChange
Handles property changes for existing circles. This method iterates through a list of change data and calls the abstract `updateCircleProperties` method for each circle that has been modified.

**Signature**
```kotlin
override suspend fun onChange(
    data: List<CircleOverlayRendererInterface.ChangeParamsInterface<ActualCircle>>
): List<ActualCircle?>
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<CircleOverlayRendererInterface.ChangeParamsInterface<ActualCircle>>` | A list of parameters, where each element contains the previous and current state for a circle to be updated. |

**Returns**

| Type | Description |
| :--- | :--- |
| `List<ActualCircle?>` | A list of the updated platform-specific circle objects. |

---

### onRemove
Handles the removal of circles from the map. This method iterates through a list of circle entities and calls the abstract `removeCircle` method for each one.

**Signature**
```kotlin
override suspend fun onRemove(data: List<CircleEntityInterface<ActualCircle>>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<CircleEntityInterface<ActualCircle>>` | A list of circle entities to be removed from the map. |

## Example

Here is a conceptual example of how to subclass `AbstractCircleOverlayRenderer` for Google Maps.

```kotlin
// Assuming GoogleMap.Circle is the ActualCircle type
// and GoogleMapViewHolder implements MapViewHolderInterface
class GoogleCircleOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope
) : AbstractCircleOverlayRenderer<Circle>() {

    private val googleMap: GoogleMap?
        get() = holder.getMap()

    override suspend fun createCircle(state: CircleState): Circle? {
        val map = googleMap ?: return null
        
        val circleOptions = CircleOptions()
            .center(state.center.toLatLng())
            .radius(state.radius)
            .strokeColor(state.strokeColor)
            .fillColor(state.fillColor)
            .zIndex(state.zIndex)

        return map.addCircle(circleOptions)
    }

    override suspend fun updateCircleProperties(
        circle: Circle,
        current: CircleEntityInterface<Circle>,
        prev: CircleEntityInterface<Circle>
    ): Circle? {
        // Update properties that have changed
        if (current.state.center != prev.state.center) {
            circle.center = current.state.center.toLatLng()
        }
        if (current.state.radius != prev.state.radius) {
            circle.radius = current.state.radius
        }
        if (current.state.fillColor != prev.state.fillColor) {
            circle.fillColor = current.state.fillColor
        }
        // ... update other properties as needed
        
        return circle
    }

    override suspend fun removeCircle(entity: CircleEntityInterface<Circle>) {
        // The platform-specific circle object is stored in the entity
        entity.circle.remove()
    }
}
```