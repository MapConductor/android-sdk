Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

***

# Heatmap SDK Documentation

This document provides detailed information about the classes and objects used for configuring heatmap visualizations.

## `HeatmapGradientStop`

A data class that represents a single color stop within a `HeatmapGradient`. Each stop defines a color at a specific point along the gradient's range.

### Signature

```kotlin
data class HeatmapGradientStop(
    val position: Double,
    val color: Int
)
```

### Description

`HeatmapGradientStop` is the basic building block for creating a custom heatmap gradient. It pairs a relative position with a color. A collection of these stops is used to initialize a `HeatmapGradient`.

### Parameters

| Parameter  | Type     | Description                                                                                             |
| :--------- | :------- | :------------------------------------------------------------------------------------------------------ |
| `position` | `Double` | The relative position of the color stop, where `0.0` is the start and `1.0` is the end of the gradient. |
| `color`    | `Int`    | The ARGB color integer for this stop. Use `android.graphics.Color` to create this value.                |

### Example

```kotlin
import android.graphics.Color

// A stop at 50% of the gradient with a yellow color
val yellowStop = HeatmapGradientStop(
    position = 0.5,
    color = Color.YELLOW
)

// A stop at the beginning of the gradient with a transparent blue color
val transparentBlueStop = HeatmapGradientStop(
    position = 0.0,
    color = Color.argb(128, 0, 0, 255)
)
```

---

## `HeatmapGradient`

A class that defines the color gradient used to render the heatmap. It is constructed from a list of `HeatmapGradientStop` objects.

### Signature

```kotlin
class HeatmapGradient(stops: List<HeatmapGradientStop>)
```

### Description

The `HeatmapGradient` class manages the color stops for a heatmap. Upon creation, it validates and sorts the provided stops.

**Constructor Validation:**
*   The list of stops must not be empty.
*   The `position` of each `HeatmapGradientStop` must be within the inclusive range of `[0.0, 1.0]`.
An `IllegalArgumentException` will be thrown if these conditions are not met.

### Parameters

| Parameter | Type                          | Description                                                                                                                            |
| :-------- | :---------------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `stops`   | `List<HeatmapGradientStop>` | A list of one or more `HeatmapGradientStop` objects that define the gradient. The list will be sorted by `position` internally. |

### Properties

| Property | Type                          | Description                                      |
| :------- | :---------------------------- | :----------------------------------------------- |
| `stops`  | `List<HeatmapGradientStop>` | The validated and sorted list of gradient stops. |

### Methods

#### `colors()`

Returns an array of color integers from the gradient stops.

*   **Signature:** `fun colors(): IntArray`
*   **Returns:** `IntArray` - An array of ARGB color integers, sorted according to the stop positions. This is useful for APIs that require a simple array of colors.

### Companion Object

#### `DEFAULT`

A predefined, default gradient.

*   **Signature:** `val DEFAULT: HeatmapGradient`
*   **Description:** Provides a ready-to-use gradient that transitions from green (at position `0.2`) to red (at position `1.0`).

### Example

```kotlin
import android.graphics.Color

// 1. Create a custom three-color gradient (blue -> yellow -> red)
val customGradient = HeatmapGradient(
    stops = listOf(
        HeatmapGradientStop(position = 1.0, color = Color.RED),
        HeatmapGradientStop(position = 0.5, color = Color.YELLOW),
        HeatmapGradientStop(position = 0.0, color = Color.BLUE)
    )
)

// Get the color array from the custom gradient
val colorArray: IntArray = customGradient.colors() // [Color.BLUE, Color.YELLOW, Color.RED]

// 2. Use the predefined default gradient
val defaultGradient = HeatmapGradient.DEFAULT
```

---

## `HeatmapDefaults`

A singleton object that provides default constant values for various heatmap properties.

### Signature

```kotlin
object HeatmapDefaults
```

### Description

Use `HeatmapDefaults` to access standard, sensible default values for configuring a heatmap layer. This helps ensure consistency and provides a fallback when user-defined values are not available.

### Properties

| Property              | Type     | Default Value | Description                                                                                             |
| :-------------------- | :------- | :------------ | :------------------------------------------------------------------------------------------------------ |
| `DEFAULT_RADIUS_PX`   | `Int`    | `20`          | The default radius in pixels for each data point on the heatmap.                                        |
| `DEFAULT_OPACITY`     | `Double` | `0.7`         | The default global opacity of the heatmap layer, from `0.0` (transparent) to `1.0` (opaque).            |
| `DEFAULT_MAX_ZOOM`    | `Int`    | `22`          | The default maximum map zoom level at which the heatmap is rendered.                                    |

### Example

```kotlin
// Example of using the default values to configure a heatmap
val heatmapOptions = HeatmapOptions(
    radius = userProvidedRadius ?: HeatmapDefaults.DEFAULT_RADIUS_PX,
    opacity = userProvidedOpacity ?: HeatmapDefaults.DEFAULT_OPACITY,
    maxZoom = HeatmapDefaults.DEFAULT_MAX_ZOOM
)
```