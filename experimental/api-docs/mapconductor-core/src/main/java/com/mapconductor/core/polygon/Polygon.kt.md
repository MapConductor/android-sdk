Excellent. Here is the high-quality SDK documentation for the provided code snippet.

***

# Polygon SDK Documentation

This document provides detailed documentation for the `PolygonState` class and its related components, which are used to manage and display polygons on a map within the MapConductor ecosystem.

## `PolygonState`

### Signature
```kotlin
class PolygonState(...) : ComponentState
```

### Description
The `PolygonState` class is a state holder that defines the properties and behavior of a single polygon on the map. It is designed to be used in a reactive environment like Jetpack Compose, where changes to its properties will automatically trigger UI updates.

This class encapsulates all aspects of a polygon, including its geometry (vertices and holes), visual appearance (colors and stroke width), and interactivity (click handling).

### Parameters
The `PolygonState` is initialized with the following parameters:

| Parameter | Type | Default | Description |
|---|---|---|---|
| `points` | `List<GeoPointInterface>` | - | A list of geographical points that define the outer boundary of the polygon. The list should represent a closed loop (the first and last points should be the same). |
| `holes` | `List<List<GeoPointInterface>>` | `emptyList()` | A list of inner boundaries (holes). Each inner list represents a separate hole and must be a closed loop of points. |
| `id` | `String?` | `null` | An optional unique identifier for the polygon. If `null`, a stable ID is automatically generated based on the polygon's properties. |
| `strokeColor` | `Color` | `Color.Black` | The color of the polygon's outline. |
| `strokeWidth` | `Dp` | `2.dp` | The width of the polygon's outline. |
| `fillColor` | `Color` | `Color.Transparent` | The color used to fill the area of the polygon. |
| `geodesic` | `Boolean` | `false` | If `true`, the polygon's edges are drawn as geodesic lines, which follow the curvature of the Earth. If `false`, edges are drawn as straight lines on the map's 2D projection. |
| `zIndex` | `Int` | `0` | The drawing order of the polygon relative to other map overlays. Polygons with a higher `zIndex` are drawn on top of those with a lower `zIndex`. |
| `extra` | `Serializable?` | `null` | Optional, user-defined data that can be associated with the polygon. This data must be serializable. |
| `onClick` | `OnPolygonEventHandler?` | `null` | A callback lambda function that is invoked when the user clicks on the polygon. It receives a `PolygonEvent` object. |

---

## Methods

### `copy`
Creates a new `PolygonState` instance with optionally modified properties.

#### Signature
```kotlin
fun copy(
    points: List<GeoPointInterface> = this.points,
    holes: List<List<GeoPointInterface>> = this.holes,
    id: String? = this.id,
    strokeColor: Color = this.strokeColor,
    strokeWidth: Dp = this.strokeWidth,
    fillColor: Color = this.fillColor,
    geodesic: Boolean = this.geodesic,
    zIndex: Int = this.zIndex,
    extra: Serializable? = this.extra,
    onClick: OnPolygonEventHandler? = this.onClick,
): PolygonState
```

#### Description
This function creates a shallow copy of the `PolygonState` object. You can override any of the existing properties by providing new values as arguments. This is useful for creating a new state based on an existing one without modifying the original.

#### Returns
A new `PolygonState` instance with the updated properties.

---

### `asFlow`
Returns a `Flow` that emits updates when the polygon's state changes.

#### Signature
```kotlin
fun asFlow(): Flow<PolygonFingerPrint>
```

#### Description
This function provides a reactive stream of the polygon's state. It returns a `Flow` that emits a `PolygonFingerPrint` whenever any property of the `PolygonState` changes. The flow is configured with `distinctUntilChanged()`, ensuring that emissions only occur when the fingerprint is actually different from the previous one. This is highly efficient for observing state changes.

#### Returns
A `Flow<PolygonFingerPrint>` that emits a new value upon a state change.

---

## Related Data Classes & Type Aliases

### `PolygonEvent`
A data class that encapsulates information about a click event on a polygon.

#### Signature
```kotlin
data class PolygonEvent(
    val state: PolygonState,
    val clicked: GeoPointInterface,
)
```

#### Properties
| Property | Type | Description |
|---|---|---|
| `state` | `PolygonState` | The `PolygonState` of the polygon that was clicked. |
| `clicked` | `GeoPointInterface` | The specific geographical coordinate (`GeoPointInterface`) on the polygon where the click occurred. |

---

### `OnPolygonEventHandler`
A type alias for the polygon click event handler function.

#### Signature
```kotlin
typealias OnPolygonEventHandler = (PolygonEvent) -> Unit
```

#### Description
This defines the signature for a function that handles polygon click events. It takes a single `PolygonEvent` parameter and returns `Unit`.

---

### `PolygonFingerPrint`
A data class used for efficient change detection of a `PolygonState`.

#### Signature
```kotlin
data class PolygonFingerPrint(
    val id: Int,
    val strokeColor: Int,
    val strokeWidth: Int,
    val fillColor: Int,
    val geodesic: Int,
    val zIndex: Int,
    val points: Int,
    val holes: Int,
    val extra: Int,
)
```

#### Description
This class holds a collection of hash codes representing the properties of a `PolygonState`. It is used internally by the `asFlow()` method to efficiently determine if the polygon's state has changed. Developers typically do not need to interact with this class directly.

---

## Example

The following example demonstrates how to create a `PolygonState`, handle click events, and use the `copy()` method to create a modified version.

```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint // Assuming a concrete implementation

fun main() {
    // 1. Define the vertices for the polygon's outer boundary
    val outerBoundary = listOf(
        GeoPoint(40.7128, -74.0060), // New York City
        GeoPoint(34.0522, -118.2437), // Los Angeles
        GeoPoint(29.7604, -95.3698),  // Houston
        GeoPoint(40.7128, -74.0060)   // Close the loop
    )

    // 2. Define the vertices for a hole inside the polygon
    val holeBoundary = listOf(
        GeoPoint(39.0, -98.0),
        GeoPoint(38.0, -108.0),
        GeoPoint(37.0, -98.0),
        GeoPoint(39.0, -98.0) // Close the loop
    )

    // 3. Define a click handler
    val onPolygonClick: OnPolygonEventHandler = { event ->
        println("Polygon with ID ${event.state.id} was clicked at ${event.clicked}.")
        // You can access any property of the clicked polygon
        println("Fill color is ${event.state.fillColor}")
    }

    // 4. Create an instance of PolygonState
    val myPolygon = PolygonState(
        points = outerBoundary,
        holes = listOf(holeBoundary),
        strokeColor = Color.Blue,
        strokeWidth = 5.dp,
        fillColor = Color.Blue.copy(alpha = 0.3f),
        geodesic = true,
        zIndex = 1,
        extra = "MyCustomData123",
        onClick = onPolygonClick
    )

    println("Original Polygon ID: ${myPolygon.id}")
    println("Original Fill Color: ${myPolygon.fillColor}")

    // 5. Use the copy() method to create a new state with a different fill color
    val highlightedPolygon = myPolygon.copy(
        fillColor = Color.Red.copy(alpha = 0.5f),
        zIndex = 2 // Bring it to the front
    )

    println("Highlighted Polygon ID: ${highlightedPolygon.id}")
    println("Highlighted Fill Color: ${highlightedPolygon.fillColor}")

    // This `highlightedPolygon` can now be used to update the UI, showing
    // the polygon in a "highlighted" state.
}
```