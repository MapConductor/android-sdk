# PolygonController<ActualPolygon>

## Description

The `PolygonController` is an abstract base class responsible for managing a collection of polygons on a map. It orchestrates the addition, update, and removal of polygons, handles user interactions like clicks, and synchronizes the polygon state with the map view.

This controller implements a diffing mechanism to efficiently update the map, only applying changes, additions, or removals as needed. It delegates the platform-specific rendering to a `PolygonOverlayRendererInterface` and state management to a `PolygonManagerInterface`.

All data modification operations (`add`, `update`, `clear`) are thread-safe, ensuring that concurrent modifications do not corrupt the state.

### Generic Parameters

| Name | Description |
| :--- | :--- |
| `ActualPolygon` | The concrete polygon type of the underlying map SDK (e.g., `com.google.android.gms.maps.model.Polygon`). |

## Constructor

### Signature

```kotlin
abstract class PolygonController<ActualPolygon>(
    val polygonManager: PolygonManagerInterface<ActualPolygon>,
    open val renderer: PolygonOverlayRendererInterface<ActualPolygon>,
    override var clickListener: OnPolygonEventHandler? = null,
)
```

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `polygonManager` | `PolygonManagerInterface<ActualPolygon>` | An instance that manages the lifecycle and state of polygon entities. |
| `renderer` | `PolygonOverlayRendererInterface<ActualPolygon>` | An instance responsible for the platform-specific drawing of polygons on the map. |
| `clickListener` | `OnPolygonEventHandler?` | An optional global event handler that is invoked when any polygon managed by this controller is clicked. Defaults to `null`. |

## Properties

### clickListener

A global event handler that is invoked when any polygon managed by this controller is clicked. This is called in addition to any specific `onClick` handler defined in the polygon's `PolygonState`.

**Signature**
```kotlin
open var clickListener: OnPolygonEventHandler?
```

### zIndex

The z-index of the polygon layer, which determines its drawing order relative to other map overlays. Higher values are drawn on top.

**Signature**
```kotlin
override val zIndex: Int = 3
```

## Methods

### add

Asynchronously adds, updates, or removes polygons to match the provided list of `PolygonState` objects. The method performs a diffing operation to efficiently determine which polygons are new, which have been modified, and which should be removed. All operations are performed atomically.

**Signature**
```kotlin
override suspend fun add(data: List<PolygonState>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<PolygonState>` | The complete and final list of polygon states to be displayed on the map. |

### update

Asynchronously updates a single polygon based on its new `PolygonState`. If the state has not changed (based on an internal `fingerPrint` comparison), the operation is skipped to improve performance.

**Signature**
```kotlin
override suspend fun update(state: PolygonState)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `PolygonState` | The new state for the polygon to be updated. The polygon is identified by `state.id`. |

### clear

Asynchronously removes all polygons currently managed by this controller from the map.

**Signature**
```kotlin
override suspend fun clear()
```

### find

Finds the topmost polygon entity at a given geographical position. This is useful for identifying which polygon a user has tapped on.

**Signature**
```kotlin
override fun find(position: GeoPointInterface): PolygonEntityInterface<ActualPolygon>?
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPointInterface` | The geographical coordinate to search at. |

**Returns**

| Type | Description |
| :--- | :--- |
| `PolygonEntityInterface<ActualPolygon>?` | The found polygon entity, or `null` if no polygon exists at the specified position. |

### dispatchClick

Dispatches a click event to the appropriate listeners. This method invokes both the polygon-specific `onClick` handler (if defined in its `PolygonState`) and the controller's global `clickListener`.

**Signature**
```kotlin
fun dispatchClick(event: PolygonEvent)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `event` | `PolygonEvent` | The click event object, containing details about the clicked polygon. |

### onCameraChanged

A callback invoked when the map camera position changes. The base implementation is empty and is intended to be overridden by subclasses for custom logic, such as implementing level-of-detail rendering.

**Signature**
```kotlin
override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `mapCameraPosition` | `MapCameraPosition` | An object containing the new camera position, zoom, tilt, and bearing. |

### destroy

Cleans up resources used by the controller. The base implementation is empty, but subclasses should override it to release any native resources or listeners.

**Signature**
```kotlin
override fun destroy()
```

## Example

Here is an example of how to create a concrete implementation of `PolygonController` for Google Maps and use it.

```kotlin
import com.google.android.gms.maps.model.Polygon as GoogleMapPolygon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 1. Define a concrete controller class
class GoogleMapPolygonController(
    polygonManager: PolygonManagerInterface<GoogleMapPolygon>,
    renderer: PolygonOverlayRendererInterface<GoogleMapPolygon>,
    clickListener: OnPolygonEventHandler? = null
) : PolygonController<GoogleMapPolygon>(polygonManager, renderer, clickListener) {

    // Optionally, override methods for custom behavior
    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        super.onCameraChanged(mapCameraPosition)
        // Add custom logic, e.g., simplify polygon shapes on zoom out
        println("Camera changed: zoom=${mapCameraPosition.zoom}")
    }
}

// 2. Instantiate and use the controller
fun setupPolygonController(
    manager: PolygonManagerInterface<GoogleMapPolygon>,
    renderer: PolygonOverlayRendererInterface<GoogleMapPolygon>
) {
    val polygonController = GoogleMapPolygonController(manager, renderer)

    // Define the states for the polygons you want to display
    val polygonState1 = PolygonState(id = "polygon-1", points = listOf(...))
    val polygonState2 = PolygonState(id = "polygon-2", points = listOf(...))
    val allPolygons = listOf(polygonState1, polygonState2)

    // Use a coroutine to add the polygons to the map
    CoroutineScope(Dispatchers.Main).launch {
        polygonController.add(allPolygons)
    }

    // Later, to update a single polygon
    val updatedState1 = polygonState1.copy(fillColor = Color.BLUE)
    CoroutineScope(Dispatchers.Main).launch {
        polygonController.update(updatedState1)
    }

    // To clear all polygons
    CoroutineScope(Dispatchers.Main).launch {
        polygonController.clear()
    }
}
```