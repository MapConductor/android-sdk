# SDK Documentation

This document provides details on the `CompositionLocal` providers available within the MapConductor SDK for Jetpack Compose. These locals are essential for interacting with the map and its components from within your custom composables.

---

## `LocalMapOverlayRegistry`

A `CompositionLocal` that provides access to the `MapOverlayRegistry` instance.

### Signature

```kotlin
val LocalMapOverlayRegistry: CompositionLocal<MapOverlayRegistry>
```

### Description

This `CompositionLocal` provides access to the `MapOverlayRegistry` instance associated with the current `MapView`. The registry is the primary mechanism for adding, removing, and managing map overlays (like markers, polylines, and polygons) declaratively within your composable hierarchy.

**Important:** Any composable that accesses `LocalMapOverlayRegistry.current` must be a descendant of a `<MapView />` component in the UI tree. Accessing it outside of a `MapView`'s composition scope will throw a runtime `error`.

### Returns

| Type | Description |
| --- | --- |
| `MapOverlayRegistry` | The `MapOverlayRegistry` instance for the current map scope. |

### Example

The following example demonstrates how to create a custom composable that accesses the `MapOverlayRegistry` to add a new overlay to the map.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.mapconductor.core.map.LocalMapOverlayRegistry
import com.mapconductor.core.MapView

@Composable
fun CustomMarkerOverlay() {
    // Access the registry from the composition local.
    val overlayRegistry = LocalMapOverlayRegistry.current

    // Use a LaunchedEffect to add the overlay once when the composable enters
    // the composition. The registry will handle its lifecycle.
    LaunchedEffect(Unit) {
        val marker = /* ... create your marker overlay object ... */
        overlayRegistry.addOverlay(marker)
    }
}

// Usage within a MapView
@Composable
fun MyMapScreen() {
    MapView {
        // CustomMarkerOverlay is a descendant of MapView, so it can
        // safely access LocalMapOverlayRegistry.
        CustomMarkerOverlay()
    }
}
```

---

## `LocalMapViewController`

A `CompositionLocal` that provides access to the `MapViewControllerInterface`.

### Signature

```kotlin
val LocalMapViewController: CompositionLocal<MapViewControllerInterface>
```

### Description

This `CompositionLocal` provides access to the `MapViewControllerInterface` for the current `MapView`. The controller is used to programmatically manipulate the map's view, such as animating the camera to a new location, changing the zoom level, or updating the map's tilt and bearing.

**Important:** This local must be accessed from within the composition scope of a `<MapView />` component. Attempting to access `LocalMapViewController.current` elsewhere will result in a runtime `error`.

### Returns

| Type | Description |
| --- | --- |
| `MapViewControllerInterface` | The controller instance for manipulating the current map view. |

### Example

This example shows a button that, when clicked, uses the `MapViewControllerInterface` to animate the map camera to a specific geographic coordinate.

```kotlin
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.LocalMapViewController
import com.mapconductor.core.MapView
// Other necessary imports for LatLng, CameraUpdateFactory, etc.

@Composable
fun GoToLocationButton(latitude: Double, longitude: Double, zoom: Float) {
    // Access the map controller from the composition local.
    val mapController = LocalMapViewController.current

    Button(onClick = {
        val targetCoordinates = LatLng(latitude, longitude)
        // Use the controller to animate the map camera.
        mapController.animateCamera(
            CameraUpdateFactory.newLatLngZoom(targetCoordinates, zoom)
        )
    }) {
        Text("Go to Location")
    }
}

// Usage within a MapView
@Composable
fun MyInteractiveMap() {
    MapView {
        // This Box and Button are descendants of MapView, allowing them
        // to safely access the LocalMapViewController.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            GoToLocationButton(latitude = 34.0522, longitude = -118.2437, zoom = 12f)
        }
    }
}
```