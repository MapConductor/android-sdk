Excellent! Here is the high-quality SDK documentation for the provided code snippet.

### `MapboxPolygonLayer` Class

**Description**

The `MapboxPolygonLayer` class is a controller for managing and rendering a collection of polygons on a Mapbox map. It encapsulates a Mapbox `GeoJsonSource` and a corresponding `FillLayer`, simplifying the process of drawing and styling polygon features.

The layer is pre-configured to style polygons based on properties within their GeoJSON features. Specifically, it uses the `fillColor` property for the polygon's color and the `zIndex` property to control the rendering order of polygons within the same layer (higher `zIndex` values are drawn on top).

### Constructor

**Signature**

```kotlin
MapboxPolygonLayer(
    val sourceId: String,
    val layerId: String
)
```

**Description**

Creates a new instance of `MapboxPolygonLayer`. This initializes the underlying `GeoJsonSource` and `FillLayer` with the provided unique identifiers.

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `sourceId` | `String` | A unique identifier for the GeoJSON source. |
| `layerId` | `String` | A unique identifier for the `FillLayer`. |

### `Prop` Object

**Description**

A nested object that contains constants for the property keys used within the GeoJSON features to control styling.

**Properties**

| Property | Value | Description |
| :--- | :--- | :--- |
| `FILL_COLOR` | `"fillColor"` | The key for the property that defines the polygon's fill color (e.g., `"#FF0000"`). |
| `Z_INDEX` | `"zIndex"` | The key for the property that defines the polygon's rendering order. Higher values are drawn on top. |

### Properties

#### `source`

**Signature**

```kotlin
val source: GeoJsonSource
```

**Description**

The underlying Mapbox `GeoJsonSource` instance managed by this class. You must add this source to the map's style for the polygons to be available for rendering.

#### `layer`

**Signature**

```kotlin
val layer: FillLayer
```

**Description**

The underlying Mapbox `FillLayer` instance managed by this class. This layer is configured to render data from the `source`. It dynamically sets the `fill-color` and `fill-sort-key` based on the `fillColor` and `zIndex` properties of each feature in the source. You must add this layer to the map's style to make the polygons visible.

### Methods

#### `draw`

**Signature**

```kotlin
fun draw(entities: List<PolygonEntityInterface<MapboxActualPolygon>>)
```

**Description**

Updates the GeoJSON source with a new set of polygon features, which causes them to be rendered on the map. This method processes a list of `PolygonEntityInterface` objects, extracts their geometries and properties, and builds a `FeatureCollection` to update the source.

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `entities` | `List<PolygonEntityInterface<MapboxActualPolygon>>` | A list of polygon entities to draw. Each entity must provide its polygon geometry (`MapboxActualPolygon`, which resolves to a list of GeoJSON `Feature`s) and state (including `zIndex`). |

### Example

The following example demonstrates how to initialize `MapboxPolygonLayer`, create polygon features, draw them on the map, and add the necessary source and layer to the map's style.

```kotlin
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.MapView
import com.mapbox.maps.getStyle
import com.mapconductor.core.polygon.PolygonEntityInterface
import com.mapconductor.mapbox.MapboxActualPolygon
import com.mapconductor.mapbox.polygon.MapboxPolygonLayer

// Assume these data classes and typealias for the example.
// MapboxActualPolygon is a typealias for List<Feature>.
typealias MapboxActualPolygon = List<Feature>

data class PolygonState(val zIndex: Int)
data class MyPolygonEntity(
    override val polygon: MapboxActualPolygon,
    override val state: PolygonState
) : PolygonEntityInterface<MapboxActualPolygon>

// --- Usage within your map setup code ---

// 1. Define unique IDs for the source and layer
val polygonSourceId = "my-polygon-source"
val polygonLayerId = "my-polygon-layer"

// 2. Create an instance of MapboxPolygonLayer
val polygonLayer = MapboxPolygonLayer(polygonSourceId, polygonLayerId)

// 3. Add the source and layer to the map style.
// This only needs to be done once when the style is loaded.
val mapboxMap = mapView.getMapboxMap()
mapboxMap.getStyle { style ->
    style.addSource(polygonLayer.source)
    style.addLayer(polygonLayer.layer)
}

// 4. Create polygon features to be drawn.
// Each feature MUST have "fillColor" and "zIndex" properties.
val redPolygonFeature = Feature.fromGeometry(
    Polygon.fromLngLats(listOf(listOf(
        Point.fromLngLat(-122.4, 37.8),
        Point.fromLngLat(-122.5, 37.8),
        Point.fromLngLat(-122.5, 37.7),
        Point.fromLngLat(-122.4, 37.7),
        Point.fromLngLat(-122.4, 37.8)
    )))
)
redPolygonFeature.addStringProperty(MapboxPolygonLayer.Prop.FILL_COLOR, "#F44336") // Red
redPolygonFeature.addNumberProperty(MapboxPolygonLayer.Prop.Z_INDEX, 10)

val bluePolygonFeature = Feature.fromGeometry(
    Polygon.fromLngLats(listOf(listOf(
        Point.fromLngLat(-122.3, 37.7),
        Point.fromLngLat(-122.4, 37.7),
        Point.fromLngLat(-122.4, 37.6),
        Point.fromLngLat(-122.3, 37.6),
        Point.fromLngLat(-122.3, 37.7)
    )))
)
bluePolygonFeature.addStringProperty(MapboxPolygonLayer.Prop.FILL_COLOR, "#2196F3") // Blue
bluePolygonFeature.addNumberProperty(MapboxPolygonLayer.Prop.Z_INDEX, 20) // Higher zIndex, will draw on top

// 5. Create entity objects from the features
val redEntity = MyPolygonEntity(
    polygon = listOf(redPolygonFeature),
    state = PolygonState(zIndex = 10)
)
val blueEntity = MyPolygonEntity(
    polygon = listOf(bluePolygonFeature),
    state = PolygonState(zIndex = 20)
)

// 6. Call draw() to render the polygons on the map
polygonLayer.draw(listOf(redEntity, blueEntity))
```