Excellent. Here is the high-quality SDK documentation for the provided code snippet.

***

# ArcGISMarkerController

The `ArcGISMarkerController` class is responsible for managing the lifecycle of markers on an ArcGIS map. It extends `AbstractMarkerController` and provides a concrete implementation for the ArcGIS Maps SDK.

This controller handles adding, updating, removing, and finding markers. A key feature is its performance optimization strategy: it can dynamically switch between rendering markers as individual `Graphic` objects and rendering them as a single, server-generated tiled raster layer. This tiling mechanism is automatically engaged when the number of markers exceeds a configurable threshold, significantly improving performance for large datasets.

## Creating an Instance

The `ArcGISMarkerController` is instantiated using the `create` factory method.

### create

```kotlin
fun create(
    holder: ArcGISMapViewHolder,
    markerTiling: MarkerTilingOptions = MarkerTilingOptions.Default,
): ArcGISMarkerController
```

#### Description

A static factory method that creates and initializes a new instance of `ArcGISMarkerController`. It sets up the required `GraphicsOverlay` for individual markers and configures the controller with the specified tiling options.

#### Parameters

| Name | Type | Description |
| :--- | :--- | :--- |
| `holder` | `ArcGISMapViewHolder` | The view holder that manages the ArcGIS map view instance. |
| `markerTiling` | `MarkerTilingOptions` | (Optional) Configuration for the marker tiling feature. If omitted, `MarkerTilingOptions.Default` is used. |

#### Returns

| Type | Description |
| :--- | :--- |
| `ArcGISMarkerController` | A new, fully configured instance of the controller. |

#### Example

```kotlin
// Define tiling options (optional)
val tilingOptions = MarkerTilingOptions(
    enabled = true,
    minMarkerCount = 500,
    // ... other options
)

// Create the controller
val markerController = ArcGISMarkerController.create(
    holder = myArcGISMapViewHolder,
    markerTiling = tilingOptions
)
```

## Methods

### setRasterLayerCallback

```kotlin
fun setRasterLayerCallback(callback: MarkerTileRasterLayerCallback?)
```

#### Description

Sets a callback that is invoked when the raster layer for tiled markers is created, updated, or removed. When the controller switches to tiling mode, it generates a `RasterLayerState`. The host application must use this callback to receive the state and add the corresponding `RasterLayer` to the map. When the tile layer is no longer needed, the callback is invoked with `null`.

#### Parameters

| Name | Type | Description |
| :--- | :--- | :--- |
| `callback` | `MarkerTileRasterLayerCallback?` | The callback to handle raster layer state changes. Pass `null` to remove a previously set callback. |

### find

```kotlin
override fun find(position: GeoPointInterface): MarkerEntityInterface<ArcGISActualMarker>?
```

#### Description

Finds the nearest marker entity to a given geographic position based on screen space. This method calculates if the touch point falls within the tappable area of a marker's icon, which includes the icon's dimensions, anchor point, and a predefined tolerance.

#### Parameters

| Name | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPointInterface` | The geographic coordinate (latitude/longitude) to search near. |

#### Returns

| Type | Description |
| :--- | :--- |
| `MarkerEntityInterface<ArcGISActualMarker>?` | The found marker entity, or `null` if no marker is within the tap tolerance of the given position. |

### add

```kotlin
override suspend fun add(data: List<MarkerState>)
```

#### Description

Adds a list of new markers to the map. The controller's ingestion engine processes the markers. If marker tiling is enabled and the total number of markers meets the `minMarkerCount`, eligible markers (i.e., those that are not draggable and have no animations) are rendered as part of a tiled raster layer. All other markers are rendered as individual `Graphic` objects.

#### Parameters

| Name | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<MarkerState>` | A list of `MarkerState` objects, each representing a marker to be added. |

### update

```kotlin
override suspend fun update(state: MarkerState)
```

#### Description

Updates an existing marker with a new state. The method checks if the marker's state has actually changed before applying updates. It intelligently handles transitions between rendering modes. For example, if a marker that was part of the tile layer is updated to be draggable, it will be removed from the tiles and rendered as an individual `Graphic`.

#### Parameters

| Name | Type | Description |
| :--- | :--- | :--- |
| `state` | `MarkerState` | The new state for the marker. The `id` within the state must match an existing marker. |

### clear

```kotlin
override suspend fun clear()
```

#### Description

Removes all markers from the map. This operation clears both individual `Graphic` markers and removes the tiled raster layer if it is active.

### onCameraChanged

```kotlin
override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

#### Description

A lifecycle method that should be called when the map's camera position (e.g., zoom, pan) changes. The controller uses this information to track the current zoom level, which is essential for managing the tiled marker layer. This method is typically invoked by the map framework, not directly by the developer.

#### Parameters

| Name | Type | Description |
| :--- | :--- | :--- |
| `mapCameraPosition` | `MapCameraPosition` | The new camera position of the map. |

### destroy

```kotlin
override fun destroy()
```

#### Description

Cleans up and releases all resources used by the controller. This includes unregistering the tile server, clearing raster layer callbacks, and removing any remaining markers. This method must be called when the controller is no longer needed to prevent memory leaks and other resource issues.