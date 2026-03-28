Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# PolylineController<ActualPolyline>

## Description

An abstract base class responsible for managing the lifecycle of polyline overlays on a map. It acts as a coordinator between the data state (`PolylineState`), the state management logic (`PolylineManager`), and the platform-specific rendering (`PolylineOverlayRenderer`).

All public methods that modify the state of polylines (`add`, `update`, `clear`) are thread-safe, using a semaphore to ensure that operations are executed sequentially and prevent race conditions.

This class is designed to be extended by a concrete implementation specific to a map provider (e.g., `GoogleMapPolylineController`).

### Generic Type Parameters

| Parameter | Description |
| :--- | :--- |
| `ActualPolyline` | The platform-specific polyline object type (e.g., `com.google.android.gms.maps.model.Polyline`). |

## Constructor

```kotlin
abstract class PolylineController<ActualPolyline>(
    val polylineManager: PolylineManagerInterface<ActualPolyline>,
    open val renderer: PolylineOverlayRendererInterface<ActualPolyline>,
    override var clickListener: OnPolylineEventHandler? = null,
)
```

Creates a new instance of `PolylineController`.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `polylineManager` | `PolylineManagerInterface<ActualPolyline>` | The manager responsible for storing and querying polyline entities. |
| `renderer` | `PolylineOverlayRendererInterface<ActualPolyline>` | The renderer responsible for drawing and updating polylines on the map. |
| `clickListener` | `OnPolylineEventHandler?` | An optional global listener that is invoked when any polyline managed by this controller is clicked. Defaults to `null`. |

## Properties

| Property | Type | Description |
| :--- | :--- | :--- |
| `polylineManager` | `PolylineManagerInterface<ActualPolyline>` | The manager for polyline state. |
| `renderer` | `PolylineOverlayRendererInterface<ActualPolyline>` | The renderer for drawing polylines. |
| `clickListener` | `OnPolylineEventHandler?` | A global click listener for all polylines managed by this controller. |
| `zIndex` | `Int` | The z-index for the polyline layer, fixed at `5`. |

## Methods

### dispatchClick

Dispatches a click event. This method triggers both the polyline-specific `onClick` handler (defined in `PolylineState`) and the controller's global `clickListener`.

**Signature**
```kotlin
fun dispatchClick(event: PolylineEvent)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `event` | `PolylineEvent` | The polyline event object containing details about the click. |

### add

Adds, updates, or removes polylines to synchronize the map with the provided list of `PolylineState`. The method performs a diff against the current polylines and efficiently applies only the necessary changes (additions, updates, removals) via the `renderer`. This operation is thread-safe.

**Signature**
```kotlin
override suspend fun add(data: List<PolylineState>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<PolylineState>` | The complete list of polyline states that should be displayed on the map. |

### update

Updates a single existing polyline based on the new `PolylineState`. For efficiency, it first checks if the state has actually changed using a fingerprint comparison. If there are changes, it delegates the update to the `renderer`. This operation is thread-safe.

**Signature**
```kotlin
override suspend fun update(state: PolylineState)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `PolylineState` | The new state for the polyline to be updated. |

### clear

Removes all polylines currently managed by this controller from the map. This operation is thread-safe.

**Signature**
```kotlin
override suspend fun clear()
```

### find

Finds the topmost polyline entity at a given geographic coordinate. The search tolerance may depend on the current map camera position (e.g., zoom level).

**Signature**
```kotlin
override fun find(position: GeoPointInterface): PolylineEntityInterface<ActualPolyline>?
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPointInterface` | The geographic coordinates to search at. |

**Returns**

| Type | Description |
| :--- | :--- |
| `PolylineEntityInterface<ActualPolyline>?` | The found polyline entity, or `null` if no polyline is found at the specified position. |

### findWithClosestPoint

Performs a hit test at the given geographic coordinate and returns a detailed result, including the polyline entity and the closest point on that polyline to the given coordinate.

**Signature**
```kotlin
fun findWithClosestPoint(position: GeoPointInterface): PolylineHitResult<ActualPolyline>?
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPointInterface` | The geographic coordinates to search at. |

**Returns**

| Type | Description |
| :--- | :--- |
| `PolylineHitResult<ActualPolyline>?` | A `PolylineHitResult` object containing the entity and closest point, or `null` if no polyline is found. |

### onCameraChanged

Callback method invoked when the map's camera position changes. The controller stores this position to use in calculations for other methods, such as `find`.

**Signature**
```kotlin
override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `mapCameraPosition` | `MapCameraPosition` | The new camera position of the map. |

### destroy

Cleans up resources used by the controller. In this base implementation, this method is empty as there are no specific native resources to release.

**Signature**
```kotlin
override fun destroy()
```

## Example

The following example demonstrates how to create a concrete implementation of `PolylineController` and use it to manage polylines on a map.

```kotlin
// Assume MyMapPolyline is the platform-specific polyline class
// e.g., com.google.android.gms.maps.model.Polyline

// 1. Define a concrete implementation of the PolylineController
class MyMapPolylineController(
    polylineManager: PolylineManagerInterface<MyMapPolyline>,
    renderer: PolylineOverlayRendererInterface<MyMapPolyline>,
    clickListener: OnPolylineEventHandler? = null
) : PolylineController<MyMapPolyline>(polylineManager, renderer, clickListener) {
    // Custom logic for your specific map implementation can be added here
}

// 2. In your map setup code, instantiate the controller with its dependencies
val myPolylineManager = MyPolylineManager() // Your implementation of PolylineManagerInterface
val myPolylineRenderer = MyPolylineRenderer() // Your implementation of PolylineOverlayRendererInterface
val polylineController = MyMapPolylineController(myPolylineManager, myPolylineRenderer)

// 3. Use the controller to manage polylines
suspend fun updatePolylinesOnMap() {
    val polylineStates = listOf(
        PolylineState(id = "route-66", points = listOf(geoPoint1, geoPoint2)),
        PolylineState(id = "scenic-drive", points = listOf(geoPoint3, geoPoint4), color = Color.BLUE)
    )

    // Add the polylines to the map
    polylineController.add(polylineStates)

    // Later, update a single polyline
    val updatedState = PolylineState(id = "route-66", points = listOf(geoPoint1, geoPoint2, geoPoint5))
    polylineController.update(updatedState)

    // Find a polyline at a specific location from a user tap
    val userTapLocation: GeoPointInterface = // ... get from map click event
    val clickedPolyline = polylineController.find(userTapLocation)
    if (clickedPolyline != null) {
        println("User clicked on polyline with ID: ${clickedPolyline.state.id}")
    }

    // When finished, clear all polylines
    polylineController.clear()
}
```