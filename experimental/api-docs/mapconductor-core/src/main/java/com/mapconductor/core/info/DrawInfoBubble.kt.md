# DrawInfoBubble

## Signature
```kotlin
@Composable
internal fun DrawInfoBubble(
    modifier: Modifier,
    bubbleColor: Color,
    borderColor: Color,
    contentPadding: Dp,
    cornerRadius: Dp,
    tailSize: Dp,
    content: @Composable () -> Unit,
)
```

## Description
Renders a customizable info bubble with a triangular tail at the bottom center. This composable is designed to act as a container for other UI elements, commonly used for map annotations, tooltips, or callouts.

It uses a `Canvas` to draw the bubble shape with a specified fill color and border. The provided `content` is then placed inside with appropriate padding to fit within the bubble's boundaries, accounting for the space occupied by the tail.

**Note:** This is an `internal` function, intended for use only within its own module.

## Parameters
| Parameter | Type | Description |
| --- | --- | --- |
| `modifier` | `Modifier` | The `Modifier` to be applied to the bubble container. |
| `bubbleColor` | `Color` | The background `Color` of the bubble. |
| `borderColor` | `Color` | The `Color` of the bubble's border. |
| `contentPadding` | `Dp` | The padding `Dp` applied to the content within the bubble on all sides. The bottom padding is automatically increased to accommodate the tail. |
| `cornerRadius` | `Dp` | The corner radius `Dp` for the rounded corners of the bubble. |
| `tailSize` | `Dp` | The size `Dp` of the triangular tail at the bottom of the bubble. This defines the height and base width of the tail. |
| `content` | `@Composable () -> Unit` | A composable lambda that defines the content to be displayed inside the bubble. |

## Returns
This composable does not return any value.

## Example
Here is an example of how to use `DrawInfoBubble` to display a simple text message.

```kotlin
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoBubbleExample() {
    DrawInfoBubble(
        modifier = Modifier.padding(16.dp),
        bubbleColor = Color.White,
        borderColor = Color.Gray,
        contentPadding = 12.dp,
        cornerRadius = 8.dp,
        tailSize = 10.dp
    ) {
        Text(
            text = "Location Details:\nLatitude: 40.7128\nLongitude: -74.0060",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )
    }
}
```