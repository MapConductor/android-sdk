Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

### `toArcGISColor()`

An internal extension function that converts a Jetpack Compose `Color` object into its equivalent ArcGIS Maps SDK `Color` representation.

### Signature

```kotlin
internal fun Color.toArcGISColor(): com.arcgismaps.Color
```

### Description

This function provides a convenient way to convert a `androidx.compose.ui.graphics.Color` instance to a `com.arcgismaps.Color`. It maps the float-based RGBA channel values (ranging from 0.0f to 1.0f) of the Compose `Color` to the integer-based RGBA values (ranging from 0 to 255) used by the ArcGIS `Color`.

As an `internal` function, it is designed for use only within its containing module and is not part of the public API.

### Parameters

| Parameter | Type                                  | Description                                           |
|-----------|---------------------------------------|-------------------------------------------------------|
| `this`    | `androidx.compose.ui.graphics.Color`  | The source Jetpack Compose `Color` instance to convert. |

### Returns

**Type:** `com.arcgismaps.Color`

A new `com.arcgismaps.Color` object that is visually identical to the source Compose `Color`.

### Example

Here is an example of how to convert a standard Compose `Color` to an ArcGIS `Color`.

```kotlin
import androidx.compose.ui.graphics.Color
// Assuming the extension function is accessible within the current scope
// import com.mapconductor.arcgis.toArcGISColor

// 1. Define a Jetpack Compose Color (e.g., a semi-transparent blue)
val composeBlue = Color(red = 0.0f, green = 0.0f, blue = 1.0f, alpha = 0.5f)

// 2. Convert it to an ArcGIS Color using the extension function
val arcGisBlue = composeBlue.toArcGISColor()

// The resulting arcGisBlue is a com.arcgismaps.Color instance
// with integer RGBA values: R=0, G=0, B=255, A=127.
println("ArcGIS Color: R=${arcGisBlue.red}, G=${arcGisBlue.green}, B=${arcGisBlue.blue}, A=${arcGisBlue.alpha}")
// Output: ArcGIS Color: R=0, G=0, B=255, A=127
```