Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

# MapLibreMapView

## `MapLibreMapView`

A Jetpack Compose component for displaying an interactive MapLibre map. This is the primary entry point for integrating MapLibre into a Compose application.

This composable manages the map's lifecycle, state, camera position, and user interactions. Map overlays such as markers, polylines, and polygons are added as children within the trailing `content` lambda, which provides a `MapLibreMapViewScope`.

### Signature
```kotlin
@Composable
fun MapLibreMapView(
    state: MapLibreViewState,
    modifier: Modifier = Modifier,
    markerTiling: MarkerTilingOptions? = null,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    content: (@Composable MapLibreMapViewScope.() -> Unit)? = null,
)
```

### Description
This function sets up the MapLibre map view and binds it to the provided `MapLibreViewState`. It handles the initialization of the MapLibre SDK, map style loading, and camera updates. Event listeners for map interactions like clicks and camera movements can be provided.

### Parameters
| Parameter | Type | Description |
| --- | --- | --- |
| `state` | `MapLibreViewState` | The state object that manages the map's properties, such as camera position and map style. See `rememberMapLibreViewState`. |
| `modifier` | `Modifier` | A `Modifier` to be applied to the map container. |
| `markerTiling` | `MarkerTilingOptions?` | Optional configuration for marker tiling and clustering. Defaults to `null`. |
| `sdkInitialize` | `(suspend (Context) -> Boolean)?` | An optional suspend lambda to perform custom initialization of the MapLibre SDK. If `null`, the default `MapLibre.getInstance(context)` is used. |
| `onMapLoaded` | `OnMapLoadedHandler?` | A callback invoked when the map and its style have been fully loaded and the map is ready for interaction. |
| `onMapClick` | `OnMapEventHandler?` | A callback invoked when the user clicks on the map. It receives the `LatLng` and screen `Point` of the click. Return `true` to indicate the event has been handled. |
| `onCameraMoveStart` | `OnCameraMoveHandler?` | A callback invoked when the camera starts moving. |
| `onCameraMove` | `OnCameraMoveHandler?` | A callback invoked repeatedly while the camera is moving. |
| `onCameraMoveEnd` | `OnCameraMoveHandler?` | A callback invoked when the camera movement has finished. |
| `content` | `(@Composable MapLibreMapViewScope.() -> Unit)?` | A trailing lambda where you can declare map overlays like `Marker`, `Polyline`, `Polygon`, and `Circle` within the `MapLibreMapViewScope`. |

### Example
Here is a basic example of how to use `MapLibreMapView` in a Composable function.

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.state.rememberMapLibreViewState
import com.mapconductor.maplibre.marker.Marker
import com.mapconductor.maplibre.state.rememberMarkerState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.types.LatLng

@Composable
fun MyMapScreen() {
    // Create and remember the map's state
    val mapState = rememberMapLibreViewState(
        mapDesignType = MapDesignType.Default,
        cameraPosition = MapCameraPosition(
            target = LatLng(37.7749, -122.4194), // San Francisco
            zoom = 12.0
        )
    )

    MapLibreMapView(
        state = mapState,
        modifier = Modifier.fillMaxSize(),
        onMapLoaded = {
            println("Map is fully loaded and ready.")
        },
        onMapClick = { latLng, point ->
            println("Map clicked at: $latLng")
            true // Event was handled
        }
    ) {
        // Add map overlays within this scope
        Marker(
            state = rememberMarkerState(
                position = LatLng(37.7749, -122.4194),
                onClick = { marker, latLng ->
                    println("Marker clicked!")
                    true // Event was handled
                }
            ),
            title = "San Francisco"
        )
    }
}
```

---

## `MapLibreMapView` (Deprecated)

**Deprecated:** Use the primary `MapLibreMapView` composable instead. The event handlers for map overlays (`onMarkerClick`, `onPolylineClick`, etc.) have been moved to their respective state objects (e.g., `rememberMarkerState(onClick = { ... })`). This provides a more granular and Compose-idiomatic way to handle events.

### Signature
```kotlin
@Deprecated("Use CircleState/PolylineState/PolygonState onClick instead.")
@Composable
fun MapLibreMapView(
    state: MapLibreViewState,
    modifier: Modifier = Modifier,
    markerTiling: MarkerTilingOptions? = null,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    onMarkerClick: OnMarkerEventHandler?,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapLibreMapViewScope.() -> Unit)? = null,
)
```

### Description
This deprecated version of `MapLibreMapView` provides global event handlers for all markers, polylines, circles, and polygons on the map. For new implementations, it is strongly recommended to use the primary `MapLibreMapView` and handle click events on the state objects of individual overlays (e.g., `rememberMarkerState`, `rememberPolylineState`).

### Parameters
| Parameter | Type | Description |
| --- | --- | --- |
| `state` | `MapLibreViewState` | The state object that manages the map's properties. |
| `modifier` | `Modifier` | A `Modifier` to be applied to the map container. |
| `markerTiling` | `MarkerTilingOptions?` | Optional configuration for marker tiling and clustering. |
| `sdkInitialize` | `(suspend (Context) -> Boolean)?` | Optional lambda for custom SDK initialization. |
| `onMapLoaded` | `OnMapLoadedHandler?` | Callback invoked when the map is fully loaded. |
| `onMapClick` | `OnMapEventHandler?` | Callback invoked when the user clicks on the map. |
| `onCameraMoveStart` | `OnCameraMoveHandler?` | Callback invoked when the camera starts moving. |
| `onCameraMove` | `OnCameraMoveHandler?` | Callback invoked while the camera is moving. |
| `onCameraMoveEnd` | `OnCameraMoveHandler?` | Callback invoked when the camera movement has finished. |
| `onMarkerClick` | `OnMarkerEventHandler?` | **Deprecated.** A global callback for marker click events. Use the `onClick` parameter in `rememberMarkerState` instead. |
| `onMarkerDragStart` | `OnMarkerEventHandler?` | **Deprecated.** A global callback for marker drag start events. Use the `onDragStart` parameter in `rememberMarkerState` instead. |
| `onMarkerDrag` | `OnMarkerEventHandler?` | **Deprecated.** A global callback for marker drag events. Use the `onDrag` parameter in `rememberMarkerState` instead. |
| `onMarkerDragEnd` | `OnMarkerEventHandler?` | **Deprecated.** A global callback for marker drag end events. Use the `onDragEnd` parameter in `rememberMarkerState` instead. |
| `onMarkerAnimateStart` | `OnMarkerEventHandler?` | **Deprecated.** A global callback for marker animation start events. |
| `onMarkerAnimateEnd` | `OnMarkerEventHandler?` | **Deprecated.** A global callback for marker animation end events. |
| `onPolylineClick` | `OnPolylineEventHandler?` | **Deprecated.** A global callback for polyline click events. Use the `onClick` parameter in `rememberPolylineState` instead. |
| `onCircleClick` | `OnCircleEventHandler?` | **Deprecated.** A global callback for circle click events. Use the `onClick` parameter in `rememberCircleState` instead. |
| `onPolygonClick` | `OnPolygonEventHandler?` | **Deprecated.** A global callback for polygon click events. Use the `onClick` parameter in `rememberPolygonState` instead. |
| `content` | `(@Composable MapLibreMapViewScope.() -> Unit)?` | A trailing lambda for declaring map overlays. |