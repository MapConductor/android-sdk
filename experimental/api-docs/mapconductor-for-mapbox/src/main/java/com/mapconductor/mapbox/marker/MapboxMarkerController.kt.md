Of course! Here is the high-quality SDK documentation for the provided `MapboxMarkerController` code snippet.

---

# MapboxMarkerController

## Class Description

The `MapboxMarkerController` is the primary class for managing the lifecycle of markers on a Mapbox map. It serves as the main interface for adding, updating, removing, and finding markers.

A key feature of this controller is its ability to handle a large number of markers efficiently through an optional tiling mechanism. When the number of markers exceeds a certain threshold, the controller can automatically switch to rendering them as a raster tile layer instead of individual objects. This significantly improves performance by reducing the number of objects managed by the map's renderer.

The controller also manages marker selection and dragging states, moving selected markers to a dedicated drag layer for smooth user interaction.

## Creating an Instance

The `MapboxMarkerController` is instantiated using the `create` factory method within its `companion object`.

### `create()`

Creates and initializes a new instance of `MapboxMarkerController`.

#### Signature

```kotlin
fun create(
    holder: MapboxMapViewHolder,
    markerManager: MarkerManager<MapboxActualMarker>,
    markerLayer: MarkerLayer,
    dragLayer: MarkerDragLayer,
    markerTiling: MarkerTilingOptions = MarkerTilingOptions.Default,
): MapboxMarkerController
```

#### Description

This factory method constructs a `MapboxMarkerController` and wires it up with the necessary Mapbox-specific components and configuration. It initializes the renderer and sets the initial camera position.

#### Parameters

| Parameter       | Type                        | Description                                                                                             |
| :-------------- | :-------------------------- | :------------------------------------------------------------------------------------------------------ |
| `holder`        | `MapboxMapViewHolder`       | The view holder that provides access to the Mapbox map instance and its context.                        |
| `markerManager` | `MarkerManager`             | The manager responsible for the in-memory storage and querying of marker data.                          |
| `markerLayer`   | `MarkerLayer`               | The Mapbox layer where standard, non-tiled markers will be rendered.                                    |
| `dragLayer`     | `MarkerDragLayer`           | The dedicated Mapbox layer for rendering a selected or dragged marker, ensuring it appears above others.  |
| `markerTiling`  | `MarkerTilingOptions`       | (Optional) Configuration options for the marker tiling feature. Defaults to `MarkerTilingOptions.Default`. |

#### Returns

| Type                     | Description                                  |
| :----------------------- | :------------------------------------------- |
| `MapboxMarkerController` | A new, fully initialized `MapboxMarkerController` instance. |

#### Example

```kotlin
val markerController = MapboxMarkerController.create(
    holder = mapViewHolder,
    markerManager = myMarkerManager,
    markerLayer = myMapboxMarkerLayer,
    dragLayer = myMapboxDragLayer,
    markerTiling = MarkerTilingOptions(enabled = true)
)
```

## Properties

### `selectedMarker`

Gets or sets the currently selected marker.

#### Signature

```kotlin
var selectedMarker: MarkerEntityInterface<MapboxActualMarker>?
```

#### Description

Setting this property to a `MarkerEntityInterface` visually "lifts" the corresponding marker from the standard marker layer to a dedicated `dragLayer`. This is typically done when a user begins to drag a marker. The marker is temporarily removed from the `markerManager` to prevent it from being rendered in two places.

Setting the property to `null` deselects the current marker, returning it to the standard marker layer and re-registering it with the `markerManager`.

## Public Functions

### `setRasterLayerCallback()`

Sets the callback for receiving updates about the raster layer used for tiled markers.

#### Signature

```kotlin
fun setRasterLayerCallback(callback: MarkerTileRasterLayerCallback?)
```

#### Description

This function registers a callback that will be invoked whenever the state of the marker tile raster layer changes (e.g., when it's created, updated, or removed). This is essential for integrating the tiled markers with the map's layer management system.

**Note:** This method must be called before adding markers if you intend to use the marker tiling feature.

#### Parameters

| Parameter  | Type                          | Description                                                                                             |
| :--------- | :---------------------------- | :------------------------------------------------------------------------------------------------------ |
| `callback` | `MarkerTileRasterLayerCallback?` | The callback to be invoked with `RasterLayerState` updates, or `null` to clear the existing callback. |

### `find()`

Finds a marker entity at a given geographic position, accounting for touch tolerance.

#### Signature

```kotlin
override fun find(position: GeoPointInterface): MarkerEntityInterface<MapboxActualMarker>?
```

#### Description

This function searches for the nearest marker to the specified `position`. It then checks if the `position` falls within the marker's tappable area, which is defined by the marker's icon size, anchor point, and a system-defined touch tolerance. This makes it ideal for handling user taps on markers.

#### Parameters

| Parameter  | Type                | Description                               |
| :--------- | :------------------ | :---------------------------------------- |
| `position` | `GeoPointInterface` | The geographic coordinate to search near. |

#### Returns

| Type                                       | Description                                                              |
| :----------------------------------------- | :----------------------------------------------------------------------- |
| `MarkerEntityInterface<MapboxActualMarker>?` | The found marker entity, or `null` if no marker is within the touch area. |

### `add()`

Adds a list of markers to the map.

#### Signature

```kotlin
override suspend fun add(data: List<MarkerState>)
```

#### Description

This suspend function processes and adds a list of markers defined by their `MarkerState`. It intelligently decides whether to render markers individually or as part of a tiled raster layer. This decision is based on whether tiling is enabled, the total number of markers, and the properties of each individual marker (e.g., non-draggable markers are candidates for tiling).

#### Parameters

| Parameter | Type                | Description                               |
| :-------- | :------------------ | :---------------------------------------- |
| `data`    | `List<MarkerState>` | A list of `MarkerState` objects to add. |

### `clear()`

Removes all markers from the map.

#### Signature

```kotlin
override suspend fun clear()
```

#### Description

This suspend function removes all markers currently managed by the controller. It clears both individually rendered markers and removes the tiled marker raster layer if it exists.

### `update()`

Updates the state of a single existing marker.

#### Signature

```kotlin
override suspend fun update(state: MarkerState)
```

#### Description

This suspend function updates an existing marker identified by `state.id`. For performance, it first checks if the marker's visual representation has changed. If so, it applies the changes.

The function also handles transitions for a marker between a tiled and non-tiled state. For example, if a marker becomes draggable, it will be moved from the tile layer to the individual marker layer. Conversely, if it ceases to be draggable, it may be moved to the tile layer.

#### Parameters

| Parameter | Type          | Description                                                              |
| :-------- | :------------ | :----------------------------------------------------------------------- |
| `state`   | `MarkerState` | The new state for the marker. The `id` field must match an existing marker. |

### `destroy()`

Cleans up all resources used by the controller.

#### Signature

```kotlin
override fun destroy()
```

#### Description

This function releases all resources, including unregistering tile server endpoints, signaling for the removal of any raster layers, and canceling internal coroutines. It is crucial to call this method when the controller is no longer needed (e.g., in `onDestroy` of a Fragment/Activity) to prevent memory leaks and resource contention.