Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# Interface `MapViewControllerInterface`

## Description

The `MapViewControllerInterface` defines the primary contract for interacting with and controlling a map view. It provides a high-level, platform-agnostic API for managing the map's camera, handling user interaction events like clicks and camera movements, and managing map overlays. Implementations of this interface act as the central controller for all map-related operations.

## Properties

### holder
Provides access to the underlying `MapViewHolderInterface`, which encapsulates the platform-specific map view instance.

**Signature**
```kotlin
val holder: MapViewHolderInterface<*, *>
```

### coroutine
A `CoroutineScope` tied to the lifecycle of the map view. This scope should be used for launching any long-running or asynchronous operations that need to be automatically cancelled when the map view is destroyed, preventing memory leaks and unnecessary work.

**Signature**
```kotlin
val coroutine: CoroutineScope
```

---

## Functions

### clearOverlays
Asynchronously removes all overlays (e.g., markers, polylines, polygons) from the map. As a `suspend` function, it must be called from within a coroutine or another suspend function.

**Signature**
```kotlin
suspend fun clearOverlays()
```

**Example**
```kotlin
// Assuming 'mapViewController' is an instance of MapViewControllerInterface
mapViewController.coroutine.launch {
    // Remove all existing overlays from the map
    mapViewController.clearOverlays()
    println("All overlays have been cleared.")
}
```

---

### setCameraMoveStartListener
Sets a listener that is invoked exactly once when the map camera begins to move. This can be triggered by user gestures (panning, zooming) or programmatic camera updates.

**Signature**
```kotlin
fun setCameraMoveStartListener(listener: OnCameraMoveHandler?)
```

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `listener` | `OnCameraMoveHandler?` | The callback to be invoked when camera movement starts. Pass `null` to remove the existing listener. |

**Example**
```kotlin
mapViewController.setCameraMoveStartListener {
    println("Camera movement has started.")
}

// To remove the listener
mapViewController.setCameraMoveStartListener(null)
```

---

### setCameraMoveListener
Sets a listener that is invoked repeatedly as the camera position changes during movement. This is useful for tracking the camera's state in real-time while it is in motion.

**Signature**
```kotlin
fun setCameraMoveListener(listener: OnCameraMoveHandler?)
```

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `listener` | `OnCameraMoveHandler?` | The callback to be invoked continuously during camera movement. Pass `null` to remove the existing listener. |

**Example**
```kotlin
mapViewController.setCameraMoveListener { cameraPosition ->
    println("Camera is moving. Current zoom: ${cameraPosition.zoom}")
}
```

---

### setCameraMoveEndListener
Sets a listener that is invoked exactly once when the map camera has finished moving and has settled in its new position.

**Signature**
```kotlin
fun setCameraMoveEndListener(listener: OnCameraMoveHandler?)
```

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `listener` | `OnCameraMoveHandler?` | The callback to be invoked when camera movement ends. Pass `null` to remove the existing listener. |

**Example**
```kotlin
mapViewController.setCameraMoveEndListener { finalPosition ->
    println("Camera movement ended at position: ${finalPosition.target}")
}
```

---

### setMapClickListener
Sets a listener that is invoked when the user performs a single tap (click) on the map. The listener receives the geographic coordinates (`MapCoordinate`) of the tapped point.

**Signature**
```kotlin
fun setMapClickListener(listener: OnMapEventHandler?)
```

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `listener` | `OnMapEventHandler?` | The callback to be invoked on a map click. It receives the `MapCoordinate` of the click location. Pass `null` to remove the listener. |

**Example**
```kotlin
mapViewController.setMapClickListener { coordinate ->
    println("Map clicked at Latitude: ${coordinate.latitude}, Longitude: ${coordinate.longitude}")
}
```

---

### setMapLongClickListener
Sets a listener that is invoked when the user performs a long press on the map. The listener receives the geographic coordinates (`MapCoordinate`) of the point that was long-pressed.

**Signature**
```kotlin
fun setMapLongClickListener(listener: OnMapEventHandler?)
```

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `listener` | `OnMapEventHandler?` | The callback to be invoked on a map long click. It receives the `MapCoordinate` of the location. Pass `null` to remove the listener. |

**Example**
```kotlin
mapViewController.setMapLongClickListener { coordinate ->
    println("Map long-pressed at Latitude: ${coordinate.latitude}, Longitude: ${coordinate.longitude}")
}
```

---

### moveCamera
Instantly repositions the map's camera to a specified `MapCameraPosition`. This change is immediate and does not involve any animation.

**Signature**
```kotlin
fun moveCamera(position: MapCameraPosition)
```

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `position` | `MapCameraPosition` | The target camera position, which includes properties like target coordinates, zoom level, tilt, and bearing. |

**Example**
```kotlin
// Define a target location and camera properties
val newYorkCity = MapCoordinate(40.7128, -74.0060)
val cameraPosition = MapCameraPosition(
    target = newYorkCity,
    zoom = 12.0
)

// Instantly move the camera to the new position
mapViewController.moveCamera(cameraPosition)
```

---

### animateCamera
Smoothly animates the map's camera from its current position to a new specified `MapCameraPosition` over a given duration.

**Signature**
```kotlin
fun animateCamera(
    position: MapCameraPosition,
    duration: Long,
)
```

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `position` | `MapCameraPosition` | The destination camera position for the animation. |
| `duration` | `Long` | The duration of the animation in milliseconds. |

**Example**
```kotlin
// Define a target location
val sanFrancisco = MapCoordinate(37.7749, -122.4194)
val cameraPosition = MapCameraPosition(
    target = sanFrancisco,
    zoom = 14.0,
    tilt = 30.0
)

// Animate the camera to the new position over 2 seconds
mapViewController.animateCamera(
    position = cameraPosition,
    duration = 2000L
)
```

---

### registerOverlayController
Registers an `OverlayControllerInterface` with the map view controller. This is used to link controllers for specific types of overlays (e.g., markers, polygons) to the main map controller, allowing for modular management of map elements. The default implementation is an empty function.

**Signature**
```kotlin
fun registerOverlayController(controller: OverlayControllerInterface<*, *, *>)
```

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `controller` | `OverlayControllerInterface<*, *, *>` | The overlay controller instance to register. |

**Example**
```kotlin
// Assuming 'markerController' is an implementation of OverlayControllerInterface
// for managing map markers.
val markerController = MyMarkerController()

// Register the marker controller with the main map view controller
mapViewController.registerOverlayController(markerController)
```