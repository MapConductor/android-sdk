Of course! Here is the high-quality SDK documentation for the provided code snippet.

# Class: `CircleManager<ActualCircle>`

A thread-safe manager for handling the lifecycle of circle entities on a map. It provides methods to add, remove, retrieve, and query circle objects. This class is responsible for maintaining a collection of `CircleEntityInterface` objects and uses a `ConcurrentHashMap` for its internal storage, ensuring safe concurrent access.

The manager uses a generic type `ActualCircle` to represent the platform-specific circle implementation (e.g., a Google Maps `Circle` or a Mapbox circle annotation).

## Methods

### `registerEntity`

Adds a new circle entity to the manager. If an entity with the same ID already exists, it will be replaced.

**Signature**
```kotlin
fun registerEntity(entity: CircleEntityInterface<ActualCircle>)
```

**Description**
This method registers a given `CircleEntityInterface` with the manager, using the entity's ID as the unique key for storage.

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `entity` | `CircleEntityInterface<ActualCircle>` | The circle entity to register. |

**Example**
```kotlin
// Assuming circleManager is an instance of CircleManager
// and newCircle is an object implementing CircleEntityInterface
circleManager.registerEntity(newCircle)
```

---

### `updateEntity`

Updates an existing circle entity or adds it if it doesn't exist. This method functions as an alias for `registerEntity`.

**Signature**
```kotlin
fun updateEntity(entity: CircleEntityInterface<ActualCircle>)
```

**Description**
This method adds or updates a circle entity in the manager. It is functionally identical to `registerEntity`.

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `entity` | `CircleEntityInterface<ActualCircle>` | The circle entity to add or update. |

**Example**
```kotlin
// Get an existing entity and modify its state
val existingCircle = circleManager.getEntity("circle-id-1")
existingCircle?.state?.radiusMeters = 500.0

// Update the entity in the manager
if (existingCircle != null) {
    circleManager.updateEntity(existingCircle)
}
```

---

### `removeEntity`

Removes a circle entity from the manager based on its unique ID.

**Signature**
```kotlin
fun removeEntity(id: String): CircleEntityInterface<ActualCircle>?
```

**Description**
This method finds and removes the circle entity associated with the specified `id`.

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `String` | The unique identifier of the circle entity to remove. |

**Returns**
| Type | Description |
|------|-------------|
| `CircleEntityInterface<ActualCircle>?` | The removed circle entity if it was found, or `null` if no entity with the specified ID exists. |

**Example**
```kotlin
val removedEntity = circleManager.removeEntity("circle-id-to-delete")
if (removedEntity != null) {
    println("Circle ${removedEntity.state.id} was successfully removed.")
} else {
    println("Circle not found.")
}
```

---

### `getEntity`

Retrieves a circle entity from the manager by its unique ID.

**Signature**
```kotlin
fun getEntity(id: String): CircleEntityInterface<ActualCircle>?
```

**Description**
This method provides read-only access to a circle entity without removing it from the manager.

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `String` | The unique identifier of the circle entity to retrieve. |

**Returns**
| Type | Description |
|------|-------------|
| `CircleEntityInterface<ActualCircle>?` | The `CircleEntityInterface` corresponding to the given ID, or `null` if not found. |

**Example**
```kotlin
val circle = circleManager.getEntity("circle-id-123")
circle?.let {
    // Use the retrieved circle entity
    println("Found circle with radius: ${it.state.radiusMeters}")
}
```

---

### `hasEntity`

Checks if a circle entity with the specified ID is currently being managed.

**Signature**
```kotlin
fun hasEntity(id: String): Boolean
```

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `String` | The unique identifier to check for. |

**Returns**
| Type | Description |
|--------|-------------|
| `Boolean` | Returns `true` if an entity with the given ID exists, `false` otherwise. |

**Example**
```kotlin
if (circleManager.hasEntity("circle-id-456")) {
    println("The manager contains the circle.")
} else {
    println("The circle is not in the manager.")
}
```

---

### `allEntities`

Returns a list of all circle entities currently managed.

**Signature**
```kotlin
fun allEntities(): List<CircleEntityInterface<ActualCircle>>
```

**Returns**
| Type | Description |
|------|-------------|
| `List<CircleEntityInterface<ActualCircle>>` | A `List` containing all `CircleEntityInterface` objects. The list will be empty if no entities are managed. |

**Example**
```kotlin
val allCircles = circleManager.allEntities()
println("Total circles being managed: ${allCircles.size}")
for (circle in allCircles) {
    // Process each circle
}
```

---

### `clear`

Removes all circle entities from the manager, leaving it empty.

**Signature**
```kotlin
fun clear()
```

**Description**
This method is useful for resetting the manager's state and clearing all circles from the map view.

**Example**
```kotlin
// Remove all circles from the manager
circleManager.clear()
assert(circleManager.allEntities().isEmpty())
```

---

### `find`

Finds the top-most, clickable circle entity that contains a given geographic coordinate.

**Signature**
```kotlin
fun find(position: GeoPointInterface): CircleEntityInterface<ActualCircle>?
```

**Description**
This method implements a "hit test" for circles. The search logic is as follows:
1.  Filters all managed circles to find those whose radius contains the specified `position`.
2.  From the filtered list, it only considers circles that are marked as clickable (`entity.state.clickable` is `true`).
3.  If multiple clickable circles are found at the position, it returns the one with the highest `zIndex`. The `zIndex` is determined by `entity.state.zIndex` if set; otherwise, it's calculated based on the circle's center coordinate.

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `position` | `GeoPointInterface` | The geographic coordinate (e.g., from a map tap event) to search at. |

**Returns**
| Type | Description |
|------|-------------|
| `CircleEntityInterface<ActualCircle>?` | The matching `CircleEntityInterface` with the highest `zIndex`, or `null` if no clickable circle is found at the given position. |

**Example**
```kotlin
// A GeoPoint representing a user's tap on the map
val tapPosition = GeoPoint(40.7128, -74.0060) 

val tappedCircle = circleManager.find(tapPosition)

tappedCircle?.let {
    println("User tapped on circle with ID: ${it.state.id}")
} ?: println("No clickable circle was found at this location.")
```