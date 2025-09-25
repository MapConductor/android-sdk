# Marker Icons

MapConductor provides several marker icon types to customize the appearance of markers on the map. All marker icons implement the `MarkerIcon` interface and can be used with any map provider.

## MarkerIcon Interface

The base interface for all marker icons:

```kotlin
interface MarkerIcon {
    // Common properties for all marker icon implementations
}
```

## Icon Types

### DefaultIcon (ColorDefaultIcon)

The standard colored marker icon with customizable appearance and text labels.

> **Note**: `DefaultIcon` is an alias for `ColorDefaultIcon`.

```kotlin
DefaultIcon(
    scale: Float = 1.0f,
    label: String? = null,
    fillColor: Color = Color.Red,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    labelTextColor: Color = Color.White,
    labelStrokeColor: Color? = null,
    debug: Boolean = false
)
```

#### Parameters

- **`scale`**: Size multiplier for the icon (1.0 = normal size, 0.5 = half size, 2.0 = double size)
- **`label`**: Text displayed on the marker (optional)
- **`fillColor`**: Background color of the marker
- **`strokeColor`**: Border color of the marker
- **`strokeWidth`**: Width of the border
- **`labelTextColor`**: Color of the label text
- **`labelStrokeColor`**: Optional outline color for label text
- **`debug`**: Shows debug information when enabled

#### Usage Examples

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
    // Basic red marker
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon()
    )

    // Custom colored marker with label
    Marker(
        position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
        icon = DefaultIcon(
            scale = 1.2f,
            label = "SF",
            fillColor = Color.Blue,
            strokeColor = Color.White,
            strokeWidth = 2.dp
        )
    )

    // Small marker with custom styling
    Marker(
        position = GeoPointImpl.fromLatLong(37.7649, -122.4294),
        icon = DefaultIcon(
            scale = 0.8f,
            fillColor = Color.Green,
            labelTextColor = Color.Black,
            label = "POI"
        )
    )
}
```

### DrawableDefaultIcon

Uses a drawable resource as the background for the marker, with optional styling overlays.

```kotlin
DrawableDefaultIcon(
    backgroundDrawable: Drawable,
    scale: Float = 1.0f,
    strokeColor: Color? = null,
    strokeWidth: Dp = 1.dp
)
```

#### Parameters

- **`backgroundDrawable`**: The drawable resource used as the marker background
- **`scale`**: Size multiplier for the icon
- **`strokeColor`**: Optional border color overlay
- **`strokeWidth`**: Width of the border overlay

#### Usage Examples

```kotlin
@Composable
fun DrawableIconExamples() {
    val context = LocalContext.current

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        // Custom drawable marker
        AppCompatResources.getDrawable(context, R.drawable.custom_marker)?.let { drawable ->
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DrawableDefaultIcon(
                    backgroundDrawable = drawable,
                    scale = 1.0f
                )
            )
        }

        // Drawable with custom border
        AppCompatResources.getDrawable(context, R.drawable.pin_icon)?.let { drawable ->
            Marker(
                position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
                icon = DrawableDefaultIcon(
                    backgroundDrawable = drawable,
                    scale = 1.5f,
                    strokeColor = Color.Yellow,
                    strokeWidth = 3.dp
                )
            )
        }
    }
}
```

### ImageDefaultIcon

Uses a custom image drawable with precise anchor positioning control.

```kotlin
ImageDefaultIcon(
    drawable: Drawable,
    anchor: Offset = Offset(0.5f, 0.5f),
    debug: Boolean = false
)
```

#### Parameters

- **`drawable`**: The image drawable to display
- **`anchor`**: Anchor point relative to the image (0.0-1.0 range)
  - `Offset(0.5f, 0.5f)`: Center anchor (default)
  - `Offset(0.5f, 1.0f)`: Bottom center anchor
  - `Offset(0.0f, 0.0f)`: Top left anchor
  - `Offset(1.0f, 1.0f)`: Bottom right anchor
- **`debug`**: Shows debug anchor visualization

#### Usage Examples

```kotlin
@Composable
fun ImageIconExamples() {
    val context = LocalContext.current

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        // Weather station icon with bottom center anchor
        AppCompatResources.getDrawable(context, R.drawable.weather_station)?.let { icon ->
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = ImageDefaultIcon(
                    drawable = icon,
                    anchor = Offset(0.5f, 1.0f), // Bottom center
                    debug = false
                )
            )
        }

        // Direction arrow with center anchor
        AppCompatResources.getDrawable(context, R.drawable.direction_arrow)?.let { icon ->
            Marker(
                position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
                icon = ImageDefaultIcon(
                    drawable = icon,
                    anchor = Offset(0.5f, 0.5f), // Center
                    debug = true // Shows anchor point
                )
            )
        }
    }
}
```

## Advanced Usage

### Dynamic Icon Selection

```kotlin
@Composable
fun DynamicIconExample() {
    val iconType by remember { mutableStateOf("default") }
    val context = LocalContext.current

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        val icon = when (iconType) {
            "default" -> DefaultIcon(
                fillColor = Color.Blue,
                label = "D"
            )
            "drawable" -> AppCompatResources.getDrawable(context, R.drawable.custom_pin)?.let {
                DrawableDefaultIcon(backgroundDrawable = it)
            }
            "image" -> AppCompatResources.getDrawable(context, R.drawable.location_icon)?.let {
                ImageDefaultIcon(
                    drawable = it,
                    anchor = Offset(0.5f, 1.0f)
                )
            }
            else -> DefaultIcon()
        }

        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = icon
        )
    }
}
```

### Icon Factory Pattern

```kotlin
object MarkerIconFactory {
    fun createLocationIcon(color: Color, label: String? = null): MarkerIcon {
        return DefaultIcon(
            fillColor = color,
            strokeColor = Color.White,
            strokeWidth = 2.dp,
            label = label,
            scale = 1.0f
        )
    }

    fun createCustomIcon(context: Context, drawableRes: Int, scale: Float = 1.0f): MarkerIcon? {
        return AppCompatResources.getDrawable(context, drawableRes)?.let {
            DrawableDefaultIcon(
                backgroundDrawable = it,
                scale = scale
            )
        }
    }

    fun createDirectionalIcon(context: Context, direction: Float): MarkerIcon? {
        return AppCompatResources.getDrawable(context, R.drawable.arrow)?.let { drawable ->
            // Apply rotation to drawable if needed
            val rotatedDrawable = drawable.mutate()
            rotatedDrawable.setColorFilter(null)

            ImageDefaultIcon(
                drawable = rotatedDrawable,
                anchor = Offset(0.5f, 0.5f)
            )
        }
    }
}
```

## Best Practices

### Performance Considerations

1. **Reuse Icon Instances**: Create icons once and reuse them for multiple markers
2. **Optimize Drawable Resources**: Use appropriately sized drawable resources
3. **Limit Custom Drawables**: Too many unique drawables can impact memory usage

```kotlin
// Good: Reuse icon instances
val blueIcon = DefaultIcon(fillColor = Color.Blue)
val redIcon = DefaultIcon(fillColor = Color.Red)

markers.forEach { markerData ->
    val icon = if (markerData.isSelected) blueIcon else redIcon
    Marker(position = markerData.position, icon = icon)
}
```

### Visual Consistency

1. **Consistent Scaling**: Use consistent scale values across your application
2. **Color Schemes**: Maintain a consistent color palette for marker types
3. **Anchor Points**: Use appropriate anchors for icon types (bottom center for pins, center for circles)

### Accessibility

1. **Meaningful Labels**: Provide descriptive labels when using `DefaultIcon`
2. **Color Contrast**: Ensure good contrast between fill and stroke colors
3. **Alternative Indicators**: Don't rely solely on color to convey information

## Common Use Cases

### Status Indicators

```kotlin
enum class MarkerStatus { ACTIVE, INACTIVE, WARNING, ERROR }

fun createStatusIcon(status: MarkerStatus): MarkerIcon = DefaultIcon(
    fillColor = when (status) {
        MarkerStatus.ACTIVE -> Color.Green
        MarkerStatus.INACTIVE -> Color.Gray
        MarkerStatus.WARNING -> Color.Yellow
        MarkerStatus.ERROR -> Color.Red
    },
    strokeColor = Color.White,
    strokeWidth = 2.dp,
    scale = 1.0f
)
```

### Category Markers

```kotlin
sealed class PoiCategory(val icon: MarkerIcon) {
    object Restaurant : PoiCategory(DefaultIcon(fillColor = Color(0xFF4CAF50), label = "🍽️"))
    object Hotel : PoiCategory(DefaultIcon(fillColor = Color(0xFF2196F3), label = "🏨"))
    object GasStation : PoiCategory(DefaultIcon(fillColor = Color(0xFFFF9800), label = "⛽"))
    object Hospital : PoiCategory(DefaultIcon(fillColor = Color(0xFFF44336), label = "🏥"))
}
```

## Troubleshooting

### Common Issues

1. **Icons Not Displaying**: Verify drawable resources exist and are accessible
2. **Incorrect Positioning**: Check anchor points for custom icons
3. **Performance Issues**: Reduce number of unique icon instances
4. **Scale Problems**: Ensure scale values are positive and reasonable (0.1 - 3.0)

### Debug Mode

Enable debug mode to visualize anchor points and icon bounds:

```kotlin
Marker(
    position = position,
    icon = ImageDefaultIcon(
        drawable = customDrawable,
        anchor = Offset(0.5f, 1.0f),
        debug = true // Shows anchor point and bounds
    )
)
```