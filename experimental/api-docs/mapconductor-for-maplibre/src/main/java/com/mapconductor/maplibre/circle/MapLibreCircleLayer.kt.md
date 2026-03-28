Of course. Here is the high-quality SDK documentation for the `MapLibreCircleLayer` class, meticulously crafted to be clear, accurate, and developer-friendly, with all feedback incorporated.

---

# MapLibreCircleLayer

The `MapLibreCircleLayer` class is a comprehensive utility for managing and rendering circles on a MapLibre map. It encapsulates a `GeoJsonSource` and a `CircleLayer`, simplifying the process of drawing circles whose radii are defined in meters.

The class automatically handles the complex conversion from real-world meters to screen pixels, ensuring circles maintain their correct proportional size across different zoom levels and latitudes.

## Signature

```kotlin
class MapLibreCircleLayer(
    val sourceId: String,
    val layerId: String
)
```

## Description

This class creates and manages the necessary MapLibre components (`GeoJsonSource` and `CircleLayer`) for displaying circles. You instantiate it with unique IDs for the source and layer. The `layer` property is pre-configured with expressions to dynamically style circles based on properties set in the GeoJSON features. The primary method, `draw()`, is used to update the source with a list of circle entities, causing them to be rendered on the map.

## Parameters

| Parameter  | Type     | Description                                                              |
|------------|----------|--------------------------------------------------------------------------|
| `sourceId` | `String` | A unique identifier for the underlying `GeoJsonSource` that will hold the circle data. |
| `layerId`  | `String` | A unique identifier for the `CircleLayer` that will render the circles.  |

## Properties

### `source: GeoJsonSource`

The `GeoJsonSource` instance managed by this class. You must add this source to the map's `Style` before drawing.

### `layer: CircleLayer`

The `CircleLayer` instance managed by this class. It is pre-configured to style circles using properties from the source. You must add this layer to the map's `Style` before drawing.

## Inner Object: `Prop`

The `Prop` object contains constant keys used to define the properties of each circle within its GeoJSON `Feature`.

| Constant              | Type     | Description                                                                                                                                                           |
|-----------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `RADIUS`              | `String` | The key for the circle's radius in meters.                                                                                                                            |
| `LATITUDE_CORRECTION` | `String` | The key for the latitude correction factor, required for accurate radius rendering in Mercator projection. Calculated as `cos(Math.toRadians(latitude))`.               |
| `FILL_COLOR`          | `String` | The key for the circle's fill color, expressed as a string (e.g., `"#FF0000"`).                                                                                        |
| `STROKE_COLOR`        | `String` | The key for the circle's stroke (outline) color, expressed as a string.                                                                                               |
| `STROKE_WIDTH`        | `String` | The key for the circle's stroke width in pixels.                                                                                                                      |
| `Z_INDEX`             | `String` | The key for the circle's sort key. Higher values are drawn on top of lower values. This controls the rendering order of overlapping circles within this same layer. |

## Methods

### `draw`

Updates the `GeoJsonSource` with a list of circle entities, redrawing them on the map.

#### Signature

```kotlin
fun draw(
    entities: List<CircleEntityInterface<MapLibreActualCircle>>,
    style: org.maplibre.android.maps.Style
)
```

#### Description

This method takes a list of entities that represent circles and updates the map's data source. It extracts the GeoJSON `Feature` from each entity and sets it on the `GeoJsonSource`. If the source is already part of the map's style, it will be updated efficiently. Otherwise, the internal source object is updated, which will be used when the source is next added to the style.

#### Parameters

| Parameter  | Type                                                      | Description                                                                                                                                                                                          |
|------------|-----------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `entities` | `List<CircleEntityInterface<MapLibreActualCircle>>`       | A list of circle entities to be drawn on the map. Each entity must provide a `MapLibreActualCircle` (which is a `typealias` for a MapLibre `Feature`) via its `circle` property. |
| `style`    | `org.maplibre.android.maps.Style`                         | The current `Style` object from the MapLibre map. This is used to find and update the source directly for better performance.                                                                       |

#### Returns

This method does not return any value.

## Example

The following example demonstrates how to initialize `MapLibreCircleLayer`, add it to a map, and draw a circle.

```kotlin
import com.google.gson.JsonObject
import com.mapconductor.core.circle.CircleEntityInterface
import com.mapconductor.maplibre.MapLibreActualCircle // This is a typealias for org.maplibre.geojson.Feature
import com.mapconductor.maplibre.circle.MapLibreCircleLayer
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import kotlin.math.cos
import kotlin.math.toRadians

// Assume this is in your Activity or Fragment that contains a MapView.
class MyMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var maplibreMap: MapLibreMap
    private lateinit var circleLayer: MapLibreCircleLayer

    // For demonstration, we define a simple entity that conforms to the interface.
    // In a real app, this would be part of your domain model.
    data class MyCircleEntity(override val circle: MapLibreActualCircle) : CircleEntityInterface<MapLibreActualCircle>

    override fun onMapReady(maplibreMap: MapLibreMap) {
        this.maplibreMap = maplibreMap
        maplibreMap.setStyle(Style.Builder().fromUri(Style.MAPTILER_STREETS)) { style ->
            // 1. Initialize the MapLibreCircleLayer
            circleLayer = MapLibreCircleLayer(
                sourceId = "my-circle-source",
                layerId = "my-circle-layer"
            )

            // 2. Add the source and layer to the map's style
            style.addSource(circleLayer.source)
            style.addLayer(circleLayer.layer)

            // 3. Create the circle entities to draw
            val circleEntities = createCircleEntities()

            // 4. Call draw to render the circles
            circleLayer.draw(circleEntities, style)
        }
    }

    private fun createCircleEntities(): List<CircleEntityInterface<MapLibreActualCircle>> {
        val latitude = 35.6812
        val longitude = 139.7671 // Tokyo Station

        // Create the GeoJSON properties for the circle
        val properties = JsonObject().apply {
            addProperty(MapLibreCircleLayer.Prop.RADIUS, 500.0) // 500 meters
            addProperty(MapLibreCircleLayer.Prop.FILL_COLOR, "#E57373") // Red fill
            addProperty(MapLibreCircleLayer.Prop.STROKE_COLOR, "#B71C1C") // Dark red stroke
            addProperty(MapLibreCircleLayer.Prop.STROKE_WIDTH, 2.5f)
            addProperty(MapLibreCircleLayer.Prop.Z_INDEX, 10.0)
            // The latitude correction is crucial for accurate radius rendering
            addProperty(MapLibreCircleLayer.Prop.LATITUDE_CORRECTION, cos(latitude.toRadians()))
        }

        // Create the GeoJSON Feature for the circle
        val circleFeature = Feature.fromGeometry(
            Point.fromLngLat(longitude, latitude),
            properties
        )

        // Wrap the feature in our entity class
        val myCircle = MyCircleEntity(circleFeature)

        // Return a list of entities. The type matches the 'draw' method signature.
        return listOf(myCircle)
    }

    // ... other lifecycle methods for MapView
}
```