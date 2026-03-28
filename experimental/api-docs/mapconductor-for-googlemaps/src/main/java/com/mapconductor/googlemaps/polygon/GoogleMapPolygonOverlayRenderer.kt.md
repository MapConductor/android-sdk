Excellent. Here is the high-quality SDK documentation for the provided code snippet.

***

# class GoogleMapPolygonOverlayRenderer

## Description

The `GoogleMapPolygonOverlayRenderer` is a specialized class responsible for rendering, updating, and removing polygon overlays on a Google Map. It extends `AbstractPolygonOverlayRenderer` and is tailored for the Google Maps SDK environment.

A key feature of this renderer is its advanced handling of polygons with holes. For simple polygons (without holes), it uses the native Google Maps `Polygon` object. However, for polygons with holes, it employs a sophisticated technique:
1.  It renders the outer boundary of the polygon with a transparent fill color.
2.  It dynamically generates and serves a raster tile overlay that precisely fills the area between the outer boundary and the holes.

This approach ensures that complex polygons are rendered correctly and efficiently. The class also implements adaptive geodesic interpolation, which adds intermediate points to polygon edges based on the current map zoom level. This results in smooth, geographically accurate curves that adapt to the user's perspective, enhancing visual quality while optimizing performance.

## Constructor

### GoogleMapPolygonOverlayRenderer(...)

Creates an instance of the `GoogleMapPolygonOverlayRenderer`.

```kotlin
class GoogleMapPolygonOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    private val rasterLayerController: GoogleMapRasterLayerController,
    private val tileServer: LocalTileServer = TileServerRegistry.get(forceNoStoreCache = true),
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolygonOverlayRenderer<GoogleMapActualPolygon>()
```

### Parameters

| Parameter             | Type                               | Description                                                                                                                            |
| :-------------------- | :--------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `holder`              | `GoogleMapViewHolder`              | The view holder that provides access to the `GoogleMap` instance.                                                                      |
| `rasterLayerController` | `GoogleMapRasterLayerController`   | The controller used to manage the raster tile layers required for rendering the fill of polygons with holes.                           |
| `tileServer`          | `LocalTileServer`                  | **Optional**. The local tile server for serving the raster mask tiles. If not provided, a default instance is used.                     |
| `coroutine`           | `CoroutineScope`                   | **Optional**. The coroutine scope for managing asynchronous rendering operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

## Functions

### createPolygon

Asynchronously creates and adds a new polygon to the map based on the provided state. If the polygon contains holes, this method also sets up the necessary raster tile layer to render the fill correctly.

**Signature**
```kotlin
override suspend fun createPolygon(state: PolygonState): GoogleMapActualPolygon?
```

**Parameters**
| Parameter | Type           | Description                                                                                                |
| :-------- | :------------- | :--------------------------------------------------------------------------------------------------------- |
| `state`   | `PolygonState` | An object defining all properties of the polygon, including points, holes, colors, stroke width, and z-index. |

**Returns**
| Type                     | Description                                                                                                                            |
| :----------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `GoogleMapActualPolygon?` | The created Google Maps `Polygon` object (`GoogleMapActualPolygon` is a type alias) or `null` if the polygon could not be added to the map. |

### updatePolygonProperties

Asynchronously updates the properties of an existing polygon on the map. It intelligently compares the previous and current states to apply only the necessary changes, optimizing performance. This includes updating geometry (points, holes), appearance (colors, stroke), and z-index. It also manages the underlying raster mask layer if the polygon's hole configuration changes.

**Signature**
```kotlin
override suspend fun updatePolygonProperties(
    polygon: GoogleMapActualPolygon,
    current: PolygonEntityInterface<GoogleMapActualPolygon>,
    prev: PolygonEntityInterface<GoogleMapActualPolygon>,
): GoogleMapActualPolygon?
```

**Parameters**
| Parameter | Type                                           | Description                                                                    |
| :-------- | :--------------------------------------------- | :----------------------------------------------------------------------------- |
| `polygon` | `GoogleMapActualPolygon`                       | The native Google Maps `Polygon` object to be updated.                         |
| `current` | `PolygonEntityInterface<GoogleMapActualPolygon>` | The entity representing the new, updated state of the polygon.                 |
| `prev`    | `PolygonEntityInterface<GoogleMapActualPolygon>` | The entity representing the previous state of the polygon, used for comparison. |

**Returns**
| Type                     | Description                      |
| :----------------------- | :------------------------------- |
| `GoogleMapActualPolygon?` | The updated `Polygon` object. |

### removePolygon

Asynchronously removes a polygon from the map. If the polygon had an associated raster mask layer (for rendering holes), that layer is also removed and its resources are cleaned up.

**Signature**
```kotlin
override suspend fun removePolygon(entity: PolygonEntityInterface<GoogleMapActualPolygon>)
```

**Parameters**
| Parameter | Type                                           | Description                           |
| :-------- | :--------------------------------------------- | :------------------------------------ |
| `entity`  | `PolygonEntityInterface<GoogleMapActualPolygon>` | The polygon entity to be removed. |

## Example

Here is an example of how to instantiate and use the `GoogleMapPolygonOverlayRenderer` to draw a complex polygon with a hole.

```kotlin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.PolygonState

// Assume these are provided by your application's setup
val googleMap: GoogleMap = getGoogleMap()
val mapViewHolder = GoogleMapViewHolder(googleMap)
val rasterController = GoogleMapRasterLayerController(mapViewHolder)
val coroutineScope = CoroutineScope(Dispatchers.Main)

// 1. Instantiate the renderer
val polygonRenderer = GoogleMapPolygonOverlayRenderer(
    holder = mapViewHolder,
    rasterLayerController = rasterController,
    coroutine = coroutineScope
)

// 2. Define the state for a polygon with a hole
val outerRing = listOf(
    GeoPoint(40.7128, -74.0060), // NYC
    GeoPoint(34.0522, -118.2437), // LA
    GeoPoint(29.7604, -95.3698)  // Houston
)

val innerHole = listOf(
    GeoPoint(39.9526, -75.1652), // Philadelphia
    GeoPoint(38.9072, -77.0369), // Washington D.C.
    GeoPoint(39.2904, -76.6122)  // Baltimore
)

val polygonState = PolygonState(
    id = "complex-polygon-1",
    points = outerRing,
    holes = listOf(innerHole),
    fillColor = Color(0x8800FF00), // Semi-transparent green
    strokeColor = Color.Black,
    strokeWidth = 2.0, // in dp
    zIndex = 10,
    geodesic = true
)

// 3. Use a coroutine to create the polygon on the map
coroutineScope.launch {
    val polygonEntity = polygonRenderer.createPolygon(polygonState)
    
    if (polygonEntity != null) {
        println("Polygon created successfully.")
        // The renderer automatically handles creating the raster layer for the fill.
    } else {
        println("Failed to create polygon.")
    }
}
```