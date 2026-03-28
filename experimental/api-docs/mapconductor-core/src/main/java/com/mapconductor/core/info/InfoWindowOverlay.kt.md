Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

## InfoBubbleOverlay

An internal composable used to position and display an info bubble relative to a marker's icon on the map.

This component calculates the precise screen coordinates for the info bubble based on the marker's position, the size and anchor points of the icon, and the anchor point of the info bubble itself.

**Note:** This is an internal component and is not intended for direct use. It is used by the map system to render the info bubbles defined in `InfoBubbleEntry`.

### Signature
```kotlin
@Composable
internal fun InfoBubbleOverlay(
    positionOffset: Offset,
    iconSize: Size,
    iconOffset: Offset,
    infoAnchorOffset: Offset,
    tailOffset: Offset,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
```

### Description
The `InfoBubbleOverlay` is responsible for the complex logic of placing an info bubble's content correctly on the screen. It takes into account multiple offsets and sizes to ensure the bubble appears anchored to the correct point on its associated marker icon. The final position is calculated by applying offsets based on the info bubble's own size, the marker icon's size, and the various anchor points.

### Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| `positionOffset` | `Offset` | The screen pixel offset of the marker's geographical position. |
| `iconSize` | `Size` | The size of the marker's icon in pixels. |
| `iconOffset` | `Offset` | The anchor point on the icon that connects to the map's geographical coordinate. The values range from `(0.0, 0.0)` (top-left) to `(1.0, 1.0)` (bottom-right). For example, `(0.5, 1.0)` anchors the bottom-center of the icon to the marker's position. |
| `infoAnchorOffset` | `Offset` | The anchor point on the icon where the info bubble connects. For example, `(0.5, 0.0)` would anchor the bubble to the top-center of the icon. |
| `tailOffset` | `Offset` | The anchor point on the info bubble itself that connects to the `infoAnchorOffset` on the icon. For example, `(0.5, 1.0)` means the bottom-center of the info bubble (where the "tail" usually is) will connect to the icon. |
| `modifier` | `Modifier` | A standard `Modifier` for this composable. |
| `content` | `@Composable () -> Unit` | The composable content to be displayed inside the info bubble. |

<br/>

## InfoBubbleEntry

A data class that encapsulates the information required to render an info bubble for a specific marker.

### Signature
```kotlin
data class InfoBubbleEntry(
    val marker: MarkerState,
    val tailOffset: Offset = Offset(0.5f, 1.0f),
    val content: @Composable () -> Unit,
)
```

### Description
This data class holds the state for an info bubble, linking it to a `MarkerState` and defining its UI content and anchor point. Instances of `InfoBubbleEntry` are collected by the `MapView` to be rendered using the `InfoBubbleOverlay`.

### Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| `marker` | `MarkerState` | The `MarkerState` to which this info bubble is attached. |
| `tailOffset` | `Offset` | The anchor point on the info bubble where its "tail" connects to the marker icon. The default value `Offset(0.5f, 1.0f)` anchors the bottom-center of the bubble to the marker. |
| `content` | `@Composable () -> Unit` | A composable lambda that defines the UI content of the info bubble. |

### Example
The following example shows how you might define an `InfoBubbleEntry` when creating a marker. When the marker's info window is shown, the `MapView` will use this entry to render the bubble.

```kotlin
@Composable
fun MyMapScreen() {
    val markerState = rememberMarkerState(position = LatLng(35.68, 139.76))

    // This would be inside your MapView composable
    Marker(
        state = markerState,
        title = "Tokyo Station",
        // When the info window is shown, it uses this entry
        infoBubble = InfoBubbleEntry(
            marker = markerState,
            // Custom tail offset if needed, otherwise defaults are used
            // tailOffset = Offset(0.5f, 1.0f), 
            content = {
                // Define the UI for the info bubble
                Column(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Tokyo Station", fontWeight = FontWeight.Bold)
                    Text("A major railway station in Tokyo.")
                }
            }
        )
    )
}
```

<br/>

## LocalInfoBubbleCollector

A `CompositionLocal` used to collect info bubble states from markers within a `MapView`.

### Signature
```kotlin
val LocalInfoBubbleCollector: ProvidableCompositionLocal<MutableStateFlow<MutableMap<String, InfoBubbleEntry>>>
```

### Description
`LocalInfoBubbleCollector` is an internal mechanism that allows the `MapView` to collect `InfoBubbleEntry` definitions from its `Marker` children. It provides a `MutableStateFlow` containing a map of active info bubbles, which the `MapView` then observes to render them as overlays.

This implementation detail ensures that info bubbles can be defined alongside their respective markers but rendered at the top level of the map, preventing them from being clipped by map tiles. An error is thrown if a component that provides an `InfoBubbleEntry` is not placed within a `MapView`, as the collector would not be available.