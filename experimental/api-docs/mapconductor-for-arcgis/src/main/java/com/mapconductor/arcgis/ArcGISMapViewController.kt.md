# ArcGISMapViewController

The `ArcGISMapViewController` class is the primary controller for managing the ArcGIS map view. It orchestrates interactions between the map, camera, and various data overlays like markers, polylines, and polygons. This controller handles user input events (taps, drags), manages the lifecycle of map features, and provides an interface for programmatic camera control.

## Signature

```kotlin
class ArcGISMapViewController(
    override val holder: ArcGISMapViewHolder,
    private val markerController: ArcGISMarkerController,
    private val polylineController: ArcGISPolylineOverlayController,
    private val polygonController: ArcGISPolygonOverlayController,
    private val circleController: ArcGISCircleOverlayController,
    private val groundImageController: ArcGISGroundImageController,
    private val rasterLayerController: ArcGISRasterLayerController,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(), ArcGISMapViewControllerInterface
```

## Constructor

Initializes a new instance of the `ArcGISMapViewController`.

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `holder` | `ArcGISMapViewHolder` | The view holder that contains the ArcGIS `MapView` instance. |
| `markerController` | `ArcGISMarkerController` | The controller responsible for managing marker overlays. |
| `polylineController` | `ArcGISPolylineOverlayController` | The controller for managing polyline overlays. |
| `polygonController` | `ArcGISPolygonOverlayController` | The controller for managing polygon overlays. |
| `circleController` | `ArcGISCircleOverlayController` | The controller for managing circle overlays. |
| `groundImageController` | `ArcGISGroundImageController` | The controller for managing ground image overlays. |
| `rasterLayerController` | `ArcGISRasterLayerController` | The controller for managing raster layer overlays. |
| `coroutine` | `CoroutineScope` | The coroutine scope used for managing asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Default)`. |

## Properties

These properties are inherited from `BaseMapViewController` and are used to set callbacks for various map events.

| Property | Type | Description |
| :--- | :--- | :--- |
| `mapClickCallback` | `((GeoPoint) -> Unit)?` | A callback invoked when the user taps on the map at a location where no other overlay was tapped. The `GeoPoint` represents the coordinate of the tap. |
| `mapLongClickCallback` | `((GeoPoint) -> Unit)?` | A callback invoked when the user long-presses on the map. The `GeoPoint` represents the coordinate of the long-press. |
| `cameraMoveStartCallback` | `((MapCameraPosition) -> Unit)?` | A callback invoked when the map camera starts moving. |
| `cameraMoveCallback` | `((MapCameraPosition) -> Unit)?` | A callback invoked continuously while the map camera is moving. |
| `cameraMoveEndCallback` | `((MapCameraPosition) -> Unit)?` | A callback invoked when the map camera has finished moving. This is debounced to prevent rapid firing. |
| `mapLoadedCallback` | `(() -> Unit)?` | A one-time callback invoked when the map has finished its initial load. It is set to `null` after being called. |

## Methods

### Camera Control

#### moveCamera

Instantly moves the map camera to a specified position without animation.

**Signature**
```kotlin
fun moveCamera(position: MapCameraPosition)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `MapCameraPosition` | The target camera position, including location, zoom, bearing, and tilt. |

---

#### animateCamera

Animates the map camera from its current position to a specified position over a given duration.

**Signature**
```kotlin
suspend fun animateCamera(
    position: MapCameraPosition,
    duration: Long
)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `MapCameraPosition` | The target camera position to animate to. |
| `duration` | `Long` | The duration of the animation in milliseconds. |

---

### Overlay Management

#### clearOverlays

Removes all overlays (markers, polylines, polygons, circles, ground images, and raster layers) from the map.

**Signature**
```kotlin
suspend fun clearOverlays()
```

---

#### compositionMarkers

Adds a list of new markers to the map.

**Signature**
```kotlin
suspend fun compositionMarkers(data: List<MarkerState>)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<MarkerState>` | A list of `MarkerState` objects to be added to the map. |

---

#### updateMarker

Updates the state of an existing marker on the map.

**Signature**
```kotlin
suspend fun updateMarker(state: MarkerState)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `MarkerState` | The new state for the marker to be updated. |

---

#### compositionPolylines

Adds a list of new polylines to the map.

**Signature**
```kotlin
suspend fun compositionPolylines(data: List<PolylineState>)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<PolylineState>` | A list of `PolylineState` objects to be added. |

---

#### updatePolyline

Updates the state of an existing polyline on the map.

**Signature**
```kotlin
suspend fun updatePolyline(state: PolylineState)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `PolylineState` | The new state for the polyline to be updated. |

---

#### compositionPolygons

Adds a list of new polygons to the map.

**Signature**
```kotlin
suspend fun compositionPolygons(data: List<PolygonState>)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<PolygonState>` | A list of `PolygonState` objects to be added. |

---

#### updatePolygon

Updates the state of an existing polygon on the map.

**Signature**
```kotlin
suspend fun updatePolygon(state: PolygonState)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `PolygonState` | The new state for the polygon to be updated. |

---

#### compositionCircles

Adds a list of new circles to the map.

**Signature**
```kotlin
suspend fun compositionCircles(data: List<CircleState>)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<CircleState>` | A list of `CircleState` objects to be added. |

---

#### updateCircle

Updates the state of an existing circle on the map.

**Signature**
```kotlin
suspend fun updateCircle(state: CircleState)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `CircleState` | The new state for the circle to be updated. |

---

#### compositionGroundImages

Adds a list of new ground images to the map.

**Signature**
```kotlin
suspend fun compositionGroundImages(data: List<GroundImageState>)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<GroundImageState>` | A list of `GroundImageState` objects to be added. |

---

#### updateGroundImage

Updates the state of an existing ground image on the map.

**Signature**
```kotlin
suspend fun updateGroundImage(state: GroundImageState)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `GroundImageState` | The new state for the ground image to be updated. |

---

#### compositionRasterLayers

Adds a list of new raster layers to the map.

**Signature**
```kotlin
suspend fun compositionRasterLayers(data: List<RasterLayerState>)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<RasterLayerState>` | A list of `RasterLayerState` objects to be added. |

---

#### updateRasterLayer

Updates the state of an existing raster layer on the map.

**Signature**
```kotlin
suspend fun updateRasterLayer(state: RasterLayerState)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `RasterLayerState` | The new state for the raster layer to be updated. |

---

### Overlay Checks

These methods check for the existence of a specific overlay entity on the map.

| Method | Description | Returns |
| :--- | :--- | :--- |
| `hasMarker(state: MarkerState)` | Checks if a marker with the given state's ID exists. | `Boolean` |
| `hasPolyline(state: PolylineState)` | Checks if a polyline with the given state's ID exists. | `Boolean` |
| `hasPolygon(state: PolygonState)` | Checks if a polygon with the given state's ID exists. | `Boolean` |
| `hasCircle(state: CircleState)` | Checks if a circle with the given state's ID exists. | `Boolean` |
| `hasGroundImage(state: GroundImageState)` | Checks if a ground image with the given state's ID exists. | `Boolean` |
| `hasRasterLayer(state: RasterLayerState)` | Checks if a raster layer with the given state's ID exists. | `Boolean` |

---

### Map Configuration

#### setMapDesignType

Sets the visual style (basemap) of the map.

**Signature**
```kotlin
fun setMapDesignType(value: ArcGISDesignTypeInterface)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `value` | `ArcGISDesignTypeInterface` | The desired map design type (e.g., `ArcGISDesign.Streets`). |

---

#### setMapDesignTypeChangeListener

Registers a listener that is invoked when the map's design type changes. The listener is also immediately called with the current design type upon registration.

**Signature**
```kotlin
fun setMapDesignTypeChangeListener(listener: ArcGISDesignTypeChangeHandler)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `listener` | `ArcGISDesignTypeChangeHandler` | The callback to be invoked with the map design type. |

---

### Initialization

#### sendInitialCameraUpdate

Manually triggers a camera position update. This is useful for notifying listeners of the initial camera state after the map view is ready.

**Signature**
```kotlin
fun sendInitialCameraUpdate()
```

---

### Deprecated Event Listeners

These methods are deprecated. It is recommended to set event handlers directly on the `State` object for each overlay (e.g., `MarkerState.onClick`, `PolylineState.onClick`).

| Method | Deprecation Reason |
| :--- | :--- |
| `setOnCircleClickListener(listener: OnCircleEventHandler?)` | Use `CircleState.onClick` instead. |
| `setOnGroundImageClickListener(listener: OnGroundImageEventHandler?)` | Use `GroundImageState.onClick` instead. |
| `setOnMarkerDragStart(listener: OnMarkerEventHandler?)` | Use `MarkerState.onDragStart` instead. |
| `setOnMarkerDrag(listener: OnMarkerEventHandler?)` | Use `MarkerState.onDrag` instead. |
| `setOnMarkerDragEnd(listener: OnMarkerEventHandler?)` | Use `MarkerState.onDragEnd` instead. |
| `setOnMarkerAnimateStart(listener: OnMarkerEventHandler?)` | Use `MarkerState.onAnimateStart` instead. |
| `setOnMarkerAnimateEnd(listener: OnMarkerEventHandler?)` | Use `MarkerState.onAnimateEnd` instead. |
| `setOnMarkerClickListener(listener: OnMarkerEventHandler?)` | Use `MarkerState.onClick` instead. |
| `setOnPolylineClickListener(listener: OnPolylineEventHandler?)` | Use `PolylineState.onClick` instead. |
| `setOnPolygonClickListener(listener: OnPolygonEventHandler?)` | Use `PolygonState.onClick` instead. |

## Example

Here is an example of how to initialize the `ArcGISMapViewController`, add a marker, and handle map click events.

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.MarkerState
import kotlinx.coroutines.launch

// Assume 'mapViewController' is an initialized instance of ArcGISMapViewController

// 1. Set a callback for map clicks
mapViewController.mapClickCallback = { geoPoint ->
    println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
}

// 2. Define a marker
val tokyoStation = GeoPoint.fromLongLat(139.7671, 35.6812)
val marker = MarkerState(
    id = "tokyo-station-marker",
    position = tokyoStation,
    title = "Tokyo Station",
    draggable = true
).apply {
    // Set an onClick listener directly on the marker state
    onClick = { event ->
        println("Marker '${event.state.title}' clicked!")
        // Return true to indicate the event was handled
        true 
    }
}

// 3. Add the marker to the map within a coroutine scope
mapViewController.coroutine.launch {
    mapViewController.compositionMarkers(listOf(marker))
}

// 4. Move the camera to the marker's location
val cameraPosition = MapCameraPosition(
    position = tokyoStation,
    zoom = 15.0
)
mapViewController.moveCamera(cameraPosition)
```