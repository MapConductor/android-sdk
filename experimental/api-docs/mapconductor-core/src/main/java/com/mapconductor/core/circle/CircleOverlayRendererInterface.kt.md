Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# CircleOverlayRendererInterface<ActualCircle>

## Signature

```kotlin
interface CircleOverlayRendererInterface<ActualCircle>
```

## Description

The `CircleOverlayRendererInterface` defines a contract for rendering and managing the lifecycle of circle overlays on a map. Implement this interface to create a custom renderer that handles the addition, modification, and removal of circles based on state changes.

The interface is generic, allowing it to work with different map SDKs by specifying the platform-specific circle object type for `ActualCircle` (e.g., `com.google.android.gms.maps.model.Circle` for Google Maps).

## Type Parameters

| Name           | Description                                                                                             |
| :------------- | :------------------------------------------------------------------------------------------------------ |
| `ActualCircle` | The platform-specific class representing a circle object on the map (e.g., `GoogleMap.Circle`). |

---

## Nested Interfaces

### AddParamsInterface

**Signature**
```kotlin
interface AddParamsInterface
```

**Description**
A data structure that holds the required information to add a new circle to the map.

**Properties**

| Name    | Type          | Description                               |
| :------ | :------------ | :---------------------------------------- |
| `state` | `CircleState` | The state object containing all the properties for the new circle (e.g., center, radius, color). |

### ChangeParamsInterface<ActualCircle>

**Signature**
```kotlin
interface ChangeParamsInterface<ActualCircle>
```

**Description**
A data structure that holds the information needed to update an existing circle. It provides both the previous and the new state of the circle entity.

**Type Parameters**

| Name           | Description                                                                                             |
| :------------- | :------------------------------------------------------------------------------------------------------ |
| `ActualCircle` | The platform-specific class representing a circle object on the map. |

**Properties**

| Name      | Type                                     | Description                                                              |
| :-------- | :--------------------------------------- | :----------------------------------------------------------------------- |
| `current` | `CircleEntityInterface<ActualCircle>`    | The circle entity containing the **new** state and properties to be applied. |
| `prev`    | `CircleEntityInterface<ActualCircle>`    | The circle entity containing the **previous** state and properties.      |

---

## Methods

### onAdd

**Signature**
```kotlin
suspend fun onAdd(data: List<AddParamsInterface>): List<ActualCircle?>
```

**Description**
Asynchronously adds a batch of new circles to the map. This method is called when new circle states are introduced.

**Parameters**

| Name   | Type                           | Description                                                              |
| :----- | :----------------------------- | :----------------------------------------------------------------------- |
| `data` | `List<AddParamsInterface>`     | A list of `AddParamsInterface` objects, each describing a circle to be added. |

**Returns**

`List<ActualCircle?>`: A list of the newly created, platform-specific `ActualCircle` objects. An element can be `null` if the creation of a specific circle failed. The order of the returned list must correspond to the order of the input `data` list.

### onChange

**Signature**
```kotlin
suspend fun onChange(data: List<ChangeParamsInterface<ActualCircle>>): List<ActualCircle?>
```

**Description**
Asynchronously updates a batch of existing circles on the map. This method is called when the properties of existing circles have changed.

**Parameters**

| Name   | Type                                         | Description                                                              |
| :----- | :------------------------------------------- | :----------------------------------------------------------------------- |
| `data` | `List<ChangeParamsInterface<ActualCircle>>`  | A list of `ChangeParamsInterface` objects, each describing the changes for a circle. |

**Returns**

`List<ActualCircle?>`: A list of the updated, platform-specific `ActualCircle` objects. An element can be `null` if the update failed. The order of the returned list must correspond to the order of the input `data` list.

### onRemove

**Signature**
```kotlin
suspend fun onRemove(data: List<CircleEntityInterface<ActualCircle>>)
```

**Description**
Asynchronously removes a batch of circles from the map.

**Parameters**

| Name   | Type                                      | Description                                                              |
| :----- | :---------------------------------------- | :----------------------------------------------------------------------- |
| `data` | `List<CircleEntityInterface<ActualCircle>>` | A list of `CircleEntityInterface` objects representing the circles to be removed. |

### onPostProcess

**Signature**
```kotlin
suspend fun onPostProcess()
```

**Description**
A lifecycle callback that is executed after a batch of `onAdd`, `onChange`, and `onRemove` operations has been completed. This method can be used for cleanup, final rendering adjustments, or other post-processing tasks.

---

## Example

Here is a conceptual example of how to implement `CircleOverlayRendererInterface` for a hypothetical map SDK.

```kotlin
// Assume these are defined elsewhere in the map SDK
// class MapView
// class MapCircle
// data class CircleOptions(val center: LatLng, val radius: Double, val color: Int)
// interface CircleEntityInterface<MapCircle> { val actual: MapCircle?, val state: CircleState }
// data class CircleState(val center: LatLng, val radius: Double, val color: Int)

class MyCircleRenderer(private val mapView: MapView) : CircleOverlayRendererInterface<MapCircle> {

    override suspend fun onAdd(data: List<AddParamsInterface>): List<MapCircle?> {
        return data.map { addParams ->
            // Create circle options from the state
            val options = CircleOptions(
                center = addParams.state.center,
                radius = addParams.state.radius,
                color = addParams.state.color
            )
            // Add the circle to the map and return the native circle object
            mapView.addCircle(options)
        }
    }

    override suspend fun onChange(data: List<ChangeParamsInterface<MapCircle>>): List<MapCircle?> {
        return data.map { changeParams ->
            val circleToUpdate = changeParams.prev.actual
            val newState = changeParams.current.state

            // Update the properties of the existing circle
            circleToUpdate?.apply {
                this.center = newState.center
                this.radius = newState.radius
                this.color = newState.color
            }
            // Return the updated circle
            circleToUpdate
        }
    }

    override suspend fun onRemove(data: List<CircleEntityInterface<MapCircle>>) {
        data.forEach { circleEntity ->
            // Remove the circle from the map
            circleEntity.actual?.removeFromMap()
        }
    }

    override suspend fun onPostProcess() {
        // For example, force a redraw of the map if needed
        mapView.invalidate()
        println("Circle batch processing complete.")
    }
}
```