Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# Heatmap Points

These composable functions are used to add data points to a `HeatmapOverlay`. Each point has a geographical position and an optional weight that determines its intensity on the heatmap.

All `HeatmapPoint` and `HeatmapPoints` composables must be placed within the content of a `<HeatmapOverlay />` to function correctly.

### HeatmapPoint

Adds a single, weighted geographical point to the heatmap. This is the standard method for adding individual points declaratively. The point is automatically added to the map when the composable enters the composition and removed when it leaves.

#### Signature
```kotlin
@Composable
fun MapViewScope.HeatmapPoint(
    position: GeoPointInterface,
    weight: Double = 1.0,
    id: String? = null,
    extra: Serializable? = null,
)
```

#### Description
Use this composable to represent a single data point on the heatmap. You can specify its location, intensity (weight), and optional metadata. For rendering a large number of points, consider using the `HeatmapPoints` composable for better performance.

#### Parameters

| Parameter  | Type                | Default | Description                                                                                             |
|------------|---------------------|---------|---------------------------------------------------------------------------------------------------------|
| `position` | `GeoPointInterface` | -       | The geographical location of the heatmap point.                                                         |
| `weight`   | `Double`            | `1.0`   | The intensity of the point. Higher values contribute more to the heatmap's "heat" in that area.         |
| `id`       | `String?`           | `null`  | An optional unique identifier for the point. Useful for managing or referencing specific points.        |
| `extra`    | `Serializable?`     | `null`  | Optional, serializable data to associate with the point. Can be used to store custom metadata.          |

#### Example

Here's how to add two distinct heatmap points inside a `HeatmapOverlay`.

```kotlin
import com.mapconductor.heatmap.HeatmapOverlay
import com.mapconductor.heatmap.HeatmapPoint
import com.mapconductor.geo.GeoPoint // Assuming a GeoPoint implementation

// ...

MapView {
    HeatmapOverlay {
        // Add a point with default weight
        HeatmapPoint(
            position = GeoPoint(40.7128, -74.0060) // New York City
        )

        // Add another point with a higher weight
        HeatmapPoint(
            position = GeoPoint(34.0522, -118.2437), // Los Angeles
            weight = 3.5,
            id = "la-point"
        )
    }
}
```

---

### HeatmapPoints

Efficiently renders a list of heatmap points. This composable is optimized for large datasets, as it updates the entire collection of points in a single operation, avoiding the performance overhead of adding each point individually.

#### Signature
```kotlin
@Composable
fun MapViewScope.HeatmapPoints(states: List<HeatmapPointState>)
```

#### Description
When you have a dynamic or large list of points, use `HeatmapPoints` to render them. On its initial composition and whenever the `states` list changes, it replaces all existing points in the overlay with the new list. When this composable leaves the composition, it automatically clears all points it was managing.

> **Note:** This function replaces the entire set of points on the map. It does not append to any existing points added by other `HeatmapPoint` or `HeatmapPoints` composables.

#### Parameters

| Parameter | Type                      | Description                                                              |
|-----------|---------------------------|--------------------------------------------------------------------------|
| `states`  | `List<HeatmapPointState>` | The complete list of `HeatmapPointState` objects to display on the heatmap. |

#### Example

This example demonstrates how to display a list of points fetched from a ViewModel.

```kotlin
import com.mapconductor.heatmap.HeatmapOverlay
import com.mapconductor.heatmap.HeatmapPoints
import com.mapconductor.heatmap.HeatmapPointState
import com.mapconductor.geo.GeoPoint // Assuming a GeoPoint implementation

// In your ViewModel or state holder
val heatmapData = remember {
    mutableStateOf(
        listOf(
            HeatmapPointState(position = GeoPoint(48.8566, 2.3522), weight = 2.0), // Paris
            HeatmapPointState(position = GeoPoint(51.5074, -0.1278), weight = 1.5), // London
            HeatmapPointState(position = GeoPoint(41.9028, 12.4964)) // Rome (default weight 1.0)
        )
    )
}

// In your Composable
MapView {
    HeatmapOverlay {
        HeatmapPoints(states = heatmapData.value)
    }
}
```

---

### HeatmapPoint (State-based)

A lower-level composable that adds a single heatmap point using a pre-constructed `HeatmapPointState` object.

#### Signature
```kotlin
@Composable
fun MapViewScope.HeatmapPoint(state: HeatmapPointState)
```

#### Description
This variant is typically used internally or for advanced use cases where you manage the `HeatmapPointState` directly. For most scenarios, the overload that accepts individual parameters (`position`, `weight`, etc.) is more convenient.

#### Parameters

| Parameter | Type                | Description                                      |
|-----------|---------------------|--------------------------------------------------|
| `state`   | `HeatmapPointState` | The state object representing the heatmap point. |