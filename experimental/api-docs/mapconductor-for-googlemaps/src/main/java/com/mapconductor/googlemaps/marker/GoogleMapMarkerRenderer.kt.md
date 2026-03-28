Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

# class GoogleMapMarkerRenderer

A renderer class responsible for managing and displaying `Marker` objects on a Google Map.

This class is a concrete implementation of `AbstractMarkerOverlayRenderer` tailored for the Google Maps SDK. It handles the complete lifecycle of markers on the map, including their creation (`onAdd`), modification (`onChange`), and deletion (`onRemove`). It uses Kotlin Coroutines to ensure that all interactions with the Google Map instance are performed on the correct thread.

**Note:** The type `GoogleMapActualMarker` is a type alias for `com.google.android.gms.maps.model.Marker`.

## Constructor

### Signature

```kotlin
GoogleMapMarkerRenderer(
    holder: GoogleMapViewHolder,
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
)
```

### Description

Creates a new instance of the `GoogleMapMarkerRenderer`.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `holder` | `GoogleMapViewHolder` | The view holder that contains the `GoogleMap` instance where markers will be rendered. |
| `coroutine` | `CoroutineScope` | (Optional) The coroutine scope used to execute map operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

---

## Methods

### setMarkerPosition

#### Signature

```kotlin
override fun setMarkerPosition(
    markerEntity: MarkerEntityInterface<GoogleMapActualMarker>,
    position: GeoPoint,
)
```

#### Description

Asynchronously updates the geographical position of a single, existing marker on the map.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `markerEntity` | `MarkerEntityInterface<GoogleMapActualMarker>` | The marker entity whose position is being updated. |
| `position` | `GeoPoint` | The new geographical coordinates for the marker. |

---

### onAdd

#### Signature

```kotlin
override suspend fun onAdd(
    data: List<MarkerOverlayRendererInterface.AddParamsInterface>
): List<GoogleMapActualMarker?>
```

#### Description

Asynchronously adds a batch of new markers to the map. For each item in the `data` list, it constructs and displays a new Google Maps `Marker` with the specified properties (position, icon, anchor, etc.).

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<MarkerOverlayRendererInterface.AddParamsInterface>` | A list of parameter objects, each defining the properties for a new marker to be added. |

#### Returns

| Type | Description |
| :--- | :--- |
| `List<GoogleMapActualMarker?>` | A list containing the newly created `Marker` objects. An element in the list will be `null` if the corresponding marker failed to be created. |

---

### onRemove

#### Signature

```kotlin
override suspend fun onRemove(
    data: List<MarkerEntityInterface<GoogleMapActualMarker>>
)
```

#### Description

Asynchronously removes a batch of markers from the map.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<MarkerEntityInterface<GoogleMapActualMarker>>` | A list of marker entities that should be removed from the map. |

---

### onPostProcess

#### Signature

```kotlin
override suspend fun onPostProcess()
```

#### Description

A lifecycle callback executed after all other rendering operations (add, remove, change) for a frame are complete. In this specific implementation, this method performs no action.

---

### onChange

#### Signature

```kotlin
override suspend fun onChange(
    data: List<MarkerOverlayRendererInterface.ChangeParamsInterface<GoogleMapActualMarker>>
): List<Marker?>
```

#### Description

Asynchronously processes a batch of changes for existing markers. It efficiently updates marker properties such as icon, position, visibility, and z-index based on the provided previous and current states. If a marker to be changed does not yet exist on the map (e.g., `params.prev.marker` is null), it will be created and added.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<MarkerOverlayRendererInterface.ChangeParamsInterface<GoogleMapActualMarker>>` | A list of change parameter objects. Each object contains the previous and current state of a marker, enabling efficient updates. |

#### Returns

| Type | Description |
| :--- | :--- |
| `List<Marker?>` | A list of the updated or newly created `Marker` instances. An element can be `null` if the marker update or creation failed. |