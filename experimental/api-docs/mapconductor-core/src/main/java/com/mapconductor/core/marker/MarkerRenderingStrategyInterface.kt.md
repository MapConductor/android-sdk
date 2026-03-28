Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# MarkerRenderingStrategyInterface<ActualMarker>

## Description

The `MarkerRenderingStrategyInterface` defines a contract for handling the rendering and management of markers on a map, especially in response to camera movements. This interface employs a strategy pattern, allowing for different implementations to be created for various map providers (e.g., Google Maps, Mapbox) to ensure optimal performance and behavior.

Implementations of this interface are responsible for deciding which markers to add, remove, or update based on the map's current viewport and camera position. This is crucial for performance-intensive features like marker clustering or culling markers that are outside the visible area.

The generic type `<ActualMarker>` represents the native marker class provided by the underlying map SDK (e.g., `com.google.android.gms.maps.model.Marker`).

## Properties

### markerManager

Provides access to the `MarkerManager` instance that this strategy uses to manage the lifecycle of the actual marker objects on the map.

**Signature**
```kotlin
val markerManager: MarkerManager<ActualMarker>
```

---

## Functions

### clear

Removes all markers currently managed by this strategy from the map. This is typically called when the marker layer is being completely torn down.

**Signature**
```kotlin
fun clear()
```

---

### onAdd

Handles the addition of a new list of markers. The implementation should process this list and decide which markers to render on the map, typically based on whether they fall within the current `viewport`.

**Signature**
```kotlin
suspend fun onAdd(
    data: List<MarkerState>,
    viewport: GeoRectBounds,
    renderer: MarkerOverlayRendererInterface<ActualMarker>,
): Boolean
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<MarkerState>` | A list of `MarkerState` objects representing the markers to be added. |
| `viewport` | `GeoRectBounds` | The current visible geographical bounds of the map. |
| `renderer` | `MarkerOverlayRendererInterface<ActualMarker>` | The renderer responsible for creating and drawing the actual marker objects on the map. |

**Returns**

`Boolean` - Returns `true` if the operation resulted in a change to the rendered markers, `false` otherwise.

---

### onUpdate

Handles the update of a single marker's state. The implementation should find the corresponding marker on the map and update its visual representation if necessary.

**Signature**
```kotlin
suspend fun onUpdate(
    state: MarkerState,
    viewport: GeoRectBounds,
    renderer: MarkerOverlayRendererInterface<ActualMarker>,
): Boolean
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `MarkerState` | The updated state for a single marker. |
| `viewport` | `GeoRectBounds` | The current visible geographical bounds of the map. |
| `renderer` | `MarkerOverlayRendererInterface<ActualMarker>` | The renderer used for updating the marker's visual properties. |

**Returns**

`Boolean` - Returns `true` if the marker was visually updated on the map, `false` otherwise.

---

### onCameraChanged

Handles camera position changes. This is a critical function for performance optimization. The implementation should use the new camera position to update the set of visible markers, such as by adding markers that have entered the viewport and removing those that have left.

**Signature**
```kotlin
suspend fun onCameraChanged(
    cameraPosition: MapCameraPosition,
    renderer: MarkerOverlayRendererInterface<ActualMarker>,
)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `cameraPosition` | `MapCameraPosition` | The new position, zoom, tilt, and bearing of the map camera. |
| `renderer` | `MarkerOverlayRendererInterface<ActualMarker>` | The renderer used for adding or removing markers from the map. |