Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# MapLibrePolygonOverlayRenderer

## Signature

```kotlin
class MapLibrePolygonOverlayRenderer(
    val layer: MapLibrePolygonLayer,
    val polygonManager: PolygonManagerInterface<MapLibreActualPolygon>,
    override val holder: MapLibreMapViewHolderInterface,
    private val rasterLayerController: MapLibreRasterLayerController,
    private val tileServer: LocalTileServer = TileServerRegistry.get(forceNoStoreCache = true),
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolygonOverlayRenderer<MapLibreActualPolygon>()
```

## Description

Renders polygon overlays on a MapLibre map. This class is an implementation of `AbstractPolygonOverlayRenderer` and is responsible for the platform-specific drawing logic.

This renderer employs a dual strategy for optimal performance and feature support:
1.  **Native Polygons**: For simple polygons (those without holes), it leverages MapLibre's efficient, native polygon rendering.
2.  **Raster Tile Overlay**: For complex polygons that include one or more holes, it dynamically generates a raster tile overlay. A transparent native polygon is drawn to define the outer boundary, while a local tile server (`LocalTileServer`) provides raster images that "paint" the fill color and cut out the holes. This ensures that complex shapes are displayed correctly, overcoming limitations in some native polygon implementations.

## Constructor

Initializes a new instance of the `MapLibrePolygonOverlayRenderer`.

### Parameters

| Parameter             | Type                                        | Description                                                                                                                              |
| --------------------- | ------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| `layer`               | `MapLibrePolygonLayer`                      | The layer responsible for drawing the polygon features on the map.                                                                       |
| `polygonManager`      | `PolygonManagerInterface<MapLibreActualPolygon>` | The manager that holds the state of all polygons to be rendered.                                                                         |
| `holder`              | `MapLibreMapViewHolderInterface`            | The view holder providing access to the MapLibre map instance and its controller.                                                        |
| `rasterLayerController` | `MapLibreRasterLayerController`             | Controller for managing raster tile layers on the map, used for rendering polygons with holes.                                           |
| `tileServer`          | `LocalTileServer`                           | **Optional**. A local tile server for serving dynamically generated polygon raster tiles. Defaults to a new instance from `TileServerRegistry`. |
| `coroutine`           | `CoroutineScope`                            | **Optional**. The coroutine scope for running asynchronous rendering tasks. Defaults to `CoroutineScope(Dispatchers.Main)`.               |

---

## Methods

### createPolygon

Creates the underlying map-specific polygon object based on the provided state.

This method implements the core rendering logic: if the polygon state contains holes, it sets up a raster tile mask to render the shape. Otherwise, it creates a standard native polygon.

#### Signature

```kotlin
override suspend fun createPolygon(state: PolygonState): MapLibreActualPolygon?
```

#### Parameters

| Parameter | Type           | Description                                                                                             |
| --------- | -------------- | ------------------------------------------------------------------------------------------------------- |
| `state`   | `PolygonState` | The state object containing all properties of the polygon to be created, such as points, holes, and color. |

#### Returns

| Type                      | Description                                                                                                                                                           |
| ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `MapLibreActualPolygon?` | The newly created `MapLibreActualPolygon` object, or `null` if creation fails. For polygons with holes, the returned object will have a transparent fill, as the visual appearance is handled by a separate raster layer. |

---

### updatePolygonProperties

Updates an existing polygon's properties. If there are any changes to the polygon's geometry, appearance, or other defining properties (as determined by its `fingerPrint`), the method will trigger a full recreation of the polygon by calling `createPolygon`. Otherwise, it returns the existing polygon object.

#### Signature

```kotlin
override suspend fun updatePolygonProperties(
    polygon: MapLibreActualPolygon,
    current: PolygonEntityInterface<MapLibreActualPolygon>,
    prev: PolygonEntityInterface<MapLibreActualPolygon>,
): MapLibreActualPolygon?
```

#### Parameters

| Parameter | Type                                        | Description                                                              |
| --------- | ------------------------------------------- | ------------------------------------------------------------------------ |
| `polygon` | `MapLibreActualPolygon`                     | The current map-specific polygon object.                                 |
| `current` | `PolygonEntityInterface<MapLibreActualPolygon>` | The entity representing the new, updated state of the polygon.           |
| `prev`    | `PolygonEntityInterface<MapLibreActualPolygon>` | The entity representing the previous state of the polygon.               |

#### Returns

| Type                      | Description                                                                                             |
| ------------------------- | ------------------------------------------------------------------------------------------------------- |
| `MapLibreActualPolygon?` | The updated (or recreated) `MapLibreActualPolygon` object, or the previous polygon if no changes were detected. |

---

### removePolygon

Handles the removal of a single polygon. This method primarily focuses on cleaning up any associated raster mask layers used for polygons with holes. The actual removal of the polygon feature from the map is handled globally by the `onPostProcess` method, which redraws the entire collection of remaining polygons.

#### Signature

```kotlin
override suspend fun removePolygon(entity: PolygonEntityInterface<MapLibreActualPolygon>)
```

#### Parameters

| Parameter | Type                                        | Description                      |
| --------- | ------------------------------------------- | -------------------------------- |
| `entity`  | `PolygonEntityInterface<MapLibreActualPolygon>` | The polygon entity to be removed. |

---

### onPostProcess

Called after a batch of add, update, or remove operations. This method gathers all current polygon entities from the `polygonManager` and triggers a complete redraw on the map. This ensures the map view is always in sync with the latest polygon state.

#### Signature

```kotlin
override suspend fun onPostProcess()
```