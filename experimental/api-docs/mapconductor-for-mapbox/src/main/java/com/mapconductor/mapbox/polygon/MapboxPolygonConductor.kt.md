Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# MapboxPolygonConductor

## Class: `MapboxPolygonConductor`

### Description

The `MapboxPolygonConductor` class is a controller responsible for managing and rendering polygon overlays on a Mapbox map. It implements the `OverlayControllerInterface` to handle the lifecycle of polygon features, including their filled areas and outlines.

This conductor uses two distinct renderers: one for the polygon fills (`MapboxPolygonOverlayRenderer`) and another for the polyline outlines (`MapboxPolylineOverlayRenderer`). It orchestrates these renderers to add, update, clear, and find polygons, as well as to handle user interactions like clicks.

### Signature

```kotlin
class MapboxPolygonConductor(
    val polygonOverlay: MapboxPolygonOverlayRenderer,
    val polylineOverlay: MapboxPolylineOverlayRenderer,
) : OverlayControllerInterface<
        PolygonState,
        PolygonEntityInterface<PolygonState>,
        PolygonEvent,
    >
```

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `polygonOverlay` | `MapboxPolygonOverlayRenderer` | The renderer responsible for drawing the filled area of the polygons. |
| `polylineOverlay` | `MapboxPolylineOverlayRenderer` | The renderer responsible for drawing the outlines (strokes) of the polygons. |

## Properties

### zIndex

The stacking order of this overlay controller on the map. Overlays with higher z-indices are drawn on top of those with lower indices.

**Signature**
```kotlin
override val zIndex: Int = 2
```

### clickListener

A callback function that is invoked when any polygon managed by this conductor is clicked. This provides a centralized way to handle click events for all polygons.

**Signature**
```kotlin
override var clickListener: ((PolygonEvent) -> Unit)? = null
```

## Methods

### add

Adds a list of polygons to the map. This method performs a diffing operation: it removes any previously added polygons that are not in the new `data` list and then adds or updates the polygons specified in the list. For each polygon, it also creates a corresponding closed polyline to serve as its outline.

**Signature**
```kotlin
override suspend fun add(data: List<PolygonState>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<PolygonState>` | A list of `PolygonState` objects representing the polygons to be displayed on the map. |

### update

Adds or updates a single polygon on the map. If a polygon with the same ID already exists, it will be replaced with the new state. A corresponding outline is also created or updated.

**Signature**
```kotlin
override suspend fun update(state: PolygonState)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `PolygonState` | The state of the single polygon to add or update. |

### dispatchClick

Dispatches a click event. This method is typically called by the underlying map framework when a tap is detected on a polygon. It triggers two callbacks in order:
1. The `onClick` handler defined within the specific `PolygonState` of the clicked polygon.
2. The global `clickListener` set on this `MapboxPolygonConductor` instance.

**Signature**
```kotlin
fun dispatchClick(event: PolygonEvent)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `event` | `PolygonEvent` | The event object containing details about the click, including the state of the clicked polygon. |

### find

Finds the topmost polygon entity at a given geographic coordinate.

**Signature**
```kotlin
override fun find(position: GeoPointInterface): PolygonEntityInterface<PolygonState>?
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPointInterface` | The geographic coordinates (latitude and longitude) to search at. |

**Returns**

| Type | Description |
| :--- | :--- |
| `PolygonEntityInterface<PolygonState>?` | The found polygon entity, or `null` if no polygon exists at the specified position. |

### clear

Removes all polygons and their associated outlines from the map that are managed by this conductor.

**Signature**
```kotlin
override suspend fun clear()
```

### onCameraChanged

A lifecycle method called when the map's camera position changes. This implementation is currently empty as no action is required for polygons on camera change.

**Signature**
```kotlin
override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `mapCameraPosition` | `MapCameraPosition` | The new camera position of the map. |

### destroy

Cleans up resources used by the controller. This implementation is empty as there are no specific native resources to release for polygons.

**Signature**
```kotlin
override fun destroy()
```

## Example

Here is an example of how to instantiate `MapboxPolygonConductor` and use it to manage polygons on a map.

```kotlin
// Assume polygonOverlay and polylineOverlay are already initialized
val polygonConductor = MapboxPolygonConductor(polygonOverlay, polylineOverlay)

// Set a global click listener for all polygons
polygonConductor.clickListener = { event ->
    println("Polygon with ID ${event.state.id} was clicked!")
}

// Define the state for two polygons
val polygon1State = PolygonState(
    id = "p1",
    points = listOf(
        GeoPoint(40.7128, -74.0060), // New York
        GeoPoint(34.0522, -118.2437), // Los Angeles
        GeoPoint(29.7604, -95.3698)  // Houston
    ),
    fillColor = Color.argb(100, 0, 0, 255), // Semi-transparent blue
    strokeColor = Color.BLUE,
    strokeWidth = 2f,
    onClick = { event ->
        println("Specific onClick for polygon ${event.state.id}")
    }
)

val polygon2State = PolygonState(
    id = "p2",
    points = listOf(
        GeoPoint(41.8781, -87.6298), // Chicago
        GeoPoint(39.9526, -75.1652), // Philadelphia
        GeoPoint(33.4484, -112.0740) // Phoenix
    ),
    fillColor = Color.argb(100, 255, 0, 0) // Semi-transparent red
)

// Add the polygons to the map
// This must be called from a coroutine scope
coroutineScope.launch {
    polygonConductor.add(listOf(polygon1State, polygon2State))
}

// ... later, to remove all polygons
coroutineScope.launch {
    polygonConductor.clear()
}
```