# MapboxMapView

A Jetpack Compose composable that renders an interactive Mapbox map. This is the primary entry point for displaying a map and its contents. It manages the map's lifecycle, state, and user interactions. Map overlays, such as markers, polylines, and polygons, are added declaratively within the `content` lambda.

## Signature

```kotlin
@Composable
fun MapboxMapView(
    state: MapboxViewState,
    modifier: Modifier = Modifier,
    markerTiling: MarkerTilingOptions? = null,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    content: (@Composable MapboxMapViewScope.() -> Unit)? = null,
)
```

## Description

The `MapboxMapView` composable integrates a Mapbox map into your Compose UI. It requires a `MapboxViewState` to manage the map's properties, such as camera position and map style. It also provides various callbacks for map events like loading, camera movement, and clicks.

Map overlays (e.g., `Marker`, `Polyline`, `Polygon`) are defined within the trailing `content` lambda, which provides a `MapboxMapViewScope`. This declarative approach allows you to tie your UI state directly to the map's content.

## Parameters

| Parameter | Type | Description |
| --- | --- | --- |
| `state` | `MapboxViewState` | Manages the map's state, including camera position, map style, and provides imperative control over the map. |
| `modifier` | `Modifier` | Standard Jetpack Compose modifier to be applied to the map view. Defaults to `Modifier`. |
| `markerTiling` | `MarkerTilingOptions?` | Optional configuration for marker tiling and clustering. If `null`, default options are used. |
| `sdkInitialize` | `(suspend (Context) -> Boolean)?` | An optional lambda to handle custom initialization of the Mapbox SDK. If not provided, a default initialization is performed. |
| `onMapLoaded` | `OnMapLoadedHandler?` | A callback invoked once the map has finished loading its style and is ready for interaction. |
| `onMapClick` | `OnMapEventHandler?` | A callback invoked when the user clicks on a point on the map that is not an overlay. |
| `onCameraMoveStart` | `OnCameraMoveHandler?` | A callback invoked when the map camera starts moving. The new camera position is provided. |
| `onCameraMove` | `OnCameraMoveHandler?` | A callback invoked continuously while the map camera is moving. The new camera position is provided. |
| `onCameraMoveEnd` | `OnCameraMoveHandler?` | A callback invoked when the map camera finishes moving. The final camera position is provided. |
| `content` | `(@Composable MapboxMapViewScope.() -> Unit)?` | A composable lambda block where map overlays like `Marker`, `Polyline`, and `Polygon` can be declared. It operates within a `MapboxMapViewScope`. |

## Returns

This is a composable function that emits UI and does not have a direct return value.

## Example

Here's a basic example of how to set up `MapboxMapView` and add a `Marker`.

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.util.Log
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.types.LatLng
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.marker.Marker
import com.mapconductor.mapbox.state.rememberMapboxViewState
import com.mapconductor.mapbox.state.rememberMarkerState

@Composable
fun MyMapScreen() {
    // 1. Define the initial camera position.
    val cameraPosition = MapCameraPosition(
        target = LatLng(37.7749, -122.4194), // San Francisco
        zoom = 12.0
    )

    // 2. Create and remember the map state.
    val mapState = rememberMapboxViewState(cameraPosition)

    // 3. Add the MapboxMapView composable to your UI.
    MapboxMapView(
        state = mapState,
        modifier = Modifier.fillMaxSize(),
        onMapLoaded = {
            Log.d("MyMapScreen", "Map is loaded and ready.")
        },
        onMapClick = { latLng ->
            Log.d("MyMapScreen", "Map clicked at: $latLng")
        }
    ) {
        // 4. Add map content declaratively within the content lambda.
        Marker(
            state = rememberMarkerState(
                position = LatLng(37.7749, -122.4194)
            ),
            title = "San Francisco City Hall",
            onClick = {
                Log.d("MyMapScreen", "Marker clicked!")
                // Return true to indicate the event was consumed
                true 
            }
        )
    }
}
```

---

## MapboxMapView (Deprecated)

This overload is deprecated. It provides direct click and drag event handlers for markers, circles, polylines, and polygons at the map level.

**Reason for Deprecation:** The recommended approach is to use the `onClick` handlers available on the respective state objects (e.g., `MarkerState`, `CircleState`) or composables (`Marker`, `Circle`). This provides a more idiomatic and scalable pattern in Jetpack Compose by associating behavior directly with the UI element it belongs to.

### Signature

```kotlin
@Deprecated("Use CircleState/PolylineState/PolygonState onClick instead.")
@Composable
fun MapboxMapView(
    state: MapboxViewState,
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
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapboxMapViewScope.() -> Unit)? = null,
)
```

### Deprecated Parameters

| Parameter | Type | Description |
| --- | --- | --- |
| `onMarkerClick` | `OnMarkerEventHandler?` | **Deprecated**. Use the `onClick` lambda on the `Marker` composable instead. |
| `onMarkerDragStart` | `OnMarkerEventHandler?` | **Deprecated**. Drag events are handled via `MarkerState` and its `draggable` property. |
| `onMarkerDrag` | `OnMarkerEventHandler?` | **Deprecated**. Drag events are handled via `MarkerState` and its `draggable` property. |
| `onMarkerDragEnd` | `OnMarkerEventHandler?` | **Deprecated**. Drag events are handled via `MarkerState` and its `draggable` property. |
| `onCircleClick` | `OnCircleEventHandler?` | **Deprecated**. Use the `onClick` lambda on the `Circle` composable instead. |
| `onPolylineClick` | `OnPolylineEventHandler?` | **Deprecated**. Use the `onClick` lambda on the `Polyline` composable instead. |
| `onPolygonClick` | `OnPolygonEventHandler?` | **Deprecated**. Use the `onClick` lambda on the `Polygon` composable instead. |