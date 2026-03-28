Of course! Here is the high-quality SDK documentation for the provided code snippet.

# GroundImage

The `GroundImage` composable is used to overlay an image onto a specific geographical area of the map. It must be used within the content lambda of a `MapView` composable.

There are two overloads for this function. The primary overload accepts individual properties to define the ground image, while the other accepts a pre-configured `GroundImageState` object for more advanced state management.

---

## GroundImage (Declarative)

This is the primary and most convenient way to add a ground image to the map. You provide the image, its geographical bounds, and other optional properties directly as parameters.

### Signature

```kotlin
@Composable
fun MapViewScope.GroundImage(
    bounds: GeoRectBounds,
    image: Drawable,
    opacity: Float = 0.5f,
    tileSize: Int = GroundImageTileProvider.DEFAULT_TILE_SIZE,
    id: String? = null,
    extra: Serializable? = null,
    onClick: OnGroundImageEventHandler? = null,
)
```

### Description

A composable function that overlays a `Drawable` image onto a specified rectangular geographical area of the map. The image is automatically anchored to the provided `bounds`. This function should be called within the `MapView` composable's content scope.

### Parameters

| Parameter  | Type                        | Description                                                                                                                            |
|------------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `bounds`   | `GeoRectBounds`             | The rectangular geographical coordinates where the image will be placed.                                                               |
| `image`    | `Drawable`                  | The Android `Drawable` to be displayed on the map.                                                                                     |
| `opacity`  | `Float`                     | The opacity of the image, ranging from `0.0` (fully transparent) to `1.0` (fully opaque). Defaults to `0.5f`.                           |
| `tileSize` | `Int`                       | The size of the tiles (in pixels) used to render the image on the map. Defaults to `GroundImageTileProvider.DEFAULT_TILE_SIZE`.         |
| `id`       | `String?`                   | An optional unique identifier for the ground image. If not provided, one will be generated internally.                                 |
| `extra`    | `Serializable?`             | Optional, serializable data to associate with the ground image. This data is passed to the `onClick` handler.                          |
| `onClick`  | `OnGroundImageEventHandler?`| An optional callback lambda that is invoked with the image's `id` and `extra` data when the user clicks on the ground image overlay.     |

### Returns

This composable does not return a value.

### Example

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.mapconductor.core.MapView
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.groundimage.GroundImage

@Composable
fun MapWithGroundOverlay() {
    MapView(
        // ... other map properties
    ) {
        val context = LocalContext.current
        val imageDrawable = ContextCompat.getDrawable(context, R.drawable.historical_map_overlay)
        
        val overlayBounds = GeoRectBounds(
            northEast = GeoPoint(40.7128, -74.0060), // New York City NE corner
            southWest = GeoPoint(40.7028, -74.0160)  // New York City SW corner
        )

        if (imageDrawable != null) {
            GroundImage(
                bounds = overlayBounds,
                image = imageDrawable,
                opacity = 0.7f,
                id = "historical-nyc-map",
                onClick = { id, extra ->
                    Log.d("MapView", "GroundImage clicked! ID: $id")
                }
            )
        }
    }
}
```

---

## GroundImage (State-based)

This overload is useful for advanced use cases where you need to manage the `GroundImageState` externally, for instance, by hoisting it or using `remember`.

### Signature

```kotlin
@Composable
fun MapViewScope.GroundImage(state: GroundImageState)
```

### Description

Adds a ground image to the map using a pre-configured `GroundImageState` object. This allows for the state of the ground image (its properties, image, and handlers) to be managed separately from its composition.

### Parameters

| Parameter | Type               | Description                                                                                                                            |
|-----------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `state`   | `GroundImageState` | The state object that defines all properties of the ground image, including its bounds, image, opacity, and event handlers.              |

### Returns

This composable does not return a value.

### Example

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.mapconductor.core.MapView
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.groundimage.GroundImage
import com.mapconductor.core.groundimage.GroundImageState

@Composable
fun MapWithManagedGroundOverlay() {
    MapView(
        // ... other map properties
    ) {
        val context = LocalContext.current
        val imageDrawable = ContextCompat.getDrawable(context, R.drawable.park_layout)
        
        val parkBounds = GeoRectBounds(
            northEast = GeoPoint(34.0522, -118.2437),
            southWest = GeoPoint(34.0422, -118.2537)
        )

        val groundImageState = remember(imageDrawable, parkBounds) {
            imageDrawable?.let {
                GroundImageState(
                    bounds = parkBounds,
                    image = it,
                    opacity = 0.8f,
                    id = "park-layout-overlay"
                )
            }
        }

        groundImageState?.let {
            GroundImage(state = it)
        }
    }
}
```