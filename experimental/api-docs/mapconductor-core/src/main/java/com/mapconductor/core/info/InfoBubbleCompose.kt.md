Of course! Here is the high-quality SDK documentation for the provided code snippet.

# Info Bubbles

These composable functions are used to display informational windows, or "bubbles," attached to markers on the map. They must be called from within a `MapViewScope`.

---

## InfoBubble

Displays a standard, customizable info bubble attached to a map marker. This composable provides a default speech-bubble appearance with a tail pointing towards the specified `marker`. You can customize its colors, corners, padding, and tail size.

### Signature
```kotlin
@Composable
fun MapViewScope.InfoBubble(
    marker: MarkerState,
    bubbleColor: Color = Color.Companion.White,
    borderColor: Color = Color.Companion.Black,
    contentPadding: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    tailSize: Dp = 8.dp,
    content: @Composable () -> Unit,
)
```

### Description
The `InfoBubble` composable is the standard way to show information for a marker. It automatically draws a rectangular bubble with a triangular tail that points to the marker's anchor point. The bubble's appearance is easily customized through its parameters. The content of the bubble is defined by the `content` composable lambda.

The bubble is automatically added to the map when the composable enters the composition and removed when it leaves, managed by a `DisposableEffect`.

### Parameters
| Parameter | Type | Description |
|---|---|---|
| `marker` | `MarkerState` | The marker state to which this info bubble will be attached. |
| `bubbleColor` | `Color` | The background color of the bubble. Defaults to `Color.White`. |
| `borderColor` | `Color` | The color of the bubble's border and tail outline. Defaults to `Color.Black`. |
| `contentPadding` | `Dp` | The padding between the bubble's border and its content. Defaults to `8.dp`. |
| `cornerRadius` | `Dp` | The corner radius for the rectangular part of the bubble. Defaults to `4.dp`. |
| `tailSize` | `Dp` | The size of the triangular tail pointing to the marker. Defaults to `8.dp`. |
| `content` | `@Composable () -> Unit` | The composable content to be displayed inside the bubble. |

### Returns
This composable does not return a value.

### Example
Here is an example of showing an `InfoBubble` for a marker when it is selected.

```kotlin
// Assume this is within a Composable function
val markerState = rememberMarkerState(
    position = GeoPosition(34.0522, -118.2437)
)
var isMarkerSelected by remember { mutableStateOf(false) }

MapView { // MapViewScope
    Marker(
        state = markerState,
        onClick = {
            isMarkerSelected = !isMarkerSelected
            true // Consume the click event
        }
    )

    if (isMarkerSelected) {
        InfoBubble(
            marker = markerState,
            bubbleColor = Color(0xFFE0F7FA),
            borderColor = Color(0xFF006064),
            cornerRadius = 8.dp,
            contentPadding = 12.dp
        ) {
            Text(
                text = "Los Angeles City Hall",
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }
}
```

---

## InfoBubbleCustom

Renders a fully custom info bubble attached to a map marker, positioned by the map's overlay engine.

### Signature
```kotlin
@Composable
fun MapViewScope.InfoBubbleCustom(
    marker: MarkerState,
    tailOffset: Offset,
    content: @Composable () -> Unit,
)
```

### Description
The `InfoBubbleCustom` composable offers complete control over the visual appearance of an info bubble. Unlike `InfoBubble`, it does not draw any default shape or tail. You are responsible for drawing the entire bubble UI, including any desired shape, background, and tail, within the `content` lambda.

The map's overlay engine will use the `tailOffset` parameter to correctly position your custom content relative to the marker. This function is ideal when you need a non-standard bubble shape or complex internal layout.

### Parameters
| Parameter | Type | Description |
|---|---|---|
| `marker` | `MarkerState` | The marker state to which this custom info bubble will be attached. |
| `tailOffset` | `Offset` | Specifies the connection point within your custom content's bounding box, using a relative coordinate system from (0, 0) to (1, 1). For example, `Offset(0.5f, 1.0f)` means the bottom-center of your content will point to the marker. `Offset(0.0f, 0.5f)` would be the middle of the left edge. |
| `content` | `@Composable () -> Unit` | The composable content that defines the entire visual representation of the custom bubble. |

### Returns
This composable does not return a value.

### Example
This example demonstrates creating a circular info bubble with a custom background and shadow. The `tailOffset` is set to `Offset(0.5f, 1.0f)` to ensure the bottom-center of the `Card` points to the marker.

```kotlin
// Assume this is within a Composable function and MapViewScope
val markerState = rememberMarkerState(
    position = GeoPosition(40.7128, -74.0060)
)

// Show a custom bubble for this marker
InfoBubbleCustom(
    marker = markerState,
    // The connection point is the bottom-center of our custom content.
    tailOffset = Offset(0.5f, 1.0f)
) {
    // The content lambda is responsible for drawing the entire bubble.
    Card(
        modifier = Modifier.size(120.dp),
        shape = CircleShape,
        backgroundColor = Color.Yellow,
        elevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("NYC", fontSize = 24.sp)
        }
    }
}
```