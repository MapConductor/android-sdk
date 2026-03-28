Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# MapLibreCircleOverlayRenderer

## Description

The `MapLibreCircleOverlayRenderer` is a concrete implementation of `AbstractCircleOverlayRenderer` responsible for rendering and managing circle overlays on a MapLibre map. It acts as a bridge between the abstract `CircleState` provided by the core framework and the tangible `Feature` objects that MapLibre uses to display circles on a map layer.

This renderer handles the creation, update, and removal of circles by translating their properties (such as center, radius, color, and stroke) into MapLibre-compatible feature properties. It works in conjunction with a `MapLibreCircleLayer` to perform the final drawing operations on the map.

### Constructor

```kotlin
class MapLibreCircleOverlayRenderer(
    val layer: MapLibreCircleLayer,
    val circleManager: CircleManagerInterface<MapLibreActualCircle>,
    override val holder: MapLibreMapViewHolderInterface,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractCircleOverlayRenderer<MapLibreActualCircle>()
```

Initializes a new instance of the `MapLibreCircleOverlayRenderer`.

### Parameters

| Parameter       | Type                                       | Description                                                                                                |
| :-------------- | :----------------------------------------- | :--------------------------------------------------------------------------------------------------------- |
| `layer`         | `MapLibreCircleLayer`                      | The layer responsible for drawing the circle features on the map.                                          |
| `circleManager` | `CircleManagerInterface<MapLibreActualCircle>` | The manager that holds the state of all circle entities.                                                   |
| `holder`        | `MapLibreMapViewHolderInterface`           | The view holder interface providing access to the map instance and its controller.                         |
| `coroutine`     | `CoroutineScope`                           | The coroutine scope used for launching asynchronous operations, defaulting to `Dispatchers.Main`.            |

---

## Methods

### createCircle

```kotlin
override suspend fun createCircle(state: CircleState): MapLibreActualCircle?
```

Creates a new MapLibre `Feature` that represents a circle on the map. This method translates the properties from the abstract `CircleState` into a concrete GeoJSON `Feature` with properties that the `MapLibreCircleLayer` can interpret and render.

For geodesic circles, it calculates a latitude correction factor to ensure the circle's radius is rendered accurately in meters across different latitudes.

#### Parameters

| Parameter | Type          | Description                                        |
| :-------- | :------------ | :------------------------------------------------- |
| `state`   | `CircleState` | The state object containing the circle's properties. |

#### Returns

| Type                   | Description                                                                                             |
| :--------------------- | :------------------------------------------------------------------------------------------------------ |
| `MapLibreActualCircle?` | A MapLibre `Feature` representing the circle, or `null` if creation fails. This is a typealias for `Feature`. |

---

### updateCircleProperties

```kotlin
override suspend fun updateCircleProperties(
    circle: MapLibreActualCircle,
    current: CircleEntityInterface<MapLibreActualCircle>,
    prev: CircleEntityInterface<MapLibreActualCircle>,
): MapLibreActualCircle?
```

Updates the properties of an existing circle by creating a new `Feature` with the latest state. The underlying rendering mechanism replaces the old feature with this new one.

#### Parameters

| Parameter | Type                                         | Description                                           |
| :-------- | :------------------------------------------- | :---------------------------------------------------- |
| `circle`  | `MapLibreActualCircle`                       | The actual MapLibre `Feature` object to be updated.   |
| `current` | `CircleEntityInterface<MapLibreActualCircle>` | The entity wrapper containing the new, updated state. |
| `prev`    | `CircleEntityInterface<MapLibreActualCircle>` | The entity wrapper containing the previous state.     |

#### Returns

| Type                   | Description                                                                                             |
| :--------------------- | :------------------------------------------------------------------------------------------------------ |
| `MapLibreActualCircle?` | A new MapLibre `Feature` with the updated properties, or `null` if the update fails. This is a typealias for `Feature`. |

---

### removeCircle

```kotlin
override suspend fun removeCircle(entity: CircleEntityInterface<MapLibreActualCircle>)
```

Handles the removal of a circle. This method is intentionally empty. Circle removal is managed implicitly by the `onPostProcess` method, which redraws the entire layer using only the currently active circles from the `circleManager`. Any circle marked for removal will simply be excluded from the next draw cycle.

#### Parameters

| Parameter | Type                                         | Description                                |
| :-------- | :------------------------------------------- | :----------------------------------------- |
| `entity`  | `CircleEntityInterface<MapLibreActualCircle>` | The circle entity scheduled for removal. |

---

### onPostProcess

```kotlin
override suspend fun onPostProcess()
```

This method is called after all individual create, update, and remove operations in a render pass are complete. It fetches the complete list of current circle features from the `circleManager` and instructs the `MapLibreCircleLayer` to draw them on the map. This ensures that all changes are batched and rendered together efficiently.

---

## Example

The following example demonstrates how to set up and instantiate the `MapLibreCircleOverlayRenderer` within a typical application structure.

```kotlin
import com.mapconductor.maplibre.circle.MapLibreCircleLayer
import com.mapconductor.maplibre.circle.MapLibreCircleOverlayRenderer
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.maplibre.MapLibreMapViewHolderInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.maplibre.maps.Style

// Assume these dependencies are provided by your application
val mapViewHolder: MapLibreMapViewHolderInterface = // ... get your map view holder
val style: Style = // ... get the map's style instance
val mainScope = CoroutineScope(Dispatchers.Main)

// 1. Initialize the layer that will draw the circles
val circleLayer = MapLibreCircleLayer(layerId = "my-circle-layer")

// 2. Initialize the manager to hold circle data
val circleManager = CircleManager()

// 3. Create the renderer instance
val circleRenderer = MapLibreCircleOverlayRenderer(
    layer = circleLayer,
    circleManager = circleManager,
    holder = mapViewHolder,
    coroutine = mainScope
)

// The circleRenderer is now set up. The broader map framework would
// typically call its methods (createCircle, onPostProcess, etc.)
// automatically as you add, update, or remove circles via the circleManager.
```