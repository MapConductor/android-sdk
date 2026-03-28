Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# AbstractViewportStrategy<ActualMarker>

## Signature

```kotlin
abstract class AbstractViewportStrategy<ActualMarker>(
    semaphore: Semaphore,
    geocell: HexGeocellInterface,
) : AbstractMarkerRenderingStrategy<ActualMarker>
```

## Description

An abstract base class for marker rendering strategies that use viewport-based optimization. This class manages the lifecycle of markers, deciding which markers should be rendered based on whether they fall within the current map viewport.

It handles the core logic for adding, updating, and removing markers by comparing incoming data with the current state. The platform-specific rendering operations (e.g., creating the actual map marker object) are delegated to a `MarkerOverlayRendererInterface`. Markers outside the viewport are tracked in the `markerManager` but are not passed to the renderer, saving rendering resources.

Subclasses can extend this class to implement specific rendering behaviors while leveraging the built-in viewport culling logic.

### Generic Parameters

| Name | Description |
| :--- | :--- |
| `ActualMarker` | The platform-specific marker object type (e.g., `GoogleMap.Marker`, `Mapbox.Annotation`). |

## Constructor

### `AbstractViewportStrategy(semaphore, geocell)`

Creates an instance of `AbstractViewportStrategy`.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `semaphore` | `Semaphore` | A coroutine semaphore to ensure thread-safe access to marker collections during rendering operations. |
| `geocell` | `HexGeocellInterface` | The geocell system used for spatial indexing and efficient management of markers. |

## Properties

### `markerManager`

An instance of `MarkerManager` that stores and manages the state of all markers, whether they are currently rendered on the map or not. It uses the provided `geocell` system for spatial organization.

#### Signature

```kotlin
override val markerManager: MarkerManager<ActualMarker>
```

## Methods

### `onAdd`

Processes a list of marker states to add, update, or remove markers from the map. This method implements the core viewport optimization logic. It determines which markers are new, which have been updated, and which are no longer present.

Crucially, it only invokes the `renderer` to draw or update markers that are currently within the visible `viewport`. Markers outside the viewport are registered in the `markerManager` without being rendered, ensuring their state is preserved for when the user pans the map.

#### Signature

```kotlin
override suspend fun onAdd(
    data: List<MarkerState>,
    viewport: GeoRectBounds,
    renderer: MarkerOverlayRendererInterface<ActualMarker>,
): Boolean
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<MarkerState>` | The complete list of marker states that should be displayed. |
| `viewport` | `GeoRectBounds` | The geographical boundaries of the current map viewport. |
| `renderer` | `MarkerOverlayRendererInterface<ActualMarker>` | The renderer responsible for the actual drawing of markers on the map. |

#### Returns

| Type | Description |
| :--- | :--- |
| `Boolean` | Always returns `true`. |

### `onUpdate`

Handles the update of a single marker's state. It first checks if the marker's data has meaningfully changed to avoid unnecessary processing. The marker's state is always updated within the internal `markerManager`. However, the visual rendering via the `renderer` is only triggered if the marker's position is within the current `viewport`.

#### Signature

```kotlin
override suspend fun onUpdate(
    state: MarkerState,
    viewport: GeoRectBounds,
    renderer: MarkerOverlayRendererInterface<ActualMarker>,
): Boolean
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `MarkerState` | The new state for the marker to be updated. |
| `viewport` | `GeoRectBounds` | The geographical boundaries of the current map viewport. |
| `renderer` | `MarkerOverlayRendererInterface<ActualMarker>` | The renderer responsible for updating the marker on the map. |

#### Returns

| Type | Description |
| :--- | :--- |
| `Boolean` | Always returns `true`. |

### `clear`

Removes all markers and clears all internal state from the `markerManager`.

#### Signature

```kotlin
override fun clear()
```

## Example

Since `AbstractViewportStrategy` is an abstract class, you must create a concrete implementation to use it. The primary purpose of a subclass is to provide any specialized logic, though in many cases, simply extending the class is sufficient.

```kotlin
import com.google.android.gms.maps.model.Marker as GoogleMarker
import kotlinx.coroutines.sync.Semaphore
import com.mapconductor.core.geocell.HexGeocellInterface
import com.mapconductor.core.marker.AbstractViewportStrategy

/**
 * A concrete implementation of AbstractViewportStrategy for Google Maps.
 * This class inherits the viewport optimization logic and can be used directly
 * or extended further if custom behavior is needed.
 */
class GoogleMapsViewportStrategy(
    semaphore: Semaphore,
    geocell: HexGeocellInterface
) : AbstractViewportStrategy<GoogleMarker>(semaphore, geocell) {
    // No additional overrides are needed if the default viewport
    // culling logic is sufficient for your use case.
    // You can add custom logic here if required.
}

// Usage:
// val strategy = GoogleMapsViewportStrategy(Semaphore(1), HexGeocell())
// markerController.setStrategy(strategy)
```