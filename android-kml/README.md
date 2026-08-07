# MapConductor KML Layer

`android-kml` adds a tile-rendered KML overlay to MapConductor map views. It parses OGC KML 2.2
documents into feature models, renders them through MapConductor's raster tile layer pipeline,
and provides hit-testing for feature selection.

It shares the tile-rendering architecture of `android-geojson-layer`: rendering is tile based and
parsed features are supplied as lightweight data objects, so it scales to large KML datasets.

## Features

- Parses `Point`, `LineString`, `LinearRing`, `Polygon` (with `innerBoundaryIs` holes),
  and `MultiGeometry`.
- Traverses nested `<Document>` and `<Folder>` containers.
- Resolves KML styling: `LineStyle` (color, width), `PolyStyle` (color, fill, outline),
  and `IconStyle` (color), including shared `<Style>` / `<StyleMap>` references via `styleUrl`.
  KML `aabbggrr` colors are converted to Android ARGB.
- Reads `<name>`, `<description>`, and `<ExtendedData>` (`Data`/`SchemaData`) into feature
  properties.
- Supports static bulk features with `KMLFeature`.
- Supports reactive Compose features with `KMLFeatureState`, `KMLFeature`, and `KMLFeatures`.
- Supports layer-level and feature-level styling with a pluggable `KMLStyleProviderInterface`.
- Provides touch hit-testing through `KMLLayerState.processClick`.

## Installation

When developing inside the MapConductor SDK repository, include the local Gradle module:

```kotlin
dependencies {
    implementation(project(":android-kml"))
}
```

For published artifacts, use the configured MapConductor coordinates:

```kotlin
dependencies {
    implementation("com.mapconductor:kml:<version>")
}
```

The module depends on MapConductor core and the Jetpack Compose runtime.

## Basic Usage

Parse a KML file from assets and render it inside any MapConductor map view content scope:

```kotlin
@Composable
fun MapViewScope.KmlExample(context: Context) {
    var features by remember { mutableStateOf<List<KMLFeature>>(emptyList()) }

    val layerState =
        remember {
            KMLLayerState(
                // Fallback style used when a placemark carries no KML <Style>.
                strokeColor = Color.argb(255, 250, 36, 29),
                fillColor = Color.argb(96, 250, 36, 29),
                strokeWidth = 3f,
                pointRadius = 8f,
                onClick = { feature, position ->
                    // feature.properties holds <name>, <description>, and <ExtendedData> values
                },
            )
        }

    LaunchedEffect(Unit) {
        features =
            withContext(Dispatchers.IO) {
                context.assets.open("sample.kml").use { KMLParser.parse(it) }
            }
    }

    KMLLayer(state = layerState, features = features)
}
```

Call `KMLLayerState.processClick` from your map's `onMapClick` handler to hit-test features:

```kotlin
onMapClick = { clicked ->
    layerState.processClick(clicked, pixelTolerance = 12.0, zoom = state.cameraPosition.zoom)
}
```

## Styling

Each `KMLFeature` produced by `KMLParser` carries the stroke/fill/width resolved from its KML
style. Feature-level values take precedence over the `KMLLayerState` defaults. To fully customize
resolution (for example, coloring by a property value), supply a `KMLStyleProviderInterface`:

```kotlin
layerState.styleProvider =
    KMLStyleProviderInterface { feature, default ->
        val color = if (feature.properties["category"] == "station") Color.GREEN else default.fillColor
        default.copy(fillColor = color)
    }
```

## License

Apache License 2.0. See the repository `LICENSE` file.
