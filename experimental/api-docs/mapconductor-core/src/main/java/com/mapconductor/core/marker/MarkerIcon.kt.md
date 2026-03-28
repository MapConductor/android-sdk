Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

***

## Marker Icon API Reference

This document provides detailed documentation for the marker icon classes and interfaces used for creating custom map markers.

### `MarkerIconInterface`

An interface that defines the essential properties and behaviors for a map marker icon. All custom marker icon classes must implement this interface.

#### **Properties**

| Property | Type | Description |
| --- | --- | --- |
| `scale` | `Float` | The scaling factor for the icon. |
| `anchor` | `Offset` | The point on the icon (relative to its top-left corner) that is anchored to the geographical coordinate on the map. |
| `iconSize` | `Dp` | The size of the icon in density-independent pixels (Dp). |
| `infoAnchor` | `Offset` | The anchor point for an info window, relative to the icon's top-left corner. |
| `debug` | `Boolean` | A flag to enable debug mode. If `true`, visual aids like a bounding box may be rendered. |

#### **Functions**

##### `toBitmapIcon()`

Converts the marker icon definition into a `BitmapIcon` instance.

**Signature**
```kotlin
fun toBitmapIcon(): BitmapIcon
```

**Description**

This function is responsible for processing the marker icon's properties and generating a `BitmapIcon`, which is a concrete, renderable bitmap representation of the icon.

**Returns**

| Type | Description |
| --- | --- |
| `BitmapIcon` | The generated `BitmapIcon` object. |

---

### `AbstractMarkerIcon`

An abstract base class that provides a partial implementation of `MarkerIconInterface`. It serves as a convenient starting point for creating custom marker icons.

**Signature**
```kotlin
abstract class AbstractMarkerIcon : MarkerIconInterface
```

**Description**

This class implements `MarkerIconInterface` and provides a helper function for drawing a debug frame. Subclasses are required to provide concrete implementations for the abstract properties defined in the interface.

#### **Protected Functions**

##### `drawDebugFrame()`

Draws a rectangular border on a `Canvas`, which is useful for debugging the icon's boundaries.

**Signature**
```kotlin
protected fun drawDebugFrame(canvas: Canvas)
```

**Description**

This function draws a 1-pixel wide black stroke around the edges of the provided `Canvas`. It is typically called when the `debug` property is `true` to help visualize the icon's frame.

**Parameters**

| Parameter | Type | Description |
| --- | --- | --- |
| `canvas` | `Canvas` | The canvas on which the debug frame will be drawn. |

---

### `AndroidDrawableIcon`

An abstract class for creating marker icons from an Android `Drawable` resource.

**Signature**
```kotlin
abstract class AndroidDrawableIcon(val drawable: Drawable) : AbstractMarkerIcon()
```

**Description**

This class extends `AbstractMarkerIcon` and is designed to work with Android `Drawable` objects. It includes logic to convert a `Drawable` into a `Bitmap`, which can then be used as a map marker.

#### **Parameters**

| Parameter | Type | Description |
| --- | --- | --- |
| `drawable` | `Drawable` | The `Drawable` resource to be used for the marker icon. |

#### **Protected Functions**

##### `toBitmap()`

Converts a `Drawable` into a `Bitmap` of a specified size.

**Signature**
```kotlin
protected fun toBitmap(drawable: Drawable, width: Int, height: Int): Bitmap
```

**Description**

This function creates a `Bitmap` from a `Drawable`. It contains an optimization for `BitmapDrawable` instances: if debug mode is disabled, it scales the underlying bitmap directly for better performance. For all other `Drawable` types, or when debug mode is enabled, it creates a new `Bitmap` and draws the `Drawable` onto it. If the `debug` property is `true`, it will also render a debug frame around the bitmap.

**Parameters**

| Parameter | Type | Description |
| --- | --- | --- |
| `drawable` | `Drawable` | The `Drawable` to be converted. |
| `width` | `Int` | The target width of the output `Bitmap` in pixels. |
| `height` | `Int` | The target height of the output `Bitmap` in pixels. |

**Returns**

| Type | Description |
| --- | --- |
| `Bitmap` | The resulting `Bitmap` representation of the `Drawable`. |