# SDK Documentation

This document provides detailed information about the `LocalMarkerCollector` and `MarkerOverlay` components, which are essential for managing and rendering markers on the map.

## `LocalMarkerCollector`

### Signature

```kotlin
val LocalMarkerCollector: ProvidableCompositionLocal<ChildCollector<MarkerState>>
```

### Description

A `CompositionLocal` used to provide a `ChildCollector<MarkerState>` instance down the Composable tree. This mechanism allows individual `Marker` composables to register their state with a parent `MapView` component.

It is crucial that any composable accessing `LocalMarkerCollector` is a descendant of a `MapView` that provides a value for it. Failure to do so will result in an `IllegalStateException` with the message: "Marker must be under the <MapView />".

### Example

While you typically won't interact with `LocalMarkerCollector` directly as an end-user, it is used internally by `Marker` composables to register with the map.

```kotlin
// Hypothetical internal usage within a Marker composable
@Composable
fun Marker(
    // ... marker properties
    state: MarkerState
) {
    // Access the collector provided by a parent MapView
    val collector = LocalMarkerCollector.current
    
    // Use the collector to add or update the marker's state in the map's collection
    LaunchedEffect(state) {
        collector.add(state)
    }
}
```

---

## `MarkerOverlay`

### Signature

```kotlin
class MarkerOverlay(
    override val flow: StateFlow<MutableMap<String, MarkerState>>,
) : MapOverlayInterface<MarkerState>
```

### Description

`MarkerOverlay` is an implementation of the `MapOverlayInterface` specifically designed for handling map markers. It acts as a bridge between the declarative marker state management system and the imperative map controller. It observes a `StateFlow` of marker data and uses a `MarkerCapableInterface` to instruct the map controller to render the markers on the map view.

### Constructor Parameters

| Parameter | Type                                           | Description                                                                                                                            |
|-----------|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `flow`    | `StateFlow<MutableMap<String, MarkerState>>`   | A state flow that emits the current collection of markers to be displayed. The map's key is a unique identifier for the marker, and the value is the `MarkerState` object containing its properties. |

### Methods

#### `render`

##### Signature

```kotlin
override suspend fun render(
    data: MutableMap<String, MarkerState>,
    controller: MapViewControllerInterface,
)
```

##### Description

This function is called by the map's rendering engine to update the visual representation of markers. It receives the latest marker data and the map controller. The method checks if the controller is capable of handling markers (by implementing `MarkerCapableInterface`) and then delegates the rendering task to the controller's `compositionMarkers` method.

##### Parameters

| Parameter    | Type                         | Description                                                                                                                            |
|--------------|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `data`       | `MutableMap<String, MarkerState>` | The most recent map of marker states to be rendered on the map.                                                                        |
| `controller` | `MapViewControllerInterface` | The map view controller responsible for interacting with the underlying map SDK. It must conform to `MarkerCapableInterface` to render markers. |

##### Returns

This is a `suspend` function that does not return a value.