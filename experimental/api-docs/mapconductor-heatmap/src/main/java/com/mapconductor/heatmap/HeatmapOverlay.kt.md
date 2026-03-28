# HeatmapOverlay

The `HeatmapOverlay` composable is used to create and display a heatmap layer on a map. It visualizes the geographic density of data points, where areas with more points or points with higher weights appear "hotter" according to a specified color gradient.

This composable must be used as a direct or indirect child of a `MapView`. You define the data for the heatmap by placing `HeatmapPoint` composables within the `content` lambda of the `HeatmapOverlay`.

There are two overloads for this composable: a simple version that accepts a `HeatmapOverlayState` object, and a more detailed version that allows for fine-grained control over each property.

---

## HeatmapOverlay (State-based)

This is a convenience overload that uses a `HeatmapOverlayState` object to configure the heatmap's appearance and behavior. It is the recommended approach when you need to manage the heatmap's properties externally as a single, observable state object.

### Signature

```kotlin
@Composable
fun MapViewScope.HeatmapOverlay(
    state: HeatmapOverlayState,
    content: @Composable () -> Unit,
)
```

### Description

This function renders a heatmap layer configured by the provided `state` object. The data points for the heatmap are defined by the `HeatmapPoint` composables placed within the `content` block.

### Parameters

| Name      | Type                      | Description                                                                                                                                                           |
| :-------- | :------------------------ | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `state`   | `HeatmapOverlayState`     | The state object that holds the configuration for the heatmap, including properties like `radiusPx`, `opacity`, `gradient`, `maxIntensity`, and `weightProvider`.         |
| `content` | `@Composable () -> Unit`  | A composable lambda where `HeatmapPoint` children are placed. These points constitute the data used to generate the heatmap.                                            |

### Example

This example demonstrates creating a heatmap and controlling its properties using a `HeatmapOverlayState`.

```kotlin
import androidx.compose.runtime.Composable
import com.mapconductor.heatmap.HeatmapOverlay
import com.mapconductor.heatmap.HeatmapPoint
import com.mapconductor.heatmap.rememberHeatmapOverlayState
import com.mapconductor.core.MapView
import com.mapconductor.core.types.LatLng

@Composable
fun HeatmapWithStateExample() {
    // Create and remember the state for the heatmap
    val heatmapState = rememberHeatmapOverlayState(
        radiusPx = 35,
        opacity = 0.75
    )

    MapView {
        HeatmapOverlay(state = heatmapState) {
            // Define the data points for the heatmap
            HeatmapPoint(
                position = LatLng(34.0522, -118.2437), // Los Angeles
                weight = 0.8
            )
            HeatmapPoint(
                position = LatLng(34.0533, -118.2448), // Nearby point
                weight = 1.0
            )
            HeatmapPoint(
                position = LatLng(40.7128, -74.0060)  // New York
            ) // Uses default weight of 1.0
        }
    }
}
```

---

## HeatmapOverlay (Detailed)

This overload provides fine-grained control over all heatmap properties directly as parameters. It is useful for heatmaps with static configurations or when you prefer to manage properties individually.

### Signature

```kotlin
@Composable
fun MapViewScope.HeatmapOverlay(
    radiusPx: Int = HeatmapDefaults.DEFAULT_RADIUS_PX,
    opacity: Double = HeatmapDefaults.DEFAULT_OPACITY,
    gradient: HeatmapGradient = HeatmapGradient.DEFAULT,
    maxIntensity: Double? = null,
    weightProvider: (HeatmapPointState) -> Double = { state -> state.weight },
    tileSize: Int = HeatmapTileRenderer.DEFAULT_TILE_SIZE,
    trackPointUpdates: Boolean = false,
    disableTileServerCache: Boolean = false,
    content: @Composable () -> Unit,
)
```

### Description

This function creates a heatmap layer by rendering data points onto tiles and displaying them as a raster overlay. It offers detailed configuration options for the heatmap's appearance and performance characteristics.

### Parameters

| Name                     | Type                               | Description                                                                                                                                                                                                                         | Default                                                              |
| :----------------------- | :--------------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------- |
| `radiusPx`               | `Int`                              | The radius of influence for each data point, in pixels. Larger values create a smoother, more "blurry" heatmap.                                                                                                                      | `HeatmapDefaults.DEFAULT_RADIUS_PX`                                  |
| `opacity`                | `Double`                           | The overall opacity of the heatmap layer, from `0.0` (fully transparent) to `1.0` (fully opaque).                                                                                                                                    | `HeatmapDefaults.DEFAULT_OPACITY`                                    |
| `gradient`               | `HeatmapGradient`                  | The color gradient used to render the heatmap based on data intensity.                                                                                                                                                              | `HeatmapGradient.DEFAULT`                                            |
| `maxIntensity`           | `Double?`                          | The intensity value that maps to the "hottest" color in the gradient. If `null`, the maximum intensity is calculated automatically from the data. Set a fixed value to ensure a consistent color scale across different datasets.      | `null`                                                               |
| `weightProvider`         | `(HeatmapPointState) -> Double`    | A lambda function to extract a numerical weight from a `HeatmapPointState`. The default implementation returns the point's `weight` property. This allows for custom logic to determine the influence of each point.                  | `{ state -> state.weight }`                                          |
| `tileSize`               | `Int`                              | The size of the underlying raster tiles in pixels.                                                                                                                                                                                  | `HeatmapTileRenderer.DEFAULT_TILE_SIZE`                              |
| `trackPointUpdates`      | `Boolean`                          | If `true`, the heatmap re-renders when properties of existing `HeatmapPoint`s (e.g., `weight`) change. If `false`, it only updates when points are added or removed. Enabling this may impact performance.                             | `false`                                                              |
| `disableTileServerCache` | `Boolean`                          | If `true`, disables the internal tile server's in-memory cache. This is primarily for debugging and can degrade performance.                                                                                                        | `false`                                                              |
| `content`                | `@Composable () -> Unit`           | A composable lambda where `HeatmapPoint` children are placed. These points constitute the data used to generate the heatmap.                                                                                                         | -                                                                    |

### Example

This example shows how to create a heatmap with custom properties set directly in the composable.

```kotlin
import androidx.compose.runtime.Composable
import com.mapconductor.heatmap.HeatmapGradient
import com.mapconductor.heatmap.HeatmapOverlay
import com.mapconductor.heatmap.HeatmapPoint
import com.mapconductor.core.MapView
import com.mapconductor.core.types.LatLng

@Composable
fun DetailedHeatmapExample() {
    MapView {
        HeatmapOverlay(
            radiusPx = 50,
            opacity = 0.8,
            gradient = HeatmapGradient.DEFAULT, // Or provide a custom HeatmapGradient
            trackPointUpdates = false
        ) {
            // Add data points for the heatmap
            HeatmapPoint(
                position = LatLng(40.7128, -74.0060), // New York
                weight = 1.0
            )
            HeatmapPoint(
                position = LatLng(34.0522, -118.2437), // Los Angeles
                weight = 0.7
            )
            HeatmapPoint(
                position = LatLng(41.8781, -87.6298) // Chicago
            ) // Uses default weight of 1.0
        }
    }
}
```