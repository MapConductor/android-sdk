Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# HeatmapOverlayState

`HeatmapOverlayState` is a state holder class for the `HeatmapOverlay` composable. It manages the properties of the heatmap and allows for dynamic modifications, automatically triggering re-rendering of the overlay when its properties change.

This class is designed to be used with Jetpack Compose's state management system, typically instantiated using `remember { HeatmapOverlayState(...) }`.

## Signature

```kotlin
class HeatmapOverlayState(
    radiusPx: Int = HeatmapDefaults.DEFAULT_RADIUS_PX,
    opacity: Double = HeatmapDefaults.DEFAULT_OPACITY,
    gradient: HeatmapGradient = HeatmapGradient.DEFAULT,
    maxIntensity: Double? = null,
    weightProvider: (HeatmapPointState) -> Double = { state -> state.weight },
)
```

## Description

This constructor creates a new instance of `HeatmapOverlayState` with the specified initial properties for the heatmap. All parameters have default values, providing a convenient way to create a state object with standard settings.

### Parameters

| Parameter | Type | Description | Default |
|---|---|---|---|
| `radiusPx` | `Int` | The initial radius of each point in pixels. | `HeatmapDefaults.DEFAULT_RADIUS_PX` |
| `opacity` | `Double` | The initial overall opacity of the heatmap layer, from 0.0 (transparent) to 1.0 (opaque). | `HeatmapDefaults.DEFAULT_OPACITY` |
| `gradient` | `HeatmapGradient` | The initial color gradient to use for rendering the heatmap. | `HeatmapGradient.DEFAULT` |
| `maxIntensity` | `Double?` | The initial maximum intensity value for normalization. If `null`, the maximum intensity is calculated automatically from the provided data points. | `null` |
| `weightProvider` | `(HeatmapPointState) -> Double` | A lambda function that extracts a weight value from a `HeatmapPointState`. This weight determines the intensity of the point. | `{ state -> state.weight }` |

## Properties

The properties of `HeatmapOverlayState` can be modified at any time. Changing any of these properties will cause the associated `HeatmapOverlay` to recompose and re-render with the new values.

| Property | Type | Description |
|---|---|---|
| `radiusPx` | `Int` | The radius of each point in pixels. |
| `opacity` | `Double` | The opacity of the heatmap layer (0.0 to 1.0). |
| `gradient` | `HeatmapGradient` | The color gradient used for the heatmap. |
| `maxIntensity` | `Double?` | Optional maximum intensity value for normalization. If `null`, the maximum is calculated from the data. |
| `weightProvider` | `(HeatmapPointState) -> Double` | Function to extract a weight value from a `HeatmapPointState`. |

## Methods

### copy

Creates a new `HeatmapOverlayState` instance, copying the properties from the current state. You can optionally provide new values for any of the properties to create a modified copy.

**Signature**
```kotlin
fun copy(
    radiusPx: Int = this.radiusPx,
    opacity: Double = this.opacity,
    gradient: HeatmapGradient = this.gradient,
    maxIntensity: Double? = this.maxIntensity,
    weightProvider: (HeatmapPointState) -> Double = this.weightProvider,
): HeatmapOverlayState
```

**Parameters**

| Parameter | Type | Description |
|---|---|---|
| `radiusPx` | `Int` | The new radius in pixels. Defaults to the current `radiusPx`. |
| `opacity` | `Double` | The new opacity. Defaults to the current `opacity`. |
| `gradient` | `HeatmapGradient` | The new color gradient. Defaults to the current `gradient`. |
| `maxIntensity` | `Double?` | The new maximum intensity. Defaults to the current `maxIntensity`. |
| `weightProvider` | `(HeatmapPointState) -> Double` | The new weight provider function. Defaults to the current `weightProvider`. |

**Returns**

| Type | Description |
|---|---|
| `HeatmapOverlayState` | A new `HeatmapOverlayState` instance with the specified properties. |

## Example

Here's how to create and use `HeatmapOverlayState` within a composable function.

```kotlin
import androidx.compose.runtime.remember
import com.mapconductor.heatmap.HeatmapOverlay
import com.mapconductor.heatmap.HeatmapOverlayState
import com.mapconductor.heatmap.HeatmapPoint

@Composable
fun MyMapScreen() {
    // 1. Create and remember the state for the heatmap overlay.
    val heatmapState = remember { HeatmapOverlayState() }

    // 2. You can modify properties dynamically based on UI events or other state changes.
    // For example, in response to a slider or button click:
    // heatmapState.radiusPx = 30
    // heatmapState.opacity = 0.5

    // 3. Pass the state to the HeatmapOverlay composable.
    HeatmapOverlay(state = heatmapState) {
        // Assuming 'points' is a list of your data objects
        points.forEach { pointState ->
            HeatmapPoint(pointState)
        }
    }
}
```