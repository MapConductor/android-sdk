Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

---

## LocalGroundImageCollector

A `CompositionLocal` that provides a mechanism for `GroundImage` composables to register themselves with a parent `MapView`.

### Signature

```kotlin
val LocalGroundImageCollector: compositionLocalOf<ChildCollector<GroundImageState>>
```

### Description

`LocalGroundImageCollector` is a Jetpack Compose `CompositionLocal` used internally to manage ground image states within a `MapView`. Its primary purpose is to provide a `ChildCollector<GroundImageState>` down the composable tree.

This allows child `GroundImage` composables to add their state objects to a central collection managed by the `MapView`. If a `GroundImage` is declared outside the scope of a `MapView`, this local will not be provided, leading to a runtime error. This ensures the correct component hierarchy and prevents misconfiguration.

Developers will typically not interact with `LocalGroundImageCollector` directly but will benefit from its presence when using the `GroundImage` composable.

---

## GroundImageOverlay

An overlay class responsible for rendering a collection of ground images on the map.

### Signature

```kotlin
class GroundImageOverlay(
    override val flow: StateFlow<MutableMap<String, GroundImageState>>,
) : MapOverlayInterface<GroundImageState>
```

### Description

`GroundImageOverlay` acts as a bridge between the declarative `GroundImage` composables and the map's imperative rendering engine. It subscribes to a reactive stream (`StateFlow`) of ground image states collected from the Compose UI.

During the map's rendering cycle, this class takes the latest set of ground image states and passes them to the map controller for rendering. This process is only executed if the map controller implements the `GroundImageCapableInterface`, ensuring the controller supports ground image rendering.

### Constructor Parameters

| Parameter | Type                                                 | Description                                                                                                                                                           |
| :-------- | :--------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `flow`    | `StateFlow<MutableMap<String, GroundImageState>>` | A reactive stream that emits the current map of ground image states. The map key is a unique ID for the ground image, and the value is the `GroundImageState` object. |

### Methods

#### render

This function is called by the map's rendering system to draw the collected ground images onto the map.

**Signature**

```kotlin
override suspend fun render(
    data: MutableMap<String, GroundImageState>,
    controller: MapViewControllerInterface,
)
```

**Description**

The `render` method receives the current data and the map controller. It checks if the controller is capable of handling ground images (by casting to `GroundImageCapableInterface`) and then invokes the appropriate rendering command (`compositionGroundImages`) with the list of ground image states.

**Parameters**

| Parameter    | Type                                       | Description                                                                                             |
| :----------- | :----------------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `data`       | `MutableMap<String, GroundImageState>`     | A map containing the latest `GroundImageState` objects to be rendered, keyed by their unique identifiers. |
| `controller` | `MapViewControllerInterface`               | The map controller instance responsible for executing low-level drawing commands on the map view.         |

### Example

The following example demonstrates how `GroundImageOverlay` might be instantiated and used within the map's core architecture.

```kotlin
// Within the MapView setup
val groundImageStateFlow: StateFlow<MutableMap<String, GroundImageState>> = // ... obtain from collector

// Instantiate the overlay with the state flow
val groundImageOverlay = GroundImageOverlay(flow = groundImageStateFlow)

// The map's rendering engine would then use this overlay
// during its render pass. For example:
mapRenderer.addOverlay(groundImageOverlay)
```