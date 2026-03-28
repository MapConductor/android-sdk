# ArcGISCircleOverlayRenderer

The `ArcGISCircleOverlayRenderer` class is a concrete implementation for rendering and managing circle graphics on an ArcGIS map. It extends `AbstractCircleOverlayRenderer` and is responsible for translating abstract `CircleState` data into tangible ArcGIS `Graphic` objects on a specified `GraphicsOverlay`.

This renderer handles the entire lifecycle of a circle graphic, including its creation, removal, and property updates. It supports both geodesic (more accurate on a globe) and planar (simpler, faster) circles.

**Note:** The type `ArcGISActualCircle` is an alias for the ArcGIS `Graphic` class (`com.arcgismaps.mapping.view.Graphic`).

## Constructor

### Signature

```kotlin
class ArcGISCircleOverlayRenderer(
    val circleLayer: GraphicsOverlay,
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractCircleOverlayRenderer<ArcGISActualCircle>()
```

### Description

Initializes a new instance of the `ArcGISCircleOverlayRenderer`.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `circleLayer` | `GraphicsOverlay` | The ArcGIS graphics layer where the circle graphics will be added and managed. |
| `holder` | `ArcGISMapViewHolder` | A view holder that provides access to the map view and its context, such as the spatial reference. |
| `coroutine` | `CoroutineScope` | The coroutine scope used for executing asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

---

## Methods

### createCircle

#### Signature

```kotlin
override suspend fun createCircle(state: CircleState): ArcGISActualCircle?
```

#### Description

Creates a new circle graphic based on the provided `CircleState` and adds it to the `GraphicsOverlay`. This function can create either a geodesic or a planar circle. The geometry is generated using `GeometryEngine.bufferGeodeticOrNull` for geodesic circles or `GeometryEngine.bufferOrNull` for planar circles.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `CircleState` | The state object defining the circle's properties, including its center, radius, colors, and whether it is geodesic. |

#### Returns

| Type | Description |
| :--- | :--- |
| `ArcGISActualCircle?` | The newly created ArcGIS `Graphic` object representing the circle, or `null` if the creation process fails. |

---

### removeCircle

#### Signature

```kotlin
override suspend fun removeCircle(entity: CircleEntityInterface<ArcGISActualCircle>)
```

#### Description

Removes a specified circle graphic from the `GraphicsOverlay`. This operation is performed asynchronously on the provided coroutine scope.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `entity` | `CircleEntityInterface<ArcGISActualCircle>` | The circle entity that contains the `Graphic` to be removed from the map. |

#### Returns

This method does not return a value.

---

### updateCircleProperties

#### Signature

```kotlin
override suspend fun updateCircleProperties(
    circle: ArcGISActualCircle,
    current: CircleEntityInterface<ArcGISActualCircle>,
    prev: CircleEntityInterface<ArcGISActualCircle>,
): ArcGISActualCircle?
```

#### Description

Updates the properties of an existing circle graphic. This method efficiently checks for changes between the `current` and `prev` states using their fingerprints. It updates the geometry (center, radius, geodesic type) or the symbol properties (fill color, stroke color, stroke width) only if they have changed, minimizing unnecessary rendering operations.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `circle` | `ArcGISActualCircle` | The actual ArcGIS `Graphic` object that needs to be updated. |
| `current` | `CircleEntityInterface<ArcGISActualCircle>` | The entity representing the new, updated state of the circle. |
| `prev` | `CircleEntityInterface<ArcGISActualCircle>` | The entity representing the previous state of the circle, used for comparison to detect changes. |

#### Returns

| Type | Description |
| :--- | :--- |
| `ArcGISActualCircle?` | The updated `Graphic` object, or `null` if the update fails. |

---

## Example

Here is an example of how to instantiate `ArcGISCircleOverlayRenderer` and use it to draw a circle on the map.

```kotlin
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayRenderer
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.units.Distance
import com.mapconductor.core.units.DistanceUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Assuming you have an ArcGISMapViewHolder instance named 'mapViewHolder'
val mapViewHolder: ArcGISMapViewHolder = /* ... initialize ... */

// 1. Create a GraphicsOverlay to hold the circles
val circleGraphicsOverlay = GraphicsOverlay()
mapViewHolder.mapView.graphicsOverlays.add(circleGraphicsOverlay)

// 2. Instantiate the renderer
val circleRenderer = ArcGISCircleOverlayRenderer(
    circleLayer = circleGraphicsOverlay,
    holder = mapViewHolder
)

// 3. Define the state for a new circle
val circleState = CircleState(
    center = GeoPoint(latitude = 34.0522, longitude = -118.2437), // Los Angeles
    radiusMeters = 1000.0,
    strokeWidth = Distance(2f, DistanceUnit.PX),
    strokeColor = Color.BLUE,
    fillColor = Color.argb(128, 0, 0, 255), // Semi-transparent blue
    geodesic = true
)

// 4. Create the circle on the map within a coroutine
CoroutineScope(Dispatchers.Main).launch {
    val circleGraphic = circleRenderer.createCircle(circleState)
    if (circleGraphic != null) {
        println("Circle created successfully!")
    } else {
        println("Failed to create circle.")
    }
}
```