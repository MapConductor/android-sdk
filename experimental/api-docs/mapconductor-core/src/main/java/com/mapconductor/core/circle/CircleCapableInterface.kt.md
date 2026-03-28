# CircleCapableInterface

An interface for components that can manage and display circles, such as a map view. It provides functionalities to add, update, and interact with circles on the component.

---

## Methods

### compositionCircles

Adds or updates a collection of circles. This method is designed to manage the full set of circles, replacing any previously displayed circles with the new list provided.

**Signature**
```kotlin
suspend fun compositionCircles(data: List<CircleState>)
```

**Description**
This is a suspend function that recomposes the view with a new list of circles. It's an efficient way to display a complete set of circles, as it handles adding, updating, and removing circles to match the provided `data` list.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<CircleState>` | A list of `CircleState` objects, each defining the properties of a circle to be displayed. |

**Returns**
This is a `suspend` function and does not return a value.

**Example**
```kotlin
// Requires a CoroutineScope to launch the suspend function
coroutineScope.launch {
    val circle1 = CircleState(id = "c1", center = LatLng(34.0, -118.2), radius = 1000.0)
    val circle2 = CircleState(id = "c2", center = LatLng(36.1, -115.1), radius = 1500.0)
    
    mapView.compositionCircles(listOf(circle1, circle2))
}
```

---

### updateCircle

Updates a single existing circle with a new state.

**Signature**
```kotlin
suspend fun updateCircle(state: CircleState)
```

**Description**
This suspend function updates the properties of a single circle that is already on the map. The circle to be updated is identified by the `id` within the provided `CircleState` object.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `CircleState` | The new state for the circle. The `id` in the state must match an existing circle. |

**Returns**
This is a `suspend` function and does not return a value.

**Example**
```kotlin
// Assume a circle with id "c1" already exists on the map
coroutineScope.launch {
    val updatedCircleState = CircleState(
        id = "c1", 
        center = LatLng(34.0, -118.2), 
        radius = 2500.0, // Update the radius
        fillColor = Color.BLUE // Update the color
    )
    
    mapView.updateCircle(updatedCircleState)
}
```

---

### setOnCircleClickListener

Sets a listener to handle click events on any circle.

> **Deprecated:** This method is deprecated. Use the `onClick` lambda property within `CircleState` for handling click events on a per-circle basis.

**Signature**
```kotlin
@Deprecated("Use CircleState.onClick instead.")
fun setOnCircleClickListener(listener: OnCircleEventHandler?)
```

**Description**
Registers a callback function that will be invoked when any circle managed by this interface is clicked. Setting the listener to `null` removes any previously set listener.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `listener` | `OnCircleEventHandler?` | The listener to be invoked on a circle click, or `null` to remove the current listener. |

**Returns**
This function does not return a value.

**Example**
```kotlin
// Old, deprecated way
val circleClickListener = OnCircleEventHandler { circleState ->
    Log.d("Map", "Circle with ID ${circleState.id} was clicked.")
}
mapView.setOnCircleClickListener(circleClickListener)

// Recommended modern approach: Define the click handler directly in the state
val circleWithHandler = CircleState(
    id = "c1",
    center = LatLng(34.0, -118.2),
    radius = 1000.0,
    onClick = { state -> 
        Log.d("Map", "Circle ${state.id} clicked!") 
    }
)
```

---

### hasCircle

Checks if a specific circle is currently managed by the component.

**Signature**
```kotlin
fun hasCircle(state: CircleState): Boolean
```

**Description**
Determines whether a circle, identified by the `id` in the provided `CircleState`, exists on the map.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `CircleState` | The `CircleState` object representing the circle to check. Only the `id` is typically used for the lookup. |

**Returns**
| Type | Description |
| :--- | :--- |
| `Boolean` | Returns `true` if a circle with the same ID exists, `false` otherwise. |

**Example**
```kotlin
val circleToCheck = CircleState(id = "c1")

if (mapView.hasCircle(circleToCheck)) {
    println("Circle 'c1' is currently on the map.")
} else {
    println("Circle 'c1' is not on the map.")
}
```