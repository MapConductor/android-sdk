# Icons Module (Experimental)

The `mapconductor-icons` module provides custom-drawn marker icons with programmatic styling. This experimental module offers vector-style icons that can be customized with colors, sizes, and other properties at runtime.

> **⚠️ Experimental Module**: This module is experimental and may change significantly in future versions. Use in production with caution.

## Overview

The icons module creates high-quality marker icons using Canvas drawing operations, providing:
- **Scalable Vector Graphics**: Icons scale smoothly at any size
- **Runtime Customization**: Change colors, sizes, and properties dynamically
- **Optimized Caching**: Automatic bitmap caching for performance
- **Consistent Appearance**: Same visual style across all map providers

## Installation

Add the icons module to your `build.gradle`:

```kotlin
dependencies {
    implementation "com.mapconductor:mapconductor-icons"

    // Required: Bom module
    implementation "com.mapconductor:mapconductor-bom:$version"
    // Required: Core module
    implementation "com.mapconductor:core"

    // Choose your map provider
    implementation "com.mapconductor:for-googlemaps"
}
```

## Available Icons

### CircleIcon

A simple circular marker icon with customizable fill and stroke:

```kotlin
import com.mapconductor.icons.CircleIcon

// Basic circle icon
val basicCircle = CircleIcon()

// Customized circle icon
val customCircle = CircleIcon(
    fillColor = Color.Blue,
    strokeColor = Color.White,
    strokeWidth = 2.dp,
    scale = 1.2f,
    iconSize = 32.dp
)
```

#### CircleIcon Properties

- **`fillColor: Color`**: Interior color of the circle (default: `Color.Red`)
- **`strokeColor: Color`**: Border color (default: `Color.White`)
- **`strokeWidth: Dp`**: Border thickness (default: from Settings)
- **`scale: Float`**: Size multiplier (default: `1.0f`)
- **`iconSize: Dp`**: Base size of the icon (default: from Settings)
- **`debug: Boolean`**: Show debug outline (default: `false`)

### FlagIcon

A flag-style marker icon with pole and customizable flag:

```kotlin
import com.mapconductor.icons.FlagIcon

// Basic flag icon
val basicFlag = FlagIcon()

// Customized flag icon
val customFlag = FlagIcon(
    fillColor = Color.Green,
    strokeColor = Color.Black,
    strokeWidth = 1.5.dp,
    scale = 1.0f,
    iconSize = 40.dp
)
```

#### FlagIcon Properties

- **`fillColor: Color`**: Color of the flag and pole (default: `Color.Red`)
- **`strokeColor: Color`**: Outline color (default: `Color.White`)
- **`strokeWidth: Dp`**: Outline thickness (default: from Settings)
- **`scale: Float`**: Size multiplier (default: `1.0f`)
- **`iconSize: Dp`**: Base size of the icon (default: from Settings)
- **`debug: Boolean`**: Show debug outline (default: `false`)

## Basic Usage

### Simple Icon Usage

```kotlin
@Composable
fun BasicIconExample() {
    val circleIcon = CircleIcon(
        fillColor = Color.Blue,
        strokeColor = Color.White
    )

    val flagIcon = FlagIcon(
        fillColor = Color.Red,
        strokeColor = Color.Black
    )

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = circleIcon
        )

        Marker(
            position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
            icon = flagIcon
        )
    }
}
```

### Dynamic Icon Customization

```kotlin
@Composable
fun DynamicIconExample() {
    var iconColor by remember { mutableStateOf(Color.Red) }
    var iconSize by remember { mutableStateOf(32.dp) }

    val dynamicIcon = CircleIcon(
        fillColor = iconColor,
        strokeColor = Color.White,
        iconSize = iconSize
    )

    Column {
        // Color picker
        Row {
            Button(onClick = { iconColor = Color.Red }) { Text("Red") }
            Button(onClick = { iconColor = Color.Blue }) { Text("Blue") }
            Button(onClick = { iconColor = Color.Green }) { Text("Green") }
        }

        // Size slider
        Slider(
            value = iconSize.value,
            onValueChange = { iconSize = it.dp },
            valueRange = 16f..64f
        )
        Text("Size: ${iconSize.value.toInt()}dp")

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
        MapView(state = mapViewState) {
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = dynamicIcon
            )
        }
    }
}
```

## Advanced Usage

### Category-Based Icons

```kotlin
@Composable
fun CategoryIconExample() {
    data class POI(
        val name: String,
        val category: String,
        val position: GeoPoint
    ) : java.io.Serializable

    val pois = listOf(
        POI("Restaurant", "food", GeoPointImpl.fromLatLong(37.7749, -122.4194)),
        POI("Hotel", "lodging", GeoPointImpl.fromLatLong(37.7849, -122.4094)),
        POI("Gas Station", "fuel", GeoPointImpl.fromLatLong(37.7649, -122.4294))
    )

    fun getIconForCategory(category: String) = when (category) {
        "food" -> CircleIcon(fillColor = Color.Red, strokeColor = Color.White)
        "lodging" -> FlagIcon(fillColor = Color.Blue, strokeColor = Color.White)
        "fuel" -> CircleIcon(fillColor = Color.Yellow, strokeColor = Color.Black)
        else -> CircleIcon(fillColor = Color.Gray, strokeColor = Color.White)
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        pois.forEach { poi ->
            Marker(
                position = poi.position,
                icon = getIconForCategory(poi.category),
                extra = poi.name
            )
        }
    }
}
```

### Icon Theming

```kotlin
object IconTheme {
    data class Theme(
        val primaryColor: Color,
        val secondaryColor: Color,
        val strokeColor: Color,
        val strokeWidth: Dp
    ) : java.io.Serializable

    val light = Theme(
        primaryColor = Color(0xFF2196F3),
        secondaryColor = Color(0xFFFFFFFF),
        strokeColor = Color(0xFF000000),
        strokeWidth = 1.dp
    )

    val dark = Theme(
        primaryColor = Color(0xFF1976D2),
        secondaryColor = Color(0xFF424242),
        strokeColor = Color(0xFFFFFFFF),
        strokeWidth = 1.dp
    )
}

@Composable
fun ThemedIconExample() {
    val isDarkTheme = isSystemInDarkTheme()
    val theme = if (isDarkTheme) IconTheme.dark else IconTheme.light

    val themedCircle = CircleIcon(
        fillColor = theme.primaryColor,
        strokeColor = theme.strokeColor,
        strokeWidth = theme.strokeWidth
    )

    val themedFlag = FlagIcon(
        fillColor = theme.primaryColor,
        strokeColor = theme.strokeColor,
        strokeWidth = theme.strokeWidth
    )

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = themedCircle
        )

        Marker(
            position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
            icon = themedFlag
        )
    }
}
```

### Icon Animation

```kotlin
@Composable
fun AnimatedIconExample() {
    var scale by remember { mutableStateOf(1.0f) }
    var color by remember { mutableStateOf(Color.Red) }

    // Animate scale
    LaunchedEffect(Unit) {
        while (true) {
            animate(
                initialValue = 1.0f,
                targetValue = 1.5f,
                animationSpec = tween(1000)
            ) { value, _ -> scale = value }

            animate(
                initialValue = 1.5f,
                targetValue = 1.0f,
                animationSpec = tween(1000)
            ) { value, _ -> scale = value }
        }
    }

    // Animate color
    LaunchedEffect(Unit) {
        val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow)
        var index = 0
        while (true) {
            delay(2000)
            index = (index + 1) % colors.size
            color = colors[index]
        }
    }

    val animatedIcon = CircleIcon(
        fillColor = color,
        strokeColor = Color.White,
        scale = scale
    )

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = animatedIcon
        )
    }
}
```

## Icon Properties and Behavior

### Anchor Points

Icons have specific anchor points that determine how they're positioned relative to the geographic coordinate:

- **CircleIcon**: Anchored at left-center (0.0, 0.5)
- **FlagIcon**: Anchored near the base of the pole (0.176, 0.91)

### Info Window Anchors

Info windows (if supported by the provider) anchor to different points:

- **CircleIcon**: Center of the circle (0.5, 0.5)
- **FlagIcon**: Top of the flag (0.5, 0.0)

### Performance Considerations

#### Bitmap Caching

Icons automatically cache their rendered bitmaps based on property hash:

```kotlin
// These will share the same cached bitmap
val icon1 = CircleIcon(fillColor = Color.Red, strokeColor = Color.White)
val icon2 = CircleIcon(fillColor = Color.Red, strokeColor = Color.White)

// This will create a new cached bitmap
val icon3 = CircleIcon(fillColor = Color.Blue, strokeColor = Color.White)
```

#### Memory Management

- Cached bitmaps are automatically managed
- Icons with identical properties share bitmap instances
- Large icons use more memory - use appropriate sizes

## Debugging Icons

Enable debug mode to visualize icon boundaries and anchor points:

```kotlin
@Composable
fun DebugIconExample() {
    val debugIcon = CircleIcon(
        fillColor = Color.Red,
        strokeColor = Color.White,
        debug = true  // Show debug outline and crosshairs
    )

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = debugIcon
        )
    }
}
```

Debug mode shows:
- Icon bounding rectangle (black outline)
- Center crosshairs (black lines)
- Actual drawn content

## Custom Icon Development

### Extending AbstractMarkerIcon

To create custom icons, extend `AbstractMarkerIcon`:

```kotlin
class CustomIcon(
    private val fillColor: Color = Color.Blue,
    override val scale: Float = 1.0f,
    override val iconSize: Dp = 32.dp,
    override val debug: Boolean = false
) : AbstractMarkerIcon() {

    override val anchor: Offset = Offset(0.5f, 0.5f)
    override val infoAnchor: Offset = Offset(0.5f, 0.0f)

    override fun toBitmapIcon(): BitmapIcon {
        val id = "custom_icon_${hashCode()}".hashCode()
        BitmapIconCache.get(id)?.let { return it }

        val canvasSize = ResourceProvider.dpToPx(iconSize.value * scale)
        val bitmap = createBitmap(canvasSize.toInt(), canvasSize.toInt())
        val canvas = Canvas(bitmap)

        // Custom drawing code here
        val paint = Paint().apply {
            color = fillColor.toArgb()
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        canvas.drawRect(0f, 0f, canvasSize.toFloat(), canvasSize.toFloat(), paint)

        val result = BitmapIcon(
            bitmap = bitmap,
            anchor = anchor,
            size = Size(canvasSize.toFloat(), canvasSize.toFloat())
        )
        BitmapIconCache.put(id, result)
        return result
    }
}
```

## Best Practices

1. **Consistent Sizing**: Use consistent icon sizes for similar marker types
2. **Color Accessibility**: Ensure sufficient contrast between fill and stroke colors
3. **Performance**: Reuse identical icon instances to benefit from caching
4. **Scale Appropriately**: Consider map zoom levels when choosing icon sizes
5. **Test Across Providers**: Verify icon appearance on all target map providers

## Limitations

1. **Limited Icon Set**: Currently only CircleIcon and FlagIcon are available
2. **Static Shapes**: Icons are drawn programmatically, not from vector files
3. **Provider Differences**: Minor rendering differences may occur between map providers
4. **Memory Usage**: Large icons or many unique icon variations consume more memory

## Migration and Compatibility

This module is experimental and APIs may change. When migrating:

1. Test thoroughly with your specific use cases
2. Monitor memory usage with large numbers of unique icons
3. Have fallback options for critical functionality
4. Report issues to help improve the module

The icons module provides a foundation for custom marker styling while maintaining the unified MapConductor API across providers.