Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

### Class `MarkerDragLayer`

Manages a dedicated map layer for rendering a single marker during a drag-and-drop operation. This class is responsible for updating the marker's position and redrawing it on the map as it's being moved, providing real-time visual feedback to the user.

It extends `MarkerLayer` and is designed to work with a specific `GeoJsonSource` that contains only the marker currently being dragged.

**Constructor**

```kotlin
MarkerDragLayer(
    sourceId: String,
    layerId: String
)
```

#### Parameters

| Parameter  | Type     | Description                                                      |
|------------|----------|------------------------------------------------------------------|
| `sourceId` | `String` | The unique identifier for the `GeoJsonSource` associated with this layer. |
| `layerId`  | `String` | The unique identifier for the map layer used to render the marker. |

---

### Properties

#### `selected`

The marker entity that is currently being dragged. When a user begins to drag a marker, assign the corresponding entity to this property. To clear the drag layer (e.g., when the drag operation is complete), set this property back to `null`.

**Signature**
```kotlin
var selected: MarkerEntityInterface<MapboxActualMarker>? = null
```

---

### Methods

#### `updatePosition`

Updates the geographical coordinates of the `selected` marker entity. This method should be called continuously during a drag gesture (e.g., from a touch move event listener) to track the pointer's location.

**Signature**
```kotlin
fun updatePosition(geoPoint: GeoPoint)
```

**Parameters**

| Parameter  | Type       | Description                               |
|------------|------------|-------------------------------------------|
| `geoPoint` | `GeoPoint` | The new geographical point for the marker. |

**Returns**

This method does not return any value.

---

#### `draw`

Redraws the drag layer on the map to reflect the current state of the `selected` marker. This method converts the marker entity into a Mapbox `Feature` and updates the layer's underlying `GeoJsonSource`. If `selected` is `null`, it clears the source, effectively removing the marker from this layer.

This method should be called after `updatePosition` to render the marker's new position on the map.

**Signature**
```kotlin
fun draw()
```

**Returns**

This method does not return any value.

---

### Example

The following example demonstrates the typical lifecycle of using `MarkerDragLayer` to handle a marker drag operation.

```kotlin
// Assume 'dragLayer' is an initialized instance of MarkerDragLayer.
// 'markerToDrag' is the MarkerEntityInterface the user wants to move.

// 1. On drag start (e.g., user long-presses a marker)
// Assign the marker to the drag layer to make it "active".
dragLayer.selected = markerToDrag
dragLayer.draw() // Initially draw the marker on the drag layer.

// 2. On drag move (e.g., in a gesture listener that provides new coordinates)
// 'newGeoPoint' is the current pointer location on the map.
dragLayer.updatePosition(newGeoPoint)
dragLayer.draw() // Redraw the marker at its new position.

// 3. On drag end (e.g., user lifts their finger)
// You can get the final position from the selected marker if needed.
val finalPosition = dragLayer.selected?.state?.position

// Update the original marker's data source with the finalPosition.
// ...

// Clear the drag layer by setting 'selected' to null.
dragLayer.selected = null
dragLayer.draw() // This will remove the marker from the drag layer.
```