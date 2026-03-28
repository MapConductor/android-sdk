Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

---

# Marker State Management

This document provides a detailed reference for the marker state management components within the MapConductor SDK. These components are designed to manage the state and behavior of individual markers on a map within a Jetpack Compose environment.

## `MarkerState`

A state holder class that represents and manages the complete state of a single marker on the map. It is designed to be used with Jetpack Compose, as its properties are observable `State` objects. Changes to these properties will trigger recomposition in Composables that read them.

### Signature

```kotlin
class MarkerState(
    position: GeoPointInterface,
    id: String? = null,
    var extra: Serializable? = null,
    icon: MarkerIconInterface? = null,
    animation: MarkerAnimation? = null,
    zIndex: Int? = null,
    clickable: Boolean = true,
    draggable: Boolean = false,
    onClick: OnMarkerEventHandler? = null,
    onDragStart: OnMarkerEventHandler? = null,
    onDrag: OnMarkerEventHandler? = null,
    onDragEnd: OnMarkerEventHandler? = null,
    onAnimateStart: OnMarkerEventHandler? = null,
    onAnimateEnd: OnMarkerEventHandler? = null,
) : ComponentState
```

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPointInterface` | **Required.** The geographical coordinates where the marker is placed. |
| `id` | `String?` | An optional unique identifier for the marker. If `null`, a stable ID is automatically generated based on the marker's initial properties. |
| `extra` | `Serializable?` | Optional, serializable data that can be attached to the marker. Useful for storing custom information. |
| `icon` | `MarkerIconInterface?` | The visual representation of the marker. Defaults to the map's default marker icon if `null`. |
| `animation` | `MarkerAnimation?` | An optional animation to be applied to the marker upon its initial appearance. |
| `zIndex` | `Int?` | The stacking order of the marker relative to other map components. Higher values are drawn on top. |
| `clickable` | `Boolean` | Determines if the marker can be clicked. Defaults to `true`. |
| `draggable` | `Boolean` | Determines if the marker can be dragged. Defaults to `false`. |
| `onClick` | `OnMarkerEventHandler?` | A callback invoked when the marker is clicked. The handler receives the `MarkerState` of the clicked marker. |
| `onDragStart` | `OnMarkerEventHandler?` | A callback invoked when a drag gesture starts on the marker. |
| `onDrag` | `OnMarkerEventHandler?` | A callback invoked continuously while the marker is being dragged. |
| `onDragEnd` | `OnMarkerEventHandler?` | A callback invoked when a drag gesture ends. |
| `onAnimateStart` | `OnMarkerEventHandler?` | A callback invoked when a marker animation starts. |
| `onAnimateEnd` | `OnMarkerEventHandler?` | A callback invoked when a marker animation ends. |

### Properties

The following properties can be read and modified to dynamically update the marker's state.

| Property | Type | Description |
| :--- | :--- | :--- |
| `id` | `String` | The unique identifier of the marker. |
| `position` | `GeoPointInterface` | The current geographical position of the marker. |
| `extra` | `Serializable?` | Custom data associated with the marker. |
| `icon` | `MarkerIconInterface?` | The marker's icon. |
| `clickable` | `Boolean` | The marker's clickability. |
| `draggable` | `Boolean` | The marker's draggability. |
| `zIndex` | `Int?` | The marker's z-index. |
| `onClick` | `OnMarkerEventHandler?` | The click event handler. |
| `onDragStart` | `OnMarkerEventHandler?` | The drag start event handler. |
| `onDrag` | `OnMarkerEventHandler?` | The drag event handler. |
| `onDragEnd` | `OnMarkerEventHandler?` | The drag end event handler. |
| `onAnimateStart` | `OnMarkerEventHandler?` | The animation start event handler. |
| `onAnimateEnd` | `OnMarkerEventHandler?` | The animation end event handler. |

### Methods

#### `animate`

Starts, updates, or stops an animation on the marker.

**Signature**
```kotlin
fun animate(animation: MarkerAnimation?)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `animation` | `MarkerAnimation?` | The animation to apply. Pass `null` to stop any currently running animation. |

#### `copy`

Creates a new `MarkerState` instance with specified properties updated. This is useful for immutable state updates.

**Signature**
```kotlin
fun copy(
    id: String? = this.id,
    position: GeoPointInterface = this.position,
    // ... other parameters
): MarkerState
```

**Description**
Returns a new `MarkerState` object that is a copy of the current one, with any provided parameters overriding the existing values.

**Returns**
| Type | Description |
| :--- | :--- |
| `MarkerState` | A new `MarkerState` instance with the updated properties. |

#### `asFlow`

Creates a `Flow` that emits a `MarkerFingerPrint` whenever a significant property of the marker changes. This is an advanced feature for observing state changes reactively and efficiently.

**Signature**
```kotlin
fun asFlow(): Flow<MarkerFingerPrint>
```

**Returns**
| Type | Description |
| :--- | :--- |
| `Flow<MarkerFingerPrint>` | A cold flow that emits a new fingerprint on state change. |

### Example

```kotlin
// Assuming GeoPoint and a Bitmap are available
val initialPosition = GeoPoint(40.7128, -74.0060)
val markerIconBitmap: Bitmap = getYourBitmap()

// 1. Create a MarkerState instance
val markerState = MarkerState(
    position = initialPosition,
    extra = "marker-123-data",
    icon = BitmapIcon(
        bitmap = markerIconBitmap,
        size = Size(96f, 96f),
        anchor = Offset(0.5f, 1.0f) // Anchor to bottom-center
    ),
    draggable = true,
    onClick = { state ->
        println("Marker clicked! ID: ${state.id}, Extra data: ${state.extra}")
    },
    onDragEnd = { state ->
        println("Marker dragged to new position: ${state.position}")
    }
)

// 2. Add the marker to your map's state list
// val markers = remember { mutableStateListOf(markerState) }

// 3. Later, you can update its properties dynamically
// This will trigger a recomposition if the state is observed
markerState.position = GeoPoint(40.7580, -73.9855) // Move the marker
markerState.clickable = false
```

---

## `OnMarkerEventHandler`

A type alias for a function that handles marker-related events.

### Signature

```kotlin
typealias OnMarkerEventHandler = (MarkerState) -> Unit
```

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `(MarkerState)` | `MarkerState` | The `MarkerState` instance of the marker that triggered the event. |

---

## `BitmapIcon`

A data class that defines a marker icon using an Android `Bitmap`.

### Signature

```kotlin
data class BitmapIcon(
    val bitmap: Bitmap,
    val anchor: Offset,
    val size: Size,
)
```

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `bitmap` | `Bitmap` | The `Bitmap` image to use for the icon. |
| `anchor` | `Offset` | The anchor point of the icon that aligns with the marker's geographical position. The coordinates are normalized from 0.0 to 1.0, where `(0,0)` is the top-left corner and `(1,1)` is the bottom-right. For example, `Offset(0.5f, 1.0f)` anchors the icon at its bottom-center. |
| `size` | `Size` | The desired display size of the icon on the map. |

---

## `MarkerFingerPrint`

A data class holding a simplified, hash-based representation of `MarkerState`. This is primarily used internally by `MarkerState.asFlow()` for efficient change detection. Developers typically do not need to interact with this class directly.