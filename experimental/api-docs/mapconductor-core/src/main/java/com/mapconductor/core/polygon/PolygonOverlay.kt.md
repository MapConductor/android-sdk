Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

## `LocalPolygonCollector`

A `CompositionLocal` used to provide a `ChildCollector<PolygonState>` down the Composable tree. This collector is essential for gathering `Polygon` composables declared within a `MapView`.

### Signature

```kotlin
val LocalPolygonCollector: ProvidableCompositionLocal<ChildCollector<PolygonState>>
```

### Description

`LocalPolygonCollector` is a mechanism used internally by the `MapView` to manage `Polygon` children. Any `Polygon` composable must be a direct or indirect child of a `MapView` composable.

If you attempt to use a `Polygon` outside of a `MapView` hierarchy, the app will throw an `IllegalStateException` with the message: "Polygon must be under the <MapView />".

### Example

This shows the correct and incorrect placement of a `Polygon` composable.

```kotlin
// Correct: Polygon is a descendant of MapView
MapView(
    cameraPosition = /* ... */
) {
    Polygon(
        points = listOf(
            LatLng(34.0522, -118.2437),
            LatLng(34.0532, -118.2447),
            LatLng(34.0542, -118.2427)
        )
    )
}

// Incorrect: This will cause a runtime crash
Polygon(
    points = listOf(/* ... */)
)
```

## `PolygonOverlay`

A map overlay class responsible for managing and rendering a collection of polygons on the map. It observes a reactive stream of polygon states and delegates the rendering task to a capable map controller.

### Signature

```kotlin
class PolygonOverlay(
    override val flow: StateFlow<MutableMap<String, PolygonState>>,
) : MapOverlayInterface<PolygonState>
```

### Description

`PolygonOverlay` acts as a layer on the map dedicated to drawing polygons. It subscribes to a `StateFlow` that provides the state of all polygons to be displayed. When the state updates, this class works with the `MapViewControllerInterface` to efficiently update the polygons on the map view, handling additions, removals, and modifications. This class is typically managed by the `MapView` and not instantiated directly by the user.

### Constructor

#### `PolygonOverlay(flow)`

Creates a new instance of the polygon overlay.

| Parameter | Type                                             | Description                                                                                                                            |
| :-------- | :----------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `flow`    | `StateFlow<MutableMap<String, PolygonState>>`    | A reactive stream that emits the current state of all polygons to be displayed. The map's key is a unique ID for the polygon, and the value is its `PolygonState`. |

### Methods

#### `render`

Renders the given polygon data onto the map using the provided controller.

**Signature**
```kotlin
suspend fun render(
    data: MutableMap<String, PolygonState>,
    controller: MapViewControllerInterface,
)
```

**Description**
This function is called by the map rendering engine when polygon updates are needed. It checks if the `controller` implements the `PolygonCapableInterface`. If it does, it passes the list of `PolygonState` objects to the controller's `compositionPolygons` method for native rendering.

**Parameters**

| Parameter    | Type                               | Description                                                                    |
| :----------- | :--------------------------------- | :----------------------------------------------------------------------------- |
| `data`       | `MutableMap<String, PolygonState>` | The current map of polygon states to be rendered.                              |
| `controller` | `MapViewControllerInterface`       | The map view controller responsible for executing rendering commands.          |