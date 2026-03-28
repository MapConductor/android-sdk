Of course! Here is the high-quality SDK documentation for the provided `CircleController` code snippet.

---

# CircleController<ActualCircle>

## Signature

```kotlin
abstract class CircleController<ActualCircle>(
    val circleManager: CircleManagerInterface<ActualCircle>,
    open val renderer: CircleOverlayRendererInterface<ActualCircle>,
    override var clickListener: OnCircleEventHandler? = null,
) : OverlayControllerInterface<
        CircleState,
        CircleEntityInterface<ActualCircle>,
        CircleEvent,
    >
```

## Description

The `CircleController` is an abstract base class responsible for managing and rendering circle overlays on a map. It acts as a bridge between abstract `CircleState` data and the platform-specific circle objects (`ActualCircle`) displayed on the map.

This controller handles the lifecycle of circles, including adding, updating, and removing them in a batch or individually. It uses a `CircleManagerInterface` to manage the state of circle entities and a `CircleOverlayRendererInterface` to handle the actual platform-specific drawing operations.

All modification operations (`add`, `update`, `clear`) are thread-safe, ensured by an internal semaphore that guarantees atomic execution.

## Generic Parameters

| Parameter      | Description                                                                          |
| :------------- | :----------------------------------------------------------------------------------- |
| `ActualCircle` | The platform-specific circle object type (e.g., `com.google.android.gms.maps.model.Circle`). |

## Constructor

Creates a new instance of `CircleController`.

| Parameter       | Type                                             | Description                                                                                             |
| :-------------- | :----------------------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `circleManager` | `CircleManagerInterface<ActualCircle>`           | An instance that manages the state and entities of the circles.                                         |
| `renderer`      | `CircleOverlayRendererInterface<ActualCircle>`   | An instance responsible for the platform-specific rendering of circles on the map.                      |
| `clickListener` | `OnCircleEventHandler?`                          | An optional global listener that is invoked when any circle managed by this controller is clicked. Defaults to `null`. |

## Properties

| Property        | Type                                           | Description                                                                                             |
| :-------------- | :--------------------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `circleManager` | `CircleManagerInterface<ActualCircle>`         | The manager for circle state and entities.                                                              |
| `renderer`      | `CircleOverlayRendererInterface<ActualCircle>` | The renderer for drawing circles on the map.                                                            |
| `clickListener` | `OnCircleEventHandler?`                        | The global click listener for all circles. Can be set or updated after initialization.                  |
| `zIndex`        | `Int`                                          | The z-index for the circle layer, determining its drawing order relative to other overlays. Defaults to `3`. |
| `semaphore`     | `Semaphore`                                    | A semaphore ensuring that `add`, `update`, and `clear` operations are executed atomically and are thread-safe. |

## Methods

### dispatchClick

Dispatches a click event. This method invokes the specific `onClick` handler defined in the circle's `CircleState` and also calls the controller's global `clickListener`, if one is set.

**Signature**
```kotlin
fun dispatchClick(event: CircleEvent)
```

**Parameters**

| Parameter | Type          | Description                               |
| :-------- | :------------ | :---------------------------------------- |
| `event`   | `CircleEvent` | The circle event object containing click details. |

### add

Synchronizes the circles on the map with the provided list of `CircleState` objects. The method calculates the difference between the current state and the new data, then performs the necessary add, update, and remove operations via the `renderer`. The entire operation is performed atomically.

**Signature**
```kotlin
override suspend fun add(data: List<CircleState>)
```

**Parameters**

| Parameter | Type                | Description                               |
| :-------- | :------------------ | :---------------------------------------- |
| `data`    | `List<CircleState>` | The complete list of circle states to be displayed on the map. |

### update

Updates a single circle on the map based on the provided `CircleState`. To optimize performance, the update is only performed if the new state's "fingerprint" is different from the existing one. This is an atomic operation.

**Signature**
```kotlin
override suspend fun update(state: CircleState)
```

**Parameters**

| Parameter | Type          | Description                                  |
| :-------- | :------------ | :------------------------------------------- |
| `state`   | `CircleState` | The new state for the circle to be updated. |

### clear

Removes all circles managed by this controller from the map. This is an atomic operation.

**Signature**
```kotlin
override suspend fun clear()
```

### find

Finds the topmost circle entity at a given geographic coordinate. This is useful for implementing features like tap-to-select.

**Signature**
```kotlin
override fun find(position: GeoPointInterface): CircleEntityInterface<ActualCircle>?
```

**Parameters**

| Parameter  | Type                | Description                               |
| :--------- | :------------------ | :---------------------------------------- |
| `position` | `GeoPointInterface` | The geographic coordinate to search for a circle. |

**Returns**

`CircleEntityInterface<ActualCircle>?` — The `CircleEntityInterface` at the specified position, or `null` if no circle is found.

### onCameraChanged

A lifecycle callback invoked when the map camera position changes. The base implementation is empty and can be overridden by subclasses to add custom logic, such as level-of-detail rendering.

**Signature**
```kotlin
override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

**Parameters**

| Parameter           | Type                | Description                         |
| :------------------ | :------------------ | :---------------------------------- |
| `mapCameraPosition` | `MapCameraPosition` | The new position of the map camera. |

### destroy

Cleans up resources used by the controller. The base implementation is empty as there are no specific native resources to release at this abstract level. Subclasses should override this method to release any platform-specific resources.

**Signature**
```kotlin
override fun destroy()
```

## Example

Since `CircleController` is an abstract class, you must create a concrete implementation for your specific map platform. The following example demonstrates how you might use a hypothetical concrete implementation, `MapCircleController`.

```kotlin
import kotlinx.coroutines.runBlocking

// Assume MapCircleController is a concrete implementation of CircleController
// and other necessary classes (MapCircleManager, MapCircleRenderer) are defined.
// val mapCircleController: MapCircleController = ...

fun manageMapCircles() = runBlocking {
    // 1. Define the states for the circles you want to display
    val circleOneState = CircleState(
        id = "circle-1",
        center = GeoPoint(40.7128, -74.0060), // New York City
        radius = 1000.0, // in meters
        fillColor = Color.BLUE,
        onClick = { event -> println("Circle ${event.state.id} clicked!") }
    )

    val circleTwoState = CircleState(
        id = "circle-2",
        center = GeoPoint(34.0522, -118.2437), // Los Angeles
        radius = 1500.0,
        fillColor = Color.RED
    )

    // 2. Add the circles to the map
    println("Adding two circles...")
    mapCircleController.add(listOf(circleOneState, circleTwoState))
    
    // ... user interacts with the map ...

    // 3. Update a single circle
    println("Updating circle one...")
    val updatedCircleOneState = circleOneState.copy(
        fillColor = Color.GREEN,
        radius = 1200.0
    )
    mapCircleController.update(updatedCircleOneState)

    // 4. Remove all circles from the map
    println("Clearing all circles...")
    mapCircleController.clear()
}
```