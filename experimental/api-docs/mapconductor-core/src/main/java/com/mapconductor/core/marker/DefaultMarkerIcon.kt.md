This document provides a detailed reference for the Marker Icon SDK, covering classes that generate standard pin-shaped map markers. These classes allow for customization of fill (color, image, or drawable), stroke, labels, and more.

## `ColorDefaultIcon`

Creates a standard pin-shaped map marker icon with a solid color fill. This is the most common and straightforward marker type to use.

### Constructor

Initializes a new instance of `ColorDefaultIcon`. A convenience constructor is provided with sensible defaults for most parameters.

**Signature:**
```kotlin
constructor(
    fillColor: Color = Color.Red,
    strokeColor: Color = Color.White,
    strokeWidth: Dp = Settings.Default.iconStroke,
    scale: Float = 1f,
    label: String? = null,
    labelTextColor: Color? = Color.Black,
    labelTextSize: TextUnit = 18.sp,
    labelTypeFace: Typeface = Typeface.DEFAULT,
    labelStrokeColor: Color = Color.White,
    infoAnchor: Offset = Offset(0.5f, 0f),
    iconSize: Dp = Settings.Default.iconSize,
    debug: Boolean = false,
)
```

### Parameters

| Parameter | Type | Description | Default |
| :--- | :--- | :--- | :--- |
| `fillColor` | `Color` | The fill color of the marker body. | `Color.Red` |
| `strokeColor` | `Color` | The color of the marker's outline. | `Color.White` |
| `strokeWidth` | `Dp` | The width of the marker's outline. | `Settings.Default.iconStroke` |
| `scale` | `Float` | The scaling factor for the entire icon. | `1.0f` |
| `label` | `String?` | An optional text label to display inside the marker. | `null` |
| `labelTextColor` | `Color?` | The color of the label text. | `Color.Black` |
| `labelTextSize` | `TextUnit` | The size of the label text. | `18.sp` |
| `labelTypeFace` | `Typeface` | The typeface for the label text. | `Typeface.DEFAULT` |
| `labelStrokeColor` | `Color` | The color of the outline drawn around the label text for better visibility. | `Color.White` |
| `infoAnchor` | `Offset` | The anchor point for an info window, relative to the icon's dimensions (0,0 is top-left, 1,1 is bottom-right). | `Offset(0.5f, 0f)` (top-center) |
| `iconSize` | `Dp` | The base size of the icon before scaling. | `Settings.Default.iconSize` |
| `debug` | `Boolean` | If `true`, a debug frame will be drawn around the icon's canvas. | `false` |

### Functions

#### `copy`

Creates a new `ColorDefaultIcon` instance, allowing you to modify specific properties while keeping others the same.

**Signature:**
```kotlin
fun copy(
    fillColor: Color = this.fillColor,
    strokeColor: Color = this.strokeColor,
    strokeWidth: Dp = this.strokeWidth,
    scale: Float = this.scale,
    label: String? = this.label,
    labelTextColor: Color? = this.labelTextColor,
    labelTextSize: TextUnit = this.labelTextSize,
    labelTypeFace: Typeface = this.labelTypeFace,
    labelStrokeColor: Color = this.labelStrokeColor,
    iconSize: Dp = this.iconSize,
    debug: Boolean = this.debug,
): ColorDefaultIcon
```

### Example

```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Create a simple blue marker
val simpleMarker = ColorDefaultIcon(fillColor = Color.Blue)

// Create a larger, scaled marker with a label
val labeledMarker = ColorDefaultIcon(
    fillColor = Color(0xFF009688), // Teal
    strokeColor = Color.White,
    strokeWidth = 3.dp,
    scale = 1.5f,
    label = "A",
    labelTextColor = Color.White,
    labelTextSize = 20.sp,
    labelStrokeColor = Color.Black
)
```

---

## `ImageDefaultIcon`

Creates a standard pin-shaped map marker icon filled with a `Bitmap` image. The image is scaled to fill the marker shape using a center-crop behavior, preserving its aspect ratio.

### Constructor

Initializes a new instance of `ImageDefaultIcon`.

**Signature:**
```kotlin
constructor(
    backgroundImage: Bitmap,
    strokeColor: Color = Color.White,
    strokeWidth: Dp = Settings.Default.iconStroke,
    scale: Float = 1f,
    label: String? = null,
    // ... other parameters are the same as ColorDefaultIcon
)
```

### Parameters

| Parameter | Type | Description | Default |
| :--- | :--- | :--- | :--- |
| `backgroundImage` | `Bitmap` | The bitmap image to use as the marker's fill. | (none) |
| `strokeColor` | `Color` | The color of the marker's outline. | `Color.White` |
| `strokeWidth` | `Dp` | The width of the marker's outline. | `Settings.Default.iconStroke` |
| `scale` | `Float` | The scaling factor for the entire icon. | `1.0f` |
| `label` | `String?` | An optional text label to display inside the marker. | `null` |
| `...` | | Other parameters are identical to `ColorDefaultIcon`. | |

### Functions

#### `copy`

Creates a new `ImageDefaultIcon` instance, allowing you to modify specific properties.

**Signature:**
```kotlin
fun copy(
    backgroundImage: Bitmap = this.backgroundImage,
    // ... other parameters are the same as ColorDefaultIcon.copy
): ImageDefaultIcon
```

### Example

```kotlin
// Assume 'myBitmap' is a Bitmap object loaded from resources or network
val myBitmap: Bitmap = // ...

// Create a marker filled with the bitmap
val imageMarker = ImageDefaultIcon(
    backgroundImage = myBitmap,
    strokeWidth = 2.dp,
    scale = 1.2f
)
```

---

## `DrawableDefaultIcon`

Creates a standard pin-shaped map marker icon filled with a `Drawable`. The drawable is scaled to fill the marker shape. If the drawable has an intrinsic size, its aspect ratio is preserved using a center-crop behavior.

### Constructor

Initializes a new instance of `DrawableDefaultIcon`.

**Signature:**
```kotlin
constructor(
    backgroundDrawable: Drawable,
    strokeColor: Color = Color.White,
    strokeWidth: Dp = Settings.Default.iconStroke,
    scale: Float = 1f,
    label: String? = null,
    // ... other parameters are the same as ColorDefaultIcon
)
```

### Parameters

| Parameter | Type | Description | Default |
| :--- | :--- | :--- | :--- |
| `backgroundDrawable` | `Drawable` | The drawable to use as the marker's fill. | (none) |
| `strokeColor` | `Color` | The color of the marker's outline. | `Color.White` |
| `strokeWidth` | `Dp` | The width of the marker's outline. | `Settings.Default.iconStroke` |
| `scale` | `Float` | The scaling factor for the entire icon. | `1.0f` |
| `label` | `String?` | An optional text label to display inside the marker. | `null` |
| `...` | | Other parameters are identical to `ColorDefaultIcon`. | |

### Functions

#### `copy`

Creates a new `DrawableDefaultIcon` instance, allowing you to modify specific properties.

**Signature:**
```kotlin
fun copy(
    backgroundDrawable: Drawable = this.backgroundDrawable,
    // ... other parameters are the same as ColorDefaultIcon.copy
): DrawableDefaultIcon
```

### Example

```kotlin
import androidx.core.content.ContextCompat

// Assume 'context' is a valid Android Context
val myDrawable = ContextCompat.getDrawable(context, R.drawable.my_gradient_background)

// Create a marker filled with the drawable
val drawableMarker = myDrawable?.let {
    DrawableDefaultIcon(
        backgroundDrawable = it,
        strokeWidth = 2.dp
    )
}
```

---

## `DefaultMarkerIcon`

A type alias for `ColorDefaultIcon`. This is provided for backward compatibility. It is recommended to use `ColorDefaultIcon` directly in new code.

**Signature:**
```kotlin
typealias DefaultMarkerIcon = ColorDefaultIcon
```

---

## `AbstractDefaultIcon`

An abstract base class that provides the common framework for creating pin-shaped marker icons. It handles the rendering of the marker's shape, stroke, and label.

Developers should typically use one of the concrete subclasses (`ColorDefaultIcon`, `ImageDefaultIcon`, `DrawableDefaultIcon`) or extend this class to create custom fill behaviors.

### Properties

| Property | Type | Description |
| :--- | :--- | :--- |
| `strokeColor` | `Color` | The color of the marker's outline. |
| `strokeWidth` | `Dp` | The width of the marker's outline. |
| `scale` | `Float` | The scaling factor for the entire icon. |
| `label` | `String?` | The optional text label to display inside the marker. |
| `labelTextColor` | `Color?` | The color of the label text. |
| `labelTextSize` | `TextUnit` | The size of the label text. |
| `labelTypeFace` | `Typeface` | The typeface for the label text. |
| `labelStrokeColor` | `Color` | The color of the outline drawn around the label text. |
| `iconSize` | `Dp` | The base size of the icon before scaling. |
| `anchor` | `Offset` | The anchor point of the icon, relative to its dimensions. Fixed to `Offset(0.5f, 1f)` (bottom-center). |
| `infoAnchor` | `Offset` | The anchor point for an info window, relative to the icon's dimensions. |
| `debug` | `Boolean` | If `true`, a debug frame is drawn around the icon's canvas. |

### Abstract Functions

Subclasses must implement these methods.

#### `drawMarkerFill`

Defines how the interior (fill) of the marker is drawn.

**Signature:**
```kotlin
protected abstract fun drawMarkerFill(
    canvas: Canvas,
    path: Path,
    canvasSize: Float,
    iconScale: Float,
)
```

**Parameters:**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `canvas` | `Canvas` | The canvas to draw on. |
| `path` | `Path` | The path defining the marker's shape. |
| `canvasSize` | `Float` | The total size of the drawing area for the marker. |
| `iconScale` | `Float` | The current scale of the icon. |

#### `getUniqueProperties`

Returns an object representing the unique properties of the subclass. This is used for `equals` and `hashCode` implementations to ensure correct caching and comparison.

**Signature:**
```kotlin
protected abstract fun getUniqueProperties(): Any
```

**Returns:**
- `Any`: An object (e.g., a `Color`, a `Bitmap` hash) that uniquely identifies the state of the subclass.