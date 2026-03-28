Of course! Here is the high-quality SDK documentation for the provided code snippet, incorporating the feedback to add a comprehensive example and maintain a professional tone.

***

### `MapViewScope`

A scope that provides a declarative, composable context for defining map overlays.

`MapViewScope` acts as a receiver for a content lambda within a map composable. It is responsible for collecting the state of all declared child overlays (such as markers, polylines, and polygons) and making them available for rendering.

#### Methods

##### `buildRegistry()`

Creates and configures a `MapOverlayRegistry` containing all the overlay states collected within this scope. This registry is the central collection of all declared overlays and is essential for rendering them on the map.

**Signature**
```kotlin
fun buildRegistry(): MapOverlayRegistry
```

**Returns**
| Type | Description |
| :--- | :--- |
| `MapOverlayRegistry` | An object containing flows for each type of map overlay, ready to be consumed by `CollectAndRenderOverlays`. |

***

### `CollectAndRenderOverlays`

A composable function that bridges the declarative overlay definitions with the imperative map controller.

This function observes the state of all overlays registered in the `MapOverlayRegistry`. When an overlay's state changes, it uses the provided `MapViewControllerInterface` to execute the necessary rendering commands (add, update, or remove) on the underlying native map view.

It should be placed within the same composable that hosts the native map view and manages the `MapViewControllerInterface`.

**Signature**
```kotlin
@Composable
fun CollectAndRenderOverlays(
    registry: MapOverlayRegistry,
    controller: MapViewControllerInterface,
)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `registry` | `MapOverlayRegistry` | The registry of all declared overlays, typically obtained by calling `MapViewScope.buildRegistry()`. |
| `controller` | `MapViewControllerInterface` | The controller that interacts with the underlying native map view to perform rendering operations. |

***

### Example

The following example demonstrates how to use `MapViewScope` and `CollectAndRenderOverlays` together to create a declarative map component.

First, we define a wrapper composable, `MapView`, which sets up the scope and handles rendering. Users of this component can then declaratively add overlays like `Marker` and `Polyline` inside its content lambda.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.CollectAndRenderOverlays
import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.marker.Marker // Assumed composable
import com.mapconductor.core.polyline.Polyline // Assumed composable
import com.google.android.gms.maps.model.LatLng // Example dependency

/**
 * A custom MapView composable that provides a declarative API for adding overlays.
 *
 * @param content A lambda with `MapViewScope` as its receiver, allowing for the
 * declarative definition of map overlays like Markers, Polylines, etc.
 */
@Composable
fun MapView(
    // Other map properties like camera position can be added here
    content: @Composable MapViewScope.() -> Unit
) {
    // 1. Create a scope for collecting overlay states.
    val scope = remember { MapViewScope() }

    // 2. Initialize the controller for the imperative map view.
    //    This controller would be implemented to interact with a specific map SDK
    //    like Google Maps or Mapbox.
    val mapController = remember { /* ... initialize your MapViewController ... */ }

    // 3. Build the registry from the overlays declared in the `content` lambda.
    val registry = scope.buildRegistry()

    // 4. Instantiate the underlying native map view.
    AndroidView(
        factory = { context ->
            // Create and configure the native MapView from the map SDK
            val nativeMapView = com.google.android.gms.maps.MapView(context)
            
            // Attach the controller to the native view
            // mapController.attach(nativeMapView)
            
            nativeMapView
        },
        update = { /* Handle view updates if necessary */ }
    )

    // 5. Execute the content lambda to collect the declared overlays into the scope.
    scope.content()

    // 6. Render the collected overlays by bridging their state to the map controller.
    CollectAndRenderOverlays(
        registry = registry,
        controller = mapController
    )
}

// --- Usage Example ---

/**
 * An example screen demonstrating the declarative usage of the MapView.
 */
@Composable
fun MyMapScreen() {
    val sanFrancisco = LatLng(37.7749, -122.4194)
    val losAngeles = LatLng(34.0522, -118.2437)

    MapView { // The receiver is MapViewScope
        // Declaratively add a marker to the map.
        // The `Marker` composable would internally use the scope's `markerCollector`.
        Marker(
            id = "marker-sf",
            position = sanFrancisco,
            title = "San Francisco"
        )

        // Declaratively add a polyline between two points.
        // The `Polyline` composable would use the scope's `polylineCollector`.
        Polyline(
            id = "polyline-ca",
            points = listOf(sanFrancisco, losAngeles),
            color = 0xFF0000FF.toInt(), // Blue
            width = 10f
        )
    }
}
```