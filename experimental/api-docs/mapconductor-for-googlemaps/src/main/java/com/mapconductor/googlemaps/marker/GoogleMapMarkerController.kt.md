Of course! Here is the high-quality SDK documentation for the provided `GoogleMapMarkerController` code.

# GoogleMapMarkerController

Manages the lifecycle of markers on a Google Map. This controller handles adding, updating, removing, and finding markers. It includes an advanced performance optimization feature that automatically switches to rendering markers as raster tiles when the marker count is high, a process known as marker tiling.

An instance of this controller is created using the companion object's `create` factory method.

```kotlin
class GoogleMapMarkerController private constructor(...) : AbstractMarkerController<GoogleMapActualMarker>(...)
```

---

## Companion Object

### create

Factory method to create a new instance of `GoogleMapMarkerController`. This is the primary entry point for instantiating the controller.

**Signature**
```kotlin
fun create(
    holder: GoogleMapViewHolder,
    markerTiling: MarkerTilingOptions = MarkerTilingOptions.Default
): GoogleMapMarkerController
```

**Description**
This method initializes the marker controller with the necessary map components and tiling configuration.

**Parameters**

| Parameter      | Type                  | Description                                                                                             |
| :------------- | :-------------------- | :------------------------------------------------------------------------------------------------------ |
| `holder`       | `GoogleMapViewHolder` | The view holder that contains the `GoogleMap` instance.                                                 |
| `markerTiling` | `MarkerTilingOptions` | *(Optional)* Configuration for the marker tiling feature. Defaults to `MarkerTilingOptions.Default`. |

**Returns**

`GoogleMapMarkerController` - A new instance of the controller.

**Example**

```kotlin
// Assuming 'mapViewHolder' is an instance of GoogleMapViewHolder
val markerTilingOptions = MarkerTilingOptions(enabled = true, minMarkerCount = 100)

val markerController = GoogleMapMarkerController.create(
    holder = mapViewHolder,
    markerTiling = markerTilingOptions
)
```

---

## Methods

### setRasterLayerCallback

Sets a callback to manage the underlying `RasterLayer` used for tiled marker rendering.

**Signature**
```kotlin
fun setRasterLayerCallback(callback: MarkerTileRasterLayerCallback?)
```

**Description**
This is essential for the marker tiling feature to function. It allows the `MarkerController` to request additions, updates, or removals of the raster tile layer from a `RasterLayerController`. This method must be called before adding markers if tiling is enabled.

**Parameters**

| Parameter  | Type                            | Description                                                                                                                                                           |
| :--------- | :------------------------------ | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `callback` | `MarkerTileRasterLayerCallback?` | The callback to be invoked for raster layer updates. Pass `null` to remove the callback. See [MarkerTileRasterLayerCallback](#markertilerasterlayercallback) for details. |

**Example**

```kotlin
// Assuming 'rasterLayerController' is available
markerController.setRasterLayerCallback { rasterLayerState ->
    // The controller will pass a state when it needs to add/update the tile layer,
    // or null when it needs to be removed.
    if (rasterLayerState != null) {
        rasterLayerController.addOrUpdate(rasterLayerState)
    } else {
        // The ID of the raster layer is consistent, find and remove it.
        // (Implementation depends on the RasterLayerController API)
    }
}
```

---

### add

Asynchronously adds a list of markers to the map.

**Signature**
```kotlin
suspend fun add(data: List<MarkerState>)
```

**Description**
The controller intelligently decides whether to render markers individually or as a tiled layer based on the number of markers and the `MarkerTilingOptions` configuration. If tiling is enabled and the marker count exceeds `minMarkerCount`, markers that are not draggable and have no animation will be rendered as part of a raster tile overlay for improved performance.

**Parameters**

| Parameter | Type                | Description                                                     |
| :-------- | :------------------ | :-------------------------------------------------------------- |
| `data`    | `List<MarkerState>` | A list of `MarkerState` objects, each defining a marker to add. |

**Example**

```kotlin
val markerStates = listOf(
    MarkerState(id = "marker1", position = GeoPoint(40.7128, -74.0060)),
    MarkerState(id = "marker2", position = GeoPoint(34.0522, -118.2437))
)

// Add markers within a coroutine scope
coroutineScope.launch {
    markerController.add(markerStates)
}
```

---

### update

Asynchronously updates the state of an existing marker.

**Signature**
```kotlin
suspend fun update(state: MarkerState)
```

**Description**
This method handles transitions between individual and tiled rendering. For example, if a marker becomes draggable, it will be transitioned from the tile layer to an individual marker, and vice-versa. If the marker's state has not changed (based on its `fingerPrint`), the operation is skipped to avoid unnecessary work.

**Parameters**

| Parameter | Type          | Description                                                      |
| :-------- | :------------ | :--------------------------------------------------------------- |
| `state`   | `MarkerState` | The new state for the marker, which is identified by its `id`. |

**Example**

```kotlin
val updatedState = MarkerState(
    id = "marker1",
    position = GeoPoint(40.7128, -74.0060),
    draggable = true // Update the marker to be draggable
)

coroutineScope.launch {
    markerController.update(updatedState)
}
```

---

### clear

Asynchronously removes all markers from the map.

**Signature**
```kotlin
suspend fun clear()
```

**Description**
This method removes all individual markers and clears any active raster tile overlays being managed by the controller.

**Example**

```kotlin
coroutineScope.launch {
    markerController.clear()
}
```

---

### find

Finds the nearest marker to a given geographic position within a specified tap tolerance.

**Signature**
```kotlin
fun find(
    position: GeoPointInterface,
    zoom: Double
): MarkerEntityInterface<GoogleMapActualMarker>?
```

**Description**
This method is useful for implementing tap listeners on the map. The tolerance is calculated based on the screen density and the current map zoom level to provide a consistent user experience.

**Parameters**

| Parameter  | Type                | Description                                                              |
| :--------- | :------------------ | :----------------------------------------------------------------------- |
| `position` | `GeoPointInterface` | The geographic coordinates (latitude, longitude) to search around.       |
| `zoom`     | `Double`            | The current zoom level of the map, used to calculate the search radius. |

**Returns**

`MarkerEntityInterface<GoogleMapActualMarker>?` - The nearest marker entity if one is found within the tolerance radius, otherwise `null`.

**Example**

```kotlin
googleMap.setOnMapClickListener { latLng ->
    val clickedPosition = GeoPoint(latLng.latitude, latLng.longitude)
    val currentZoom = googleMap.cameraPosition.zoom.toDouble()

    val foundMarker = markerController.find(
        position = clickedPosition,
        zoom = currentZoom
    )

    if (foundMarker != null) {
        // A marker was tapped
        println("Tapped marker with ID: ${foundMarker.state.id}")
    }
}
```

---

### find (Convenience Override)

Finds the nearest marker to a given geographic position using the last known zoom level of the map.

**Signature**
```kotlin
override fun find(position: GeoPointInterface): MarkerEntityInterface<GoogleMapActualMarker>?
```

**Description**
This is a convenience override that calls the more specific `find` method with the controller's internally stored `lastKnownZoom`.

**Parameters**

| Parameter  | Type                | Description                                                  |
| :--------- | :------------------ | :----------------------------------------------------------- |
| `position` | `GeoPointInterface` | The geographic coordinates (latitude, longitude) to search around. |

**Returns**

`MarkerEntityInterface<GoogleMapActualMarker>?` - The nearest marker entity if one is found within the tolerance radius, otherwise `null`.

---

### onCameraChanged

Callback method that should be invoked whenever the map's camera position changes.

**Signature**
```kotlin
override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

**Description**
The controller uses this to update its internal state, such as the last known zoom level, which is crucial for the `find` operation and tiling logic.

**Parameters**

| Parameter         | Type                | Description                           |
| :---------------- | :------------------ | :------------------------------------ |
| `mapCameraPosition` | `MapCameraPosition` | The new camera position of the map. |

**Example**

```kotlin
// In your map setup
googleMap.setOnCameraIdleListener {
    val position = googleMap.cameraPosition
    val mapCameraPosition = MapCameraPosition(
        target = GeoPoint(position.target.latitude, position.target.longitude),
        zoom = position.zoom.toDouble(),
        // ... other camera properties
    )
    
    coroutineScope.launch {
        markerController.onCameraChanged(mapCameraPosition)
    }
}
```

---

### destroy

Cleans up all resources used by the controller.

**Signature**
```kotlin
override fun destroy()
```

**Description**
This method unregisters tile servers and removes any raster layers from the map. It should be called when the controller is no longer needed (e.g., in a `Fragment.onDestroyView()` or `Activity.onDestroy()` lifecycle event) to prevent memory leaks.

**Example**

```kotlin
override fun onDestroyView() {
    super.onDestroyView()
    markerController.destroy()
}
```

---

## Interfaces

### MarkerTileRasterLayerCallback

A functional (SAM) interface used to decouple the `GoogleMapMarkerController` from a `RasterLayerController`. It provides a single method to notify a listener about required changes to the raster layer used for marker tiling.

**Signature**
```kotlin
fun interface MarkerTileRasterLayerCallback
```

#### onRasterLayerUpdate

Called when the raster layer for marker tiles needs to be added, updated, or removed.

**Signature**
```kotlin
suspend fun onRasterLayerUpdate(state: RasterLayerState?)
```

**Parameters**

| Parameter | Type               | Description                                                                                                                            |
| :-------- | :----------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `state`   | `RasterLayerState?` | The new state for the raster layer. If `null`, the layer should be removed. Otherwise, the layer should be added or updated to this state. |