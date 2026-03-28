# AbstractMarkerRenderingStrategy<ActualMarker>

## Signature
```kotlin
abstract class AbstractMarkerRenderingStrategy<ActualMarker>(
    protected val semaphore: Semaphore,
) : MarkerRenderingStrategyInterface<ActualMarker>
```

## Description
`AbstractMarkerRenderingStrategy` is a base class for implementing custom marker rendering logic. It provides a foundational structure, including default no-op implementations for `onAdd` and `onUpdate` methods and a concrete implementation for `clear`.

This class is designed to be extended by concrete strategy implementations (e.g., a clustering strategy, a simple rendering strategy). Subclasses are required to provide their own `MarkerManager` instance, which handles the low-level interaction with the map's marker objects. The `Semaphore` in the constructor is intended to help manage concurrency during rendering operations.

## Generic Type Parameters
| Name | Description |
| :--- | :--- |
| `ActualMarker` | The platform-specific marker object type (e.g., `com.google.android.gms.maps.model.Marker`). |

## Properties

### markerManager
An abstract property that must be implemented by subclasses. It provides a `MarkerManager` instance responsible for the low-level creation, deletion, and management of the actual marker objects on the map.

**Signature**
```kotlin
abstract override val markerManager: MarkerManager<ActualMarker>
```

## Methods

### clear
Removes all markers managed by this strategy from the map. This is achieved by delegating the call to `markerManager.clear()`.

**Signature**
```kotlin
override fun clear()
```

### onAdd
A suspendable lifecycle method called when a collection of markers should be processed and potentially added to the map. The base implementation is a no-op and immediately returns `false`.

Subclasses should override this method to implement their specific logic for adding markers, such as filtering based on the viewport, clustering, or custom rendering logic.

**Signature**
```kotlin
override suspend fun onAdd(
    data: List<MarkerState>,
    viewport: GeoRectBounds,
    renderer: MarkerOverlayRendererInterface<ActualMarker>,
): Boolean
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<MarkerState>` | A list of marker data objects to be potentially rendered. |
| `viewport` | `GeoRectBounds` | The current visible geographical area of the map. |
| `renderer` | `MarkerOverlayRendererInterface<ActualMarker>` | The renderer interface used to perform the actual drawing operations on the map. |

**Returns**

| Type | Description |
| :--- | :--- |
| `Boolean` | Returns `true` if the strategy handled the event, `false` otherwise. The base implementation always returns `false`. |

### onUpdate
A suspendable lifecycle method called when a single marker's state has been updated. The base implementation is a no-op and immediately returns `false`.

Subclasses should override this to handle updates to individual markers, such as changing their position, icon, or other properties.

**Signature**
```kotlin
override suspend fun onUpdate(
    state: MarkerState,
    viewport: GeoRectBounds,
    renderer: MarkerOverlayRendererInterface<ActualMarker>,
): Boolean
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `MarkerState` | The updated state of the marker. |
| `viewport` | `GeoRectBounds` | The current visible geographical area of the map. |
| `renderer` | `MarkerOverlayRendererInterface<ActualMarker>` | The renderer interface used to perform the actual drawing operations on the map. |

**Returns**

| Type | Description |
| :--- | :--- |
| `Boolean` | Returns `true` if the strategy handled the event, `false` otherwise. The base implementation always returns `false`. |

## Example

Since `AbstractMarkerRenderingStrategy` is an abstract class, you cannot instantiate it directly. Instead, you must extend it to create a concrete strategy. The following example demonstrates how to create a simple rendering strategy that adds all markers to the map.

```kotlin
import com.mapconductor.core.marker.*
import com.mapconductor.core.features.GeoRectBounds
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

// Assume 'GoogleMapMarker' is the platform-specific marker type
typealias GoogleMapMarker = com.google.android.gms.maps.model.Marker

/**
 * A simple strategy that renders every marker without any clustering or filtering.
 */
class SimpleRenderingStrategy(
    // You would inject your concrete MarkerManager implementation
    override val markerManager: MarkerManager<GoogleMapMarker>
) : AbstractMarkerRenderingStrategy<GoogleMapMarker>(Semaphore(1)) {

    /**
     * Overrides onAdd to render all markers provided in the data list.
     */
    override suspend fun onAdd(
        data: List<MarkerState>,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRendererInterface<GoogleMapMarker>,
    ): Boolean {
        // Use the semaphore to ensure thread safety during rendering
        semaphore.withPermit {
            // Clear existing markers before adding new ones
            renderer.clear()
            data.forEach { markerState ->
                // Use the renderer to add each marker to the map
                renderer.addMarker(markerState)
            }
        }
        // Return true to indicate the event was handled
        return true
    }

    /**
     * Overrides onUpdate to handle changes to a single marker.
     */
    override suspend fun onUpdate(
        state: MarkerState,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRendererInterface<GoogleMapMarker>,
    ): Boolean {
        semaphore.withPermit {
            // Find and update the specific marker
            renderer.updateMarker(state)
        }
        return true
    }
}
```