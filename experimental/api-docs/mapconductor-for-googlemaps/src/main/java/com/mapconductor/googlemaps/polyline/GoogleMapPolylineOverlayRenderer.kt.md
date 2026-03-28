Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# GoogleMapPolylineOverlayRenderer

The `GoogleMapPolylineOverlayRenderer` is a concrete implementation of `AbstractPolylineOverlayRenderer` designed specifically for Google Maps. It is responsible for rendering and managing `Polyline` objects on the map.

This class handles the entire lifecycle of a polyline, including its creation, property updates (e.g., color, width, points), and removal. It translates an abstract `PolylineState` into a tangible `Polyline` on the Google Map, ensuring that the visual representation stays in sync with the state.

A key feature of this renderer is its use of adaptive interpolation for geodesic polylines. It dynamically calculates the density of points based on the current map zoom level, ensuring a visually smooth curve without sacrificing performance. It also caches these interpolated points to optimize rendering during panning and zooming.

## Constructor

### Signature

```kotlin
class GoogleMapPolylineOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolylineOverlayRenderer<GoogleMapActualPolyline>()
```

### Description

Initializes a new instance of the `GoogleMapPolylineOverlayRenderer`.

### Parameters

| Parameter   | Type                  | Description                                                                                                |
|-------------|-----------------------|------------------------------------------------------------------------------------------------------------|
| `holder`    | `GoogleMapViewHolder` | The view holder that provides access to the native `GoogleMap` instance.                                   |
| `coroutine` | `CoroutineScope`      | The coroutine scope used to execute map operations, typically on the main thread. Defaults to `Dispatchers.Main`. |

## Public Methods

### createPolyline

#### Signature

```kotlin
override suspend fun createPolyline(state: PolylineState): GoogleMapActualPolyline?
```

#### Description

Asynchronously creates a new `Polyline` on the map based on the provided `PolylineState`. It configures the polyline's points, color, width, z-index, and geodesic property. For geodesic polylines, it uses an adaptive interpolation strategy to ensure a smooth curve at various zoom levels. The new polyline's tag is set to the `state.id` for identification.

#### Parameters

| Parameter | Type           | Description                                                              |
|-----------|----------------|--------------------------------------------------------------------------|
| `state`   | `PolylineState`| The state object containing all the properties for the new polyline.     |

#### Returns

`GoogleMapActualPolyline?` — The newly created Google Maps `Polyline` object (`GoogleMapActualPolyline` is a type alias for `com.google.android.gms.maps.model.Polyline`), or `null` if creation fails.

---

### updatePolylineProperties

#### Signature

```kotlin
override suspend fun updatePolylineProperties(
    polyline: GoogleMapActualPolyline,
    current: PolylineEntityInterface<GoogleMapActualPolyline>,
    prev: PolylineEntityInterface<GoogleMapActualPolyline>,
): Polyline?
```

#### Description

Asynchronously updates the properties of an existing `Polyline` on the map. It efficiently compares the `current` and `prev` states to determine which properties have changed and applies only the necessary updates. This prevents unnecessary redraws and improves performance.

#### Parameters

| Parameter  | Type                                                  | Description                                                                      |
|------------|-------------------------------------------------------|----------------------------------------------------------------------------------|
| `polyline` | `GoogleMapActualPolyline`                             | The native `Polyline` object on the map that needs to be updated.                |
| `current`  | `PolylineEntityInterface<GoogleMapActualPolyline>`    | The entity representing the current, updated state of the polyline.              |
| `prev`     | `PolylineEntityInterface<GoogleMapActualPolyline>`    | The entity representing the previous state of the polyline, used for comparison. |

#### Returns

`Polyline?` — The updated `Polyline` object.

---

### removePolyline

#### Signature

```kotlin
override suspend fun removePolyline(entity: PolylineEntityInterface<GoogleMapActualPolyline>)
```

#### Description

Asynchronously removes a specified polyline from the map.

#### Parameters

| Parameter | Type                                               | Description                                                              |
|-----------|----------------------------------------------------|--------------------------------------------------------------------------|
| `entity`  | `PolylineEntityInterface<GoogleMapActualPolyline>` | The polyline entity to be removed. The underlying `Polyline` is accessed from this entity. |

#### Returns

This function does not return a value.

## Example

Here is an example of how to instantiate the `GoogleMapPolylineOverlayRenderer` and use it to create a polyline on the map.

```kotlin
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.polyline.GoogleMapPolylineOverlayRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Assume you have a GoogleMap instance and a CoroutineScope
val googleMap: GoogleMap = getGoogleMap() // Placeholder for obtaining the map instance
val mainScope = CoroutineScope(Dispatchers.Main)

// 1. Create a GoogleMapViewHolder
val mapViewHolder = GoogleMapViewHolder(googleMap)

// 2. Instantiate the renderer
val polylineRenderer = GoogleMapPolylineOverlayRenderer(
    holder = mapViewHolder,
    coroutine = mainScope
)

// 3. Define the state for a new polyline
val polylineState = PolylineState(
    id = "route-42",
    points = listOf(
        GeoPoint(40.7128, -74.0060), // New York
        GeoPoint(34.0522, -118.2437) // Los Angeles
    ),
    strokeColor = Color.Blue,
    strokeWidth = 10f,
    geodesic = true,
    zIndex = 10
)

// 4. Use the renderer to create the polyline on the map
mainScope.launch {
    val nativePolyline = polylineRenderer.createPolyline(polylineState)
    if (nativePolyline != null) {
        println("Polyline with ID ${nativePolyline.tag} created successfully.")
    } else {
        println("Failed to create polyline.")
    }
}
```