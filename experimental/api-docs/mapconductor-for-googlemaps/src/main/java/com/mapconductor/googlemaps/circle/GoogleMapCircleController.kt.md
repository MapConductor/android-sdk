Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# GoogleMapCircleController

### Signature

```kotlin
class GoogleMapCircleController(
    circleManager: CircleManagerInterface<GoogleMapActualCircle> = CircleManager(),
    renderer: GoogleMapCircleOverlayRenderer,
) : CircleController<GoogleMapActualCircle>(circleManager, renderer)
```

### Description

The `GoogleMapCircleController` is a specialized controller responsible for managing and rendering circle overlays on a Google Map. It acts as a bridge between the abstract circle management logic provided by a `CircleManager` and the platform-specific rendering handled by the `GoogleMapCircleOverlayRenderer`.

This controller extends the generic `CircleController`, providing a concrete implementation for the Google Maps SDK. It orchestrates the data layer (`circleManager`) and the view layer (`renderer`), ensuring that the visual representation of circles on the map stays in sync with the underlying data model. When circles are added, updated, or removed via the controller, it delegates the state management to the `circleManager` and triggers the `renderer` to apply the changes to the map view.

### Parameters

This documentation describes the parameters for the `GoogleMapCircleController` constructor.

| Parameter | Type | Description | Default |
| :--- | :--- | :--- | :--- |
| `circleManager` | `CircleManagerInterface<GoogleMapActualCircle>` | The manager responsible for handling the lifecycle and state of circle data. It tracks all the circles to be displayed. | `CircleManager()` |
| `renderer` | `GoogleMapCircleOverlayRenderer` | The platform-specific renderer that draws the circles onto the Google Map view. | *None* |

### Example

The following example demonstrates how to initialize and use the `GoogleMapCircleController` within a typical Android application that uses Google Maps. This code would typically reside in your `Activity` or `Fragment` where you handle the `onMapReady` callback.

```kotlin
import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.core.model.MapCircle // Assuming a data class for circle properties

// Assume this class implements OnMapReadyCallback
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var circleController: GoogleMapCircleController

    override fun onMapReady(googleMap: GoogleMap) {
        // 1. Initialize the platform-specific renderer with the GoogleMap instance.
        val circleRenderer = GoogleMapCircleOverlayRenderer(googleMap)

        // 2. (Optional) Initialize a custom circle manager. 
        //    The controller uses a default one if not provided.
        val circleManager = CircleManager<GoogleMapActualCircle>()

        // 3. Create the controller instance, passing the manager and renderer.
        circleController = GoogleMapCircleController(
            circleManager = circleManager,
            renderer = circleRenderer
        )

        // 4. Use the controller to add a new circle to the map.
        val sfCircle = MapCircle(
            id = "circle-sf",
            center = LatLng(37.7749, -122.4194), // San Francisco
            radius = 1000.0, // Radius in meters
            strokeWidth = 10f,
            strokeColor = Color.DKGRAY,
            fillColor = Color.argb(70, 100, 150, 250) // Semi-transparent blue
        )

        // The controller's `add` method (inherited from CircleController)
        // updates the manager and triggers the renderer to draw the circle.
        circleController.add(sfCircle)
    }
}
```