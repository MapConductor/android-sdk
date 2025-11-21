---
title: MapLibre Setup
---

This section covers the setup process for MapLibre Native Android integration with MapConductor.

> **Important**: MapConductor provides a unified API layer on top of existing map SDKs. You must set up the MapLibre Native Android SDK independently before using MapConductor's MapLibre integration.

## Prerequisites

- Android development environment
- Tile server URL (e.g., OpenStreetMap, Maptiler, or self-hosted tiles)
- Style JSON URL or configuration

## Setup Steps

### 1. Tile Server Selection

MapLibre is an open-source mapping library that requires a tile server. You have several options:

**Free/Community Options:**
- [OpenStreetMap tiles](https://wiki.openstreetmap.org/wiki/Raster_tile_providers) - Free but requires attribution
- [Protomaps](https://protomaps.com/) - Open data with self-hosting options

**Commercial Providers:**
- [Maptiler](https://www.maptiler.com/) - Free tier available
- [Stadia Maps](https://stadiamaps.com/) - Free tier available
- [MapTiler Cloud](https://cloud.maptiler.com/) - Generous free tier

**Self-Hosted:**
- Run your own tile server using tools like [tileserver-gl](https://github.com/maptiler/tileserver-gl)

### 2. Gradle Configuration

Add the dependencies to your app's `build.gradle.kts`:

```kotlin
// App build.gradle.kts
dependencies {
    // MapLibre Native Android SDK (version managed via libs.versions.toml)
    implementation(libs.maplibre.android)

    // MapConductor BOM for version management (v1.1.0)
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.0"))

    // MapConductor modules (versions managed by BOM)
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-maplibre")
}
```

The MapLibre SDK is available from Maven Central, which should already be configured in your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

### 3. Android Manifest Configuration

Add the required permissions to your `AndroidManifest.xml`:

```xml
<application>
    <!-- Add internet permission for tile loading -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
</application>
```

### 4. Style Configuration

MapLibre requires a style JSON to define how the map looks. You can use a hosted style or provide a custom one.

#### Option A: Using a Hosted Style (Recommended)

If using a commercial tile provider like Maptiler:

```kotlin
val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=YOUR_API_KEY"
```

#### Option B: Using OpenStreetMap Styles

For free OpenStreetMap-based styles:

```kotlin
// Example using OSM Liberty style
val styleUrl = "https://tiles.openfreemap.org/styles/liberty"
```

#### Option C: Custom Style JSON

You can also define your own style inline or load from assets:

```kotlin
val customStyle = """
{
  "version": 8,
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [{
    "id": "osm",
    "type": "raster",
    "source": "osm"
  }]
}
""".trimIndent()
```

### 5. API Key Configuration (If Required)

If your tile provider requires an API key (e.g., Maptiler), add it to your `secrets.properties`:

```properties
# secrets.properties
MAPLIBRE_API_KEY=your_api_key_here
```

And configure the Secrets Gradle Plugin in your root `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}
```

Apply the plugin in your app's `build.gradle.kts`:

```kotlin
plugins {
    // ... other plugins
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}
```

**Important**:
- Never commit your `secrets.properties` file to version control. Add it to `.gitignore`.
- Many MapLibre tile sources are free but require proper attribution

### 6. Setting the Style URL

When using MapConductor's MapLibre integration, set the style URL through the state:

```kotlin
@Composable
fun MapLibreExample() {
    val mapState = rememberMapLibreMapViewState(
        styleUrl = "https://tiles.openfreemap.org/styles/liberty"
    )

    MapLibreMapView(state = mapState) {
        // Your map content
    }
}
```

## Verification

To verify your MapLibre setup:

1. Build and run your app
2. Check that the map tiles load correctly
3. Test map interactions (zoom, pan, rotate)
4. Verify attribution is displayed if required by your tile provider

```kotlin
@Composable
fun TestMapLibre() {
    val mapState = rememberMapLibreMapViewState(
        styleUrl = "https://tiles.openfreemap.org/styles/liberty"
    )

    MapLibreMapView(state = mapState) {
        // If this displays correctly, your setup is working
    }
}
```

## Troubleshooting

### Common Issues

**Map not displaying (blank screen)**

- Verify the style URL is accessible and correct
- Check internet connectivity for tile loading
- Ensure the style JSON is valid
- Check Logcat for network or parsing errors

**Tiles not loading**

- Verify your tile server URL is correct
- Check rate limits for free tile services
- Ensure proper API key if using commercial providers
- Verify network permissions are granted

**Attribution missing**

- Most tile providers require attribution to be displayed
- Check your tile provider's terms of service
- Add proper attribution text to your UI

**Build errors**

- Ensure MapLibre SDK version is compatible
- Verify Maven Central repository is configured
- Check for dependency conflicts with other map SDKs

## Attribution Requirements

Many tile servers require attribution. Common examples:

- **OpenStreetMap**: "© OpenStreetMap contributors"
- **Maptiler**: "© MapTiler © OpenStreetMap contributors"
- **Stadia Maps**: Check their specific requirements

Make sure to display required attributions in your app UI according to your tile provider's terms.

## Performance Tips

- Use vector tiles when possible for better performance and smaller downloads
- Consider caching tiles for offline use
- Use appropriate zoom levels for your use case
- Monitor network usage if using metered connections

## Next Steps

Once MapLibre is properly configured, you can use MapConductor's `MapLibreMapView` component with the unified API.
