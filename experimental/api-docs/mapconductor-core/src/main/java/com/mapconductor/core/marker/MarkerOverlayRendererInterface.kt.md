Excellent. Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# Interface `MarkerOverlayRendererInterface<ActualMarker>`

## Description

The `MarkerOverlayRendererInterface` defines a contract for rendering and managing marker overlays on a map. It provides a standardized API for handling the lifecycle of markers, including their addition, modification, removal, and animation.

This interface is designed to be implemented by platform-specific renderers (e.g., for Google Maps, Mapbox, or other map SDKs), abstracting the underlying map implementation details from the core logic.

The generic type `ActualMarker` represents the native marker object provided by the specific map SDK being used (e.g., `com.google.android.gms.maps.model.Marker`).

## Properties

### `animateStartListener`

**Signature:** `var animateStartListener: OnMarkerEventHandler?`

**Description:**
An optional event handler that is invoked when a marker animation begins. This listener can be used to trigger actions at the start of an animation.

### `animateEndListener`

**Signature:** `var animateEndListener: OnMarkerEventHandler?`

**Description:**
An optional event handler that is invoked when a marker animation completes. This is useful for synchronizing state or triggering follow-up actions after an animation finishes.

---

## Nested Interfaces

### `AddParamsInterface`

**Description:**
A data structure that encapsulates all the necessary information to add a new marker to the map.

| Property     | Type         | Description                                          |
| :----------- | :----------- | :--------------------------------------------------- |
| `state`      | `MarkerState`| The state of the new marker (e.g., position, rotation). |
| `bitmapIcon` | `BitmapIcon` | The icon to be used for the marker's visual representation. |

### `ChangeParamsInterface<ActualMarker>`

**Description:**
A data structure that holds the information required to update an existing marker's properties.

| Property     | Type                                  | Description                                                |
| :----------- | :------------------------------------ | :--------------------------------------------------------- |
| `current`    | `MarkerEntityInterface<ActualMarker>` | The entity representing the **new** state of the marker.   |
| `bitmapIcon` | `BitmapIcon`                          | The new icon to be applied to the marker.                  |
| `prev`       | `MarkerEntityInterface<ActualMarker>` | The entity representing the **previous** state of the marker. |

---

## Functions

### `onAdd`

**Signature:** `suspend fun onAdd(data: List<AddParamsInterface>): List<ActualMarker?>`

**Description:**
Adds a batch of new markers to the map. The implementation should create native marker objects based on the provided data and add them to the map view. This function is a `suspend` function and should be called from a coroutine.

**Parameters:**
| Parameter | Type                          | Description                                                              |
| :-------- | :---------------------------- | :----------------------------------------------------------------------- |
| `data`    | `List<AddParamsInterface>` | A list of `AddParamsInterface` objects, each describing a new marker to add. |

**Returns:**
`List<ActualMarker?>` - A list containing the newly created native `ActualMarker` objects. The order of this list must correspond to the input `data` list. If a marker fails to be created, its corresponding element in the returned list should be `null`.

### `onChange`

**Signature:** `suspend fun onChange(data: List<ChangeParamsInterface<ActualMarker>>): List<ActualMarker?>`

**Description:**
Updates a batch of existing markers on the map. This can include changes to position, icon, rotation, or other visual properties. This function is a `suspend` function and should be called from a coroutine.

**Parameters:**
| Parameter | Type                                       | Description                                                                 |
| :-------- | :----------------------------------------- | :-------------------------------------------------------------------------- |
| `data`    | `List<ChangeParamsInterface<ActualMarker>>` | A list of `ChangeParamsInterface` objects, each describing an update for an existing marker. |

**Returns:**
`List<ActualMarker?>` - A list containing the updated native `ActualMarker` objects. The order of this list must correspond to the input `data` list. If a marker fails to be updated, its corresponding element should be `null`.

### `onRemove`

**Signature:** `suspend fun onRemove(data: List<MarkerEntityInterface<ActualMarker>>)`

**Description:**
Removes a batch of markers from the map. The implementation should find the corresponding native markers and remove them from the map view. This function is a `suspend` function and should be called from a coroutine.

**Parameters:**
| Parameter | Type                                  | Description                                                              |
| :-------- | :------------------------------------ | :----------------------------------------------------------------------- |
| `data`    | `List<MarkerEntityInterface<ActualMarker>>` | A list of `MarkerEntityInterface` objects representing the markers to be removed. |

### `onAnimate`

**Signature:** `suspend fun onAnimate(entity: MarkerEntityInterface<ActualMarker>)`

**Description:**
Performs an animation on a single marker, typically for smooth position transitions. The implementation is responsible for the visual interpolation between the marker's previous and current state. This function is a `suspend` function and should be called from a coroutine.

**Parameters:**
| Parameter | Type                                  | Description                                          |
| :-------- | :------------------------------------ | :--------------------------------------------------- |
| `entity`  | `MarkerEntityInterface<ActualMarker>` | The marker entity that needs to be animated. |

### `onPostProcess`

**Signature:** `suspend fun onPostProcess()`

**Description:**
A lifecycle hook that is called after a batch of `onAdd`, `onChange`, or `onRemove` operations has been fully processed. This function can be used for finalization tasks such as forcing a map redraw, cleaning up resources, or logging. This function is a `suspend` function and should be called from a coroutine.

---

## Example

Here is a conceptual example of how to implement `MarkerOverlayRendererInterface` for Google Maps.

```kotlin
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// Assuming MarkerEntityInterface, OnMarkerEventHandler, etc. are defined elsewhere.
// typealias ActualMarker = com.google.android.gms.maps.model.Marker

class GoogleMapMarkerRenderer(private val googleMap: GoogleMap) : MarkerOverlayRendererInterface<Marker> {

    override var animateStartListener: OnMarkerEventHandler? = null
    override var animateEndListener: OnMarkerEventHandler? = null

    override suspend fun onAdd(data: List<AddParamsInterface>): List<Marker?> {
        return data.map { params ->
            val markerOptions = MarkerOptions()
                .position(LatLng(params.state.latitude, params.state.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(params.bitmapIcon.bitmap))
                .anchor(params.bitmapIcon.anchorU, params.bitmapIcon.anchorV)
                .rotation(params.state.rotation)
            
            googleMap.addMarker(markerOptions)
        }
    }

    override suspend fun onChange(data: List<ChangeParamsInterface<Marker>>): List<Marker?> {
        return data.map { params ->
            val nativeMarker = params.current.nativeMarker
            nativeMarker?.apply {
                position = LatLng(params.current.state.latitude, params.current.state.longitude)
                setIcon(BitmapDescriptorFactory.fromBitmap(params.bitmapIcon.bitmap))
                setAnchor(params.bitmapIcon.anchorU, params.bitmapIcon.anchorV)
                rotation = params.current.state.rotation
            }
            nativeMarker
        }
    }

    override suspend fun onRemove(data: List<MarkerEntityInterface<Marker>>) {
        data.forEach { entity ->
            entity.nativeMarker?.remove()
        }
    }

    override suspend fun onAnimate(entity: MarkerEntityInterface<Marker>) {
        val nativeMarker = entity.nativeMarker ?: return
        val newPosition = LatLng(entity.state.latitude, entity.state.longitude)

        // Trigger start listener
        animateStartListener?.invoke(entity)

        // (Implementation for smooth animation logic would go here)
        // For simplicity, we just update the position.
        nativeMarker.position = newPosition

        // Trigger end listener
        animateEndListener?.invoke(entity)
    }

    override suspend fun onPostProcess() {
        // No-op for this simple example. Could be used to trigger a camera update
        // or other batch-completion tasks.
    }
}
```