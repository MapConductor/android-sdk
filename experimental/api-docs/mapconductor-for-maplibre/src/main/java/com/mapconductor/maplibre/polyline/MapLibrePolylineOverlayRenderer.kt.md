Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# MapLibrePolylineOverlayRenderer

## Signature

```kotlin
class MapLibrePolylineOverlayRenderer(
    val layer: MapLibrePolylineLayer,
    val polylineManager: PolylineManagerInterface<MapLibreActualPolyline>,
    override val holder: MapLibreMapViewHolderInterface,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolylineOverlayRenderer<MapLibreActualPolyline>()
```

## Description

The `MapLibrePolylineOverlayRenderer` is a concrete implementation of `AbstractPolylineOverlayRenderer` designed specifically for the MapLibre map framework. It acts as the bridge between the abstract polyline data model and the visual representation on a MapLibre map.

This class is responsible for the entire lifecycle of polyline overlays, including their creation, property updates, and removal. It collaborates with a `MapLibrePolylineLayer` to efficiently draw and manage polylines as features on the map style.

Key behaviors include:
- Translating abstract `PolylineState` into tangible `MapLibreActualPolyline` objects.
- Recreating polylines to apply property updates.
- Managing removal by redrawing the entire set of polylines in a subsequent processing step.

## Parameters

This class is instantiated with the following parameters:

| Parameter         | Type                                                 | Description                                                                                             |
| ----------------- | ---------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `layer`           | `MapLibrePolylineLayer`                              | The layer responsible for drawing the polyline features onto the MapLibre map style.                    |
| `polylineManager` | `PolylineManagerInterface<MapLibreActualPolyline>`   | The manager that holds the state and collection of all polyline entities.                               |
| `holder`          | `MapLibreMapViewHolderInterface`                     | The view holder interface that provides access to the MapLibre map instance and its controller.         |
| `coroutine`       | `CoroutineScope`                                     | The coroutine scope used for launching asynchronous drawing operations. Defaults to `Dispatchers.Main`. |

---

## Methods

### createPolyline

**Signature**
```kotlin
override suspend fun createPolyline(state: PolylineState): MapLibreActualPolyline?
```

**Description**
Creates a new `MapLibreActualPolyline` from a given `PolylineState`. This method translates the abstract state (points, color, width, etc.) into a concrete polyline feature that can be rendered on the map. The `zIndex` is resolved from the state's `zIndex` property or an optional `extra` integer property.

**Parameters**
| Parameter | Type            | Description                                        |
| --------- | --------------- | -------------------------------------------------- |
| `state`   | `PolylineState` | The data object containing the properties for the new polyline. |

**Returns**
| Type                       | Description                                                              |
| -------------------------- | ------------------------------------------------------------------------ |
| `MapLibreActualPolyline?`  | The newly created polyline object, or `null` if creation was unsuccessful. |

---

### updatePolylineProperties

**Signature**
```kotlin
override suspend fun updatePolylineProperties(
    polyline: MapLibreActualPolyline,
    current: PolylineEntityInterface<MapLibreActualPolyline>,
    prev: PolylineEntityInterface<MapLibreActualPolyline>,
): MapLibreActualPolyline?
```

**Description**
Updates an existing polyline's properties. This implementation handles updates by completely recreating the polyline feature with the new properties defined in the `current` state.

**Parameters**
| Parameter  | Type                                                 | Description                                                              |
| ---------- | ---------------------------------------------------- | ------------------------------------------------------------------------ |
| `polyline` | `MapLibreActualPolyline`                             | The actual polyline object on the map that needs to be updated.          |
| `current`  | `PolylineEntityInterface<MapLibreActualPolyline>`    | The entity containing the new, updated state for the polyline.           |
| `prev`     | `PolylineEntityInterface<MapLibreActualPolyline>`    | The entity containing the previous state of the polyline before the update. |

**Returns**
| Type                       | Description                                                              |
| -------------------------- | ------------------------------------------------------------------------ |
| `MapLibreActualPolyline?`  | The new polyline object that replaces the old one, or `null` on failure. |

---

### removePolyline

**Signature**
```kotlin
override suspend fun removePolyline(entity: PolylineEntityInterface<MapLibreActualPolyline>)
```

**Description**
Marks a polyline for removal. Note that this method does not immediately remove the polyline from the map. Instead, the actual removal occurs during the `onPostProcess` step, where all remaining polylines are redrawn, effectively excluding the one marked for removal.

**Parameters**
| Parameter | Type                                              | Description                               |
| --------- | ------------------------------------------------- | ----------------------------------------- |
| `entity`  | `PolylineEntityInterface<MapLibreActualPolyline>` | The polyline entity to be removed.        |

---

### onPostProcess

**Signature**
```kotlin
override suspend fun onPostProcess()
```

**Description**
This method is called after a batch of create, update, or remove operations. It synchronizes the visual state of the map with the current data model by fetching all active polyline entities from the `polylineManager` and instructing the `MapLibrePolylineLayer` to draw them. This is the step where removals are finalized and all changes become visible.

---

### redraw

**Signature**
```kotlin
fun redraw()
```

**Description**
Manually triggers a full redraw of all polylines managed by the associated `polylineManager`. This is a utility function that can be called to force a refresh of the polyline layer at any time, ensuring the map's visual state is perfectly synchronized with the data.

---

## Example

```kotlin
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

// Assume these are your mock or actual implementations
val mockMapLibreLayer = mock<MapLibrePolylineLayer>()
val mockPolylineManager = mock<PolylineManagerInterface<MapLibreActualPolyline>>()
val mockMapViewHolder = mock<MapLibreMapViewHolderInterface>()
val mockStyle = mock<Style>()

// Set up mocks to return necessary objects
whenever(mockMapViewHolder.map.style).thenReturn(mockStyle)
whenever(mockPolylineManager.allEntities()).thenReturn(listOf(/* ... your polyline entities ... */))

// 1. Instantiate the renderer
val polylineRenderer = MapLibrePolylineOverlayRenderer(
    layer = mockMapLibreLayer,
    polylineManager = mockPolylineManager,
    holder = mockMapViewHolder,
    coroutine = CoroutineScope(Dispatchers.Main)
)

// 2. Create a new polyline state
val newState = PolylineState(
    id = "polyline-1",
    points = listOf(
        Point.fromLngLat(-122.4194, 37.7749),
        Point.fromLngLat(-122.425, 37.78)
    ),
    strokeColor = Color.BLUE,
    strokeWidth = 5.0f
)

// 3. Use the renderer to create the polyline (typically called by a manager)
val actualPolyline = runBlocking {
    polylineRenderer.createPolyline(newState)
}

// 4. Manually trigger a redraw to display all polylines on the map
polylineRenderer.redraw()

// The `redraw` function will internally call layer.draw(...)
// to render all polylines from the polylineManager onto the map.
verify(mockMapLibreLayer).draw(any(), eq(mockStyle))
```