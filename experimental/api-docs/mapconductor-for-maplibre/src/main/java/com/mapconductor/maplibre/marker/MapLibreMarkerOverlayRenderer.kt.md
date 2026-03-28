Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# MapLibreMarkerOverlayRenderer

The `MapLibreMarkerOverlayRenderer` is a concrete implementation of `AbstractMarkerOverlayRenderer` designed specifically for the MapLibre map engine. It manages the entire lifecycle of markers on the map, including adding, removing, and updating them.

This class handles the conversion of abstract marker data into MapLibre-specific `Feature` objects, manages icon resources within the map's style, and orchestrates rendering updates on dedicated marker layers. It uses an efficient batch-processing approach, where changes are collected and then applied by redrawing the entire marker layer from a GeoJSON source.

## Signature

```kotlin
class MapLibreMarkerOverlayRenderer(
    holder: MapLibreMapViewHolderInterface,
    val markerManager: MarkerManager<MapLibreActualMarker>,
    val markerLayer: MarkerLayer,
    val dragLayer: MarkerDragLayer,
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractMarkerOverlayRenderer<MapLibreMapViewHolderInterface, MapLibreActualMarker>(
        holder = holder,
        coroutine = coroutine,
    )
```

## Constructor

Initializes a new instance of the `MapLibreMarkerOverlayRenderer`.

### Parameters

| Parameter       | Type                               | Description                                                                                             |
| :-------------- | :--------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `holder`        | `MapLibreMapViewHolderInterface`   | The view holder that provides access to the `MapLibreMap` instance and its controller.                  |
| `markerManager` | `MarkerManager<MapLibreActualMarker>` | The manager responsible for maintaining the state of all marker entities.                                |
| `markerLayer`   | `MarkerLayer`                      | The dedicated layer for rendering standard markers.                                                     |
| `dragLayer`     | `MarkerDragLayer`                  | The dedicated layer for rendering a marker while it is being dragged.                                   |
| `coroutine`     | `CoroutineScope`                   | (Optional) The coroutine scope for launching asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

## Nested Objects

### `Prop`

A collection of constant property keys used within the GeoJSON features that represent markers.

| Constant            | Value          | Description                               |
| :------------------ | :------------- | :---------------------------------------- |
| `ICON_ID`           | `"icon_id"`    | The key for the marker's icon ID.         |
| `DEFAULT_MARKER_ID` | `"default"`    | The ID for the default marker icon.       |
| `SCALE`             | `"scale"`      | The key for the marker's scale factor.    |
| `ICON_ANCHOR`       | `"icon-offset"`| The key for the marker's icon offset.     |
| `Z_INDEX`           | `"zIndex"`     | The key for the marker's z-index value.   |

### `IconAnchor`

Defines constants for specifying the anchor point of a marker's icon.

| Constant         | Value              |
| :--------------- | :----------------- |
| `CENTER`         | `"center"`         |
| `LEFT`           | `"left"`           |
| `RIGHT`          | `"right"`          |
| `BOTTOM`         | `"bottom"`         |
| `TOP_LEFT`       | `"top-left"`       |
| `TOP_RIGHT`      | `"top-right"`      |
| `BOTTOM_LEFT`    | `"bottom-left"`    |
| `BOTTOM_RIGHT`   | `"bottom-right"`   |

### `IconTranslateAnchor`

Defines constants for specifying the icon's translation anchor behavior.

| Constant   | Value        | Description                               |
| :--------- | :----------- | :---------------------------------------- |
| `MAP`      | `"map"`      | Anchored relative to the map.             |
| `VIEWPORT` | `"viewport"` | Anchored relative to the device's screen. |

## Methods

### ensureDefaultIcon

Checks if the default marker icon exists in the provided MapLibre style and adds it if it's missing. This is particularly useful for scenarios where the map style is reloaded, ensuring that markers without a custom icon can still be rendered.

#### Signature

```kotlin
fun ensureDefaultIcon(style: org.maplibre.android.maps.Style)
```

#### Parameters

| Parameter | Type                               | Description                               |
| :-------- | :--------------------------------- | :---------------------------------------- |
| `style`   | `org.maplibre.android.maps.Style`  | The MapLibre `Style` object to check.     |

### setMarkerPosition

Updates the geographical position of a specific marker on the map. This method regenerates the marker's feature with the new coordinates and updates the GeoJSON source to reflect the change immediately.

#### Signature

```kotlin
override fun setMarkerPosition(
    markerEntity: MarkerEntityInterface<MapLibreActualMarker>,
    position: GeoPoint,
)
```

#### Parameters

| Parameter      | Type                                       | Description                               |
| :------------- | :----------------------------------------- | :---------------------------------------- |
| `markerEntity` | `MarkerEntityInterface<MapLibreActualMarker>` | The marker entity to update.              |
| `position`     | `GeoPoint`                                 | The new geographical position for the marker. |

### onAdd

Handles the addition of new markers. It processes a list of marker parameters, adds the necessary icon bitmaps to the map style, and creates a list of MapLibre `Feature` objects to be rendered. It also manages a reference count for icons to optimize resource usage.

#### Signature

```kotlin
override suspend fun onAdd(
    data: List<MarkerOverlayRendererInterface.AddParamsInterface>,
): List<MapLibreActualMarker?>
```

#### Parameters

| Parameter | Type                                                       | Description                                                                                             |
| :-------- | :--------------------------------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `data`    | `List<MarkerOverlayRendererInterface.AddParamsInterface>`  | A list of `AddParamsInterface` objects, each containing the state and icon data for a new marker.       |

#### Returns

A `List` of newly created `MapLibreActualMarker` (`Feature`) objects, corresponding to the input data.

### onRemove

A lifecycle hook called when markers are scheduled for removal. The actual removal from the map is handled by the `onPostProcess` and `redraw` methods, which rebuild the feature collection without the removed markers.

#### Signature

```kotlin
override suspend fun onRemove(data: List<MarkerEntityInterface<MapLibreActualMarker>>)
```

#### Parameters

| Parameter | Type                                         | Description                          |
| :-------- | :------------------------------------------- | :----------------------------------- |
| `data`    | `List<MarkerEntityInterface<MapLibreActualMarker>>` | A list of marker entities to be removed. |

### drawDragLayer

Triggers a redraw of the dedicated drag layer. This should be called when a marker is being dragged to render it separately from the static markers, providing visual feedback to the user.

#### Signature

```kotlin
fun drawDragLayer()
```

### redraw

Forces a complete redraw of the main marker layer. It retrieves all current marker entities from the `markerManager` and updates the GeoJSON source for the `markerLayer`. This is the primary mechanism for applying batched changes to the map.

#### Signature

```kotlin
fun redraw()
```

### onPostProcess

A lifecycle hook called after a batch of add or remove operations. It triggers a `redraw()` to update the map display with the changes, ensuring the visual state of the markers is synchronized with the data model.

#### Signature

```kotlin
override suspend fun onPostProcess()
```

### onChange

Handles property changes for existing markers (e.g., icon, z-index). It compares the previous and current state of each marker, updates icon reference counts, and generates a new list of `Feature` objects with the updated properties.

#### Signature

```kotlin
override suspend fun onChange(
    data: List<MarkerOverlayRendererInterface.ChangeParamsInterface<MapLibreActualMarker>>,
): List<MapLibreActualMarker?>
```

#### Parameters

| Parameter | Type                                                                 | Description                                                                                             |
| :-------- | :------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `data`    | `List<MarkerOverlayRendererInterface.ChangeParamsInterface<MapLibreActualMarker>>` | A list of `ChangeParamsInterface` objects, each containing the previous and current state of a marker. |

#### Returns

A `List` of updated `MapLibreActualMarker` (`Feature`) objects reflecting the changes.

## Example

While direct instantiation is part of a larger framework, the conceptual usage within that framework would look like this:

```kotlin
// 1. Initialize the renderer (typically done by a map controller)
val markerRenderer = MapLibreMarkerOverlayRenderer(
    holder = myMapViewHolder,
    markerManager = myMarkerManager,
    markerLayer = myMarkerLayer,
    dragLayer = myDragLayer
)

// 2. The MarkerManager would then delegate operations to the renderer.

// To add a marker
// The manager would call onAdd internally with the marker's data.
// The result is a new Feature that is stored.
val newFeatures = markerRenderer.onAdd(listOf(addParamsForNewMarker))
// After adding, the manager calls onPostProcess to update the map.
markerRenderer.onPostProcess()

// To change a marker's icon
// The manager would call onChange with the old and new state.
val updatedFeatures = markerRenderer.onChange(listOf(changeParamsForMarker))
// After changing, the manager calls onPostProcess to update the map.
markerRenderer.onPostProcess()

// To update a marker's position
markerRenderer.setMarkerPosition(markerEntity, newGeoPoint)
```