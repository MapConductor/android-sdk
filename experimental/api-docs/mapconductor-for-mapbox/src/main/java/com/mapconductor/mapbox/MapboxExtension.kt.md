Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

# Mapbox Utility Extensions

This document provides details on utility extension functions used for converting custom types into Mapbox-specific formats.

## `BitmapIcon.toPointAnnotationOptions()`

> **Note:** This is an `internal` function and is not part of the public API.

Converts a `BitmapIcon` object into a Mapbox `PointAnnotationOptions` object, which is used to display a marker on the map.

The function translates the relative anchor point of the `BitmapIcon` into a pixel offset required by Mapbox. It uses `IconAnchor.BOTTOM` as a base and calculates the difference to correctly position the icon according to the specified anchor.

### Signature
```kotlin
internal fun BitmapIcon.toPointAnnotationOptions(): PointAnnotationOptions
```

### Description
This extension function is called on a `BitmapIcon` instance. It reads the `bitmap` and `anchor` properties to create a fully configured `PointAnnotationOptions` object. The bitmap is copied to ensure it's mutable (`ARGB_8888`), which is a requirement for Mapbox annotations.

### Returns
| Type | Description |
| :--- | :--- |
| `PointAnnotationOptions` | An options object configured with the icon image, anchor, and calculated offset, ready to be used to create a point annotation on a Mapbox map. |

<br/>

---

## `Color.toMapboxColorString()`

Converts a Jetpack Compose `Color` object into a Mapbox-compatible RGBA color string.

### Signature
```kotlin
fun Color.toMapboxColorString(): String
```

### Description
This extension function is called on a `androidx.compose.ui.graphics.Color` instance. It transforms the color's float-based components (0.0 to 1.0) into the standard 0-255 range for RGB values. The resulting string is formatted as `"rgba(r, g, b, a)"`, which can be used in various Mapbox style properties.

### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `this` | `androidx.compose.ui.graphics.Color` | The Jetpack Compose `Color` instance to convert. |

### Returns
| Type | Description |
| :--- | :--- |
| `String` | A `String` representing the color in the `"rgba(r, g, b, a)"` format. |

### Example
```kotlin
import androidx.compose.ui.graphics.Color

// Convert a predefined color
val composeColor = Color.Red
val mapboxColorString = composeColor.toMapboxColorString()
// Result: "rgba(255.0, 0.0, 0.0, 1.0)"

// Convert a custom semi-transparent color
val semiTransparentBlue = Color(red = 0f, green = 0f, blue = 1f, alpha = 0.5f)
val mapboxColorString2 = semiTransparentBlue.toMapboxColorString()
// Result: "rgba(0.0, 0.0, 255.0, 0.5)"
```