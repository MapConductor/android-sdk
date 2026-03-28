Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

# MapboxPolylineOverlayRenderer

## Class Signature

```kotlin
class MapboxPolylineOverlayRenderer(
    val layer: MapboxPolylineLayer,
    val polylineManager: PolylineManagerInterface<MapboxActualPolyline>,
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolylineOverlayRenderer<MapboxActualPolyline>()
```

## Description

The `MapboxPolylineOverlayRenderer` is a concrete implementation of `AbstractPolylineOverlayRenderer` responsible for rendering and managing polyline overlays on a Mapbox map. It handles the entire lifecycle of a polyline's visual representation, including creation, updates, and removal from the map.

This renderer interacts with a `MapboxPolylineLayer` to draw polylines as features on a GeoJSON source. When a polyline's properties are updated, this class recreates the underlying map feature to reflect the changes, as direct property mutation is not optimal for Mapbox GL sources.

### Constructor

Initializes a new instance of the `MapboxPolylineOverlayRenderer`.

#### Parameters

| Parameter         | Type                                                     | Description                                                                                             |
| ----------------- | -------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `layer`           | `MapboxPolylineLayer`                                    | The target Mapbox layer where the polylines will be rendered.                                           |
| `polylineManager` | `PolylineManagerInterface<MapboxActualPolyline>`         | The manager that holds the state of all polyline entities to be rendered.                               |
| `holder`          | `MapboxMapViewHolder`                                    | A view holder that provides access to the Mapbox map instance.                                          |
| `coroutine`       | `CoroutineScope`                                         | The coroutine scope used for launching asynchronous operations, defaulting to `Dispatchers.Main`.       |

## Methods

### createPolyline

#### Signature

```kotlin
override suspend fun createPolyline(state: PolylineState): MapboxActualPolyline?
```

#### Description

Creates the visual representation of a single polyline on the map based on its state. This method is called by the polyline management system when a new polyline is added. It constructs the necessary Mapbox feature(s) to be drawn on the layer.

The z-index is resolved by first checking `state.zIndex`. If it's `0`, it attempts to use `state.extra` cast as an `Int`.

#### Parameters

| Parameter | Type            | Description                               |
| --------- | --------------- | ----------------------------------------- |
| `state`   | `PolylineState` | The state object containing all polyline properties like points, color, and width. |

#### Returns

| Type                   | Description                                                              |
| ---------------------- | ------------------------------------------------------------------------ |
| `MapboxActualPolyline?` | The newly created Mapbox-specific polyline object, or `null` if creation fails. |

---

### updatePolylineProperties

#### Signature

```kotlin
override suspend fun updatePolylineProperties(
    polyline: MapboxActualPolyline,
    current: PolylineEntityInterface<MapboxActualPolyline>,
    prev: PolylineEntityInterface<MapboxActualPolyline>,
): MapboxActualPolyline?
```

#### Description

Updates a polyline's visual properties. For the Mapbox implementation, this method handles updates by completely recreating the polyline's underlying features rather than modifying them in place. It uses the `current` state to generate a new visual representation.

#### Parameters

| Parameter  | Type                                           | Description                                                              |
| ---------- | ---------------------------------------------- | ------------------------------------------------------------------------ |
| `polyline` | `MapboxActualPolyline`                         | The existing Mapbox polyline object that needs to be updated.            |
| `current`  | `PolylineEntityInterface<MapboxActualPolyline>`| The entity containing the new, updated state of the polyline.            |
| `prev`     | `PolylineEntityInterface<MapboxActualPolyline>`| The entity containing the previous state of the polyline before the update. |

#### Returns

| Type                   | Description                                                              |
| ---------------------- | ------------------------------------------------------------------------ |
| `MapboxActualPolyline?` | The new Mapbox-specific polyline object created to reflect the updated properties. |

---

### removePolyline

#### Signature

```kotlin
override suspend fun removePolyline(entity: PolylineEntityInterface<MapboxActualPolyline>)
```

#### Description

Removes the visual representation of a polyline from the map. It identifies the corresponding features in the `MapboxPolylineLayer`'s source and removes them.

#### Parameters

| Parameter | Type                                           | Description                               |
| --------- | ---------------------------------------------- | ----------------------------------------- |
| `entity`  | `PolylineEntityInterface<MapboxActualPolyline>`| The polyline entity to be removed from the map. |

---

### onPostProcess

#### Signature

```kotlin
override suspend fun onPostProcess()
```

#### Description

A lifecycle callback that is triggered after a batch of create, update, or remove operations has been processed. This implementation uses the callback to trigger a full redraw of all polylines on the associated `MapboxPolylineLayer`, ensuring the map is in a consistent state.

---

## Example

Here is an example of how to instantiate and use the `MapboxPolylineOverlayRenderer`.

```kotlin
import com.mapbox.maps.MapboxMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

// Assume these are your existing, properly initialized components:
val mapboxMap: MapboxMap = getMyMapboxMap()
val myPolylineLayer: MapboxPolylineLayer = MapboxPolylineLayer("my-polyline-layer-id", mapboxMap)
val myPolylineManager: PolylineManagerInterface<MapboxActualPolyline> = getMyPolylineManager()
val myMapViewHolder: MapboxMapViewHolder = MapboxMapViewHolder(mapboxMap)
val mainScope = CoroutineScope(Dispatchers.Main)

// Instantiate the renderer
val polylineRenderer = MapboxPolylineOverlayRenderer(
    layer = myPolylineLayer,
    polylineManager = myPolylineManager,
    holder = myMapViewHolder,
    coroutine = mainScope
)

// The renderer would typically be used by a higher-level manager,
// which would call its methods like createPolyline, updatePolylineProperties, etc.,
// in response to changes in the application's data model.

// For example, when a new polyline needs to be drawn, the manager would call:
// val polylineState = PolylineState(...)
// polylineRenderer.createPolyline(polylineState)

// After processing changes, the manager might call:
// polylineRenderer.onPostProcess()
```