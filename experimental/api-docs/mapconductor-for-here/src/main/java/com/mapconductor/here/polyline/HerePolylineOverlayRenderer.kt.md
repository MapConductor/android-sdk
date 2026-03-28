Of course! Here is the high-quality SDK documentation for the provided code snippet.

# Class: `HerePolylineOverlayRenderer`

Manages the rendering and lifecycle of polyline overlays on a HERE map. This class acts as a concrete implementation of `AbstractPolylineOverlayRenderer` for the HERE SDK, bridging the gap between a generic `PolylineState` and the platform-specific `MapPolyline`.

It handles the creation, property updates, and removal of polylines on the map. All map-related operations are performed asynchronously using a provided `CoroutineScope`.

```kotlin
class HerePolylineOverlayRenderer(
    override val holder: HereViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractPolylineOverlayRenderer<HereActualPolyline>()
```

## Constructor

### Signature

```kotlin
HerePolylineOverlayRenderer(
    holder: HereViewHolder, 
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default)
)
```

### Description

Creates an instance of the `HerePolylineOverlayRenderer`.

### Parameters

| Parameter   | Type              | Description                                                                                             |
|-------------|-------------------|---------------------------------------------------------------------------------------------------------|
| `holder`    | `HereViewHolder`  | The view holder that contains the HERE `MapView` instance where polylines will be rendered.             |
| `coroutine` | `CoroutineScope`  | The scope used to launch asynchronous map operations. Defaults to a scope on `Dispatchers.Default`. |

---

## Methods

### `createPolyline`

#### Signature

```kotlin
suspend fun createPolyline(state: PolylineState): HereActualPolyline?
```

#### Description

Creates a new `MapPolyline` from a given `PolylineState` and adds it to the map. This method constructs the polyline's geometry, handling both geodesic and linear interpolation, and sets its visual representation (e.g., color, width, z-index). The created polyline is then added to the map asynchronously.

#### Parameters

| Parameter | Type            | Description                                                                                                                            |
|-----------|-----------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `state`   | `PolylineState` | An object containing all the configuration for the polyline, including its geographical points, color, width, z-index, and geodesic flag. |

#### Returns

| Type                 | Description                                                                                                                            |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `HereActualPolyline?` | The newly created `MapPolyline` instance that was added to the map. `HereActualPolyline` is a type alias for `com.here.sdk.mapview.MapPolyline`. |

---

### `updatePolylineProperties`

#### Signature

```kotlin
suspend fun updatePolylineProperties(
    polyline: HereActualPolyline,
    current: PolylineEntityInterface<HereActualPolyline>,
    prev: PolylineEntityInterface<HereActualPolyline>,
): HereActualPolyline?
```

#### Description

Updates the properties of an existing `MapPolyline` on the map. This method performs an efficient update by comparing the properties of the `current` and `prev` states. Only the properties that have changed (e.g., points, color, width, z-index) are applied to the native `MapPolyline` object. If necessary, the polyline is removed and re-added to the map to ensure visual correctness.

#### Parameters

| Parameter  | Type                                        | Description                                                              |
|------------|---------------------------------------------|--------------------------------------------------------------------------|
| `polyline` | `HereActualPolyline`                        | The existing `MapPolyline` object to be updated.                         |
| `current`  | `PolylineEntityInterface<HereActualPolyline>` | The entity wrapper containing the new, updated `PolylineState`.          |
| `prev`     | `PolylineEntityInterface<HereActualPolyline>` | The entity wrapper containing the previous `PolylineState` for comparison. |

#### Returns

| Type                 | Description                                                                                                                            |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `HereActualPolyline?` | The updated `MapPolyline` instance. `HereActualPolyline` is a type alias for `com.here.sdk.mapview.MapPolyline`. |

---

### `removePolyline`

#### Signature

```kotlin
suspend fun removePolyline(entity: PolylineEntityInterface<HereActualPolyline>)
```

#### Description

Asynchronously removes a specified `MapPolyline` from the map.

#### Parameters

| Parameter | Type                                        | Description                                                              |
|-----------|---------------------------------------------|--------------------------------------------------------------------------|
| `entity`  | `PolylineEntityInterface<HereActualPolyline>` | The entity wrapper containing the `MapPolyline` instance to be removed. |

---

## Example

The following example demonstrates the complete lifecycle of a polyline using `HerePolylineOverlayRenderer`: creating it, updating its color, and finally removing it.

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.types.StrokeWidth
import com.mapconductor.here.HereViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

// Assume 'hereViewHolder' is an initialized instance of HereViewHolder
// containing your MapView.
lateinit var hereViewHolder: HereViewHolder

// 1. Initialize the renderer
val renderer = HerePolylineOverlayRenderer(
    holder = hereViewHolder,
    coroutine = CoroutineScope(Dispatchers.Main) // Use Main dispatcher for map updates
)

fun managePolylineLifecycle() = CoroutineScope(Dispatchers.Main).launch {
    // 2. Define the initial state of the polyline
    val initialPoints = listOf(
        GeoPoint(52.530, 13.384),
        GeoPoint(52.531, 13.385),
        GeoPoint(52.532, 13.386)
    )
    val initialState = PolylineState(
        points = initialPoints,
        strokeColor = Color.Blue,
        strokeWidth = StrokeWidth(10f),
        zIndex = 100,
        geodesic = true
    )

    // 3. Create the polyline and add it to the map
    println("Creating polyline...")
    val mapPolyline = renderer.createPolyline(initialState)
    
    // Create an entity to hold the state and the actual polyline object
    var polylineEntity = PolylineEntity(initialState, mapPolyline!!)
    println("Polyline created.")

    // (Wait for some time or user action)
    
    // 4. Define an updated state for the polyline (e.g., change color)
    val updatedState = initialState.copy(strokeColor = Color.Red)
    val updatedEntity = polylineEntity.copy(state = updatedState)

    // 5. Update the polyline's properties on the map
    println("Updating polyline color to Red...")
    renderer.updatePolylineProperties(
        polyline = polylineEntity.polyline,
        current = updatedEntity,
        prev = polylineEntity
    )
    polylineEntity = updatedEntity // Keep the entity reference updated
    println("Polyline updated.")

    // (Wait for some time or user action)

    // 6. Remove the polyline from the map
    println("Removing polyline...")
    renderer.removePolyline(polylineEntity)
    println("Polyline removed.")
}
```