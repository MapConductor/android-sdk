Of course! Here is the high-quality SDK documentation for the provided `HeatmapPoint` data class.

---

### `HeatmapPoint`

A data class representing a single data point on a heatmap. Each point combines a geographical location with an intensity value (weight).

This class is `Serializable`, allowing its instances to be easily saved to a `Bundle` or transferred across processes.

### Signature

```kotlin
data class HeatmapPoint(
    val position: GeoPointInterface,
    val weight: Double = 1.0
) : Serializable
```

### Description

The `HeatmapPoint` is the fundamental building block for generating a heatmap layer. It encapsulates a specific geographic coordinate (`position`) and its contribution to the heatmap's intensity (`weight`). A collection of these points is typically used to create a `HeatmapProvider`.

The `weight` parameter allows you to represent varying magnitudes at different locations. For example, when visualizing sales data, a location with 10 sales could have a weight of `10.0`, while a location with 1 sale could have a weight of `1.0`. If all points are of equal importance, the default weight of `1.0` can be used.

### Parameters

| Parameter  | Type                | Description                                                                                                                                                           | Required |
| :--------- | :------------------ | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------- |
| `position` | `GeoPointInterface` | The geographical coordinate of the data point. This must be an object that conforms to the `GeoPointInterface`.                                                        | Yes      |
| `weight`   | `Double`            | The intensity or magnitude of the data point. This value influences how "hot" the point appears on the heatmap. Higher values contribute more. **Default**: `1.0`. | No       |

### Example

The following example demonstrates how to create instances of `HeatmapPoint`, both with a default weight and a custom weight.

```kotlin
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.heatmap.HeatmapPoint

// A sample implementation of GeoPointInterface for the example.
data class GeoPoint(override val latitude: Double, override val longitude: Double) : GeoPointInterface

fun main() {
    // Create a list to hold heatmap data points.
    val heatmapData = mutableListOf<HeatmapPoint>()

    // 1. Create a HeatmapPoint with a default weight of 1.0.
    //    Useful when all points have equal importance.
    val pointWithDefaultWeight = HeatmapPoint(
        position = GeoPoint(latitude = 34.0522, longitude = -118.2437) // Los Angeles
    )
    heatmapData.add(pointWithDefaultWeight)
    println("Point 1: Position=${pointWithDefaultWeight.position}, Weight=${pointWithDefaultWeight.weight}")
    // Expected output: Point 1: Position=GeoPoint(latitude=34.0522, longitude=-118.2437), Weight=1.0

    // 2. Create a HeatmapPoint with a custom weight.
    //    Useful for representing data with varying intensity, like sales volume or event counts.
    val pointWithCustomWeight = HeatmapPoint(
        position = GeoPoint(latitude = 40.7128, longitude = -74.0060), // New York City
        weight = 5.5
    )
    heatmapData.add(pointWithCustomWeight)
    println("Point 2: Position=${pointWithCustomWeight.position}, Weight=${pointWithCustomWeight.weight}")
    // Expected output: Point 2: Position=GeoPoint(latitude=40.7128, longitude=-74.006), Weight=5.5

    // This list of HeatmapPoint objects can now be passed to a heatmap provider.
}
```