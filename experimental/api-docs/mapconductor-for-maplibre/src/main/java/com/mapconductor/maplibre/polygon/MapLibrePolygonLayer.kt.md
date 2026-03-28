# SDK Documentation: `MapLibrePolygonLayer`

## `MapLibrePolygonLayer`

### Signature

```kotlin
class MapLibrePolygonLayer(
    val sourceId: String,
    val layerId: String,
)
```

### Description

The `MapLibrePolygonLayer` class manages a dedicated layer for rendering polygons on a MapLibre map. It encapsulates a `GeoJsonSource` to hold polygon data and a `FillLayer` to control their visual representation. This class simplifies the process of creating, styling, and updating a collection of polygons by handling the underlying MapLibre objects and data flow.

### Constructor

#### Signature

```kotlin
MapLibrePolygonLayer(sourceId: String, layerId: String)
```

#### Description

Creates a new instance of `MapLibrePolygonLayer`. Upon instantiation, it initializes a `GeoJsonSource` and a `FillLayer` with the provided IDs.

#### Parameters

| Parameter  | Type     | Description                                          |
|------------|----------|------------------------------------------------------|
| `sourceId` | `String` | A unique identifier for the underlying `GeoJsonSource`. |
| `layerId`  | `String` | A unique identifier for the `FillLayer`.             |

---

## Nested Objects

### `Prop`

#### Signature

```kotlin
object Prop
```

#### Description

A companion object that defines constant keys for properties used within the GeoJSON features. These keys are essential for dynamically styling features based on their data attributes.

#### Properties

| Property     | Type     | Description                                                                                             |
|--------------|----------|---------------------------------------------------------------------------------------------------------|
| `FILL_COLOR` | `String` | The key for the feature property that defines the polygon's fill color (e.g., `"#FF0000"`). Value: `"fillColor"`. |
| `Z_INDEX`    | `String` | The key for the feature property that defines the polygon's stacking order. Value: `"zIndex"`.          |

---

## Properties

### `source: GeoJsonSource`

The `GeoJsonSource` instance that contains the polygon feature data. This source must be added to the map's `Style` to make the data available for rendering. It is initialized with an empty `FeatureCollection`.

### `layer: FillLayer`

The `FillLayer` instance used to render the polygons from the `source`. This layer is pre-configured to derive its fill color from the `fillColor` property of each GeoJSON feature. It must be added to the map's `Style` to make the polygons visible.

---

## Methods

### `draw`

#### Signature

```kotlin
fun draw(
    entities: List<PolygonEntityInterface<MapLibreActualPolygon>>,
    style: org.maplibre.android.maps.Style,
)
```

#### Description

Renders or updates the polygons on the map. This method processes a list of `PolygonEntityInterface` objects, converts them into GeoJSON `Feature`s, and updates the `GeoJsonSource`. The polygons are sorted by their `zIndex` property to control drawing order.

The method first attempts to update the source directly from the map's `Style` object for optimal performance. If the style-bound source is not accessible (e.g., during initial setup), it falls back to updating its local `source` property.

#### Parameters

| Parameter  | Type                                                          | Description                                                                                             |
|------------|---------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| `entities` | `List<PolygonEntityInterface<MapLibreActualPolygon>>` | A list of polygon entities to display on the map. Each entity provides its geometry and state properties. |
| `style`    | `org.maplibre.android.maps.Style`                             | The current `Style` object of the map to which the source is (or will be) attached.                     |

#### Returns

`Unit` - This method does not return a value.

---

### Example

The following example demonstrates how to initialize `MapLibrePolygonLayer`, add it to a map, and use the `draw` method to render polygons.

```kotlin
// This code typically runs inside your map's onStyleLoaded callback.
map.getStyle { style ->
    // 1. Initialize the polygon layer manager
    val polygonLayer = MapLibrePolygonLayer(
        sourceId = "polygon-source-id",
        layerId = "polygon-layer-id"
    )

    // 2. Add the layer's source and the layer itself to the map style
    style.addSource(polygonLayer.source)
    style.addLayer(polygonLayer.layer)

    // 3. Define polygon geometry and create a Feature.
    // Add properties using the keys from `MapLibrePolygonLayer.Prop`.
    val polygonGeometry = Polygon.fromLngLats(listOf(
        listOf(
            Point.fromLngLat(-122.419, 37.774),
            Point.fromLngLat(-122.429, 37.774),
            Point.fromLngLat(-122.429, 37.784),
            Point.fromLngLat(-122.419, 37.784),
            Point.fromLngLat(-122.419, 37.774)
        )
    ))
    val feature1 = Feature.fromGeometry(polygonGeometry)
    feature1.addStringProperty(MapLibrePolygonLayer.Prop.FILL_COLOR, "#3bb2d0") // Set color
    feature1.addNumberProperty(MapLibrePolygonLayer.Prop.Z_INDEX, 1) // Used for sorting

    // 4. Wrap the feature(s) in the required entity structure.
    // Note: The implementation of `PolygonEntityInterface` and `MapLibreActualPolygon`
    // depends on your specific application architecture.
    val polygonEntity1 = createPolygonEntity(
        features = listOf(feature1),
        zIndex = 1
    )

    // 5. Call the draw method to update the map with the new entities
    polygonLayer.draw(listOf(polygonEntity1), style)
}

/**
 * Example helper function to create a PolygonEntityInterface.
 * `MapLibreActualPolygon` is assumed to be a typealias for `List<Feature>`.
 */
fun createPolygonEntity(features: List<Feature>, zIndexValue: Int): PolygonEntityInterface<List<Feature>> {
    return object : PolygonEntityInterface<List<Feature>> {
        override val polygon: List<Feature> = features
        override val state = object {
            val zIndex: Int = zIndexValue
        }
    }
}
```