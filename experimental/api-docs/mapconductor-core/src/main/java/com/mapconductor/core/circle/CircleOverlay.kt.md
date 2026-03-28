Excellent. Here is the high-quality SDK documentation for the provided code snippet.

***

### `LocalCircleCollector`

**Signature**

```kotlin
val LocalCircleCollector: ProvidableCompositionLocal<ChildCollector<CircleState>>
```

**Description**

A `CompositionLocal` that provides access to a `ChildCollector` for `CircleState` objects. This collector is used internally by circle-related composables (e.g., `<Circle />`) to register their state with the parent `<MapView />` component.

Attempting to use a composable that relies on this collector outside of a `<MapView />` component will result in a runtime error, as the `MapView` is responsible for providing the collector instance.

### `CircleOverlay`

**Signature**

```kotlin
class CircleOverlay(
    override val flow: StateFlow<MutableMap<String, CircleState>>
) : MapOverlayInterface<CircleState>
```

**Description**

`CircleOverlay` is an internal class that manages the rendering of all circle objects on the map. It implements the `MapOverlayInterface` and is responsible for observing a stream of circle states and passing them to the map controller for rendering.

This class is typically instantiated and managed by the `<MapView />` component and is not intended for direct use by developers. It serves as a bridge between the declarative `Circle` composables and the underlying map rendering engine.

#### Constructor Parameters

| Parameter | Type                                                 | Description                                                                                                                            |
|-----------|------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `flow`    | `StateFlow<MutableMap<String, CircleState>>`         | A state flow that emits the current map of circle states. The key is a unique identifier for the circle, and the value is its `CircleState`. |

---

### Methods

#### `render`

**Signature**

```kotlin
override suspend fun render(
    data: MutableMap<String, CircleState>,
    controller: MapViewControllerInterface
)
```

**Description**

This function is called by the map's rendering system to draw the circles on the map. It delegates the rendering task to the provided `controller` by casting it to a `CircleCapableInterface` and invoking its `compositionCircles` method with the latest circle data.

**Note:** This is part of the `MapOverlayInterface` implementation and should not be called directly from application code.

**Parameters**

| Parameter    | Type                               | Description                                                                                             |
|--------------|------------------------------------|---------------------------------------------------------------------------------------------------------|
| `data`       | `MutableMap<String, CircleState>`  | A map containing the state of all circles to be rendered.                                               |
| `controller` | `MapViewControllerInterface`       | The map view controller responsible for executing rendering commands. It must implement `CircleCapableInterface`. |

---

### Example

While `CircleOverlay` and `LocalCircleCollector` are used internally, the following example illustrates how a developer would typically add a circle to a map. This interaction relies on these components behind the scenes.

```kotlin
import androidx.compose.runtime.Composable
import com.mapconductor.core.MapView
import com.mapconductor.core.circle.Circle
import com.mapconductor.geo.LatLng
import androidx.compose.ui.graphics.Color

@Composable
fun MyMapWithCircle() {
    // The MapView component provides the LocalCircleCollector 
    // and uses CircleOverlay to render the collected circles.
    MapView {
        // The Circle composable uses LocalCircleCollector to register its state
        // with the parent MapView.
        Circle(
            center = LatLng(34.0522, -118.2437), // Los Angeles
            radius = 1000.0, // in meters
            strokeColor = Color.Blue,
            fillColor = Color.Blue.copy(alpha = 0.3f)
        )
    }
}
```