# MapLibreMarkerController

The `MapLibreMarkerController` class is responsible for managing the lifecycle of markers on a MapLibre map. It handles adding, updating, removing, and finding markers. For performance optimization with a large number of markers, it can automatically switch to rendering markers as raster tiles.

This controller extends `AbstractMarkerController` and works in conjunction with a `MapLibreMarkerOverlayRenderer` to draw markers on the map.

## Signature

```kotlin
class MapLibreMarkerController(
    override val renderer: MapLibreMarkerOverlayRenderer,
    private val markerTiling: MarkerTilingOptions = MarkerTilingOptions.Default,
) : AbstractMarkerController<MapLibreActualMarker>
```

## Constructor

Creates an instance of the marker controller.

### Parameters

| Parameter      | Type                            | Description                                                                                                                            |
| :------------- | :------------------------------ | :------------------------------------------------------------------------------------------------------------------------------------- |
| `renderer`     | `MapLibreMarkerOverlayRenderer` | The renderer responsible for drawing markers and overlays on the map.                                                                  |
| `markerTiling` | `MarkerTilingOptions`           | (Optional) Configuration for the marker tiling feature, which improves performance for large datasets. Defaults to `MarkerTilingOptions.Default`. |

---

## Methods

### setRasterLayerCallback

Registers a callback to be invoked when the raster layer for tiled markers is created, updated, or removed. This is essential for integrating the tiled marker layer into the map's style, as the map needs to be told to add, update, or remove the corresponding `RasterLayerState`.

#### Signature

```kotlin
fun setRasterLayerCallback(callback: MarkerTileRasterLayerCallback?)
```

#### Parameters

| Parameter  | Type                              | Description                                                                                             |
| :--------- | :-------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `callback` | `MarkerTileRasterLayerCallback?`  | The callback to handle raster layer state changes. Pass `null` to remove the currently registered callback. |

---

### find

Finds the nearest marker to a given geographic coordinate. The search considers the marker's icon size, anchor point, and a predefined tap tolerance, making it ideal for implementing marker click/tap listeners.

#### Signature

```kotlin
override fun find(position: GeoPointInterface): MarkerEntityInterface<MapLibreActualMarker>?
```

#### Parameters

| Parameter  | Type                | Description                                                              |
| :--------- | :------------------ | :----------------------------------------------------------------------- |
| `position` | `GeoPointInterface` | The geographic coordinate (e.g., from a map tap event) to search around. |

#### Returns

`MarkerEntityInterface<MapLibreActualMarker>?` — The found marker entity, or `null` if no marker is within the tap tolerance of the specified position.

---

### add

Asynchronously adds a list of new markers to the map. Based on the `MarkerTilingOptions` and the number of markers, the controller will decide whether to render them individually or as part of a raster tile layer for better performance.

#### Signature

```kotlin
override suspend fun add(data: List<MarkerState>)
```

#### Parameters

| Parameter | Type                | Description                                                              |
| :-------- | :------------------ | :----------------------------------------------------------------------- |
| `data`    | `List<MarkerState>` | A list of `MarkerState` objects, each defining the properties of a marker. |

---

### update

Asynchronously updates the state of an existing marker, identified by `state.id`. If the marker's properties have changed, it will be redrawn. This method also handles the transition of a marker between being rendered individually and as part of a tile layer (e.g., if a marker becomes non-draggable, it may be moved to the tile layer).

#### Signature

```kotlin
override suspend fun update(state: MarkerState)
```

#### Parameters

| Parameter | Type          | Description                                                                                             |
| :-------- | :------------ | :------------------------------------------------------------------------------------------------------ |
| `state`   | `MarkerState` | The new state for the marker. The `id` field must match the ID of an existing marker to be updated. |

---

### clear

Asynchronously removes all markers from the map and cleans up any associated tiled raster layers.

#### Signature

```kotlin
override suspend fun clear()
```

---

### destroy

Releases all resources held by the controller. This includes unregistering the tile provider from the `TileServerRegistry` and signaling the removal of any active raster layers. This method must be called when the controller is no longer needed (e.g., when the map view is destroyed) to prevent memory leaks.

#### Signature

```kotlin
override fun destroy()
```

---

## Example

The following example demonstrates the basic lifecycle of using `MapLibreMarkerController`.

```kotlin
// Assume 'map' is your MapLibre map instance and 'renderer' is an initialized MapLibreMarkerOverlayRenderer.
// 1. Initialize the controller
val markerController = MapLibreMarkerController(renderer)

// 2. Set the raster layer callback to handle tiled markers
markerController.setRasterLayerCallback { rasterLayerState ->
    // This lambda is called when the tiled marker layer changes.
    // You should add, update, or remove this layer from your map's style.
    if (rasterLayerState != null) {
        myMap.style.updateLayer(rasterLayerState)
    } else {
        // The ID of the layer is consistent, so you can remove it by ID.
        myMap.style.removeLayer("marker-tile-...")
    }
}

// Coroutine scope for async operations
val coroutineScope = CoroutineScope(Dispatchers.Main)

coroutineScope.launch {
    // 3. Add markers to the map
    val markersToAdd = listOf(
        MarkerState(id = "marker1", position = GeoPoint(40.7128, -74.0060)),
        MarkerState(id = "marker2", position = GeoPoint(34.0522, -118.2437))
    )
    markerController.add(markersToAdd)
    println("Added 2 markers.")

    // 4. Update a marker
    val updatedMarkerState = MarkerState(
        id = "marker1",
        position = GeoPoint(40.7128, -74.0060),
        icon = MyCustomIcon(), // Change the icon
        draggable = true
    )
    markerController.update(updatedMarkerState)
    println("Updated marker1.")

    // 5. Find a marker at a specific location (e.g., after a map tap)
    val tappedPoint = GeoPoint(40.7128, -74.0060)
    val foundMarker = markerController.find(tappedPoint)
    if (foundMarker != null) {
        println("Found marker with ID: ${foundMarker.state.id}")
    }

    // 6. Clear all markers
    markerController.clear()
    println("All markers cleared.")
}

// 7. Clean up resources when the controller is no longer needed
// This should typically be done in a lifecycle method like onDestroy().
markerController.destroy()
```