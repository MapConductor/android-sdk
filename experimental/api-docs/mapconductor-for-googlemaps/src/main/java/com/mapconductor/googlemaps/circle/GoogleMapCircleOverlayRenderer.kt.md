Excellent! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

***

# GoogleMapCircleOverlayRenderer

## Class Signature

```kotlin
class GoogleMapCircleOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractCircleOverlayRenderer<GoogleMapActualCircle>()
```

## Description

The `GoogleMapCircleOverlayRenderer` is responsible for rendering, updating, and removing circle overlays on a Google Map.

This renderer implements circle drawing by creating `Polygon` objects rather than using the native `Circle` object from the Google Maps SDK. This approach allows for more precise control and consistent visual behavior, especially for geodesic circles and stroke width rendering. The radius is adjusted internally to account for the stroke width, ensuring the outer edge of the circle's stroke aligns with the specified radius.

All map operations are executed on the main thread via the provided `CoroutineScope`.

### Constructor

```kotlin
GoogleMapCircleOverlayRenderer(
    holder: GoogleMapViewHolder, 
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main)
)
```

Initializes a new instance of the `GoogleMapCircleOverlayRenderer`.

#### Parameters

| Parameter   | Type                | Description                                                                                             |
| :---------- | :------------------ | :------------------------------------------------------------------------------------------------------ |
| `holder`    | `GoogleMapViewHolder` | The view holder that contains the `GoogleMap` instance on which the circles will be rendered.           |
| `coroutine` | `CoroutineScope`    | The coroutine scope used to execute map operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

---

## Methods

### createCircle

#### Signature

```kotlin
override suspend fun createCircle(state: CircleState): GoogleMapActualCircle?
```

#### Description

Creates and draws a new circle on the map based on the provided state. This method generates a polygon representation of the circle, configures its visual properties (colors, stroke, etc.), and adds it to the map.

#### Parameters

| Parameter | Type          | Description                                                                                             |
| :-------- | :------------ | :------------------------------------------------------------------------------------------------------ |
| `state`   | `CircleState` | An object containing all configuration details for the circle, such as center, radius, colors, and style. |

#### Returns

| Type                    | Description                                                                                             |
| :---------------------- | :------------------------------------------------------------------------------------------------------ |
| `GoogleMapActualCircle?` | The created `Polygon` object (aliased as `GoogleMapActualCircle`) that represents the circle, or `null` if creation fails. |

---

### removeCircle

#### Signature

```kotlin
override suspend fun removeCircle(entity: CircleEntityInterface<GoogleMapActualCircle>)
```

#### Description

Removes a specified circle polygon from the map.

#### Parameters

| Parameter | Type                                          | Description                                                              |
| :-------- | :-------------------------------------------- | :----------------------------------------------------------------------- |
| `entity`  | `CircleEntityInterface<GoogleMapActualCircle>` | The entity wrapper that contains the circle polygon instance to be removed. |

---

### updateCircleProperties

#### Signature

```kotlin
override suspend fun updateCircleProperties(
    circle: GoogleMapActualCircle,
    current: CircleEntityInterface<GoogleMapActualCircle>,
    prev: CircleEntityInterface<GoogleMapActualCircle>,
): GoogleMapActualCircle?
```

#### Description

Updates the properties of an existing circle polygon on the map. This method performs an efficient update by comparing the `current` and `prev` states and only applying the properties that have changed.

If the center, radius, geodesic, or stroke width properties have changed, the polygon's points are regenerated to reflect the new geometry. Otherwise, only the visual properties (e.g., colors, z-index) are updated.

#### Parameters

| Parameter | Type                                          | Description                                                                                             |
| :-------- | :-------------------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `circle`  | `GoogleMapActualCircle`                       | The actual `Polygon` object on the map that needs to be updated.                                        |
| `current` | `CircleEntityInterface<GoogleMapActualCircle>` | The entity wrapper containing the new, updated state for the circle.                                    |
| `prev`    | `CircleEntityInterface<GoogleMapActualCircle>` | The entity wrapper containing the previous state of the circle, used for diffing to determine what changed. |

#### Returns

| Type                    | Description                               |
| :---------------------- | :---------------------------------------- |
| `GoogleMapActualCircle?` | The updated `Polygon` object.             |

---

## Example

Here is a conceptual example of how to use `GoogleMapCircleOverlayRenderer` to draw a circle on a map.

```kotlin
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.types.latitude
import com.mapconductor.core.types.longitude
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.circle.GoogleMapCircleOverlayRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Assume you have a GoogleMap instance and a CoroutineScope
val googleMap: GoogleMap = // ... get your GoogleMap instance
val coroutineScope = CoroutineScope(Dispatchers.Main)

// 1. Create a GoogleMapViewHolder
val mapViewHolder = GoogleMapViewHolder(googleMap)

// 2. Instantiate the renderer
val circleRenderer = GoogleMapCircleOverlayRenderer(mapViewHolder, coroutineScope)

// 3. Define the state for the circle
val circleState = CircleState(
    id = "my-unique-circle-id",
    center = GeoPoint(latitude = 37.7749.latitude, longitude = (-122.4194).longitude),
    radiusMeters = 1000.0,
    strokeColor = Color.Blue,
    strokeWidth = 10f, // in dp
    fillColor = Color(0x880000FF), // Blue with 50% transparency
    geodesic = true
)

// 4. Launch a coroutine to create the circle on the map
coroutineScope.launch {
    val createdCircle = circleRenderer.createCircle(circleState)
    if (createdCircle != null) {
        println("Circle successfully created on the map with tag: ${createdCircle.tag}")
    } else {
        println("Failed to create circle.")
    }
}
```