Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# Class: `RoundInfoBubbleIcon`

**Package:** `com.mapconductor.icons`

## Description

The `RoundInfoBubbleIcon` class represents a customizable marker icon styled as a rounded bubble. It is designed to display a small icon alongside a text label, making it ideal for showing brief information like prices, ratings, or short identifiers on a map.

This class extends `AbstractMarkerIcon` and handles the rendering of its properties into a `BitmapIcon`, which can then be used by a map view. The generated bitmap is cached for performance, preventing re-rendering of identical icons.

## Constructor

Creates a new instance of `RoundInfoBubbleIcon`.

### Signature

```kotlin
constructor(
    iconDrawable: Drawable,
    label: String,
    fillColor: Color = Color.White,
    scale: Float = 1f,
    iconSize: Dp = MarkerIconSize.Small,
    debug: Boolean = false,
)
```

### Parameters

| Parameter      | Type       | Default Value         | Description                                                                                                                            |
| :------------- | :--------- | :-------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `iconDrawable` | `Drawable` | -                     | The drawable resource to display as the icon on the left side of the bubble.                                                           |
| `label`        | `String`   | -                     | The text string to display to the right of the icon.                                                                                   |
| `fillColor`    | `Color`    | `Color.White`         | The background color of the bubble.                                                                                                    |
| `scale`        | `Float`    | `1f`                  | A multiplier to adjust the final rendered size of the icon.                                                                            |
| `iconSize`     | `Dp`       | `MarkerIconSize.Small`| The base size of the icon drawable, which influences the overall dimensions of the bubble.                                               |
| `debug`        | `Boolean`  | `false`               | If `true`, a black border is drawn around the generated bitmap, which is useful for layout debugging.                                  |

## Properties

### `anchor: Offset`

Specifies the point on the icon that is "anchored" to the geographical location on the map.
- **Type**: `Offset`
- **Value**: `Offset(0.5f, 1.0f)`
- **Description**: The anchor is set to the center-bottom of the icon's bounding box. This means the tip of the bubble's pointer will align precisely with the map coordinate.

### `infoAnchor: Offset`

Specifies the point on the icon from which an info window will appear.
- **Type**: `Offset`
- **Value**: `Offset(0.5f, 1.0f)`
- **Description**: This is aligned with the `anchor` property, ensuring that info windows also emerge from the bottom-center of the icon.

## Methods

### `toBitmapIcon`

Renders the `RoundInfoBubbleIcon` into a `BitmapIcon`. This method draws the bubble shape, icon, and text onto a bitmap. The result is cached internally based on the icon's properties to optimize performance and avoid redundant rendering.

#### Signature

```kotlin
fun toBitmapIcon(): BitmapIcon
```

#### Returns

- **Type**: `BitmapIcon`
- **Description**: An object containing the generated bitmap, its size, and anchor information, ready to be used by a map renderer.

### `copy`

Creates a new `RoundInfoBubbleIcon` instance with one or more properties modified. This allows for creating variations of an icon in an immutable way.

#### Signature

```kotlin
fun copy(
    iconDrawable: Drawable = this.iconDrawable,
    label: String = this.label,
    fillColor: Color = this.fillColor,
    scale: Float = this.scale,
    iconSize: Dp,
): RoundInfoBubbleIcon
```

#### Parameters

| Parameter      | Type       | Description                                          |
| :------------- | :--------- | :--------------------------------------------------- |
| `iconDrawable` | `Drawable` | A new drawable for the icon.                         |
| `label`        | `String`   | A new text label.                                    |
| `fillColor`    | `Color`    | A new background color for the bubble.               |
| `scale`        | `Float`    | A new scaling factor.                                |
| `iconSize`     | `Dp`       | A new base size for the icon.                        |

#### Returns

- **Type**: `RoundInfoBubbleIcon`
- **Description**: A new `RoundInfoBubbleIcon` instance with the updated properties.

## Example

The following example demonstrates how to create a `RoundInfoBubbleIcon` and display it within a Jetpack Compose `Image` composable.

```kotlin
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.settings.MarkerIconSize

@Composable
fun DisplayRoundInfoBubbleIcon() {
    val context = LocalContext.current

    // 1. Define the properties for the icon.
    val icon = remember {
        RoundInfoBubbleIcon(
            iconDrawable = ContextCompat.getDrawable(
                context,
                // Replace with your drawable resource
                com.mapconductor.core.R.drawable.default_marker 
            )!!,
            label = "$197",
            fillColor = Color.White,
            iconSize = MarkerIconSize.Small,
            scale = 1.2f // Make it 20% larger
        )
    }

    // 2. Generate the BitmapIcon. This operation is cached.
    val bitmapIcon: BitmapIcon = remember(icon) {
        icon.toBitmapIcon()
    }

    // 3. Convert the bitmap to an ImageBitmap for use in Compose.
    val imageBitmap = remember(bitmapIcon) {
        bitmapIcon.bitmap.asImageBitmap()
    }

    // 4. Display the generated icon in an Image composable.
    Box {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Price marker icon with label $197"
        )
    }
}
```