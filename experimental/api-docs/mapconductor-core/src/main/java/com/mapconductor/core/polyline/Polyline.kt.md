Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# PolylineState

The `PolylineState` class manages the state and appearance of a single polyline on the map. It is designed for use within a Jetpack Compose environment, making its properties observable for reactive UI updates.

This class encapsulates all properties of a polyline, such as its geographical points, color, width, and behavior. It also provides mechanisms for handling user interactions like clicks.

## `PolylineState`

### Signature

```kotlin
class PolylineState(
    points: List<GeoPointInterface>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    geodesic: Boolean = false,
    zIndex: Int = 0,
    extra: Serializable? = null,
    onClick: OnPolylineEventHandler? = null,
) : ComponentState
```

### Description

Creates and manages the state for a polyline component on the map. If an `id` is not provided, a unique ID is automatically generated based on the polyline's properties. All properties are mutable and observable by the Compose runtime.

### Parameters

| Parameter | Type | Description |
| --- | --- | --- |
| `points` | `List<GeoPointInterface>` | A list of `GeoPointInterface` objects that define the vertices of the polyline. |
| `id` | `String?` | An optional unique identifier for the polyline. If `null`, a stable ID is generated from the other properties. Defaults to `null`. |
| `strokeColor` | `Color` | The color of the polyline stroke. Defaults to `Color.Black`. |
| `strokeWidth` | `Dp` | The width of the polyline stroke. Defaults to `1.dp`. |
| `geodesic` | `Boolean` | If `true`, the polyline is drawn as a geodesic curve, which is the shortest path between two points on the Earth's surface. If `false`, it's drawn as a straight line on the 2D map projection. Defaults to `false`. |
| `zIndex` | `Int` | The drawing order of the polyline. Polylines with higher `zIndex` values are drawn on top of those with lower values. Defaults to `0`. |
| `extra` | `Serializable?` | Optional, serializable data that can be attached to the polyline state. Useful for storing custom metadata. Defaults to `null`. |
| `onClick` | `OnPolylineEventHandler?` | A lambda function that is invoked when the user clicks on the polyline. The handler receives a `PolylineEvent` object. Defaults to `null`. |

## Methods

### `copy`

Creates a new `PolylineState` instance, allowing for the modification of specific properties while retaining the others. This is the recommended way to update polyline state in an immutable fashion.

#### Signature

```kotlin
fun copy(
    points: List<GeoPointInterface> = this.points,
    id: String? = this.id,
    strokeColor: Color = this.strokeColor,
    strokeWidth: Dp = this.strokeWidth,
    geodesic: Boolean = this.geodesic,
    zIndex: Int = this.zIndex,
    extra: Serializable? = this.extra,
    onClick: OnPolylineEventHandler? = this.onClick,
): PolylineState
```

#### Parameters

The parameters are identical to the `PolylineState` constructor and allow you to override any of the existing properties.

#### Returns

| Type | Description |
| --- | --- |
| `PolylineState` | A new `PolylineState` instance with the updated properties. |

### `asFlow`

Returns a `Flow` that emits a `PolylineFingerPrint` whenever any of the polyline's properties change. This is useful for observing state changes reactively and triggering updates efficiently.

#### Signature

```kotlin
fun asFlow(): Flow<PolylineFingerPrint>
```

#### Returns

| Type | Description |
| --- | --- |
| `Flow<PolylineFingerPrint>` | A Kotlin Flow that emits a new fingerprint upon state change. |

### `fingerPrint`

Generates a lightweight `PolylineFingerPrint` of the current state. This is primarily used internally for efficient change detection within the `asFlow` stream.

#### Signature

```kotlin
fun fingerPrint(): PolylineFingerPrint
```

#### Returns

| Type | Description |
| --- | --- |
| `PolylineFingerPrint` | A `PolylineFingerPrint` object representing the current state. |

## Related Types

### `OnPolylineEventHandler`

A type alias for the function that handles click events on a polyline.

#### Signature

```kotlin
typealias OnPolylineEventHandler = (PolylineEvent) -> Unit
```

### `PolylineEvent`

A data class that represents a click event on a polyline. It is passed to the `OnPolylineEventHandler` when a click occurs.

#### Signature

```kotlin
data class PolylineEvent(
    val state: PolylineState,
    val clicked: GeoPointInterface,
)
```

#### Parameters

| Parameter | Type | Description |
| --- | --- | --- |
| `state` | `PolylineState` | The state of the polyline that was clicked. |
| `clicked` | `GeoPointInterface` | The geographical point on the polyline where the click occurred. |

### `PolylineFingerPrint`

A data class that holds a lightweight, hash-based representation of a `PolylineState`. It is used for efficient change detection in reactive streams. This is mainly for internal use.

#### Signature

```kotlin
data class PolylineFingerPrint(
    val id: Int,
    val strokeColor: Int,
    val strokeWidth: Int,
    val geodesic: Int,
    val zIndex: Int,
    val points: Int,
    val extra: Int,
)
```

## Example

Here is an example of how to create and manage a `PolylineState`.

```kotlin
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.polyline.PolylineState

// Assume a simple implementation of GeoPointInterface for the example
data class GeoPoint(val latitude: Double, val longitude: Double) : GeoPointInterface {
    // Interface members would be implemented here
}

fun main() {
    // 1. Define the points for the polyline
    val routePoints = listOf(
        GeoPoint(latitude = 34.0522, longitude = -118.2437), // Los Angeles
        GeoPoint(latitude = 39.7392, longitude = -104.9903), // Denver
        GeoPoint(latitude = 41.8781, longitude = -87.6298)   // Chicago
    )

    // 2. Define a click handler
    val onPolylineClick: OnPolylineEventHandler = { event ->
        println("Polyline with ID ${event.state.id} was clicked!")
        println("Click location: Lat=${event.clicked.latitude}, Lon=${event.clicked.longitude}")
        // You could use event.state.extra to retrieve custom data
    }

    // 3. Create an initial PolylineState
    var polyline by mutableStateOf(
        PolylineState(
            points = routePoints,
            strokeColor = Color.Blue,
            strokeWidth = 5.dp,
            geodesic = true,
            zIndex = 1,
            extra = "Route 66 segment",
            onClick = onPolylineClick
        )
    )

    println("Initial polyline color: ${polyline.strokeColor}")

    // 4. Update the state using the copy() method
    // This creates a new state, which would trigger a recomposition in a Compose UI
    polyline = polyline.copy(strokeColor = Color.Red)

    println("Updated polyline color: ${polyline.strokeColor}")

    // Simulate a click event for demonstration
    val simulatedClickPoint = GeoPoint(latitude = 39.7392, longitude = -104.9903)
    polyline.onClick?.invoke(PolylineEvent(state = polyline, clicked = simulatedClickPoint))
}
```