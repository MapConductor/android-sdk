Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

***

# CircleState SDK Documentation

This document provides detailed information about the `CircleState` class and its related components, which are used to define and manage a circle on a map.

## `CircleState`

### Signature
```kotlin
class CircleState(
    center: GeoPointInterface,
    radiusMeters: Double,
    geodesic: Boolean = true,
    clickable: Boolean = true,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 1.dp,
    fillColor: Color,
    id: String? = null,
    zIndex: Int? = null,
    extra: Serializable? = null,
    onClick: OnCircleEventHandler? = null,
) : ComponentState
```

### Description
The `CircleState` class is a state holder that defines the properties and behavior of a circle drawn on a map. It is designed to be used within a Jetpack Compose environment, where changes to its properties will automatically trigger UI updates.

An instance of `CircleState` represents a single circle. It manages properties such as position, size, appearance, and interactivity. If an `id` is not provided, a unique one is generated based on the circle's properties.

### Parameters
| Parameter | Type | Description | Default |
|---|---|---|---|
| `center` | `GeoPointInterface` | The geographic coordinates for the center of the circle. | (required) |
| `radiusMeters` | `Double` | The radius of the circle in meters. | (required) |
| `geodesic` | `Boolean` | Specifies if the circle should be drawn as a geodesic shape. A geodesic circle correctly represents the shape on the Earth's curved surface. | `true` |
| `clickable` | `Boolean` | Determines if the circle can receive click events. If `true`, the `onClick` handler will be invoked on user clicks. | `true` |
| `strokeColor` | `Color` | The color of the circle's outline. | `Color.Red` |
| `strokeWidth` | `Dp` | The width of the circle's outline. | `1.dp` |
| `fillColor` | `Color` | The color used to fill the interior of the circle. | semi-transparent white |
| `id` | `String?` | An optional unique identifier for the circle. If `null`, an ID will be generated automatically. | `null` |
| `zIndex` | `Int?` | The z-index of the circle, which determines its stacking order relative to other map components. Higher values are drawn on top. | `null` |
| `extra` | `Serializable?` | Optional, serializable data that can be associated with the circle. | `null` |
| `onClick` | `OnCircleEventHandler?` | A callback function that is invoked when the circle is clicked. This requires `clickable` to be `true`. | `null` |

---

## Member Functions

### `copy()`

#### Signature
```kotlin
fun copy(
    center: GeoPointInterface = this.center,
    radiusMeters: Double = this.radiusMeters,
    // ... other parameters
): CircleState
```

#### Description
Creates a new `CircleState` instance with the same properties as the original, allowing for specific properties to be overridden. This is useful for creating a modified version of a state without changing the original object.

#### Returns
| Type | Description |
|---|---|
| `CircleState` | A new `CircleState` instance with the updated properties. |

### `asFlow()`

#### Signature
```kotlin
fun asFlow(): Flow<CircleFingerPrint>
```

#### Description
Returns a `Flow` that emits a new `CircleFingerPrint` whenever a property of the `CircleState` changes. This is useful for observing state changes in a reactive way. The flow is configured to emit only when the state has genuinely changed.

#### Returns
| Type | Description |
|---|---|
| `Flow<CircleFingerPrint>` | A flow that emits a fingerprint of the circle's state upon any change. |

---

## Related Components

### `CircleEvent`

#### Signature
```kotlin
data class CircleEvent(
    val state: CircleState,
    val clicked: GeoPointInterface,
)
```

#### Description
A data class that encapsulates information about a click event on a circle. An object of this type is passed to the `OnCircleEventHandler` when a user clicks a circle.

#### Properties
| Property | Type | Description |
|---|---|---|
| `state` | `CircleState` | The `CircleState` of the circle that was clicked. |
| `clicked` | `GeoPointInterface` | The exact geographical coordinate where the click occurred. |

### `OnCircleEventHandler`

#### Signature
```kotlin
typealias OnCircleEventHandler = (CircleEvent) -> Unit
```

#### Description
A type alias for the function that handles circle click events. It defines a function that takes a `CircleEvent` as a parameter and returns `Unit`.

---

## Example

The following example demonstrates how to create a `CircleState`, define a click handler, and use the `copy()` method to create a modified version.

```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint // Assuming GeoPoint implements GeoPointInterface

// 1. Define a click handler
val handleCircleClick: OnCircleEventHandler = { circleEvent ->
    println("Circle clicked: ${circleEvent.state.id}")
    println("Click position: ${circleEvent.clicked.latitude}, ${circleEvent.clicked.longitude}")
}

// 2. Define the center point for the circle
val circleCenter = GeoPoint(latitude = 34.0522, longitude = -118.2437)

// 3. Create an instance of CircleState
val myCircle = CircleState(
    center = circleCenter,
    radiusMeters = 1000.0,
    strokeColor = Color.Blue,
    strokeWidth = 2.dp,
    fillColor = Color.Blue.copy(alpha = 0.3f),
    clickable = true,
    onClick = handleCircleClick,
    extra = "MyCircleData"
)

// 4. Use the copy() method to create a new circle with a larger radius
val largerCircle = myCircle.copy(
    radiusMeters = 2000.0,
    id = "larger-circle" // Assign a new ID
)

// Now `myCircle` and `largerCircle` can be added to a map component.
// When `myCircle` is clicked, the `handleCircleClick` lambda will be executed.
```