Of course! Here is a high-quality SDK document for the `GoogleMapView` composable function, formatted in Markdown.

---

# GoogleMapView

`GoogleMapView` is a Jetpack Compose composable that displays a Google Map. It provides a declarative way to manage the map's state, handle user interactions, and draw overlays such as markers, polylines, and polygons. The component is lifecycle-aware and integrates seamlessly with the Compose UI framework.

It offers two overloads: a comprehensive version with callbacks for all map and overlay events, and a simplified version for use cases that don't require detailed marker, polyline, or polygon event handling.

### Signature

```kotlin
// Full signature
@Composable
fun GoogleMapView(
    state: GoogleMapViewState,
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
    onGroundImageClick: OnGroundImageEventHandler? = null,
    content: (@Composable GoogleMapViewScope.() -> Unit)? = null,
)

// Simplified signature
@Composable
fun GoogleMapView(
    state: GoogleMapViewState,
    modifier: Modifier = Modifier,
    markerTiling: MarkerTilingOptions? = null,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    onGroundImageClick: OnGroundImageEventHandler? = null,
    content: (@Composable GoogleMapViewScope.() -> Unit)? = null,
)
```

### Description

This composable function renders a Google Map view and manages its lifecycle. It serves as the root container for all map-related UI, including overlays and controls. You can interact with the map and listen to events through various callback parameters. Map overlays like markers and shapes are added declaratively within the `content` lambda.

### Parameters

This section describes the parameters for the comprehensive `GoogleMapView` function.

| Parameter | Type | Description |
|---|---|---|
| `state` | `GoogleMapViewState` | **Required.** Manages the map's state, including camera position and map design type. Use `rememberGoogleMapViewState()` to create and remember an instance. |
| `modifier` | `Modifier` | An optional `Modifier` to be applied to the map container. |
| `markerTiling` | `MarkerTilingOptions?` | Optional configuration for marker tiling (clustering). If `null`, default options are used. |
| `sdkInitialize` | `(suspend (Context) -> Boolean)?` | An optional asynchronous lambda to initialize the Google Maps SDK. It should return `true` on success. If not provided, a default initialization is assumed. |
| `onMapLoaded` | `OnMapLoadedHandler?` | A callback invoked once the map has finished loading and is ready for interaction. |
| `onMapClick` | `OnMapEventHandler?` | A callback invoked when the user clicks on the map. Returns the `GeoPoint` of the click location. |
| `onCameraMoveStart` | `OnCameraMoveHandler?` | A callback invoked when the map camera starts moving. Provides the current `MapCameraPositionInterface`. |
| `onCameraMove` | `OnCameraMoveHandler?` | A callback invoked repeatedly while the map camera is moving. Provides the current `MapCameraPositionInterface`. |
| `onCameraMoveEnd` | `OnCameraMoveHandler?` | A callback invoked when the map camera finishes moving. Provides the final `MapCameraPositionInterface`. |
| `onMarkerClick` | `OnMarkerEventHandler?` | A callback invoked when a marker is clicked. Return `true` to indicate the event has been consumed. |
| `onMarkerDragStart` | `OnMarkerEventHandler?` | A callback invoked when the user starts dragging a marker. |
| `onMarkerDrag` | `OnMarkerEventHandler?` | A callback invoked repeatedly while a marker is being dragged. |
| `onMarkerDragEnd` | `OnMarkerEventHandler?` | A callback invoked when the user finishes dragging a marker. |
| `onMarkerAnimateStart`| `OnMarkerEventHandler?` | A callback invoked when a marker animation begins. |
| `onMarkerAnimateEnd` | `OnMarkerEventHandler?` | A callback invoked when a marker animation ends. |
| `onCircleClick` | `OnCircleEventHandler?` | A callback invoked when a circle overlay is clicked. |
| `onPolylineClick` | `OnPolylineEventHandler?` | A callback invoked when a polyline is clicked. |
| `onPolygonClick` | `OnPolygonEventHandler?` | A callback invoked when a polygon is clicked. |
| `onGroundImageClick`| `OnGroundImageEventHandler?` | A callback invoked when a ground image overlay is clicked. |
| `content` | `@Composable GoogleMapViewScope.() -> Unit` | A composable lambda within the `GoogleMapViewScope` where you can declaratively add map overlays like `Marker`, `Polyline`, `Polygon`, etc. |

### Returns

This is a `@Composable` function and does not have a return value. It emits the Google Map view into the composition.

### Example

Here is an example of how to use `GoogleMapView` in a Composable screen.

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.marker.Marker
import com.mapconductor.googlemaps.state.GoogleMapViewState
import com.mapconductor.googlemaps.state.rememberGoogleMapViewState

@Composable
fun MyMapScreen() {
    // 1. Remember the map's state.
    // This will survive recompositions and configuration changes.
    val mapState: GoogleMapViewState = rememberGoogleMapViewState(
        initialPosition = GeoPoint(40.7128, -74.0060), // New York City
        initialZoom = 12.0
    )

    // 2. Add the GoogleMapView to your composition.
    GoogleMapView(
        state = mapState,
        modifier = Modifier.fillMaxSize(),
        onMapClick = { geoPoint ->
            println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
        },
        onMarkerClick = { marker ->
            println("Marker clicked: ${marker.id}")
            // Return true to indicate the event was consumed and prevent default behavior.
            true
        }
    ) {
        // 3. Add overlays declaratively within the content lambda.
        // This Marker will be displayed on the map.
        Marker(
            id = "nyc-marker",
            position = GeoPoint(40.7128, -74.0060),
            title = "New York City",
            snippet = "The Big Apple"
        )
    }
}

@Preview
@Composable
fun MyMapScreenPreview() {
    // Note: Map previews may not render in the Android Studio editor
    // without special configuration. Run on an emulator or device.
    MyMapScreen()
}
```