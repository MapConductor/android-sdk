Excellent. Here is the high-quality SDK documentation for the provided code snippet.

***

# MapConductor Core SDK

This document provides detailed information about the `ResourceProvider` object and the `IconResource` data class from the MapConductor Core library.

## `IconResource`

A data class that represents a drawable resource for an icon, containing its dimensions, anchor points, and a reference to the resource.

### Signature

```kotlin
data class IconResource(
    val name: String,
    val width: Double,
    val height: Double,
    val anchorX: Double,
    val anchorY: Double
)
```

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `name` | `String` | The unique identifier for the icon. |
| `width` | `Double` | The width of the icon in device-independent pixels (dp). |
| `height` | `Double` | The height of the icon in device-independent pixels (dp). |
| `anchorX` | `Double` | The horizontal anchor point as a fraction of the width (from 0.0 for the left edge to 1.0 for the right edge). |
| `anchorY` | `Double` | The vertical anchor point as a fraction of the height (from 0.0 for the top edge to 1.0 for the bottom edge). |

---

## `ResourceProvider`

A singleton object that provides utility functions for accessing and converting Android resources, such as display metrics, densities, and dimension units (dp, sp, px).

**Important:** You must initialize the `ResourceProvider` by calling the `init()` method before using any other functions in this object.

### `init`

Initializes the `ResourceProvider` with the application context. This method **must** be called once, typically in your `Application` class's `onCreate` method, before any other methods of this object are used.

#### Signature

```kotlin
fun init(context: Context)
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `context` | `Context` | The application context. Using `context.applicationContext` is recommended to avoid memory leaks. |

#### Example

```kotlin
// In your custom Application class
import android.app.Application
import com.mapconductor.core.ResourceProvider

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ResourceProvider.init(this)
    }
}
```

---

### `getDisplayMetrics`

Retrieves the system's current display metrics.

#### Signature

```kotlin
fun getDisplayMetrics(): DisplayMetrics
```

#### Returns

| Type | Description |
| :--- | :--- |
| `DisplayMetrics` | An object containing information about the display, such as its size, density, and font scaling. |

---

### `getSystemConfiguration`

Retrieves the system's current configuration.

#### Signature

```kotlin
fun getSystemConfiguration(): Configuration
```

#### Returns

| Type | Description |
| :--- | :--- |
| `Configuration` | An object describing all device configuration information, such as screen orientation, font scale, etc. |

---

### `getDensity`

Gets the logical density of the display. This is a scaling factor used to convert between dp units and pixel units.

#### Signature

```kotlin
fun getDensity(): Float
```

#### Returns

| Type | Description |
| :--- | :--- |
| `Float` | The screen's logical density factor. |

---

### `setBitmapDensity`

Sets a custom density to be used for bitmap creation. This is particularly useful for map providers that automatically scale bitmaps based on their `density` property. If `null` is provided, the system's default density will be used.

#### Signature

```kotlin
fun setBitmapDensity(density: Float?)
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `density` | `Float?` | The density to use for bitmap creation, or `null` to reset to the system density. |

---

### `getBitmapDensity`

Gets the density that should be used for bitmap creation. It returns the custom override density if one has been set via `setBitmapDensity()`, otherwise, it falls back to the system's display density.

#### Signature

```kotlin
fun getBitmapDensity(): Float
```

#### Returns

| Type | Description |
| :--- | :--- |
| `Float` | The density to use for bitmaps. |

---

### `dpToPx`

Converts a value from device-independent pixels (dp) to physical pixels (px) based on the screen's density. Overloads are available for `Float`, `Dp`, and `Double` types.

#### Signature

```kotlin
fun dpToPx(dp: Double): Double
fun dpToPx(dp: Float): Double
fun dpToPx(dp: Dp): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `dp` | `Double` \| `Float` \| `Dp` | The value in device-independent pixels (dp). |

#### Returns

| Type | Description |
| :--- | :--- |
| `Double` | The equivalent value in physical pixels (px). |

---

### `dpToPxForBitmap`

Converts a dp value to pixels specifically for creating bitmaps. This method calculates the pixel size using the device's actual screen density, which is suitable for creating the raw bitmap data. The `bitmapDensityOverride` (set via `setBitmapDensity`) is intended to be set on the `Bitmap.density` property *after* creation, not for calculating the pixel dimensions. Overloads are available for `Float` and `Dp` types.

#### Signature

```kotlin
fun dpToPxForBitmap(dp: Double): Double
fun dpToPxForBitmap(dp: Float): Double
fun dpToPxForBitmap(dp: Dp): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `dp` | `Double` \| `Float` \| `Dp` | The value in device-independent pixels (dp). |

#### Returns

| Type | Description |
| :--- | :--- |
| `Double` | The equivalent value in physical pixels (px) for bitmap dimensions. |

---

### `pxToSp`

Converts a value from physical pixels (px) to scale-independent pixels (sp). This calculation accounts for both the screen density and the user's font size preference.

#### Signature

```kotlin
fun pxToSp(px: Double): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `px` | `Double` | The value in physical pixels (px). |

#### Returns

| Type | Description |
| :--- | :--- |
| `Double` | The equivalent value in scale-independent pixels (sp). |

---

### `spToPx`

Converts a value from scale-independent pixels (sp) to physical pixels (px). This conversion considers the user's font size preference. Overloads are available for `Float`, `TextUnit`, and `Double` types.

#### Signature

```kotlin
fun spToPx(sp: Double): Double
fun spToPx(sp: Float): Double
fun spToPx(sp: TextUnit): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `sp` | `Double` \| `Float` \| `TextUnit` | The value in scale-independent pixels (sp). |

#### Returns

| Type | Description |
| :--- | :--- |
| `Double` | The equivalent value in physical pixels (px). |

---

### `getFontScale`

Gets the scaling factor for fonts, based on the user's font size preference in the system settings.

#### Signature

```kotlin
fun getFontScale(): Float
```

#### Returns

| Type | Description |
| :--- | :--- |
| `Float` | The font scaling factor. |

---

### `getEffectiveScaledDensity`

Calculates the effective scaled density. This method provides a consistent way to get the scaled density across different Android versions. On Android 14 (API 34) and higher, it is calculated as `density * fontScale`. On older versions, it returns the value of `DisplayMetrics.scaledDensity`.

#### Signature

```kotlin
fun getEffectiveScaledDensity(): Float
```

#### Returns

| Type | Description |
| :--- | :--- |
| `Float` | The effective scaled density. |

---

### `getOptimalTileSize`

Determines the optimal tile size for components like map tiles based on the device's screen density. It returns 512 for high-density screens (density >= 2.0) and 256 for lower-density screens.

#### Signature

```kotlin
fun getOptimalTileSize(): Int
```

#### Returns

| Type | Description |
| :--- | :--- |
| `Int` | The optimal tile size in pixels (either 256 or 512). |