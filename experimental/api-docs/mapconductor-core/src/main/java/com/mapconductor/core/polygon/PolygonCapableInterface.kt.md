# PolygonCapableInterface

The `PolygonCapableInterface` defines a contract for components that can display and manage polygons on a map. It provides methods for adding, updating, checking, and handling interactions with polygons.

---

## Methods

### compositionPolygons

Sets or replaces the entire collection of polygons displayed on the map. This function is asynchronous and should be called from a coroutine. Any polygons currently on the map that are not in the provided `data` list will be removed.

#### Signature

```kotlin
suspend fun compositionPolygons(data: List<PolygonState>)
```

#### Parameters

| Parameter | Type                 | Description                                                  |
| :-------- | :------------------- | :----------------------------------------------------------- |
| `data`    | `List<PolygonState>` | A list of `PolygonState` objects to be displayed on the map. |

#### Returns

This is a suspend function and does not return a value.

#### Example

```kotlin
// Assuming 'mapController' implements PolygonCapableInterface
// and 'polygon1State' and 'polygon2State' are valid PolygonState objects.

val polygonList = listOf(polygon1State, polygon2State)

// Launch a coroutine to update the polygons on the map
coroutineScope.launch {
    mapController.compositionPolygons(polygonList)
}
```

---

### updatePolygon

Updates the properties of a single, existing polygon on the map. The specific polygon to be updated is identified by an ID within the provided `PolygonState` object. If no polygon with the matching ID exists, the call may be ignored. This function is asynchronous.

#### Signature

```kotlin
suspend fun updatePolygon(state: PolygonState)
```

#### Parameters

| Parameter | Type           | Description                                                              |
| :-------- | :------------- | :----------------------------------------------------------------------- |
| `state`   | `PolygonState` | The `PolygonState` object containing the updated properties for a polygon. |

#### Returns

This is a suspend function and does not return a value.

#### Example

```kotlin
// Create an updated state for an existing polygon with id "polygon-1"
val updatedPolygonState = PolygonState(
    id = "polygon-1",
    points = newListOfGeoPoints,
    fillColor = Color.BLUE
)

// Launch a coroutine to apply the update
coroutineScope.launch {
    mapController.updatePolygon(updatedPolygonState)
}
```

---

### setOnPolygonClickListener

<p style="background-color: #FFF3CD; color: #664D03; border-left: 5px solid #FFC107; padding: 15px;">
  <strong><span style="font-family: 'Courier New', Courier, monospace;">@Deprecated</span></strong><br>
  This method is deprecated. Use the <code>onClick</code> lambda property within the <code>PolygonState</code> object for handling click events on a per-polygon basis.
</p>

Sets a global listener for click events on any polygon.

#### Signature

```kotlin
fun setOnPolygonClickListener(listener: OnPolygonEventHandler?)
```

#### Parameters

| Parameter  | Type                    | Description                                                              |
| :--------- | :---------------------- | :----------------------------------------------------------------------- |
| `listener` | `OnPolygonEventHandler?` | The event handler to be invoked when a polygon is clicked. Pass `null` to remove the current listener. |

#### Returns

This function does not return a value.

#### Example

```kotlin
// Note: This method is deprecated.

// Define a listener
val polygonClickListener = object : OnPolygonEventHandler {
    override fun onPolygonClick(polygonId: String) {
        println("Polygon with ID $polygonId was clicked.")
    }
}

// Set the listener
mapController.setOnPolygonClickListener(polygonClickListener)

// To remove the listener
mapController.setOnPolygonClickListener(null)
```

---

### hasPolygon

Checks if a polygon with a specific ID (derived from the `PolygonState`) exists on the map.

#### Signature

```kotlin
fun hasPolygon(state: PolygonState): Boolean
```

#### Parameters

| Parameter | Type           | Description                                                                                             |
| :-------- | :------------- | :------------------------------------------------------------------------------------------------------ |
| `state`   | `PolygonState` | The `PolygonState` object representing the polygon to check for. The check is typically based on the `id` property. |

#### Returns

| Type      | Description                                      |
| :-------- | :----------------------------------------------- |
| `Boolean` | Returns `true` if the polygon exists, `false` otherwise. |

#### Example

```kotlin
val polygonToCheck = PolygonState(id = "polygon-alpha")

if (mapController.hasPolygon(polygonToCheck)) {
    println("Polygon 'polygon-alpha' is currently on the map.")
} else {
    println("Polygon 'polygon-alpha' was not found.")
}
```