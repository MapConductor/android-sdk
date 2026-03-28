Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

***

# MapLibrePolylineLayer

## `MapLibrePolylineLayer` Class

### Signature

```kotlin
class MapLibrePolylineLayer(
    val sourceId: String,
    val layerId: String,
)
```

### Description

The `MapLibrePolylineLayer` class is a controller responsible for managing and rendering a collection of polylines on a MapLibre map. It encapsulates a MapLibre `GeoJsonSource` and a corresponding `LineLayer`, simplifying the process of adding, updating, and styling polylines.

This class links the visual properties of the polylines (like color and width) to the properties of the underlying GeoJSON features, allowing for dynamic styling on a per-polyline basis.

### Constructor

Initializes a new instance of the `MapLibrePolylineLayer`.

#### Signature

```kotlin
MapLibrePolylineLayer(sourceId: String, layerId: String)
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `sourceId` | `String` | A unique identifier for the `GeoJsonSource` that will store the polyline data. |
| `layerId` | `String` | A unique identifier for the `LineLayer` that will render the polylines from the source. |

---

## Properties

### `source`

The underlying MapLibre `GeoJsonSource` that holds the polyline data as a `FeatureCollection`. This source is initialized with an empty collection and is updated when the `draw()` method is called.

#### Signature

```kotlin
val source: GeoJsonSource
```

### `layer`

The MapLibre `LineLayer` used to render the polylines. It is pre-configured to use the `source` and to style the lines based on properties within each GeoJSON feature.

#### Signature

```kotlin
val layer: LineLayer
```

#### Default Style Properties

The layer is configured with the following default properties:
- **Line Join**: `ROUND`
- **Line Cap**: `ROUND`
- **Line Color**: Derived from the feature's `strokeColor` property (see `Prop.STROKE_COLOR`).
- **Line Width**: Derived from the feature's `strokeWidth` property (see `Prop.STROKE_WIDTH`).

---

## `Prop` Object

A static object containing constants for property names. These keys should be added to the properties of each GeoJSON `Feature` to control its styling.

### Signature

```kotlin
object Prop
```

### Properties

| Property | Type | Value | Description |
| :--- | :--- | :--- | :--- |
| `STROKE_COLOR` | `String` | `"strokeColor"` | The key for the feature property that defines the polyline's stroke color (e.g., `"#FF0000"`). |
| `STROKE_WIDTH` | `String` | `"strokeWidth"` | The key for the feature property that defines the polyline's stroke width in pixels (e.g., `5.0f`). |
| `Z_INDEX` | `String` | `"zIndex"` | The key for the feature property that can be used to define the polyline's z-index. **Note**: This property is not used by the `layer`'s default configuration in this class but is provided for potential custom extensions. |

---

## Methods

### `draw`

Renders or updates the polylines on the map. This method processes a list of polyline entities, converts them into a `FeatureCollection`, and updates the `GeoJsonSource`.

It first attempts to get the source directly from the map's active `Style` for optimal performance. If the style is in transition or otherwise unavailable, it gracefully falls back to updating its local `source` instance.

#### Signature

```kotlin
fun draw(
    entities: List<PolylineEntityInterface<MapLibreActualPolyline>>,
    style: org.maplibre.android.maps.Style,
)
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `entities` | `List<PolylineEntityInterface<MapLibreActualPolyline>>` | A list of polyline entities to be drawn. Each entity contains the geometric data and properties for one or more polylines. |
| `style` | `org.maplibre.android.maps.Style` | The current, fully loaded `Style` object from the MapLibre map instance. |

#### Returns

`Unit` - This method does not return a value.

---

## Example

The following example demonstrates how to initialize `MapLibrePolylineLayer`, add its source and layer to the map, and use the `draw()` method to render polylines.

```kotlin
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import org.maplibre.geojson.Feature
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style

// Assume PolylineEntity and MapLibreActualPolyline are defined elsewhere
// For this example, we'll create a simple representation.
class MyPolylineEntity(override val polyline: List<Feature>) : PolylineEntityInterface<List<Feature>>

class MyMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: MapLibreMap
    private lateinit var polylineLayer: MapLibrePolylineLayer

    // ... onCreate, etc.

    override fun onMapReady(maplibreMap: MapLibreMap) {
        this.map = maplibreMap
        map.setStyle(Style.Builder().fromUri("YOUR_STYLE_URI")) { style ->
            // 1. Initialize the polyline layer with unique IDs
            polylineLayer = MapLibrePolylineLayer(
                sourceId = "my-polyline-source",
                layerId = "my-polyline-layer"
            )

            // 2. Add the source and layer to the map style
            style.addSource(polylineLayer.source)
            style.addLayer(polylineLayer.layer)

            // 3. Prepare the polyline data
            val polylineEntities = createSamplePolylines()

            // 4. Draw the polylines on the map
            polylineLayer.draw(polylineEntities, style)
        }
    }

    private fun createSamplePolylines(): List<MyPolylineEntity> {
        // Create a line from two points
        val points = listOf(
            Point.fromLngLat(-122.483696, 37.833818),
            Point.fromLngLat(-122.483482, 37.833174)
        )
        val lineString = LineString.fromLngLats(points)

        // Create a GeoJSON feature with styling properties
        val feature = Feature.fromGeometry(lineString)
        feature.addStringProperty(MapLibrePolylineLayer.Prop.STROKE_COLOR, "#3bb2d0")
        feature.addNumberProperty(MapLibrePolylineLayer.Prop.STROKE_WIDTH, 5.0f)

        // Wrap the feature in an entity
        val entity = MyPolylineEntity(listOf(feature))

        return listOf(entity)
    }
}
```