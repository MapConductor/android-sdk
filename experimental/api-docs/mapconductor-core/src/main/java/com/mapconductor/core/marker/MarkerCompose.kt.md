Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

### `Marker`

This Composable declaratively adds a single marker to the map. It is the most convenient way to create a marker when you have its individual properties.

This function must be called from within the `MapViewScope`, such as the content lambda of a `MapView` composable.

#### Signature
```kotlin
@Composable
fun MapViewScope.Marker(
    position: GeoPointInterface,
    id: String? = null,
    zIndex: Int? = null,
    clickable: Boolean = true,
    draggable: Boolean = false,
    icon: MarkerIconInterface? = null,
    animation: MarkerAnimation? = null,
    extra: Serializable? = null,
    onClick: OnMarkerEventHandler? = null,
    onDragStart: OnMarkerEventHandler? = null,
    onDrag: OnMarkerEventHandler? = null,
    onDragEnd: OnMarkerEventHandler? = null,
    onAnimateStart: OnMarkerEventHandler? = null,
    onAnimateEnd: OnMarkerEventHandler? = null,
)
```

#### Description
Creates and manages a single map marker. The marker's lifecycle is tied to the Composable's lifecycle. When this Composable enters the composition, the marker is added to the map. When it leaves, the marker is removed.

#### Parameters
| Parameter | Type | Description |
|---|---|---|
| `position` | `GeoPointInterface` | **Required.** The geographic coordinates where the marker will be placed on the map. |
| `id` | `String?` | A unique identifier for the marker. If `null`, a unique ID will be generated. Providing a stable ID is recommended for performance and state management. |
| `zIndex` | `Int?` | The z-index of the marker, which determines its drawing order. Markers with higher z-index values are drawn on top of those with lower values. |
| `clickable` | `Boolean` | If `true`, the marker will be clickable and can receive `onClick` events. Defaults to `true`. |
| `draggable` | `Boolean` | If `true`, the user can drag the marker to a new position. Defaults to `false`. |
| `icon` | `MarkerIconInterface?` | The visual representation (icon) of the marker. If `null`, the map's default marker icon will be used. |
| `animation` | `MarkerAnimation?` | An animation to apply to the marker, such as `DROP` or `PULSE`. |
| `extra` | `Serializable?` | A serializable object containing any extra data you want to associate with the marker. |
| `onClick` | `OnMarkerEventHandler?` | A callback lambda that is invoked when the user clicks on the marker. The marker must be `clickable`. |
| `onDragStart` | `OnMarkerEventHandler?` | A callback lambda invoked when the user starts dragging the marker. The marker must be `draggable`. |
| `onDrag` | `OnMarkerEventHandler?` | A callback lambda invoked repeatedly as the user drags the marker. |
| `onDragEnd` | `OnMarkerEventHandler?` | A callback lambda invoked when the user finishes dragging the marker. |
| `onAnimateStart` | `OnMarkerEventHandler?` | A callback lambda invoked when a marker animation starts. |
| `onAnimateEnd` | `OnMarkerEventHandler?` | A callback lambda invoked when a marker animation ends. |

#### Returns
This is a Composable function and does not return any value.

#### Example
```kotlin
MapView {
    // A simple, clickable marker at a specific location
    Marker(
        position = GeoPoint(40.7128, -74.0060),
        id = "nyc-marker",
        onClick = { markerState ->
            println("Clicked on marker: ${markerState.id}")
        }
    )

    // A draggable marker with a custom icon and z-index
    Marker(
        position = GeoPoint(34.0522, -118.2437),
        id = "la-marker",
        draggable = true,
        zIndex = 10,
        icon = MarkerIconFactory.fromResource(R.drawable.custom_pin),
        onDragEnd = { markerState ->
            println("New position for ${markerState.id}: ${markerState.position}")
        }
    )
}
```

***

### `Markers`

A high-performance Composable for efficiently adding and managing a large number of markers.

#### Signature
```kotlin
@Composable
fun MapViewScope.Markers(states: List<MarkerState>)
```

#### Description
This function is designed to render thousands of markers without the significant composition overhead that would occur from using one `Marker` Composable for each item. It performs batched updates in a single effect, making it the ideal choice for displaying large, dynamic datasets on the map.

When the `states` list changes, this function efficiently calculates the difference and applies the necessary add, remove, or update operations on the map.

#### Parameters
| Parameter | Type | Description |
|---|---|---|
| `states` | `List<MarkerState>` | **Required.** A list of `MarkerState` objects, where each object defines a single marker and its properties. |

#### Returns
This is a Composable function and does not return any value.

#### Example
```kotlin
val locations = remember {
    // Imagine this list is fetched from a remote API and contains thousands of items
    listOf(
        LocationData("id1", GeoPoint(48.8566, 2.3522)),
        LocationData("id2", GeoPoint(51.5074, -0.1278)),
        LocationData("id3", GeoPoint(35.6895, 139.6917))
    )
}

// Create a list of MarkerState objects from your data
val markerStates = locations.map { location ->
    MarkerState(
        id = location.id,
        position = location.geoPoint,
        clickable = true
    )
}

MapView {
    // Render all markers efficiently
    Markers(states = markerStates)
}
```

***

### `Marker` (State-based)

A lower-level Composable that adds a single marker to the map using a `MarkerState` object.

#### Signature
```kotlin
@Composable
fun MapViewScope.Marker(state: MarkerState)
```

#### Description
This version of the `Marker` Composable takes a pre-constructed `MarkerState` object. It is useful when you are managing the state of your markers explicitly, for instance, within a `ViewModel` or a `remember` block. The marker's lifecycle is tied to this Composable's presence in the composition.

#### Parameters
| Parameter | Type | Description |
|---|---|---|
| `state` | `MarkerState` | **Required.** The state object that defines all properties of the marker, including its position, icon, and event handlers. |

#### Returns
This is a Composable function and does not return any value.

#### Example
```kotlin
MapView {
    // Create and remember a MarkerState object
    val markerState = remember {
        MarkerState(
            id = "paris-marker",
            position = GeoPoint(48.8566, 2.3522),
            draggable = true,
            onClick = { /* ... */ }
        )
    }

    // Pass the state object to the Marker composable
    Marker(state = markerState)
}
```