Of course! Here is the high-quality SDK documentation for the provided `ArcGISMapView` composable.

---

### `ArcGISMapView`

A Jetpack Compose composable that displays an interactive 3D ArcGIS map. This component is built upon the ArcGIS Maps SDK for Android `SceneView` and provides a declarative, Compose-native way to manage map state, handle user interactions, and render various overlays.

The `ArcGISMapView` is the root component for all ArcGIS map-related UI. You can add children composables like `Marker`, `Polygon`, `Polyline`, etc., within its `content` lambda to render objects on the map.

### Signature

```kotlin
@Composable
fun ArcGISMapView(
    state: ArcGISMapViewState,
    modifier: Modifier = Modifier,
    markerTiling: MarkerTilingOptions? = null,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    content: (@Composable ArcGISMapViewScope.() -> Unit)? = null,
)
```

### Description

This composable function renders an ArcGIS map and manages its lifecycle within a Compose application. It takes a `ArcGISMapViewState` to control properties like camera position and map style. It also provides callbacks for various map events, such as map loading and camera movement.

Map overlays and other UI elements are added declaratively within the trailing `content` lambda, which provides an `ArcGISMapViewScope`.

### Parameters

| Parameter | Type | Description |
| --- | --- | --- |
| `state` | `ArcGISMapViewState` | The state object that holds map configuration, such as camera position and map design. It also controls the map's lifecycle and provides an interface to interact with the map controller. |
| `modifier` | `Modifier` | The `Modifier` to be applied to the map view layout. |
| `markerTiling` | `MarkerTilingOptions?` | Optional configuration for marker tiling to optimize performance with a large number of markers. If `null`, `MarkerTilingOptions.Default` is used. |
| `sdkInitialize` | `(suspend (Context) -> Boolean)?` | An optional suspend lambda to perform custom initialization of the ArcGIS SDK. If not provided, a default initializer is used which attempts to set the API key from the app's `AndroidManifest.xml`. Return `true` on success and `false` on failure. |
| `onMapLoaded` | `OnMapLoadedHandler?` | A callback invoked once when the base map has successfully loaded and is ready for interaction. |
| `onCameraMoveStart` | `OnCameraMoveHandler?` | A callback invoked when the map camera starts moving, either due to user gesture or programmatic animation. It receives the current `MapCameraPosition`. |
| `onCameraMove` | `OnCameraMoveHandler?` | A callback invoked continuously while the map camera is moving. It receives the current `MapCameraPosition`. |
| `onCameraMoveEnd` | `OnCameraMoveHandler?` | A callback invoked when the map camera finishes moving. It receives the final `MapCameraPosition`. |
| `onMapClick` | `OnMapEventHandler?` | A callback invoked when the user clicks on a point on the map that is not an overlay. It receives the `LatLng` of the click location. |
| `content` | `(@Composable ArcGISMapViewScope.() -> Unit)?` | A composable lambda within the `ArcGISMapViewScope` where you can declaratively add map overlays like `Marker`, `Polygon`, `Polyline`, `Circle`, etc. |

### Returns

This is a `@Composable` function and does not have a return value. It emits the ArcGIS map view UI into the composition.

### Deprecated Overload

An older version of `ArcGISMapView` exists that accepts click handlers for individual overlay types (`onMarkerClick`, `onPolygonClick`, etc.). This overload is deprecated.

```kotlin
@Deprecated("Use CircleState/PolylineState/PolygonState onClick instead.")
@Composable
fun ArcGISMapView(
    // ... other parameters
    onMarkerClick: OnMarkerEventHandler?,
    onCircleClick: OnCircleEventHandler?,
    onPolylineClick: OnPolylineEventHandler?,
    onPolygonClick: OnPolygonEventHandler?,
    // ...
)
```

**Reason for Deprecation:** The modern approach is to handle click events directly on the state object associated with each overlay (e.g., `rememberMarkerState(onClick = { ... })`). This provides a more granular and idiomatic Compose API, associating the event handling logic directly with the state of the UI element.

### Example

Here is a complete example of how to use `ArcGISMapView` in a Jetpack Compose screen.

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.util.Log
import com.mapconductor.arcgis.map.ArcGISMapView
import com.mapconductor.arcgis.map.rememberArcGISMapViewState
import com.mapconductor.arcgis.marker.Marker
import com.mapconductor.arcgis.marker.rememberMarkerState
import com.mapconductor.arcgis.polygon.Polygon
import com.mapconductor.arcgis.polygon.rememberPolygonState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.types.LatLng

@Composable
fun MyMapScreen() {
    // 1. Remember the map view state to control the map.
    val mapState = rememberArcGISMapViewState(
        initialCameraPosition = MapCameraPosition(
            target = LatLng(34.0522, -118.2437), // Los Angeles
            zoom = 12.0
        )
    )

    // 2. Use the ArcGISMapView composable.
    ArcGISMapView(
        state = mapState,
        modifier = Modifier.fillMaxSize(),
        onMapLoaded = {
            Log.d("MyMapScreen", "ArcGIS map has loaded.")
        },
        onCameraMoveEnd = { newPosition ->
            Log.d("MyMapScreen", "Camera stopped at: ${newPosition.target}")
        }
    ) {
        // 3. Add overlays declaratively within the content lambda.

        // Add a marker
        val markerState = rememberMarkerState(
            position = LatLng(34.0522, -118.2437)
        )
        Marker(
            state = markerState,
            title = "Los Angeles City Hall",
            snippet = "A historic landmark."
        )

        // Add a polygon with a click listener
        val polygonState = rememberPolygonState(
            points = listOf(
                LatLng(34.06, -118.24),
                LatLng(34.06, -118.25),
                LatLng(34.05, -118.25),
                LatLng(34.05, -118.24)
            ),
            onClick = { polygonId ->
                Log.d("MyMapScreen", "Polygon $polygonId was clicked!")
            }
        )
        Polygon(
            state = polygonState,
            fillColor = Color.Blue.copy(alpha = 0.3f),
            strokeColor = Color.Blue,
            strokeWidth = 2f
        )
    }
}
```