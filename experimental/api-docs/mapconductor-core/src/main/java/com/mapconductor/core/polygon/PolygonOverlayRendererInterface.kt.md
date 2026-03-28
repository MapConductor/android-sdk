Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# PolygonOverlayRendererInterface<ActualPolygon>

## Signature

```kotlin
interface PolygonOverlayRendererInterface<ActualPolygon>
```

## Description

The `PolygonOverlayRendererInterface` defines a contract for rendering and managing polygon overlays on a map. It abstracts the platform-specific details of polygon manipulation, allowing for a consistent approach across different map providers (e.g., Google Maps, Mapbox).

Implement this interface to create a custom renderer that handles the lifecycle of polygons, including their creation, update, and removal from the map view. The renderer operates in batches to ensure efficient updates.

### Type Parameters

| Name            | Description                                                                                             |
| --------------- | ------------------------------------------------------------------------------------------------------- |
| `ActualPolygon` | The generic type representing the platform-specific polygon object (e.g., `com.google.android.gms.maps.model.Polygon`). |

---

## Nested Interfaces

### AddParamsInterface

Represents the parameters required to add a new polygon to the map.

#### Signature

```kotlin
interface AddParamsInterface
```

#### Parameters

| Name    | Type           | Description                                      |
| ------- | -------------- | ------------------------------------------------ |
| `state` | `PolygonState` | The state object containing all properties for the new polygon. |

---

### ChangeParamsInterface<ActualPolygon>

Represents the parameters required to update an existing polygon. It provides both the previous and current states to allow for efficient, targeted updates.

#### Signature

```kotlin
interface ChangeParamsInterface<ActualPolygon>
```

#### Type Parameters

| Name            | Description                                                                                             |
| --------------- | ------------------------------------------------------------------------------------------------------- |
| `ActualPolygon` | The generic type representing the platform-specific polygon object. |

#### Parameters

| Name      | Type                                      | Description                                                              |
| --------- | ----------------------------------------- | ------------------------------------------------------------------------ |
| `current` | `PolygonEntityInterface<ActualPolygon>`   | The entity representing the new, updated state of the polygon.           |
| `prev`    | `PolygonEntityInterface<ActualPolygon>`   | The entity representing the previous state of the polygon before the change. |

---

## Functions

### onAdd

This function is called to add a batch of new polygons to the map. The implementation should create the native polygon objects and add them to the map view.

#### Signature

```kotlin
suspend fun onAdd(data: List<AddParamsInterface>): List<ActualPolygon?>
```

#### Parameters

| Name   | Type                         | Description                                            |
| ------ | ---------------------------- | ------------------------------------------------------ |
| `data` | `List<AddParamsInterface>`   | A list of parameter objects for the polygons to be added. |

#### Returns

`List<ActualPolygon?>`: A list of the newly created, platform-specific polygon objects. The size and order of this list must match the input `data` list. If a polygon fails to be created, the corresponding element in the list should be `null`.

---

### onChange

This function is called to process a batch of updates for existing polygons. The implementation should compare the `prev` and `current` states for each item and apply only the necessary changes to the native polygon objects for optimal performance.

#### Signature

```kotlin
suspend fun onChange(data: List<ChangeParamsInterface<ActualPolygon>>): List<ActualPolygon?>
```

#### Parameters

| Name   | Type                                          | Description                                                              |
| ------ | --------------------------------------------- | ------------------------------------------------------------------------ |
| `data` | `List<ChangeParamsInterface<ActualPolygon>>`  | A list of objects, each containing the previous and current states for a polygon to be updated. |

#### Returns

`List<ActualPolygon?>`: A list of the updated, platform-specific polygon objects. The size and order of this list must match the input `data` list.

---

### onRemove

This function is called to remove a batch of polygons from the map.

#### Signature

```kotlin
suspend fun onRemove(data: List<PolygonEntityInterface<ActualPolygon>>)
```

#### Parameters

| Name   | Type                                        | Description                                      |
| ------ | ------------------------------------------- | ------------------------------------------------ |
| `data` | `List<PolygonEntityInterface<ActualPolygon>>` | A list of polygon entities to be removed from the map. |

---

### onPostProcess

A lifecycle callback that is invoked after all `onAdd`, `onChange`, and `onRemove` operations for a single update cycle have been completed. This function can be used for cleanup, finalization, or triggering map-wide updates if necessary.

#### Signature

```kotlin
suspend fun onPostProcess()
```

---

## Example

Here is a skeleton implementation of `PolygonOverlayRendererInterface` for Google Maps.

```kotlin
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions

// Assuming PolygonEntityInterface and PolygonState are defined elsewhere
// data class PolygonEntityInterface<ActualPolygon>(val id: String, val state: PolygonState, val actual: ActualPolygon?)
// data class PolygonState(val points: List<LatLng>, val fillColor: Int, val strokeColor: Int, val zIndex: Float)

class GoogleMapsPolygonRenderer(private val map: GoogleMap) : PolygonOverlayRendererInterface<Polygon> {

    override suspend fun onAdd(data: List<AddParamsInterface>): List<Polygon?> {
        return data.map { params ->
            val state = params.state
            val polygonOptions = PolygonOptions()
                .addAll(state.points)
                .fillColor(state.fillColor)
                .strokeColor(state.strokeColor)
                .zIndex(state.zIndex)
            
            map.addPolygon(polygonOptions)
        }
    }

    override suspend fun onChange(data: List<ChangeParamsInterface<Polygon>>): List<Polygon?> {
        return data.map { params ->
            val polygon = params.prev.actual
            val newState = params.current.state
            
            polygon?.apply {
                // Apply changes based on the new state
                points = newState.points
                fillColor = newState.fillColor
                strokeColor = newState.strokeColor
                zIndex = newState.zIndex
            }
            
            polygon // Return the updated polygon
        }
    }

    override suspend fun onRemove(data: List<PolygonEntityInterface<Polygon>>) {
        data.forEach { entity ->
            entity.actual?.remove()
        }
    }

    override suspend fun onPostProcess() {
        // Optional: Perform any cleanup or finalization after a batch update.
        // For example, you could refresh the map view if needed.
        println("Polygon batch processing complete.")
    }
}
```