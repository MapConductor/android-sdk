Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# MapLibrePolygonConductor

The `MapLibrePolygonConductor` class is a controller responsible for managing polygon overlays on a MapLibre map. It implements the `OverlayControllerInterface` to handle the lifecycle of polygons, including their creation, update, and removal. This conductor also manages the rendering of polygon outlines by creating corresponding polylines.

It acts as a bridge between the abstract `PolygonState` data and the concrete rendering implementation provided by `MapLibrePolygonOverlayRenderer` and `MapLibrePolylineOverlayRenderer`.

## Signature

```kotlin
class MapLibrePolygonConductor(
    val polygonOverlay: MapLibrePolygonOverlayRenderer,
    val polylineOverlay: MapLibrePolylineOverlayRenderer,
) : OverlayControllerInterface<
        PolygonState,
        PolygonEntityInterface<PolygonState>,
        PolygonEvent,
    >
```

## Constructor

### `MapLibrePolygonConductor(...)`

Initializes a new instance of the `MapLibrePolygonConductor`.

#### Signature

```kotlin
MapLibrePolygonConductor(
    val polygonOverlay: MapLibrePolygonOverlayRenderer,
    val polylineOverlay: MapLibrePolylineOverlayRenderer,
)
```

#### Parameters

| Parameter         | Type                               | Description                                                              |
| ----------------- | ---------------------------------- | ------------------------------------------------------------------------ |
| `polygonOverlay`  | `MapLibrePolygonOverlayRenderer`   | The renderer responsible for drawing the filled area of the polygons.    |
| `polylineOverlay` | `MapLibrePolylineOverlayRenderer`  | The renderer responsible for drawing the outlines (strokes) of the polygons. |

## Properties

### `zIndex`

Specifies the drawing order of the overlay on the map. Higher values are drawn on top of lower values.

#### Signature

```kotlin
override val zIndex: Int = 2
```

### `clickListener`

A callback function that is invoked when any polygon managed by this conductor is clicked. This provides a centralized way to handle click events for all polygons.

#### Signature

```kotlin
override var clickListener: ((PolygonEvent) -> Unit)? = null
```

## Methods

### `add`

Adds a list of polygons to the map. This method efficiently synchronizes the state of the map with the provided list. It adds new polygons, updates existing ones, and removes any polygons that are not in the new list. It also creates and manages the corresponding outlines for each polygon.

#### Signature

```kotlin
override suspend fun add(data: List<PolygonState>)
```

#### Parameters

| Parameter | Type                | Description                               |
| --------- | ------------------- | ----------------------------------------- |
| `data`    | `List<PolygonState>` | A list of `PolygonState` objects to display on the map. |

### `update`

Updates a single polygon on the map based on its `PolygonState`. If a polygon with the same ID already exists, its properties will be updated. If it does not exist, a new polygon will be created.

#### Signature

```kotlin
override suspend fun update(state: PolygonState)
```

#### Parameters

| Parameter | Type           | Description                                      |
| --------- | -------------- | ------------------------------------------------ |
| `state`   | `PolygonState` | The state object representing the polygon to update or add. |

### `dispatchClick`

Dispatches a click event. This method is typically called by the underlying map framework when a tap is detected on a polygon. It triggers the `onClick` handler defined within the specific `PolygonEvent.state` and also invokes the global `clickListener` if it has been set.

#### Signature

```kotlin
fun dispatchClick(event: PolygonEvent)
```

#### Parameters

| Parameter | Type           | Description                               |
| --------- | -------------- | ----------------------------------------- |
| `event`   | `PolygonEvent` | The event object containing details about the click. |

### `find`

Finds the topmost polygon entity at a given geographic position on the map.

#### Signature

```kotlin
override fun find(position: GeoPointInterface): PolygonEntityInterface<PolygonState>?
```

#### Parameters

| Parameter  | Type                | Description                                  |
| ---------- | ------------------- | -------------------------------------------- |
| `position` | `GeoPointInterface` | The geographic coordinate (latitude and longitude) to search at. |

#### Returns

| Type                                     | Description                                                              |
| ---------------------------------------- | ------------------------------------------------------------------------ |
| `PolygonEntityInterface<PolygonState>?`  | The found polygon entity, or `null` if no polygon is found at the specified position. |

### `clear`

Removes all polygons and their outlines that are currently managed by this conductor from the map.

#### Signature

```kotlin
override suspend fun clear()
```

### `onCameraChanged`

A lifecycle method called when the map's camera position changes. This implementation is currently empty.

#### Signature

```kotlin
override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

### `destroy`

Cleans up resources used by the conductor. This implementation is currently empty as there are no specific native resources to release for polygons.

#### Signature

```kotlin
override fun destroy()
```

## Example

Here's an example of how to instantiate and use `MapLibrePolygonConductor` to manage polygons on a map.

```kotlin
// 1. Assume polygonOverlay and polylineOverlay are already initialized
// val polygonOverlay: MapLibrePolygonOverlayRenderer = ...
// val polylineOverlay: MapLibrePolylineOverlayRenderer = ...

// 2. Create an instance of the conductor
val polygonConductor = MapLibrePolygonConductor(polygonOverlay, polylineOverlay)

// 3. Define the state for the polygons you want to add
val polygonState1 = PolygonState(
    id = "polygon-1",
    points = listOf(
        GeoPoint(40.7128, -74.0060), // New York
        GeoPoint(34.0522, -118.2437), // Los Angeles
        GeoPoint(29.7604, -95.3698)  // Houston
    ),
    fillColor = Color.argb(100, 0, 0, 255), // Semi-transparent blue
    strokeColor = Color.BLUE,
    strokeWidth = 5f,
    onClick = { event ->
        println("Clicked on polygon with ID: ${event.state.id}")
    }
)

val polygonState2 = PolygonState(
    id = "polygon-2",
    points = listOf(
        GeoPoint(41.8781, -87.6298), // Chicago
        GeoPoint(39.7392, -104.9903), // Denver
        GeoPoint(47.6062, -122.3321) // Seattle
    ),
    fillColor = Color.argb(100, 255, 0, 0) // Semi-transparent red
)

// 4. Add the polygons to the map
// This needs to be called from a coroutine scope
lifecycleScope.launch {
    polygonConductor.add(listOf(polygonState1, polygonState2))
}

// 5. Set a global click listener for all polygons
polygonConductor.clickListener = { event ->
    println("Global listener: Clicked on polygon ${event.state.id}")
    // You can, for example, show an info window here
}

// 6. Update a polygon later
lifecycleScope.launch {
    val updatedPolygonState1 = polygonState1.copy(
        fillColor = Color.argb(100, 0, 255, 0) // Change color to green
    )
    polygonConductor.update(updatedPolygonState1)
}

// 7. Clear all polygons from the map
lifecycleScope.launch {
    polygonConductor.clear()
}
```