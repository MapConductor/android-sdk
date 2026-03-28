Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

***

## `LocalPolylineCollector`

A `CompositionLocal` that provides access to a collector for polyline states.

### Signature

```kotlin
val LocalPolylineCollector: ProvidableCompositionLocal<ChildCollector<PolylineState>>
```

### Description

`LocalPolylineCollector` is a `CompositionLocal` used to provide a `ChildCollector<PolylineState>` down the Composable tree. Its primary purpose is to allow `Polyline` composables to register their state with the parent `<MapView />`.

It is crucial that any composable utilizing this `CompositionLocal` is a descendant of a `<MapView />` component, which is responsible for providing the collector instance. Failure to do so will result in an `IllegalStateException`.

### Example

The following conceptual example shows how a `Polyline` composable would use `LocalPolylineCollector` to register its state.

```kotlin
@Composable
fun Polyline(
    // Unique identifier for this polyline
    id: String,
    // Other polyline properties like points, color, etc.
    points: List<LatLng>,
    color: Color,
    width: Float
) {
    // Access the collector from the composition
    val collector = LocalPolylineCollector.current

    // Create the state for this polyline
    val polylineState = remember(id, points, color, width) {
        PolylineState(id = id, points = points, color = color, width = width)
    }

    // Use an effect to register and clean up the polyline state
    DisposableEffect(collector, polylineState) {
        collector.add(polylineState)
        onDispose {
            collector.remove(polylineState)
        }
    }
}
```

## `PolylineOverlay` Class

An overlay class responsible for managing and rendering a collection of polylines on the map.

### Signature

```kotlin
class PolylineOverlay(
    override val flow: StateFlow<MutableMap<String, PolylineState>>,
) : MapOverlayInterface<PolylineState>
```

### Description

`PolylineOverlay` implements the `MapOverlayInterface` to serve as a dedicated layer for polylines. It observes a `StateFlow` containing the states of all polylines and delegates the rendering task to a compatible map controller. This class effectively bridges the declarative polyline state with the underlying map view's rendering engine.

### Constructor Parameters

| Parameter | Type                                                 | Description                                                                                                                                                           |
| :-------- | :--------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `flow`    | `StateFlow<MutableMap<String, PolylineState>>` | A state flow that emits a map of polyline states. The map's key is a unique `String` identifier for each polyline, and the value is the `PolylineState` object to be rendered. |

### Methods

#### `render`

Renders the polylines on the map using a compatible controller.

**Signature**

```kotlin
override suspend fun render(
    data: MutableMap<String, PolylineState>,
    controller: MapViewControllerInterface,
)
```

**Description**

This function is invoked by the map's rendering system. It attempts to cast the provided `controller` to a `PolylineCapableInterface`. If the cast is successful, it calls the controller's `compositionPolylines` method, passing the current list of polyline states to be drawn on the map. If the controller does not implement `PolylineCapableInterface`, no polylines will be rendered by this overlay.

**Parameters**

| Parameter    | Type                               | Description                                                                                                                            |
| :----------- | :--------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `data`       | `MutableMap<String, PolylineState>` | A map containing the state of all polylines to be rendered in the current frame.                                                       |
| `controller` | `MapViewControllerInterface`       | The map controller instance. For polylines to be rendered, this controller must implement the `PolylineCapableInterface`. |

**Returns**

This is a `suspend` function and does not return a value.