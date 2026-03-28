# MarkerDragLayer

The `MarkerDragLayer` class extends `MarkerLayer` to manage a dedicated layer for a single, selected marker. It is primarily used to visualize a marker as it is being dragged across the map, providing methods to update its position and redraw it efficiently.

This layer is designed to hold at most one marker at a time, which is represented by the `selected` property.

## Constructor

### `MarkerDragLayer(sourceId: String, layerId: String)`

Initializes a new instance of the `MarkerDragLayer`.

#### Parameters

| Parameter | Type   | Description                               |
| :-------- | :----- | :---------------------------------------- |
| `sourceId`  | `String` | The unique identifier for the GeoJSON source of this layer. |
| `layerId`   | `String` | The unique identifier for the map layer.  |

## Properties

### `selected`

The marker entity that is currently selected for dragging. The layer will only draw this marker. When set to `null`, the layer will be empty.

**Signature**
```kotlin
var selected: MarkerEntityInterface<MapLibreActualMarker>? = null
```

## Methods

### `updatePosition`

Updates the in-memory geographical position of the `selected` marker entity. This change is not visually reflected on the map until the `draw()` method is called.

**Signature**
```kotlin
fun updatePosition(geoPoint: GeoPoint)
```

**Parameters**

| Parameter  | Type      | Description                               |
| :--------- | :-------- | :---------------------------------------- |
| `geoPoint` | `GeoPoint`  | The new geographical coordinates for the selected marker. |

**Returns**

`Unit`

### `draw`

Renders the `selected` marker on the map based on its current state. This method creates a `FeatureCollection` containing only the selected marker and updates the `GeoJsonSource` associated with this layer. If the source is already part of the map's style, it will be updated directly.

**Signature**
```kotlin
fun draw(style: Style)
```

**Parameters**

| Parameter | Type    | Description                                      |
| :-------- | :------ | :----------------------------------------------- |
| `style`   | `Style` | The `Style` object of the map where the marker will be drawn. |

**Returns**

`Unit`

## Example

Here is an example of how to use `MarkerDragLayer` to manage a marker during a drag gesture.

```kotlin
// 1. Initialize the drag layer
val dragLayer = MarkerDragLayer(sourceId = "drag-source", layerId = "drag-layer")

// Assume 'map' is your MapLibre map instance and 'style' is its loaded style
// Add the layer and source to the map style
style.addSource(dragLayer.source)
style.addLayer(dragLayer.layer)

// 2. When a marker drag begins, assign the marker to the drag layer
fun onMarkerDragStart(markerToDrag: MarkerEntityInterface<MapLibreActualMarker>) {
    // Set the marker as selected in the drag layer
    dragLayer.selected = markerToDrag
    
    // Hide the original marker from its source layer if necessary
    // ...
}

// 3. As the user's finger moves, update the marker's position
fun onMarkerDrag(newCoordinates: GeoPoint) {
    // Update the position in memory
    dragLayer.updatePosition(newCoordinates)
    
    // Redraw the marker at the new position
    map.style?.let { dragLayer.draw(it) }
}

// 4. When the drag ends, clear the drag layer
fun onMarkerDragEnd() {
    // Clear the selected marker
    dragLayer.selected = null
    
    // Redraw the layer to remove the marker
    map.style?.let { dragLayer.draw(it) }
    
    // Update the original marker's position and make it visible again
    // ...
}
```