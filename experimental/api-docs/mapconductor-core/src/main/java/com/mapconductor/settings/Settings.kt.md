Here is the high-quality SDK documentation for the given code snippet.

***

# Settings

The `Settings` sealed class and related objects provide a comprehensive way to configure the appearance and behavior of map components and their interactions.

## `Settings`

A sealed class that encapsulates all configuration options. You can extend this class to create your own custom settings profile or use the provided `Settings.Default` object for a standard configuration.

### Signature

```kotlin
sealed class Settings(
    val tapTolerance: Dp,
    val markerDropAnimateDuration: Long,
    val markerBounceAnimateDuration: Long,
    val iconSize: Dp,
    val iconStroke: Dp,
    val composeEventDebounce: Duration,
)
```

### Parameters

These are the constructor parameters for the `Settings` class. You must provide these values when creating a custom settings implementation.

| Parameter                   | Type                 | Description                                                                                                                            |
| --------------------------- | -------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `tapTolerance`              | `Dp`                 | The distance in density-independent pixels (Dp) a touch can move before it's no longer considered a tap. Useful for distinguishing taps from drags. |
| `markerDropAnimateDuration` | `Long`               | The duration in milliseconds for the marker "drop" animation when it first appears on the map.                                         |
| `markerBounceAnimateDuration` | `Long`               | The duration in milliseconds for the marker "bounce" animation, typically used to highlight a selected marker.                         |
| `iconSize`                  | `Dp`                 | The default size for marker icons. It is recommended to use a value from `MarkerIconSize`.                                             |
| `iconStroke`                | `Dp`                 | The stroke width for marker icons in density-independent pixels (Dp).                                                                  |
| `composeEventDebounce`      | `kotlin.time.Duration` | The debounce duration for processing frequent UI events (like map panning) to optimize performance and prevent excessive recompositions. |

---

## `Settings.Default`

A singleton object that provides a default, ready-to-use configuration. This is the recommended starting point for any map implementation.

### Signature

```kotlin
object Default : Settings
```

### Description

The `Default` object extends `Settings` with a set of sensible default values for a typical map use case.

### Default Values

| Property                    | Default Value                 |
| --------------------------- | ----------------------------- |
| `tapTolerance`              | `14.dp`                       |
| `markerDropAnimateDuration` | `300` (ms)                    |
| `markerBounceAnimateDuration` | `2000` (ms)                   |
| `iconSize`                  | `MarkerIconSize.Regular` (48.dp) |
| `iconStroke`                | `1.dp`                        |
| `composeEventDebounce`      | `5.milliseconds`              |

---

## `MarkerIconSize`

A helper object that provides a set of predefined, standard `Dp` values for marker icon sizes. Using these constants helps maintain visual consistency across your application.

### Signature

```kotlin
object MarkerIconSize
```

### Properties

| Property  | Value   | Description                  |
| --------- | ------- | ---------------------------- |
| `Small`   | `32.dp` | A small-sized icon.          |
| `Regular` | `48.dp` | A standard, medium-sized icon. |
| `Large`   | `60.dp` | A large-sized icon.          |

---

### Example

The following examples demonstrate how to use the default settings and how to create and apply a custom settings configuration.

#### 1. Using Default Settings

You can easily apply the default settings by passing `Settings.Default` to your component or by using it as a default parameter value.

```kotlin
import com.mapconductor.settings.Settings

// A composable that accepts a Settings object, defaulting to Settings.Default
@Composable
fun MyMapComponent(settings: Settings = Settings.Default) {
    // Use settings properties to configure the map
    val markerSize = settings.iconSize
    // ...
}

// Usage in your UI
@Composable
fun AppScreen() {
    // This will use the default settings automatically
    MyMapComponent()
}
```

#### 2. Creating and Using Custom Settings

To create a custom configuration, define a new object that inherits from `Settings` and overrides the desired properties.

```kotlin
import com.mapconductor.settings.Settings
import com.mapconductor.settings.MarkerIconSize
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds

// Define a custom settings profile with larger icons and faster animations
object FastAndLargeSettings : Settings(
    tapTolerance = 16.dp,
    markerDropAnimateDuration = 150, // Faster drop animation
    markerBounceAnimateDuration = 1000, // Faster bounce animation
    iconSize = MarkerIconSize.Large, // Use a predefined large size
    iconStroke = 1.5.dp,
    composeEventDebounce = 10.milliseconds
)

// Usage in your UI
@Composable
fun AppScreen() {
    // Pass your custom settings object to the component
    MyMapComponent(settings = FastAndLargeSettings)
}
```