Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

---

# PolylineManager

## `PolylineManager<ActualPolyline>`

### Description

The `PolylineManager` class is a comprehensive utility for managing a collection of polyline entities on a map. It provides a robust API for registering, retrieving, and removing polylines. Its key feature is the `find` method, which performs efficient hit-testing to identify the polyline closest to a given geographical coordinate, making it ideal for handling user interactions like taps on polylines.

The manager is generic, denoted by `<ActualPolyline>`, which represents the underlying platform-specific polyline object being managed.

### Methods

#### **registerEntity**

##### Signature

```kotlin
fun registerEntity(entity: PolylineEntityInterface<ActualPolyline>)
```

##### Description

Registers a new polyline entity with the manager. If an entity with the same ID already exists, it will be overwritten.

##### Parameters

| Parameter | Type                                        | Description                              |
| :-------- | :------------------------------------------ | :--------------------------------------- |
| `entity`  | `PolylineEntityInterface<ActualPolyline>` | The polyline entity to add to the manager. |

---

#### **removeEntity**

##### Signature

```kotlin
fun removeEntity(id: String): PolylineEntityInterface<ActualPolyline>?
```

##### Description

Removes a polyline entity from the manager using its unique identifier.

##### Parameters

| Parameter | Type     | Description                                |
| :-------- | :------- | :----------------------------------------- |
| `id`        | `String` | The unique ID of the polyline entity to remove. |

##### Returns

**`PolylineEntityInterface<ActualPolyline>?`**

The removed entity, or `null` if no entity with the given ID was found.

---

#### **getEntity**

##### Signature

```kotlin
fun getEntity(id: String): PolylineEntityInterface<ActualPolyline>?
```

##### Description

Retrieves a polyline entity from the manager by its unique identifier.

##### Parameters

| Parameter | Type     | Description                                  |
| :-------- | :------- | :------------------------------------------- |
| `id`        | `String` | The unique ID of the polyline entity to retrieve. |

##### Returns

**`PolylineEntityInterface<ActualPolyline>?`**

The `PolylineEntityInterface` corresponding to the given ID, or `null` if it's not found.

---

#### **hasEntity**

##### Signature

```kotlin
fun hasEntity(id: String): Boolean
```

##### Description

Checks if an entity with the specified ID is registered with the manager.

##### Parameters

| Parameter | Type     | Description                                  |
| :-------- | :------- | :------------------------------------------- |
| `id`        | `String` | The unique ID of the polyline entity to check for. |

##### Returns

**`Boolean`**

Returns `true` if an entity with the specified ID exists, `false` otherwise.

---

#### **allEntities**

##### Signature

```kotlin
fun allEntities(): List<PolylineEntityInterface<ActualPolyline>>
```

##### Description

Returns a list of all polyline entities currently registered with the manager.

##### Returns

**`List<PolylineEntityInterface<ActualPolyline>>`**

A `List` containing all registered `PolylineEntityInterface` objects.

---

#### **clear**

##### Signature

```kotlin
fun clear()
```

##### Description

Removes all polyline entities from the manager, leaving it empty.

---

#### **find**

##### Signature

```kotlin
fun find(
    position: GeoPointInterface,
    cameraPosition: MapCameraPosition? = null,
): PolylineHitResult<ActualPolyline>?
```

##### Description

Finds the polyline entity closest to a specified geographical position (e.g., a user's tap). The search is constrained by a tap tolerance radius defined in the application settings. This method calculates the search radius in meters based on the current map zoom level and efficiently identifies the polyline segment nearest to the given position. It correctly handles both geodesic and linear (straight-line) polylines.

##### Parameters

| Parameter        | Type                  | Description                                                                                                                                                           |
| :--------------- | :-------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `position`       | `GeoPointInterface`   | The geographical coordinate to search around, typically the location of a tap event.                                                                                  |
| `cameraPosition` | `MapCameraPosition?`  | **Optional.** The current state of the map camera, including zoom and visible region. Providing this improves performance by filtering out off-screen polylines. |

##### Returns

**`PolylineHitResult<ActualPolyline>?`**

A `PolylineHitResult` object containing the closest entity and the exact point of intersection, or `null` if no polyline is found within the tap tolerance.

### Example

```kotlin
// Assume these interfaces and classes are defined elsewhere
// val myPolylineEntity: PolylineEntityInterface<MyMapPolyline> = ...
// val tapLocation: GeoPointInterface = ...
// val currentCameraPosition: MapCameraPosition = ...

// 1. Initialize the manager
val polylineManager = PolylineManager<MyMapPolyline>()

// 2. Register a polyline entity
polylineManager.registerEntity(myPolylineEntity)

// 3. Find the closest polyline to a tap location
val hitResult = polylineManager.find(
    position = tapLocation,
    cameraPosition = currentCameraPosition
)

// 4. Process the result
if (hitResult != null) {
    println("Hit polyline with ID: ${hitResult.entity.state.id}")
    println("Closest point on polyline: ${hitResult.closestPoint.latitude}, ${hitResult.closestPoint.longitude}")
    // Highlight the polyline or show an info window
} else {
    println("No polyline was tapped.")
}
```

---

## Related Data Classes

### `PolylineHitResult<ActualPolyline>`

#### Description

A data class that encapsulates the result of a successful hit test performed by `PolylineManager.find`. It holds the entity that was hit and the specific point on that entity's path closest to the search location.

#### Properties

| Property       | Type                                        | Description                                                                    |
| :------------- | :------------------------------------------ | :----------------------------------------------------------------------------- |
| `entity`       | `PolylineEntityInterface<ActualPolyline>` | The polyline entity that was found to be closest to the search position.       |
| `closestPoint` | `GeoPointInterface`                         | The specific point on the `entity`'s path that is nearest to the search position. |

---

## Interfaces

### `PolylineManagerInterface<ActualPolyline>`

#### Description

This interface defines the public contract for a polyline manager. The `PolylineManager` class is the concrete implementation of this interface.

#### Methods

```kotlin
interface PolylineManagerInterface<ActualPolyline> {
    fun registerEntity(entity: PolylineEntityInterface<ActualPolyline>)
    fun removeEntity(id: String): PolylineEntityInterface<ActualPolyline>?
    fun getEntity(id: String): PolylineEntityInterface<ActualPolyline>?
    fun hasEntity(id: String): Boolean
    fun allEntities(): List<PolylineEntityInterface<ActualPolyline>>
    fun clear()
    fun find(
        position: GeoPointInterface,
        cameraPosition: MapCameraPosition? = null,
    ): PolylineHitResult<ActualPolyline>?
}
```