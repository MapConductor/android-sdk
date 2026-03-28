# MapboxCircleOverlayRenderer

The `MapboxCircleOverlayRenderer` class is a concrete implementation of `AbstractCircleOverlayRenderer` designed specifically for the Mapbox Maps SDK. It is responsible for rendering and managing circle overlays on a Mapbox map. This class translates abstract `CircleEntityInterface` objects into visual `Feature` objects that can be displayed on a map layer, handling their creation, updates, and removal.

## Constructor

### Signature

```kotlin
class MapboxCircleOverlayRenderer(
    val layer: MapboxCircleLayer = MapboxCircleLayer(
        sourceId = "circles-source",
        layerId = "circles-layer",
    ),
    val circleManager: CircleManagerInterface<MapboxActualCircle>,
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractCircleOverlayRenderer<MapboxActualCircle>()
```

### Description

Initializes a new instance of the `MapboxCircleOverlayRenderer`. This renderer links the circle data managed by a `CircleManagerInterface` to a visual representation on the map using a `MapboxCircleLayer`.

### Parameters

| Parameter       | Type                                           | Description                                                                                                                            |
| :-------------- | :--------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `layer`         | `MapboxCircleLayer`                            | The layer configuration that defines the source and layer IDs for rendering circles on the map. Defaults to a standard configuration.    |
| `circleManager` | `CircleManagerInterface<MapboxActualCircle>`   | The manager that holds the state and collection of all circle entities to be rendered.                                                 |
| `holder`        | `MapboxMapViewHolder`                          | The view holder that provides access to the underlying Mapbox `Map` instance.                                                          |
| `coroutine`     | `CoroutineScope`                               | The coroutine scope used to launch asynchronous operations, such as drawing circles on the map. Defaults to `CoroutineScope(Dispatchers.Main)`. |

---

## Methods

### createCircle

#### Signature

```kotlin
override suspend fun createCircle(state: CircleState): MapboxActualCircle?
```

#### Description

Asynchronously creates a new Mapbox `Feature` (which is type-aliased as `MapboxActualCircle`) based on the provided `CircleState`. The method constructs a GeoJSON `Feature` with a `Point` geometry and a `JsonObject` containing all the necessary style properties for rendering the circle, such as radius, colors, stroke width, and z-index. It also calculates a latitude correction factor for geodesic circles to ensure accurate rendering at different latitudes.

#### Parameters

| Parameter | Type          | Description                                                                                             |
| :-------- | :------------ | :------------------------------------------------------------------------------------------------------ |
| `state`   | `CircleState` | An object containing the complete set of properties for the new circle, including its center, radius, and visual styles. |

#### Returns

| Type                 | Description                                                                                                                            |
| :------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `MapboxActualCircle?` | A Mapbox `Feature` representing the newly created circle. The current implementation always returns a valid feature. |

### removeCircle

#### Signature

```kotlin
override suspend fun removeCircle(entity: CircleEntityInterface<MapboxActualCircle>)
```

#### Description

Asynchronously removes a circle's corresponding `Feature` from the map's GeoJSON source. The feature to be removed is identified using a unique ID derived from the `CircleEntityInterface`'s state.

#### Parameters

| Parameter | Type                                     | Description                                                                                             |
| :-------- | :--------------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `entity`  | `CircleEntityInterface<MapboxActualCircle>` | The circle entity to be removed from the map. The renderer uses its ID to find and delete the feature. |

### updateCircleProperties

#### Signature

```kotlin
override suspend fun updateCircleProperties(
    circle: MapboxActualCircle,
    current: CircleEntityInterface<MapboxActualCircle>,
    prev: CircleEntityInterface<MapboxActualCircle>,
): MapboxActualCircle?
```

#### Description

Asynchronously updates the properties of an existing circle. This method functions by creating a completely new `Feature` with the updated properties from the `current` entity state. This new feature is intended to replace the old one in the GeoJSON source, ensuring the visual representation on the map reflects the latest state.

#### Parameters

| Parameter | Type                                     | Description                                                                                             |
| :-------- | :--------------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `circle`  | `MapboxActualCircle`                     | The existing Mapbox `Feature` that is being updated.                                                    |
| `current` | `CircleEntityInterface<MapboxActualCircle>` | The circle entity containing the new, updated state.                                                    |
| `prev`    | `CircleEntityInterface<MapboxActualCircle>` | The circle entity representing the state before the update.                                             |

#### Returns

| Type                 | Description                                                                                                                            |
| :------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `MapboxActualCircle?` | A new Mapbox `Feature` containing the updated properties for the circle. |

### onPostProcess

#### Signature

```kotlin
override suspend fun onPostProcess()
```

#### Description

A lifecycle method invoked after all individual create, update, and remove operations in a given cycle have been processed. It retrieves the complete list of current circle features from the `circleManager` and launches a coroutine to redraw them on the `MapboxCircleLayer`, applying all changes to the map in a single, batched update.

---

## Example

The following example demonstrates how to instantiate the `MapboxCircleOverlayRenderer`. In a real-world application, this renderer would typically be managed by a higher-level component that orchestrates map interactions.

```kotlin
import com.mapconductor.mapbox.circle.MapboxCircleLayer
import com.mapconductor.mapbox.circle.MapboxCircleOverlayRenderer
import com.mapconductor.core.circle.CircleManagerInterface
import com.mapconductor.mapbox.MapboxActualCircle
import com.mapconductor.mapbox.MapboxMapViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

// Assume these dependencies are provided by your application's architecture
val circleManager: CircleManagerInterface<MapboxActualCircle> = getCircleManager()
val mapViewHolder: MapboxMapViewHolder = getMapViewHolder()
val mainScope = CoroutineScope(Dispatchers.Main)

// 1. Define a custom layer configuration (optional)
val customCircleLayer = MapboxCircleLayer(
    sourceId = "my-app-circles-source",
    layerId = "my-app-circles-layer"
)

// 2. Instantiate the renderer
val circleOverlayRenderer = MapboxCircleOverlayRenderer(
    layer = customCircleLayer,
    circleManager = circleManager,
    holder = mapViewHolder,
    coroutine = mainScope
)

// The renderer is now set up. A managing component would typically call its
// methods (createCircle, removeCircle, etc.) in response to data changes.
// For example, adding a new circle to the circleManager would trigger
// a call to circleOverlayRenderer.createCircle(...).
```