### `MapboxCircleLayer` Class

#### Description

The `MapboxCircleLayer` class encapsulates the creation and management of a Mapbox `CircleLayer` and its corresponding `GeoJsonSource`. It is specifically designed to render circles on a map that maintain a constant real-world radius (in meters) regardless of the map's zoom level. This is achieved by using a complex Mapbox expression that dynamically calculates the pixel radius based on the current zoom.

This class simplifies the process of adding data-driven, realistically-scaled circles to your map.

#### Constructor

##### Signature

```kotlin
MapboxCircleLayer(
    val sourceId: String,
    val layerId: String
)
```

##### Description

Creates a new instance of `MapboxCircleLayer`, initializing the underlying GeoJSON source and circle layer with the provided unique identifiers.

##### Parameters

| Parameter  | Type     | Description                                        |
|------------|----------|----------------------------------------------------|
| `sourceId` | `String` | A unique identifier for the `GeoJsonSource`.       |
| `layerId`  | `String` | A unique identifier for the `CircleLayer`.         |

---

### Properties

#### `layer`

The configured Mapbox `CircleLayer` instance. This layer is ready to be added to a `Style`. Its visual properties, such as color, stroke, and radius, are data-driven, meaning they are determined by the properties of the GeoJSON features supplied to the `source`.

##### Signature

```kotlin
val layer: CircleLayer
```

#### `source`

The `GeoJsonSource` that provides the data for the `layer`. You must add this source to the map's `Style` before adding the `layer`. The `draw()` method updates the data within this source.

##### Signature

```kotlin
val source: GeoJsonSource
```

---

### Methods

#### `draw`

Updates the map by drawing a new set of circles. This method takes a list of circle entities, converts them into a GeoJSON `FeatureCollection`, and applies it to the layer's `GeoJsonSource`. Any previously drawn circles from this layer are replaced.

##### Signature

```kotlin
fun draw(entities: List<CircleEntityInterface<MapboxActualCircle>>)
```

##### Parameters

| Parameter  | Type                                                      | Description                                                                                                                            |
|------------|-----------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `entities` | `List<CircleEntityInterface<MapboxActualCircle>>` | A list of entity objects representing the circles to be drawn on the map. Each entity must contain a `MapboxActualCircle` (GeoJSON Feature). |

---

### Nested Objects

#### `Prop`

An object that holds constant string keys for the properties of the GeoJSON features. These keys are used to link feature data to the layer's style attributes. When creating the GeoJSON features for your circles, you must add properties using these keys to control their appearance.

##### Signature

```kotlin
object Prop
```

##### Properties

| Property              | Type     | Description                                                                                             |
|-----------------------|----------|---------------------------------------------------------------------------------------------------------|
| `RADIUS`              | `String` | The key for the circle's radius in meters.                                                              |
| `LATITUDE_CORRECTION` | `String` | The key for the latitude correction factor, used for accurate radius scaling at different latitudes.      |
| `FILL_COLOR`          | `String` | The key for the circle's fill color (e.g., `"#FF0000"`).                                                 |
| `STROKE_COLOR`        | `String` | The key for the circle's stroke (outline) color.                                                        |
| `STROKE_WIDTH`        | `String` | The key for the circle's stroke width in pixels.                                                        |
| `Z_INDEX`             | `String` | The key for the circle's sort key, which influences rendering order. Higher values are drawn on top.      |

---

### Example

Here is an example of how to initialize `MapboxCircleLayer`, add it to a map, and draw circles.

```kotlin
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.Style

// Assuming you have a MapboxMap instance from your map view
val mapboxMap = getMapboxMap()

// 1. Initialize the MapboxCircleLayer
val circleLayerManager = MapboxCircleLayer(
    sourceId = "my-circle-source",
    layerId = "my-circle-layer"
)

// 2. Create circle data (as GeoJSON Features)
// Note: MapboxActualCircle is assumed to be a type alias or wrapper for com.mapbox.geojson.Feature
val circle1 = Feature.fromGeometry(
    Point.fromLngLat(-74.0060, 40.7128) // New York City
).apply {
    addStringProperty(MapboxCircleLayer.Prop.FILL_COLOR, "#F74A4A")
    addStringProperty(MapboxCircleLayer.Prop.STROKE_COLOR, "#FFFFFF")
    addNumberProperty(MapboxCircleLayer.Prop.RADIUS, 1000) // 1000 meters
    addNumberProperty(MapboxCircleLayer.Prop.STROKE_WIDTH, 2.0)
    addNumberProperty(MapboxCircleLayer.Prop.Z_INDEX, 1)
    // Latitude correction factor is 1/cos(latitude).
    // For 40.7 degrees, it's 1 / cos(40.7 * PI / 180) ≈ 1.31
    addNumberProperty(MapboxCircleLayer.Prop.LATITUDE_CORRECTION, 1.31)
}

val circle2 = Feature.fromGeometry(
    Point.fromLngLat(139.6917, 35.6895) // Tokyo
).apply {
    addStringProperty(MapboxCircleLayer.Prop.FILL_COLOR, "#3E86F0")
    addStringProperty(MapboxCircleLayer.Prop.STROKE_COLOR, "#FFFFFF")
    addNumberProperty(MapboxCircleLayer.Prop.RADIUS, 1500) // 1500 meters
    addNumberProperty(MapboxCircleLayer.Prop.STROKE_WIDTH, 2.0)
    addNumberProperty(MapboxCircleLayer.Prop.Z_INDEX, 2)
    // For 35.7 degrees, it's 1 / cos(35.7 * PI / 180) ≈ 1.23
    addNumberProperty(MapboxCircleLayer.Prop.LATITUDE_CORRECTION, 1.23)
}

// Assume CircleEntityInterface has a 'circle' property that returns a Feature
val circleEntities = listOf(
    object : CircleEntityInterface<Feature> { override val circle = circle1 },
    object : CircleEntityInterface<Feature> { override val circle = circle2 }
)

// 3. Add the source and layer to the map style
mapboxMap.getStyle { style: Style ->
    style.addSource(circleLayerManager.source)
    style.addLayer(circleLayerManager.layer)

    // 4. Draw the circles on the map
    // This can be called any time you need to update the circle data.
    circleLayerManager.draw(circleEntities)
}
```