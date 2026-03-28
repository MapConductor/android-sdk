Excellent. Here is the high-quality SDK documentation for the provided `GoogleMapRasterLayerOverlayRenderer` class.

---

# GoogleMapRasterLayerOverlayRenderer

## Class Description

The `GoogleMapRasterLayerOverlayRenderer` class is a concrete implementation of the `RasterLayerOverlayRendererInterface` for the Google Maps SDK. It is responsible for rendering and managing the lifecycle of raster tile layers on a `GoogleMap` instance.

This renderer handles the addition, modification, and removal of `TileOverlay` objects based on a provided `RasterLayerState`. It supports fetching tiles from various sources, including URL templates and ArcGIS map services. Key features include setting opacity, visibility, z-index, and custom request headers. It also provides a powerful debugging feature that overlays tile coordinates and other metadata directly onto the tile images.

## Constructor

### Signature

```kotlin
class GoogleMapRasterLayerOverlayRenderer(
    private val holder: GoogleMapViewHolder,
    private val okHttpClient: OkHttpClient,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : RasterLayerOverlayRendererInterface<TileOverlay>
```

### Description

Creates a new instance of `GoogleMapRasterLayerOverlayRenderer`.

### Parameters

| Parameter      | Type                 | Description                                                                                             |
| :------------- | :------------------- | :------------------------------------------------------------------------------------------------------ |
| `holder`       | `GoogleMapViewHolder`| The view holder that provides access to the `GoogleMap` and `MapView` instances.                        |
| `okHttpClient` | `OkHttpClient`       | The `OkHttpClient` instance to be used for all network requests to fetch tile images.                   |
| `coroutine`    | `CoroutineScope`     | The coroutine scope used for executing asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

## Methods

### onAdd

#### Signature

```kotlin
override suspend fun onAdd(
    data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>
): List<TileOverlay?>
```

#### Description

Asynchronously adds a list of new raster layers to the map. For each layer state provided in the `data` list, this method creates a corresponding `TileOverlay` and adds it to the Google Map.

The method configures a `TileProvider` for each layer to fetch tile images based on the layer's source (`UrlTemplate` or `ArcGisService`). It handles coordinate scheme transformations (TMS vs. XYZ) and sets custom HTTP headers for the requests. If a layer's `debug` flag is set to `true`, it will render a debug overlay on each tile.

**Note:** `TileJson` sources are not supported and will be ignored.

#### Parameters

| Parameter | Type                                                          | Description                                                                                             |
| :-------- | :------------------------------------------------------------ | :------------------------------------------------------------------------------------------------------ |
| `data`    | `List<RasterLayerOverlayRendererInterface.AddParamsInterface>`| A list of `AddParamsInterface` objects, where each object contains the `RasterLayerState` for a new layer. |

#### Returns

A `List` containing the newly created `TileOverlay` for each corresponding input state, or `null` if a layer could not be added (e.g., due to an unsupported source type).

---

### onChange

#### Signature

```kotlin
override suspend fun onChange(
    data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<TileOverlay>>
): List<TileOverlay?>
```

#### Description

Asynchronously processes updates for a list of existing raster layers. It intelligently compares the previous and next state of each layer.

- If the `source` or `debug` properties have changed, the existing `TileOverlay` is removed and a completely new one is created to reflect the fundamental change.
- If only other properties like `opacity`, `visible`, or `zIndex` have changed, the existing `TileOverlay` is updated in-place for better performance.

#### Parameters

| Parameter | Type                                                                      | Description                                                                                                                            |
| :-------- | :------------------------------------------------------------------------ | :------------------------------------------------------------------------------------------------------------------------------------- |
| `data`    | `List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<TileOverlay>>` | A list of `ChangeParamsInterface` objects, each containing the previous `RasterLayerEntityInterface` and the new `RasterLayerState`. |

#### Returns

A `List` containing the updated or newly created `TileOverlay` objects.

---

### onRemove

#### Signature

```kotlin
override suspend fun onRemove(data: List<RasterLayerEntityInterface<TileOverlay>>)
```

#### Description

Asynchronously removes a list of raster layer overlays from the map. Each `TileOverlay` in the provided list will be detached from the `GoogleMap` instance.

#### Parameters

| Parameter | Type                                           | Description                                                              |
| :-------- | :--------------------------------------------- | :----------------------------------------------------------------------- |
| `data`    | `List<RasterLayerEntityInterface<TileOverlay>>`| A list of `RasterLayerEntityInterface` objects representing the layers to be removed. |

#### Returns

This method does not return a value.

---

### onPostProcess

#### Signature

```kotlin
override suspend fun onPostProcess()
```

#### Description

A lifecycle method called after all add, change, and remove operations in a batch have been completed. In this implementation, this method is a no-op and performs no actions.

#### Parameters

This method takes no parameters.

#### Returns

This method does not return a value.

## Example

The `GoogleMapRasterLayerOverlayRenderer` is typically used within a larger map management system. The following conceptual example demonstrates how you might instantiate the renderer and use it to add a raster layer to a map.

```kotlin
import com.google.android.gms.maps.GoogleMap
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.googlemaps.GoogleMapViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

// Assume you have these variables initialized
val googleMap: GoogleMap
val mapView: MapView
val okHttpClient = OkHttpClient()
val coroutineScope = CoroutineScope(Dispatchers.Main)

// 1. Create a GoogleMapViewHolder
val mapViewHolder = GoogleMapViewHolder(googleMap, mapView)

// 2. Instantiate the renderer
val rasterRenderer = GoogleMapRasterLayerOverlayRenderer(
    holder = mapViewHolder,
    okHttpClient = okHttpClient,
    coroutine = coroutineScope
)

// 3. Define the state for a new raster layer
val openStreetMapLayerState = RasterLayerState(
    id = "osm-layer",
    source = RasterLayerSource.UrlTemplate(
        template = "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
        tileSize = 256,
        scheme = TileScheme.XYZ
    ),
    opacity = 0.8f,
    visible = true,
    zIndex = 1,
    debug = true // Enable debug overlay on tiles
)

// 4. Create the parameters for the onAdd call
val addParams = object : RasterLayerOverlayRendererInterface.AddParamsInterface {
    override val state: RasterLayerState = openStreetMapLayerState
}

// 5. Use the renderer to add the layer to the map
coroutineScope.launch {
    val addedOverlays = rasterRenderer.onAdd(listOf(addParams))
    if (addedOverlays.isNotEmpty() && addedOverlays[0] != null) {
        println("Successfully added OpenStreetMap layer with overlay: ${addedOverlays[0]}")
    } else {
        println("Failed to add OpenStreetMap layer.")
    }
}
```