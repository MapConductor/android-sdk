# SDK Documentation: HereMapView

This document provides a detailed reference for the `HereMapView` composable functions.

---

## HereMapView

This is the primary composable for embedding a HERE map into your Jetpack Compose application. It handles the map's lifecycle, state management, and provides a declarative scope for adding map overlays.

### Signature

```kotlin
@Composable
fun HereMapView(
    state: HereViewState,
    modifier: Modifier = Modifier,
    markerTiling: MarkerTilingOptions? = null,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    content: (@Composable HereViewScope.() -> Unit)? = null,
)
```

### Description

The `HereMapView` composable is the root component for displaying a HERE map. It integrates with the underlying HERE SDK for Android, managing its lifecycle within the Compose framework.

It uses a state-driven approach, where the `HereViewState` controls aspects like camera position and map style. The `content` lambda provides a `HereViewScope`, allowing you to declaratively add map objects such as markers, polylines, and polygons directly inside the map view.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `state` | `HereViewState` | **Required**. The state holder for the map. It controls the camera position, map design, and provides imperative access to the map controller. Typically created using `rememberHereViewState`. |
| `modifier` | `Modifier` | Optional. A standard Jetpack Compose `Modifier` to be applied to the map container. |
| `markerTiling` | `MarkerTilingOptions?` | Optional. Configuration for marker tiling (clustering) to improve performance with a large number of markers. Defaults to `MarkerTilingOptions.Default`. |
| `sdkInitialize` | `suspend (Context) -> Boolean` | Optional. A custom suspendable lambda for initializing the HERE SDK. If `null`, a default initialization is performed automatically. |
| `onMapLoaded` | `OnMapLoadedHandler?` | Optional. A callback invoked once the map scene has finished loading and the map is ready for interaction. |
| `onMapClick` | `OnMapEventHandler?` | Optional. A callback invoked when the user clicks on a point on the map that is not an overlay. The callback receives the `GeoCoordinate` of the click. |
| `onCameraMoveStart` | `OnCameraMoveHandler?` | Optional. A callback invoked when the map camera starts moving, either due to user interaction or programmatic changes. |
| `onCameraMove` | `OnCameraMoveHandler?` | Optional. A callback invoked continuously as the map camera is moving. |
| `onCameraMoveEnd` | `OnCameraMoveHandler?` | Optional. A callback invoked when the map camera has finished moving. |
| `content` | `@Composable HereViewScope.() -> Unit` | Optional. A composable lambda that defines the content to be displayed on the map. Use this to add overlays like `Marker`, `Polyline`, `Polygon`, etc. |

### Returns

This composable function does not return a value. It emits the HERE map UI into the composition.

### Example

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.here.sdk.core.GeoCoordinate
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.here.HereMapView
import com.mapconductor.here.Marker
import com.mapconductor.here.rememberHereViewState
import com.mapconductor.here.rememberMarkerState

@Composable
fun MyMapScreen() {
    // 1. Define the initial camera position
    val tokyoStation = GeoCoordinate(35.681236, 139.767125)
    val cameraPosition = MapCameraPosition(target = tokyoStation, zoom = 14.0)

    // 2. Create and remember the map's state
    val mapState = rememberHereViewState(cameraPosition = cameraPosition)

    // 3. Add the HereMapView to your composition
    HereMapView(
        state = mapState,
        modifier = Modifier.fillMaxSize(),
        onMapLoaded = {
            println("Map has finished loading.")
        },
        onMapClick = { coordinate ->
            println("Map clicked at: $coordinate")
        }
    ) {
        // 4. Add map overlays declaratively within the content scope
        Marker(
            state = rememberMarkerState(position = tokyoStation),
            title = "Tokyo Station",
            onClick = {
                println("Tokyo Station marker clicked!")
                true // Consume the click event
            }
        )
    }
}
```

---

## HereMapView (Deprecated)

This overload is deprecated. Event handling for overlays should be done on the state object of each individual overlay.

### Signature

```kotlin
@Deprecated("Use CircleState/PolylineState/PolygonState onClick instead.")
@Composable
fun HereMapView(
    // ... other parameters
    onMarkerClick: OnMarkerEventHandler?,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    // ... other parameters
)
```

### Description

**Deprecated:** This version of `HereMapView` is deprecated. It uses global callbacks for overlay events (e.g., `onMarkerClick`, `onCircleClick`). The recommended approach is to handle events on a per-overlay basis.

**Migration:** Move event handling logic from these global callbacks to the corresponding parameters on the individual overlay composables (e.g., the `onClick` parameter of the `Marker` composable). This provides a more granular, predictable, and state-driven way to manage interactions.

### Deprecated Parameters

| Parameter | Type | Migration Guide |
|---|---|---|
| `onMarkerClick` | `OnMarkerEventHandler?` | **Deprecated**. Use the `onClick` parameter on the `Marker` composable. |
| `onMarkerDragStart`| `OnMarkerEventHandler?` | **Deprecated**. Use the `onDragStart` parameter on a draggable `Marker`. |
| `onMarkerDrag` | `OnMarkerEventHandler?` | **Deprecated**. Use the `onDrag` parameter on a draggable `Marker`. |
| `onMarkerDragEnd` | `OnMarkerEventHandler?` | **Deprecated**. Use the `onDragEnd` parameter on a draggable `Marker`. |
| `onCircleClick` | `OnCircleEventHandler?` | **Deprecated**. Use the `onClick` parameter on the `Circle` composable. |
| `onPolylineClick` | `OnPolylineEventHandler?` | **Deprecated**. Use the `onClick` parameter on the `Polyline` composable. |
| `onPolygonClick` | `OnPolygonEventHandler?` | **Deprecated**. Use the `onClick` parameter on the `Polygon` composable. |

### Migration Example

**Before (Deprecated Pattern):**

```kotlin
// Deprecated: Avoid this pattern
HereMapView(
    state = mapState,
    onMarkerClick = { marker ->
        println("Marker ${marker.id} was clicked.")
        true
    }
) {
    Marker(state = rememberMarkerState(position = somePosition))
}
```

**After (Recommended Pattern):**

```kotlin
// Recommended: Handle events on the overlay itself
HereMapView(
    state = mapState
) {
    Marker(
        state = rememberMarkerState(position = somePosition),
        onClick = {
            println("Marker was clicked.")
            true // Consume the event
        }
    )
}
```