Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# ArcGISMarkerRenderer

## Signature

```kotlin
class ArcGISMarkerRenderer(
    val markerLayer: GraphicsOverlay,
    holder: ArcGISMapViewHolder,
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractMarkerOverlayRenderer<ArcGISMapViewHolder, ArcGISActualMarker>(
        holder = holder,
        coroutine = coroutine,
    )
```

## Description

The `ArcGISMarkerRenderer` is responsible for rendering and managing marker objects on an ArcGIS map. It handles the complete lifecycle of markers—including creation, removal, and updates—by manipulating `Graphic` objects within a specified `GraphicsOverlay`. This class acts as a concrete implementation for the MapConductor framework's marker rendering system on the ArcGIS platform.

Operations are performed asynchronously using coroutines to ensure the UI remains responsive.

## Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `markerLayer` | `GraphicsOverlay` | The ArcGIS graphics layer where all markers will be drawn and managed. |
| `holder` | `ArcGISMapViewHolder` | A view holder that provides access to the map, map view, and other context-related ArcGIS components. |
| `coroutine` | `CoroutineScope` | The coroutine scope used to launch asynchronous operations. Defaults to `CoroutineScope(Dispatchers.Main)`. |

---

## Methods

### setMarkerPosition

#### Signature

```kotlin
override fun setMarkerPosition(
    markerEntity: MarkerEntityInterface<Graphic>,
    position: GeoPoint,
)
```

#### Description

Asynchronously updates the geographical position of a single marker graphic on the map.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `markerEntity` | `MarkerEntityInterface<Graphic>` | The marker entity containing the `Graphic` whose position needs to be updated. |
| `position` | `GeoPoint` | The new geographical coordinates for the marker. |

---

### onAdd

#### Signature

```kotlin
override suspend fun onAdd(
    data: List<MarkerOverlayRendererInterface.AddParamsInterface>
): List<Graphic?>
```

#### Description

Asynchronously creates and adds a batch of new markers to the `markerLayer`. For each item in the `data` list, it constructs a `PictureMarkerSymbol` from the provided bitmap icon and creates a corresponding `Graphic` object at the specified location. The new graphics are then added to the map.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<AddParamsInterface>` | A list of parameter objects, where each object contains the necessary information (e.g., icon, position, state) to create a new marker. |

#### Returns

| Type | Description |
| :--- | :--- |
| `List<Graphic?>` | A list containing the newly created ArcGIS `Graphic` objects. |

---

### onRemove

#### Signature

```kotlin
override suspend fun onRemove(data: List<MarkerEntityInterface<ArcGISActualMarker>>)
```

#### Description

Asynchronously removes a batch of specified marker graphics from the `markerLayer`.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<MarkerEntityInterface<ArcGISActualMarker>>` | A list of marker entities to be removed from the map. |

---

### onPostProcess

#### Signature

```kotlin
override suspend fun onPostProcess()
```

#### Description

A lifecycle method intended for post-processing tasks after a batch of updates. In this implementation, this method performs no operations.

---

### onChange

#### Signature

```kotlin
override suspend fun onChange(
    data: List<MarkerOverlayRendererInterface.ChangeParamsInterface<ArcGISActualMarker>>
): List<ArcGISActualMarker?>
```

#### Description

Asynchronously processes a batch of changes for existing markers. This method efficiently updates marker properties such as icon, position, and visibility by modifying the existing `Graphic` object whenever possible, avoiding unnecessary object recreation.

If a `Graphic` does not yet exist for a marker entity, it will be created. The method compares the fingerprints of the previous and current states to determine if the icon needs to be updated.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<ChangeParamsInterface<ArcGISActualMarker>>` | A list of parameter objects, each containing the previous and current state of a marker to be updated. |

#### Returns

| Type | Description |
| :--- | :--- |
| `List<ArcGISActualMarker?>` | A list containing the updated marker instances. |

---

## Example

The `ArcGISMarkerRenderer` is typically instantiated and used within the MapConductor framework. The following example shows how you would create an instance of this class.

```kotlin
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.mapconductor.arcgis.marker.ArcGISMarkerRenderer
import com.mapconductor.arcgis.map.ArcGISMapViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

// Assuming 'mapViewHolder' is an initialized instance of ArcGISMapViewHolder

// 1. Create a GraphicsOverlay to hold the markers.
val markerGraphicsOverlay = GraphicsOverlay()

// 2. Add the overlay to the map view.
mapViewHolder.mapView.graphicsOverlays.add(markerGraphicsOverlay)

// 3. Instantiate the ArcGISMarkerRenderer.
val markerRenderer = ArcGISMarkerRenderer(
    markerLayer = markerGraphicsOverlay,
    holder = mapViewHolder,
    coroutine = CoroutineScope(Dispatchers.Main) // Optional: Defaults to Main scope
)

// The MapConductor framework will now use 'markerRenderer' to manage markers
// by calling its onAdd, onRemove, and onChange methods internally.
```