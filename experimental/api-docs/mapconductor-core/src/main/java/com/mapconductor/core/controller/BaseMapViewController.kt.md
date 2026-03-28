Of course! Here is the high-quality SDK documentation for the provided code snippet.

# BaseMapViewController

`BaseMapViewController` is an abstract base class that provides core functionality for managing a map view. It is designed to be extended by concrete implementations for specific map providers (e.g., Google Maps, Mapbox).

This class handles the registration of event listeners for common map interactions, such as camera movements, clicks, and long clicks. It also manages a collection of `OverlayControllerInterface` instances, automatically notifying them of camera changes.

---

## Methods

### setCameraMoveStartListener
Sets a listener that is invoked when the map camera starts moving. This is typically triggered when the user begins a pan or zoom gesture.

**Signature**
```kotlin
fun setCameraMoveStartListener(listener: OnCameraMoveHandler?)
```

**Description**
Registers a callback to be executed when the camera movement on the map begins. To remove the listener, pass `null`.

**Parameters**
| Name | Type | Description |
| :--- | :--- | :--- |
| `listener` | `OnCameraMoveHandler?` | The callback to invoke when the camera starts moving. It receives the `MapCameraPosition` at the start of the movement. |

**Example**
```kotlin
mapViewController.setCameraMoveStartListener { mapCameraPosition ->
    println("Camera move started at position: ${mapCameraPosition.target}")
}

// To remove the listener
mapViewController.setCameraMoveStartListener(null)
```

---

### setCameraMoveListener
Sets a listener that is invoked repeatedly while the map camera is in motion.

**Signature**
```kotlin
fun setCameraMoveListener(listener: OnCameraMoveHandler?)
```

**Description**
Registers a callback that is executed continuously as the camera position changes. This is useful for real-time UI updates that depend on the camera's viewport. To remove the listener, pass `null`.

**Parameters**
| Name | Type | Description |
| :--- | :--- | :--- |
| `listener` | `OnCameraMoveHandler?` | The callback to invoke during camera movement. It receives the current `MapCameraPosition`. |

**Example**
```kotlin
mapViewController.setCameraMoveListener { mapCameraPosition ->
    // Update UI with the new camera position in real-time
    updateCameraInfo(mapCameraPosition)
}

// To remove the listener
mapViewController.setCameraMoveListener(null)
```

---

### setCameraMoveEndListener
Sets a listener that is invoked when the map camera movement has finished.

**Signature**
```kotlin
fun setCameraMoveEndListener(listener: OnCameraMoveHandler?)
```

**Description**
Registers a callback to be executed once the camera has stopped moving. This is ideal for performing actions that should only happen after the user has settled on a new map view, such as fetching data for the new viewport. To remove the listener, pass `null`.

**Parameters**
| Name | Type | Description |
| :--- | :--- | :--- |
| `listener` | `OnCameraMoveHandler?` | The callback to invoke when the camera movement ends. It receives the final `MapCameraPosition`. |

**Example**
```kotlin
mapViewController.setCameraMoveEndListener { mapCameraPosition ->
    println("Camera move ended at position: ${mapCameraPosition.target}")
    // Fetch data for the new viewport
    fetchDataForVisibleRegion(mapCameraPosition)
}

// To remove the listener
mapViewController.setCameraMoveEndListener(null)
```

---

### setMapClickListener
Sets a listener that is invoked when the user clicks or taps on the map.

**Signature**
```kotlin
fun setMapClickListener(listener: OnMapEventHandler?)
```

**Description**
Registers a callback to be executed when a click event occurs on the map. The callback receives the geographical coordinates (`LatLng`) of the click location. To remove the listener, pass `null`.

**Parameters**
| Name | Type | Description |
| :--- | :--- | :--- |
| `listener` | `OnMapEventHandler?` | The callback to invoke on a map click. It receives the `LatLng` of the click location. |

**Example**
```kotlin
mapViewController.setMapClickListener { latLng ->
    println("Map clicked at coordinates: $latLng")
}

// To remove the listener
mapViewController.setMapClickListener(null)
```

---

### setMapLongClickListener
Sets a listener that is invoked when the user long-presses on the map.

**Signature**
```kotlin
fun setMapLongClickListener(listener: OnMapEventHandler?)
```

**Description**
Registers a callback to be executed when a long-click event occurs on the map. The callback receives the geographical coordinates (`LatLng`) of the long-press location. To remove the listener, pass `null`.

**Parameters**
| Name | Type | Description |
| :--- | :--- | :--- |
| `listener` | `OnMapEventHandler?` | The callback to invoke on a map long-click. It receives the `LatLng` of the long-click location. |

**Example**
```kotlin
mapViewController.setMapLongClickListener { latLng ->
    println("Map long-clicked at coordinates: $latLng")
    // For example, add a marker at the long-press location
    addMarkerAt(latLng)
}

// To remove the listener
mapViewController.setMapLongClickListener(null)
```

---

### registerOverlayController
Registers an `OverlayControllerInterface` with the map view controller.

**Signature**
```kotlin
fun registerOverlayController(controller: OverlayControllerInterface<*, *, *>)
```

**Description**
Adds an overlay controller (e.g., for markers, polygons, or other visual elements) to be managed by this `BaseMapViewController`. Registered controllers will be automatically notified of map events, such as camera changes, allowing them to update their state accordingly. A controller will only be registered once, even if this method is called multiple times with the same controller instance.

**Parameters**
| Name | Type | Description |
| :--- | :--- | :--- |
| `controller` | `OverlayControllerInterface<*, *, *>` | The overlay controller to register. |

**Example**
```kotlin
// Assuming you have a custom MarkerController that implements OverlayControllerInterface
val markerController = MarkerController(map)
mapViewController.registerOverlayController(markerController)
```