Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

# Raster Layer SDK

This document provides detailed information about the `LocalRasterLayerCollector` and `RasterLayerOverlay` components, which are essential for managing and rendering raster data on the map.

## LocalRasterLayerCollector

A `CompositionLocal` that provides a mechanism for `RasterLayer` composables to register themselves with a parent `MapView`.

### Signature

```kotlin
val LocalRasterLayerCollector: ProvidableCompositionLocal<ChildCollector<RasterLayerState>>
```

### Description

`LocalRasterLayerCollector` is a Jetpack Compose `CompositionLocal` that holds an instance of `ChildCollector<RasterLayerState>`. Its primary purpose is to allow child `RasterLayer` composables to pass their state up the composition tree to the `MapView`.

This collector is provided by the `MapView` component. Any attempt to access it from a composable that is not a descendant of `MapView` will result in an `IllegalStateException`.

### Example

While you typically won't interact with this directly, it is used internally by `RasterLayer` composables to register their state.

```kotlin
// Conceptual usage within a RasterLayer composable
@Composable
fun RasterLayer(
    // ... other parameters
) {
    // Access the collector provided by a parent MapView
    val collector = LocalRasterLayerCollector.current
    
    // The RasterLayer composable uses the collector to register its state,
    // making the map aware of its presence and properties.
    DisposableEffect(Unit) {
        val state = RasterLayerState(...)
        val id = collector.addChild(state)
        onDispose {
            collector.removeChild(id)
        }
    }
}
```

## RasterLayerOverlay

An overlay class responsible for managing and rendering a collection of raster layers on the map.

### Signature

```kotlin
class RasterLayerOverlay(
    override val flow: StateFlow<MutableMap<String, RasterLayerState>>,
) : MapOverlayInterface<RasterLayerState>
```

### Description

`RasterLayerOverlay` implements the `MapOverlayInterface` to serve as the bridge between the declarative `RasterLayer` composables and the map's rendering engine. It observes a `StateFlow` of raster layer states and, during the render pass, delegates the actual drawing commands to a compatible map controller.

### Constructor

#### Signature

```kotlin
RasterLayerOverlay(flow: StateFlow<MutableMap<String, RasterLayerState>>)
```

#### Parameters

| Parameter | Type                                                 | Description                                                                                             |
|-----------|------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| `flow`    | `StateFlow<MutableMap<String, RasterLayerState>>`    | A state flow that emits the current collection of raster layers to be displayed, indexed by a unique ID. |

### Methods

#### render

This function is called by the map system to render the raster layers onto the map view.

##### Signature

```kotlin
override suspend fun render(
    data: MutableMap<String, RasterLayerState>,
    controller: MapViewControllerInterface,
)
```

##### Description

The `render` method is invoked during the map's drawing cycle. It receives the current set of `RasterLayerState` data and the active `MapViewControllerInterface`. It then attempts to cast the controller to a `RasterLayerCapableInterface`. If successful, it calls the controller's `compositionRasterLayers` method, passing the list of layer states to be rendered on the map. This effectively delegates the platform-specific rendering logic to the controller.

##### Parameters

| Parameter    | Type                                       | Description                                                                                                                            |
|--------------|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `data`       | `MutableMap<String, RasterLayerState>`     | The most recent map of raster layer states to render. The key is the layer's unique identifier, and the value is the state object.        |
| `controller` | `MapViewControllerInterface`               | The map view controller that manages the map's state and rendering operations. It must conform to `RasterLayerCapableInterface` for rendering to occur. |