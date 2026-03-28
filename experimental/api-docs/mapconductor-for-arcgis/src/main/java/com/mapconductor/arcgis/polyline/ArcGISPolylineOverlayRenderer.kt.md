Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

# class `ArcGISPolylineOverlayRenderer`

## Description

Manages the rendering lifecycle of polylines on an ArcGIS map. This class acts as a concrete implementation of `AbstractPolylineOverlayRenderer` for the ArcGIS Maps SDK.

It is responsible for creating, updating, and removing polyline graphics from a specified `GraphicsOverlay`. The renderer translates abstract `PolylineState` objects, which define the properties of a polyline, into tangible ArcGIS `Graphic` objects that are displayed on the map.

A key feature of this renderer is its handling of polyline geometry based on the `geodesic` property:
- **Geodesic (`true`):** Renders a true geodesic curve, which represents the shortest path between two points on the surface of the Earth.
- **Non-geodesic (`false`):** Renders a straight line on the 2D map projection (a rhumb line) by performing linear interpolation between the provided vertices. This results in a visually straight line on a flat map view.

## Signature

```kotlin
class ArcGISPolylineOverlayRenderer(
    val polylineLayer: GraphicsOverlay,
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractPolylineOverlayRenderer<ArcGISActualPolyline>()
```

## Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `polylineLayer` | `GraphicsOverlay` | The ArcGIS graphics layer where the polyline graphics will be drawn. |
| `holder` | `ArcGISMapViewHolder` | A view holder that provides access to the map context. |
| `coroutine` | `CoroutineScope` | The coroutine scope used to execute asynchronous rendering operations. Defaults to `CoroutineScope(Dispatchers.Default)`. |

---

## Methods

### `createPolyline`

Creates a new polyline graphic from a `PolylineState` object and adds it to the map's `polylineLayer`.

#### Signature

```kotlin
override suspend fun createPolyline(state: PolylineState): ArcGISActualPolyline?
```

#### Description

This function constructs the geometry and symbol for a new polyline based on the provided state. It sets the polyline's path, color, and width, and then adds the resulting `Graphic` to the graphics overlay. The geometry is created as either geodesic or non-geodesic based on the `state.geodesic` flag.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `PolylineState` | An object containing the properties for the new polyline, including its vertices (`points`), `strokeColor`, `strokeWidth`, and `geodesic` flag. |

#### Returns

| Type | Description |
| :--- | :--- |
| `ArcGISActualPolyline?` | The newly created ArcGIS `Graphic` (type-aliased as `ArcGISActualPolyline`), or `null` if creation fails. |

#### Example

```kotlin
// Assuming 'renderer' is an instance of ArcGISPolylineOverlayRenderer
// and 'polylineState' is a valid PolylineState object.

viewModelScope.launch {
    val newPolylineGraphic = renderer.createPolyline(polylineState)
    if (newPolylineGraphic != null) {
        println("Polyline created and added to the map.")
    } else {
        println("Failed to create polyline.")
    }
}
```

---

### `updatePolylineProperties`

Efficiently updates the properties of an existing polyline graphic by comparing its current and previous states.

#### Signature

```kotlin
override suspend fun updatePolylineProperties(
    polyline: ArcGISActualPolyline,
    current: PolylineEntityInterface<ArcGISActualPolyline>,
    prev: PolylineEntityInterface<ArcGISActualPolyline>,
): ArcGISActualPolyline?
```

#### Description

This function checks for differences between the `current` and `prev` states. It applies changes to the `Graphic`'s geometry (`points`, `geodesic`), color (`strokeColor`), or width (`strokeWidth`) only if they have been modified. This avoids unnecessary redraws and improves performance.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `polyline` | `ArcGISActualPolyline` | The existing ArcGIS `Graphic` to be updated. |
| `current` | `PolylineEntityInterface<ArcGISActualPolyline>` | The entity representing the new, updated state of the polyline. |
| `prev` | `PolylineEntityInterface<ArcGISActualPolyline>` | The entity representing the previous state, used for comparison to detect changes. |

#### Returns

| Type | Description |
| :--- | :--- |
| `ArcGISActualPolyline?` | The updated `Graphic` object. |

---

### `removePolyline`

Asynchronously removes a polyline graphic from the map.

#### Signature

```kotlin
override suspend fun removePolyline(entity: PolylineEntityInterface<ArcGISActualPolyline>)
```

#### Description

This function launches a coroutine to remove the specified polyline's `Graphic` from the `polylineLayer`, effectively deleting it from the map view.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `entity` | `PolylineEntityInterface<ArcGISActualPolyline>` | The polyline entity containing the graphic to be removed. |

#### Returns

This function does not return a value.

#### Example

```kotlin
// Assuming 'renderer' is an instance of ArcGISPolylineOverlayRenderer
// and 'polylineEntity' is the entity to be removed.

viewModelScope.launch {
    renderer.removePolyline(polylineEntity)
    println("Polyline removal has been initiated.")
}
```