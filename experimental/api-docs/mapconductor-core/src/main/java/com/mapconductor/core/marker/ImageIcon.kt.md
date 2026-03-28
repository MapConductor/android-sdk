# ImageIcon

The `ImageIcon` class represents a map marker icon created from an Android `Drawable` object. It provides a flexible way to define icons using various drawable types, such as `BitmapDrawable`, `ColorDrawable`, or `GradientDrawable`.

This class handles the conversion of the `Drawable` into a `BitmapIcon` suitable for rendering on the map. For efficiency, it caches the generated bitmap, so subsequent requests for the same icon configuration do not require re-rendering. The equality of `ImageIcon` instances is determined by the properties of the underlying `Drawable` and the icon's configuration parameters (`iconSize`, `scale`, `anchor`, etc.), not by object identity.

## Signature

```kotlin
class ImageIcon(
    image: Drawable,
    override val iconSize: Dp = Settings.Default.iconSize,
    override val scale: Float = 1.0f,
    override val anchor: Offset = Offset(0.5f, 0.5f),
    override val infoAnchor: Offset = Offset(0.5f, 0.5f),
    override val debug: Boolean = false,
) : AndroidDrawableIcon
```

## Parameters

| Parameter | Type | Description | Default |
|-----------|------|-------------|---------|
| `image` | `Drawable` | The Android `Drawable` to be used as the icon. | (none) |
| `iconSize` | `Dp` | The base size of the icon in density-independent pixels (Dp). | `Settings.Default.iconSize` |
| `scale` | `Float` | A multiplier applied to `iconSize` to scale the icon. A value of `2.0` would double the icon's size. | `1.0f` |
| `anchor` | `Offset` | The anchor point of the icon that is attached to the map's geographical coordinate. An `Offset(0.5f, 0.5f)` represents the center of the icon. `Offset(0.0f, 0.0f)` is the top-left corner. | `Offset(0.5f, 0.5f)` |
| `infoAnchor` | `Offset` | The point on the icon to which an associated info window will be anchored. The coordinate system is the same as for `anchor`. | `Offset(0.5f, 0.5f)` |
| `debug` | `Boolean` | If `true`, enables debug visualizations for the icon, such as drawing its bounding box or anchor point. | `false` |

## Example

The following example demonstrates how to create `ImageIcon` instances from both a drawable resource and a programmatically generated `Drawable`.

```kotlin
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.graphics.drawable.GradientDrawable
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.types.LatLng

// Assuming 'context' is an Android Context instance available in your scope

// 1. Create an ImageIcon from a drawable resource with default settings
val defaultIcon = ImageIcon(
    image = ContextCompat.getDrawable(context, R.drawable.ic_marker_default)!!
)

// 2. Create a larger, semi-transparent red circle icon anchored at the bottom center
val customDrawable = GradientDrawable().apply {
    shape = GradientDrawable.OVAL
    setColor(0x80FF0000.toInt()) // Semi-transparent red
    setSize(100, 100)
}

val customIcon = ImageIcon(
    image = customDrawable,
    iconSize = 48.dp,
    scale = 1.5f,
    anchor = Offset(0.5f, 1.0f) // Anchor at the bottom-center
)

// 3. Use the icons when creating Markers for the map
val marker1 = Marker(
    position = LatLng(34.0522, -118.2437),
    icon = defaultIcon,
    title = "Los Angeles"
)

val marker2 = Marker(
    position = LatLng(40.7128, -74.0060),
    icon = customIcon,
    title = "New York City"
)

// You can now add these markers to your map controller.
```