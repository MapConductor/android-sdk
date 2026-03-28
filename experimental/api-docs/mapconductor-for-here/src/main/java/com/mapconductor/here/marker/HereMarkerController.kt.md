Excellent. Here is the high-quality SDK documentation for the provided `HereMarkerController` code snippet.

# HereMarkerController

The `HereMarkerController` is a class responsible for managing and rendering markers on a HERE map instance. It serves as a high-level abstraction over the map's native marker system, providing a unified API for adding, updating, and removing markers.

A key feature of this controller is its ability to automatically switch between rendering individual markers and using a server-side tile-based rendering strategy. This optimization, known as marker tiling, significantly improves performance when displaying a large number of markers by rendering them as raster tile images instead of individual map objects.

An instance of this controller should be created using the `HereMarkerController.create()` factory method.

## Companion Object

### create

Creates a new instance of the `HereMarkerController`. This is the designated factory method for initializing the controller and its required dependencies, such as the marker renderer and manager.

**Signature**
```kotlin
fun create(
    holder: HereViewHolder,
    markerTiling: MarkerTilingOptions = MarkerTilingOptions.Default,
): HereMarkerController
```

**Description**
This method sets up the `HereMarkerController` with the necessary components to interact with a HERE map. It initializes the `HereMarkerRenderer` for drawing markers and the `MarkerManager` for state management.

**Parameters**

| Parameter      | Type                  | Description                                                                                                                                                           |
| :------------- | :-------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `holder`       | `HereViewHolder`      | The view holder that provides the connection to the underlying `MapView` instance.                                                                                    |
| `markerTiling` | `MarkerTilingOptions` | *(Optional)* Configuration for the marker tiling optimization. Tiling is enabled when the number of markers exceeds `minMarkerCount`. Defaults to `MarkerTilingOptions.Default`. |

**Returns**

A new, fully initialized `HereMarkerController` instance.

**Example**
```kotlin
// Assuming 'hereViewHolder' is an initialized HereViewHolder instance
val markerController = HereMarkerController.create(
    holder = hereViewHolder,
    markerTiling = MarkerTilingOptions(
        enabled = true,
        minMarkerCount = 100
    )
)
```

---

## Methods

### setRasterLayerCallback

Sets a callback to receive updates for the raster layer used for tiled markers. When marker tiling is active, the controller generates a `RasterLayerState` that must be added to the map to display the tiled markers. This callback provides that state.

**Signature**
```kotlin
fun setRasterLayerCallback(callback: MarkerTileRasterLayerCallback?)
```

**Description**
Register a callback function that will be invoked whenever the raster layer for tiled markers is created, updated, or removed. Your application should use this callback to manage the `RasterLayer` on the map view. Pass `null` to remove a previously set callback.

**Parameters**

| Parameter  | Type                          | Description                                                                                                                                                           |
| :--------- | :---------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `callback` | `MarkerTileRasterLayerCallback?` | The callback to be invoked with the `RasterLayerState`. It receives the new state, or `null` if the tile layer is removed (e.g., when all markers are cleared). |

**Returns**

This method does not return a value.

**Example**
```kotlin
markerController.setRasterLayerCallback { rasterLayerState ->
    // In a real app, you would have a RasterLayerController
    // to add/update/remove layers on the map.
    if (rasterLayerState != null) {
        println("Raster layer updated: ${rasterLayerState.id}")
        // rasterLayerController.addOrUpdateLayer(rasterLayerState)
    } else {
        println("Raster layer removed.")
        // rasterLayerController.removeLayer("marker-tile-layer-id")
    }
}
```

---

### find

Finds the nearest marker to a given geographic position within a specified tap tolerance. This is primarily used to detect user interactions like taps on a marker.

**Signature**
```kotlin
override fun find(position: GeoPointInterface): MarkerEntityInterface<HereActualMarker>?
```

**Description**
This method calculates if the given `position` (e.g., from a map tap event) falls within the tappable area of a nearby marker. The tappable area is determined by the marker's icon size, anchor point, and a system-defined tolerance, ensuring a user-friendly touch target.

**Parameters**

| Parameter  | Type                | Description                                                              |
| :--------- | :------------------ | :----------------------------------------------------------------------- |
| `position` | `GeoPointInterface` | The geographic coordinate to search for a marker, typically from a user tap. |

**Returns**

A `MarkerEntityInterface<HereActualMarker>` representing the found marker, or `null` if no marker is found at the specified position.

**Example**
```kotlin
mapView.gestures.tapListener = TapListener { touchPoint ->
    val geoCoordinates = mapView.viewToGeoCoordinates(touchPoint)
    val tappedMarker = geoCoordinates?.let { markerController.find(it) }

    if (tappedMarker != null) {
        println("Marker tapped: ${tappedMarker.state.id}")
    }
}
```

---

### add

Adds a list of markers to the map. The controller will automatically decide whether to render them as individual objects or as part of a tiled layer for performance.

**Signature**
```kotlin
override suspend fun add(data: List<MarkerState>)
```

**Description**
This asynchronous method ingests a list of `MarkerState` objects. If tiling is enabled and the total marker count meets the threshold, markers that are not draggable and have no animations will be added to the tiled layer. Otherwise, they will be rendered as individual map markers.

**Parameters**

| Parameter | Type                | Description                                                              |
| :-------- | :------------------ | :----------------------------------------------------------------------- |
| `data`    | `List<MarkerState>` | A list of `MarkerState` objects, each defining a marker to be added. |

**Returns**

This is a `suspend` function and does not return a value.

**Example**
```kotlin
// This should be called from a coroutine scope
lifecycleScope.launch {
    val newMarkers = listOf(
        MarkerState(id = "marker-1", position = GeoPoint(40.7128, -74.0060)),
        MarkerState(id = "marker-2", position = GeoPoint(34.0522, -118.2437))
    )
    markerController.add(newMarkers)
}
```

---

### update

Updates the state of a single existing marker.

**Signature**
```kotlin
override suspend fun update(state: MarkerState)
```

**Description**
This asynchronous method updates an existing marker identified by the `id` in the provided `MarkerState`. It efficiently handles changes to properties like position, icon, and visibility. It also manages the transition of a marker between being individually rendered and being part of a tiled layer. If no marker with the given ID exists, the operation is ignored.

**Parameters**

| Parameter | Type          | Description                                                                                             |
| :-------- | :------------ | :------------------------------------------------------------------------------------------------------ |
| `state`   | `MarkerState` | The new state for the marker. The `id` field is used to identify which existing marker to update. |

**Returns**

This is a `suspend` function and does not return a value.

**Example**
```kotlin
// This should be called from a coroutine scope
lifecycleScope.launch {
    // Update the position of an existing marker with id "marker-1"
    val updatedState = MarkerState(
        id = "marker-1",
        position = GeoPoint(40.7130, -74.0062) // New position
    )
    markerController.update(updatedState)
}
```

---

### clear

Removes all markers from the map.

**Signature**
```kotlin
override suspend fun clear()
```

**Description**
This asynchronous method removes all individually rendered markers and clears any active marker tile layers. It effectively resets the controller to an empty state.

**Parameters**

This method takes no parameters.

**Returns**

This is a `suspend` function and does not return a value.

**Example**
```kotlin
// This should be called from a coroutine scope
lifecycleScope.launch {
    markerController.clear()
}
```

---

### onCameraChanged

A lifecycle method called by the map framework when the camera position changes.

**Signature**
```kotlin
override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

**Description**
This method is intended for internal use by the map integration layer. It listens to camera updates to track the current zoom level, which is crucial for determining which marker tiles to display. Developers typically do not need to call this method directly.

**Parameters**

| Parameter           | Type                | Description                               |
| :------------------ | :------------------ | :---------------------------------------- |
| `mapCameraPosition` | `MapCameraPosition` | The new state of the map camera. |

**Returns**

This is a `suspend` function and does not return a value.

---

### destroy

Cleans up all resources used by the controller.

**Signature**
```kotlin
override fun destroy()
```

**Description**
This method must be called to release resources when the controller is no longer needed (e.g., in `onDestroy` of an Activity or Fragment). It unregisters tile providers, clears internal references, and ensures no memory leaks occur.

**Parameters**

This method takes no parameters.

**Returns**

This method does not return a value.

**Example**
```kotlin
override fun onDestroy() {
    super.onDestroy()
    markerController.destroy()
}
```