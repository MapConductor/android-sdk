Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# ArcGISPolygonOverlayRenderer

## Description

The `ArcGISPolygonOverlayRenderer` class is a concrete implementation of `AbstractPolygonOverlayRenderer` designed to render and manage polygon graphics on an ArcGIS map. It is responsible for translating abstract `PolygonState` objects into visible ArcGIS `Graphic` objects on a specified `GraphicsOverlay`.

A key feature of this renderer is its advanced handling of polygons with holes. For simple polygons, it creates a standard `SimpleFillSymbol`. However, for complex polygons containing one or more holes, it employs a raster masking technique. It renders the outer polygon boundary with a transparent fill and dynamically generates a separate raster tile layer underneath. This raster layer provides the visible fill color, effectively "masking out" the hole areas, which allows for the correct visual representation of complex shapes.

The renderer also manages polygon properties such as fill color, stroke color, stroke width, and Z-index, ensuring that graphics are updated efficiently and drawn in the correct order.

## Constructor

### Signature

```kotlin
class ArcGISPolygonOverlayRenderer(
    val polygonLayer: GraphicsOverlay,
    override val holder: ArcGISMapViewHolder,
    private val rasterLayerController: ArcGISRasterLayerController,
    private val tileServer: LocalTileServer = TileServerRegistry.get(forceNoStoreCache = true),
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractPolygonOverlayRenderer<ArcGISActualPolygon>()
```

### Parameters

| Parameter             | Type                        | Description                                                                                                                            |
| :-------------------- | :-------------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `polygonLayer`        | `GraphicsOverlay`           | The ArcGIS `GraphicsOverlay` where the polygon graphics will be added and managed.                                                     |
| `holder`              | `ArcGISMapViewHolder`       | The view holder that provides context and access to the map instance.                                                                  |
| `rasterLayerController` | `ArcGISRasterLayerController` | The controller used to manage the raster mask layers, which are required for rendering polygons with holes.                            |
| `tileServer`          | `LocalTileServer`           | *(Optional)* The local tile server instance used to generate and serve raster tiles for the polygon hole masks. Defaults to a new instance. |
| `coroutine`           | `CoroutineScope`            | *(Optional)* The coroutine scope for executing asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Default)`.                |

---

## Methods

### createPolygon

Creates and displays a new polygon graphic on the map based on the provided state.

#### Signature

```kotlin
override suspend fun createPolygon(state: PolygonState): ArcGISActualPolygon?
```

#### Description

This function translates a `PolygonState` object into an ArcGIS `Graphic`. It configures the polygon's geometry, fill, and stroke properties. If the `state` includes holes, this method will also trigger the creation of a corresponding raster mask layer to render the fill correctly. The resulting graphic is added to the `polygonLayer`.

#### Parameters

| Parameter | Type           | Description                                        |
| :-------- | :------------- | :------------------------------------------------- |
| `state`   | `PolygonState` | The state object defining the polygon's properties. |

#### Returns

| Type                  | Description                                                              |
| :-------------------- | :----------------------------------------------------------------------- |
| `ArcGISActualPolygon?` | The created ArcGIS `Graphic` object, or `null` if creation failed. `ArcGISActualPolygon` is a type alias for `Graphic`. |

---

### updatePolygonProperties

Updates the properties of an existing polygon graphic.

#### Signature

```kotlin
override suspend fun updatePolygonProperties(
    polygon: ArcGISActualPolygon,
    current: PolygonEntityInterface<ArcGISActualPolygon>,
    prev: PolygonEntityInterface<ArcGISActualPolygon>,
): ArcGISActualPolygon?
```

#### Description

This function efficiently updates an existing polygon graphic by comparing the `current` and `prev` states. It modifies properties such as geometry, fill color, stroke, and Z-index only if they have changed. If holes are added to a polygon that previously had none, it will create the necessary raster mask layer. Conversely, if all holes are removed, it will clean up the mask layer.

#### Parameters

| Parameter | Type                                        | Description                                                              |
| :-------- | :------------------------------------------ | :----------------------------------------------------------------------- |
| `polygon` | `ArcGISActualPolygon`                       | The actual ArcGIS `Graphic` object to be updated.                        |
| `current` | `PolygonEntityInterface<ArcGISActualPolygon>` | The wrapper entity containing the new state and the polygon graphic.     |
| `prev`    | `PolygonEntityInterface<ArcGISActualPolygon>` | The wrapper entity containing the previous state for comparison.         |

#### Returns

| Type                  | Description                                                              |
| :-------------------- | :----------------------------------------------------------------------- |
| `ArcGISActualPolygon?` | The updated ArcGIS `Graphic` object, or `null` if the update failed. |

---

### removePolygon

Removes a polygon graphic from the map.

#### Signature

```kotlin
override suspend fun removePolygon(entity: PolygonEntityInterface<ArcGISActualPolygon>)
```

#### Description

This function removes the specified polygon graphic from the `polygonLayer`. If the polygon had an associated raster mask layer (for rendering holes), that layer and its resources are also removed and cleaned up.

#### Parameters

| Parameter | Type                                        | Description                                                        |
| :-------- | :------------------------------------------ | :----------------------------------------------------------------- |
| `entity`  | `PolygonEntityInterface<ArcGISActualPolygon>` | The wrapper entity containing the polygon graphic to be removed. |

---

### onPostProcess

A lifecycle method called after a batch of updates has been processed.

#### Signature

```kotlin
override suspend fun onPostProcess()
```

#### Description

This function is called after all create, update, and remove operations in a given cycle are complete. It ensures the correct visual stacking order of polygons by sorting the graphics in the `polygonLayer` based on their `zIndex` attribute. Polygons with higher `zIndex` values will be rendered on top of those with lower values.

---

## Example

The following example demonstrates how to instantiate `ArcGISPolygonOverlayRenderer` and use it to create, update, and remove polygons.

```kotlin
import com.arcgismaps.mapping.view.GraphicsOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Assume these dependencies are initialized elsewhere in your application
val graphicsOverlay = GraphicsOverlay()
val mapHolder: ArcGISMapViewHolder = getMapViewHolder()
val rasterController: ArcGISRasterLayerController = getRasterLayerController()
val coroutineScope = CoroutineScope(Dispatchers.Main)

// 1. Initialize the renderer
val polygonRenderer = ArcGISPolygonOverlayRenderer(
    polygonLayer = graphicsOverlay,
    holder = mapHolder,
    rasterLayerController = rasterController,
    coroutine = coroutineScope
)

// Add the graphics overlay to the map
mapHolder.mapView.graphicsOverlays.add(graphicsOverlay)

coroutineScope.launch {
    // 2. Define state for a simple polygon
    val simplePolygonState = PolygonState(
        id = "simple-polygon-1",
        points = listOf(
            GeoPoint(34.0, -118.0),
            GeoPoint(34.0, -118.5),
            GeoPoint(34.5, -118.5),
            GeoPoint(34.5, -118.0)
        ),
        fillColor = Color.Blue.copy(alpha = 0.5f),
        strokeColor = Color.Blue,
        strokeWidth = 2.dp,
        zIndex = 10
    )

    // Create the simple polygon
    val simplePolygonGraphic = polygonRenderer.createPolygon(simplePolygonState)
    println("Created simple polygon: ${simplePolygonGraphic?.attributes?.get("id")}")

    // 3. Define state for a polygon with a hole
    val polygonWithHoleState = PolygonState(
        id = "polygon-with-hole-1",
        points = listOf(
            GeoPoint(35.0, -119.0),
            GeoPoint(35.0, -120.0),
            GeoPoint(36.0, -120.0),
            GeoPoint(36.0, -119.0)
        ),
        holes = listOf(
            listOf( // Inner ring defining the hole
                GeoPoint(35.2, -119.2),
                GeoPoint(35.2, -119.8),
                GeoPoint(35.8, -119.8),
                GeoPoint(35.8, -119.2)
            )
        ),
        fillColor = Color.Red.copy(alpha = 0.7f),
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        zIndex = 20
    )

    // Create the polygon with a hole. This will also create a raster mask layer.
    val complexPolygonGraphic = polygonRenderer.createPolygon(polygonWithHoleState)
    println("Created polygon with hole: ${complexPolygonGraphic?.attributes?.get("id")}")

    // 4. After processing, sort the graphics by zIndex
    polygonRenderer.onPostProcess()

    // 5. To remove a polygon
    // (Assuming you have the PolygonEntityInterface instance)
    val entityToRemove = PolygonEntity(polygonWithHoleState, complexPolygonGraphic!!)
    polygonRenderer.removePolygon(entityToRemove)
    println("Removed polygon with hole.")
}
```