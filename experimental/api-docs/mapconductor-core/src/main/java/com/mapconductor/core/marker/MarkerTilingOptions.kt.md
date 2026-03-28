# MarkerTilingOptions

The `MarkerTilingOptions` data class provides configuration for marker tiling optimization.

## Description

When dealing with a large number of static markers, rendering performance can be a concern. Marker tiling is an optimization technique that addresses this by rendering groups of markers as image tiles instead of as individual objects. This significantly reduces the per-marker add/update overhead in the underlying native map SDKs, leading to a smoother user experience.

This class allows you to enable, disable, and fine-tune the behavior of the marker tiling engine.

## Parameters

| Parameter | Type | Description | Default |
| :--- | :--- | :--- | :--- |
| `enabled` | `Boolean` | If `true`, enables the marker tiling optimization. Set to `false` to disable this feature entirely. | `true` |
| `debugTileOverlay` | `Boolean` | If `true`, draws a debug overlay onto each marker tile. The overlay includes top/left border lines and a label containing the tile's z/x/y coordinates and basic rendering statistics. This is useful for debugging caching or scaling artifacts. | `false` |
| `minMarkerCount` | `Int` | The minimum number of markers that must be present on the map before the tiling optimization is activated. | `2000` |
| `cacheSize` | `Int` | The maximum size of the in-memory tile cache in bytes. | `8388608` (8MB) |
| `iconScaleCallback` | `((MarkerState, Int) -> Double)?` | An optional callback function to apply an additional scale multiplier to a marker's icon based on the current map zoom level. The function receives the `MarkerState` and the current `zoom` level and must return a `Double` representing the scale multiplier. The renderer computes the final scale as follows: `effectiveScale = (markerState.icon?.scale ?: 1.0) * (iconScaleCallback?.invoke(markerState, zoom) ?: 1.0)` | `null` |

## Companion Object

### Disabled

A pre-configured instance that completely disables marker tiling.

**Signature**
```kotlin
val Disabled: MarkerTilingOptions
```

**Usage**
```kotlin
val tilingOptions = MarkerTilingOptions.Disabled 
// Equivalent to MarkerTilingOptions(enabled = false)
```

### Default

A pre-configured instance with the default marker tiling settings.

**Signature**
```kotlin
val Default: MarkerTilingOptions
```

**Usage**
```kotlin
val tilingOptions = MarkerTilingOptions.Default
// Equivalent to MarkerTilingOptions()
```

## Example

The following examples demonstrate how to create and use `MarkerTilingOptions`.

```kotlin
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.core.marker.MarkerState

// 1. Use the default tiling options
val defaultOptions = MarkerTilingOptions.Default

// 2. Use a custom configuration
val customOptions = MarkerTilingOptions(
    enabled = true,
    minMarkerCount = 1000,
    cacheSize = 16 * 1024 * 1024, // 16MB cache
    debugTileOverlay = true
)

// 3. Use a custom configuration with a dynamic icon scale callback
// This example makes icons larger at higher zoom levels.
val scalingOptions = MarkerTilingOptions(
    minMarkerCount = 500,
    iconScaleCallback = { markerState: MarkerState, zoom: Int ->
        when {
            zoom > 15 -> 1.5
            zoom > 10 -> 1.2
            else -> 1.0
        }
    }
)

// 4. Disable marker tiling completely
val disabledOptions = MarkerTilingOptions.Disabled
```