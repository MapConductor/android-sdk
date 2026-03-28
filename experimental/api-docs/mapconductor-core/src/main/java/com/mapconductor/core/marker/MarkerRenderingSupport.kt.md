# SDK Documentation

## `MarkerRenderingSupport<ActualMarker>`

### Description

The `MarkerRenderingSupport` interface provides a map-scoped capability for creating and managing marker rendering components. It is designed to be used by plugins, such as marker clustering systems, to create renderers and event controllers for groups of markers. This decouples the marker rendering logic from the main map controller, allowing for a more modular and extensible architecture.

An implementation of this interface acts as a factory for creating the necessary objects to display and interact with markers on the map.

---

### `createMarkerRenderer`

Creates a `MarkerOverlayRendererInterface` responsible for the visual representation of markers on the map. The appearance and behavior of the rendered markers are dictated by the provided rendering strategy.

**Signature**
```kotlin
fun createMarkerRenderer(
    strategy: MarkerRenderingStrategyInterface<ActualMarker>,
): MarkerOverlayRendererInterface<ActualMarker>
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `strategy` | `MarkerRenderingStrategyInterface<ActualMarker>` | The strategy that defines how markers should be displayed. |

**Returns**

| Type | Description |
| :--- | :--- |
| `MarkerOverlayRendererInterface<ActualMarker>` | A new instance of a marker overlay renderer. |

---

### `createMarkerEventController`

Creates a `MarkerEventControllerInterface` to manage user interactions (e.g., clicks, taps) with the markers handled by a specific renderer.

**Signature**
```kotlin
fun createMarkerEventController(
    controller: StrategyMarkerController<ActualMarker>,
    renderer: MarkerOverlayRendererInterface<ActualMarker>,
): MarkerEventControllerInterface<ActualMarker>
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `controller` | `StrategyMarkerController<ActualMarker>` | The high-level strategy controller that manages the marker logic. |
| `renderer` | `MarkerOverlayRendererInterface<ActualMarker>` | The renderer associated with the markers this controller will manage. |

**Returns**

| Type | Description |
| :--- | :--- |
| `MarkerEventControllerInterface<ActualMarker>` | A new instance of a marker event controller. |

---

### `registerMarkerEventController`

Registers a `MarkerEventControllerInterface` with the map system. Once registered, the controller will be active and can start listening for and handling user interaction events on its associated markers.

**Signature**
```kotlin
fun registerMarkerEventController(controller: MarkerEventControllerInterface<ActualMarker>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `controller` | `MarkerEventControllerInterface<ActualMarker>` | The event controller to register. |

---

### `mapLoadedState`

Provides a `StateFlow` that emits the loading status of the map. Observers can collect from this flow to receive updates: `true` indicates the map is fully loaded and ready for interaction, while `false` indicates it is not. This is useful for deferring marker-related operations until the map is initialized.

The default implementation returns `null`, signifying that not all map implementations may provide this state flow.

**Signature**
```kotlin
val mapLoadedState: StateFlow<Boolean>?
```

**Returns**

| Type | Description |
| :--- | :--- |
| `StateFlow<Boolean>?` | A state flow representing the map's loaded state, or `null` if not available. |

---

### `onMarkerRenderingReady`

A lifecycle callback that is invoked when the marker rendering system is fully initialized and ready. Implementations can override this method to perform any necessary setup or initialization tasks that depend on the rendering system being available. This function has an empty default implementation.

**Signature**
```kotlin
fun onMarkerRenderingReady()
```

---

## `MarkerRenderingSupportKey`

### Description

A singleton object that serves as a registry key for looking up an instance of `MarkerRenderingSupport` from the `LocalMapServiceRegistry`. This key is essential for obtaining the map-specific marker rendering capabilities at runtime.

**Signature**
```kotlin
object MarkerRenderingSupportKey : MapServiceKey<MarkerRenderingSupport<*>>
```

### Example

You can use `MarkerRenderingSupportKey` to retrieve the `MarkerRenderingSupport` service from the service registry.

```kotlin
// Assuming 'mapServiceRegistry' is an instance of LocalMapServiceRegistry
val markerRenderingSupport = mapServiceRegistry[MarkerRenderingSupportKey]

// Now you can use the service to create renderers and controllers
if (markerRenderingSupport != null) {
    val renderer = markerRenderingSupport.createMarkerRenderer(myStrategy)
    // ... and so on
}
```