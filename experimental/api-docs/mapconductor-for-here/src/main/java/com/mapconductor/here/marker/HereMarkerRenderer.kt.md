# SDK Documentation: HereMarkerRenderer

The `HereMarkerRenderer` class is a concrete implementation of `AbstractMarkerOverlayRenderer` designed to render and manage map markers for the HERE SDK. It acts as a bridge between the generic `MapConductor` marker management system and the specific `MapMarker` objects of the HERE SDK, handling their creation, updates, and removal on the map.

This renderer is optimized for performance by updating existing marker properties (`position`, `icon`, `drawOrder`) directly, rather than recreating them when their state changes.

## Class Signature

```kotlin
class HereMarkerRenderer(
    holder: HereViewHolder,
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractMarkerOverlayRenderer<HereViewHolder, HereActualMarker>
```

## Constructor

### `HereMarkerRenderer(holder, coroutine)`

Creates a new instance of the marker renderer.

#### Parameters

| Parameter   | Type              | Description                                                                                             |
|-------------|-------------------|---------------------------------------------------------------------------------------------------------|
| `holder`    | `HereViewHolder`  | The view holder that contains the HERE `MapView` instance where markers will be rendered.               |
| `coroutine` | `CoroutineScope`  | *(Optional)* The coroutine scope used for launching asynchronous map operations. Defaults to `Main`. |

---

## Methods

### `setMarkerPosition`

Asynchronously updates the geographic position of a single map marker.

#### Signature

```kotlin
override fun setMarkerPosition(
    markerEntity: MarkerEntityInterface<HereActualMarker>,
    position: GeoPoint,
)
```

#### Parameters

| Parameter        | Type                                         | Description                                          |
|------------------|----------------------------------------------|------------------------------------------------------|
| `markerEntity`   | `MarkerEntityInterface<HereActualMarker>`    | The marker entity whose position needs to be updated. |
| `position`       | `GeoPoint`                                   | The new geographical point for the marker.           |

---

### `onAdd`

Creates and adds new map markers to the map scene based on a list of parameters. Each marker is configured with its position, icon, anchor point, and draw order.

#### Signature

```kotlin
override suspend fun onAdd(
    data: List<MarkerOverlayRendererInterface.AddParamsInterface>
): List<HereActualMarker?>
```

#### Metadata Handling

When a marker is created, its `metadata` property is populated as follows:
-   A unique ID from `MarkerState.id` is always stored with the key `"mc:id"`.
-   If `MarkerState.extra` is provided, its contents are added to the metadata:
    -   If `extra` is a `Map<*, *>`, its key-value pairs are added. Values are converted to the best-matching HERE `Metadata` type (String, Integer, Double).
    -   For any other `Serializable` type, its `toString()` representation is stored under the key `"mc:extra"`.

#### Parameters

| Parameter | Type                                                    | Description                                                              |
|-----------|---------------------------------------------------------|--------------------------------------------------------------------------|
| `data`    | `List<MarkerOverlayRendererInterface.AddParamsInterface>` | A list of parameter objects, each containing the state and icon for a new marker. |

#### Returns

| Type                       | Description                                                                                                                            |
|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `List<HereActualMarker?>`  | A list containing the newly created `MapMarker` instances corresponding to the input data. The list maintains the same order as the input. |

---

### `onRemove`

Asynchronously removes a list of specified map markers from the map scene.

#### Signature

```kotlin
override suspend fun onRemove(data: List<MarkerEntityInterface<HereActualMarker>>)
```

#### Parameters

| Parameter | Type                                       | Description                                                              |
|-----------|--------------------------------------------|--------------------------------------------------------------------------|
| `data`    | `List<MarkerEntityInterface<HereActualMarker>>` | A list of marker entities to be removed from the map.                    |

---

### `onPostProcess`

A lifecycle method called after all other processing. This method is not implemented in `HereMarkerRenderer` and performs no action.

#### Signature

```kotlin
override suspend fun onPostProcess()
```

---

### `onChange`

Efficiently updates the properties of existing markers based on state changes. This method checks for differences in the marker's icon, position, and draw order and applies updates directly to the existing `MapMarker` instance to avoid the performance cost of removing and re-adding it.

#### Signature

```kotlin
override suspend fun onChange(
    data: List<MarkerOverlayRendererInterface.ChangeParamsInterface<HereActualMarker>>
): List<HereActualMarker?>
```

#### Parameters

| Parameter | Type                                                                  | Description                                                                                             |
|-----------|-----------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| `data`    | `List<MarkerOverlayRendererInterface.ChangeParamsInterface<HereActualMarker>>` | A list of objects, each containing the previous and current state of a marker that has been changed. |

#### Returns

| Type                       | Description                                                                                                                                                                                          |
|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `List<HereActualMarker?>`  | A list containing the updated `MapMarker` instances. If a marker's visibility is set to `false`, `null` is returned in its place. The returned marker instance is the same one that was updated. |

---

## Example

The following example demonstrates how to initialize and use the `HereMarkerRenderer`. This typically happens within a class that manages a map view.

```kotlin
import android.content.Context
import com.here.sdk.mapview.MapView
import com.mapconductor.here.HereViewHolder
import com.mapconductor.here.marker.HereMarkerRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MapManager(context: Context) {

    // 1. Initialize HERE MapView
    private val mapView = MapView(context)
    
    // 2. Create a HereViewHolder to wrap the MapView
    private val hereViewHolder = HereViewHolder(mapView)

    // 3. Create a CoroutineScope for async operations
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // 4. Instantiate the HereMarkerRenderer
    val markerRenderer = HereMarkerRenderer(
        holder = hereViewHolder,
        coroutine = coroutineScope
    )

    fun setupMap() {
        // The markerRenderer is now ready to be used by a higher-level
        // component, such as a MarkerOverlay, to add, remove, and
        // update markers on the map.
        
        // For example, a MarkerOverlay would internally call:
        // markerRenderer.onAdd(...)
        // markerRenderer.onChange(...)
        // markerRenderer.onRemove(...)
    }
}
```