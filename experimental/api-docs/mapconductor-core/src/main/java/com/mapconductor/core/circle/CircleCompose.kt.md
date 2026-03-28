Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

### `Circle`

The `Circle` composable adds a circular overlay to the map. It is a convenient way to define and display a geographic area with a specific radius around a central point.

This function must be called from within the scope of a `MapView` composable.

### Signature

```kotlin
@Composable
fun MapViewScope.Circle(
    center: GeoPointInterface,
    radiusMeters: Double,
    id: String? = null,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 2.dp,
    fillColor: Color = Color.White.copy(alpha = 0.5f),
    zIndex: Int? = null,
    extra: Serializable? = null,
    onClick: OnCircleEventHandler? = null,
)
```

### Description

This composable creates and displays a circle on the map. You can customize its appearance, such as stroke color, width, and fill color, as well as its behavior, like handling click events.

### Parameters

| Parameter      | Type                  | Description                                                                                             | Default Value                  |
| :------------- | :-------------------- | :------------------------------------------------------------------------------------------------------ | :----------------------------- |
| `center`       | `GeoPointInterface`   | **Required.** The geographical coordinate for the center of the circle.                                 | -                              |
| `radiusMeters` | `Double`              | **Required.** The radius of the circle in meters.                                                       | -                              |
| `id`           | `String?`             | An optional unique identifier for the circle. Useful for later retrieval or management.                 | `null`                         |
| `strokeColor`  | `Color`               | The color of the circle's outline.                                                                      | `Color.Red`                    |
| `strokeWidth`  | `Dp`                  | The width of the circle's outline in density-independent pixels (Dp).                                   | `2.dp`                         |
| `fillColor`    | `Color`               | The color used to fill the circle's area.                                                               | `Color.White.copy(alpha=0.5f)` |
| `zIndex`       | `Int?`                | The z-index of the circle, which controls its stacking order relative to other overlays on the map.     | `null`                         |
| `extra`        | `Serializable?`       | Optional serializable data to associate with the circle, which can be retrieved in event handlers.     | `null`                         |
| `onClick`      | `OnCircleEventHandler?` | A lambda function that is invoked when the user clicks on the circle. The handler receives the circle's state. | `null`                         |

### Returns

This composable does not return any value.

### Example

Here's how to add a simple circle to a map centered on a specific location.

```kotlin
import com.mapconductor.core.MapView
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.features.GeoPoint

// ... inside a Composable function

MapView(
    // ... other MapView parameters
) {
    // Add a circle with a 500-meter radius
    Circle(
        center = GeoPoint(40.7128, -74.0060), // New York City
        radiusMeters = 500.0,
        strokeColor = Color.Blue,
        strokeWidth = 3.dp,
        fillColor = Color.Blue.copy(alpha = 0.3f),
        onClick = { circleState ->
            println("Circle clicked! ID: ${circleState.id}")
        }
    )
}
```

---

### `Circle (State-based)`

This is an alternative version of the `Circle` composable that accepts a `CircleState` object. This is useful for managing the state of circles declaratively, for instance, from a ViewModel.

### Signature

```kotlin
@Composable
fun MapViewScope.Circle(state: CircleState)
```

### Description

This composable adds a circle to the map based on the properties defined in the provided `CircleState` object. It handles the addition and removal of the circle from the map as the composable enters or leaves the composition.

### Parameters

| Parameter | Type          | Description                                                              |
| :-------- | :------------ | :----------------------------------------------------------------------- |
| `state`   | `CircleState` | A state holder object that encapsulates all properties of the circle. |

### Returns

This composable does not return any value.

### Example

This example shows how to manage a `CircleState` and pass it to the composable.

```kotlin
import com.mapconductor.core.MapView
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint

// ... inside a Composable function

// Create and remember the CircleState
val circleState = remember {
    CircleState(
        center = GeoPoint(34.0522, -118.2437), // Los Angeles
        radiusMeters = 1000.0,
        strokeColor = Color.Green,
        fillColor = Color.Green.copy(alpha = 0.2f)
    )
}

MapView(
    // ... other MapView parameters
) {
    // Add the circle using its state
    Circle(state = circleState)
}
```