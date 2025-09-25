# Provider Compatibility

This page shows which MapConductor components are supported by each map provider. While MapConductor aims to provide a unified API, some features are not available on all map providers due to underlying SDK limitations.

## Component Support Matrix

| Component | Google Maps | Mapbox | HERE Maps | ArcGIS |
|-----------|-------------|---------|-----------|---------|
| **MapViewComponent** | ✅ | ✅ | ✅ | ✅ |
| **Marker** | ✅ | ✅ | ✅ | ✅ |
| **Circle** | ✅ | ✅ | ✅ | ✅ |
| **Polyline** | ✅ | ✅ | ✅ | ✅ |
| **Polygon** | ✅ | ✅ | ✅ | ✅ |
| **GroundImage** | ✅ | ❌ | ❌ | ❌ |

### Legend
- ✅ **Fully Supported**: Feature is available and tested
- ❌ **Not Supported**: Feature is not available due to SDK limitations
- ⚠️ **Limited Support**: Feature is available with restrictions (none currently)

## Detailed Compatibility Information

### Core Components (All Providers)

#### MapViewComponent
All map providers support the basic map view functionality including:
- Camera positioning and movement
- User interaction (pan, zoom, rotate, tilt)
- Map styling and appearance
- Event handling (tap, long press, camera change)

#### Marker
All providers support marker functionality with:
- Custom icons and colors
- Click events and interactions
- Drag and drop
- Info windows (where supported by provider)
- Clustering (through strategies)

#### Circle
All providers support circular overlays with:
- Center point and radius
- Fill and stroke styling
- Interactive events
- Dynamic updates

#### Polyline
All providers support polyline rendering with:
- Multiple point paths
- Stroke styling and width
- Pattern support (where available)
- Interactive events

#### Polygon
All providers support polygon rendering with:
- Closed shape definitions
- Fill and stroke styling
- Hole support (where available)
- Interactive events

### Provider-Specific Limitations

#### GroundImage (Google Maps Only)

GroundImage overlays are only supported on Google Maps because:

**Google Maps**: Native `GroundOverlay` API provides direct support for image overlays with geographic bounds.

**Mapbox**: No direct equivalent to ground overlays in the Mapbox Maps SDK. While custom layers could potentially implement similar functionality, it would require significant custom implementation.

**HERE Maps**: The HERE SDK does not provide built-in ground overlay functionality. Custom implementation would be complex and potentially unstable.

**ArcGIS**: While ArcGIS supports raster layers, the mobile SDK doesn't provide simple ground overlay functionality equivalent to Google Maps.

## Usage Considerations

### Provider Selection

When choosing a map provider, consider these compatibility factors:

#### Choose Google Maps if:
- You need GroundImage overlays
- You want the most complete feature set
- You're targeting primarily Android users

#### Choose Mapbox if:
- You need extensive map styling customization
- You want vector-based rendering
- You need offline map support

#### Choose HERE Maps if:
- You need advanced routing and navigation
- You want enterprise-grade mapping
- You need global coverage with local expertise

#### Choose ArcGIS if:
- You need GIS and enterprise features
- You want 3D mapping capabilities
- You need extensive spatial analysis

### Handling Unsupported Features

When using components that aren't supported on all providers:

#### Runtime Detection

```kotlin
@Composable
fun CompatibilityAwareMap() {
    val mapViewState = rememberGoogleMapViewState()
    val supportsGroundImage = mapViewState is GoogleMapViewStateImpl

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        // Always supported components
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon()
        )

        Circle(
            center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            radius = 1000.0,
            fillColor = Color.Blue.copy(alpha = 0.3f)
        )

        // Conditionally supported components
        if (supportsGroundImage) {
            val imageBounds = GeoRectBounds(
                southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
            )

            val context = LocalContext.current
            AppCompatResources.getDrawable(context, R.drawable.overlay_image)?.let { drawable ->
                GroundImage(
                    bounds = imageBounds,
                    image = drawable,
                    opacity = 0.7f
                )
            }
        }
    }
}
```

#### Graceful Degradation

```kotlin
@Composable
fun GracefulDegradationExample() {
    val mapViewState = remember { /* your map state */ }
    val isGoogleMaps = mapViewState is GoogleMapViewStateImpl

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        if (isGoogleMaps) {
            // Use GroundImage on Google Maps
            GroundImageOverlay()
        } else {
            // Fallback: Use polygon with pattern fill
            PolygonFallback()
        }
    }
}

@Composable
fun GroundImageOverlay() {
    val imageBounds = GeoRectBounds(
        southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
        northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
    )

    val context = LocalContext.current
    AppCompatResources.getDrawable(context, R.drawable.overlay_image)?.let { drawable ->
        GroundImage(
            bounds = imageBounds,
            image = drawable,
            opacity = 0.7f
        )
    }
}

@Composable
fun PolygonFallback() {
    // Approximate ground image with styled polygon
    val bounds = GeoRectBounds(
        southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
        northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
    )

    val sw = bounds.southWest!!
    val ne = bounds.northEast!!

    Polygon(
        points = listOf(
            sw,
            GeoPointImpl.fromLatLong(sw.latitude, ne.longitude),
            ne,
            GeoPointImpl.fromLatLong(ne.latitude, sw.longitude),
            sw
        ),
        fillColor = Color.Blue.copy(alpha = 0.3f),
        strokeColor = Color.Blue,
        strokeWidth = 2.dp
    )
}
```

#### Provider-Agnostic Abstractions

```kotlin
interface ImageOverlayStrategy {
    fun canDisplayImageOverlay(): Boolean

    @Composable
    fun ImageOverlay(
        bounds: GeoRectBounds,
        image: Drawable,
        opacity: Float
    )
}

class GoogleMapsImageStrategy : ImageOverlayStrategy {
    override fun canDisplayImageOverlay() = true

    @Composable
    override fun ImageOverlay(bounds: GeoRectBounds, image: Drawable, opacity: Float) {
        GroundImage(
            bounds = bounds,
            image = image,
            opacity = opacity
        )
    }
}

class FallbackImageStrategy : ImageOverlayStrategy {
    override fun canDisplayImageOverlay() = false

    @Composable
    override fun ImageOverlay(bounds: GeoRectBounds, image: Drawable, opacity: Float) {
        // Fallback implementation or empty
        Text("Image overlays not supported on this provider")
    }
}

@Composable
fun AdaptiveImageOverlay() {
    val strategy = when (mapViewState) {
        is GoogleMapViewStateImpl -> GoogleMapsImageStrategy()
        else -> FallbackImageStrategy()
    }

    if (strategy.canDisplayImageOverlay()) {
        strategy.ImageOverlay(
            bounds = imageBounds,
            image = overlayImage,
            opacity = 0.7f
        )
    }
}
```

## Future Compatibility

### Planned Features

Future MapConductor releases may include:
- Additional overlay types (heatmaps, tile overlays)
- Advanced clustering algorithms
- Offline map support

### Provider Updates

As map provider SDKs evolve, compatibility may change:
- New features may become available
- Existing features may be deprecated
- Performance characteristics may improve

### Migration Strategies

When provider compatibility changes:

1. **Version Pinning**: Pin MapConductor versions for stable feature sets
2. **Feature Detection**: Use runtime detection for new capabilities
3. **Gradual Migration**: Update applications incrementally
4. **Fallback Support**: Maintain fallback implementations

## Testing Across Providers

### Compatibility Testing

```kotlin
@RunWith(Parameterized::class)
class ProviderCompatibilityTest(
    private val providerName: String,
    private val stateFactory: () -> MapViewState
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun providers() = listOf(
            arrayOf("Google Maps", { GoogleMapViewStateImpl() }),
            arrayOf("Mapbox", { MapboxViewStateImpl() }),
            arrayOf("HERE Maps", { HereViewStateImpl() }),
            arrayOf("ArcGIS", { ArcGISMapViewStateImpl() })
        )
    }

    @Test
    fun testBasicComponents() {
        val state = stateFactory()

        // Test components that should work on all providers
        assertTrue("Markers should be supported", supportsMarkers(state))
        assertTrue("Circles should be supported", supportsCircles(state))
        assertTrue("Polylines should be supported", supportsPolylines(state))
        assertTrue("Polygons should be supported", supportsPolygons(state))
    }

    @Test
    fun testProviderSpecificFeatures() {
        val state = stateFactory()

        when (state) {
            is GoogleMapViewStateImpl -> {
                assertTrue("GroundImage should be supported on Google Maps",
                    supportsGroundImage(state))
            }
            else -> {
                assertFalse("GroundImage should not be supported on $providerName",
                    supportsGroundImage(state))
            }
        }
    }
}
```

## Best Practices

1. **Feature Detection**: Always check provider capabilities before using provider-specific features
2. **Graceful Degradation**: Provide fallbacks for unsupported features
3. **Documentation**: Document which providers your app supports
4. **Testing**: Test your app with all target providers
5. **User Communication**: Inform users about provider-specific limitations

Understanding provider compatibility helps you make informed decisions about map provider selection and ensures your application works consistently across different mapping platforms.