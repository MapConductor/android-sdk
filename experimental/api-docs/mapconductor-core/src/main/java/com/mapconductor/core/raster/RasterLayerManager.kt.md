# RasterLayerManager

The `RasterLayerManager` is a generic class responsible for managing a collection of raster layer entities. It provides a simple, in-memory key-value store for registering, retrieving, and removing entities using their unique string identifiers. This manager implements the `RasterLayerManagerInterface`.

The generic type parameter `ActualLayer` represents the concrete type of the underlying layer object used in a specific map rendering system.

**Class Signature**
```kotlin
class RasterLayerManager<ActualLayer> : RasterLayerManagerInterface<ActualLayer>
```

---

## Methods

### registerEntity
Adds a new raster layer entity to the manager. If an entity with the same ID already exists, it will be replaced.

**Signature**
```kotlin
fun registerEntity(entity: RasterLayerEntityInterface<ActualLayer>)
```

**Description**
This method uses the entity's ID as the key to store it in the manager's internal collection.

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `entity` | `RasterLayerEntityInterface<ActualLayer>` | The raster layer entity to register. The entity's ID is used as the key. |

**Returns**
`Unit` - This method does not return a value.

**Example**
```kotlin
// Assuming 'myEntity' is an instance of a class implementing RasterLayerEntityInterface
// and the manager is initialized.
val manager = RasterLayerManager<Any>()
manager.registerEntity(myEntity)
```

---

### removeEntity
Removes a raster layer entity from the manager based on its unique ID.

**Signature**
```kotlin
fun removeEntity(id: String): RasterLayerEntityInterface<ActualLayer>?
```

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `String` | The unique ID of the entity to remove. |

**Returns**
The removed `RasterLayerEntityInterface<ActualLayer>` if it was found, or `null` if no entity with the specified ID exists.

**Example**
```kotlin
val manager = RasterLayerManager<Any>()
// ... register an entity with id "raster-01"
val removedEntity = manager.removeEntity("raster-01")
if (removedEntity != null) {
    println("Entity raster-01 was removed.")
} else {
    println("Entity raster-01 not found.")
}
```

---

### getEntity
Retrieves a raster layer entity by its unique ID.

**Signature**
```kotlin
fun getEntity(id: String): RasterLayerEntityInterface<ActualLayer>?
```

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `String` | The unique ID of the entity to retrieve. |

**Returns**
The `RasterLayerEntityInterface<ActualLayer>` corresponding to the given ID, or `null` if the entity is not found.

**Example**
```kotlin
val manager = RasterLayerManager<Any>()
// ... register an entity with id "raster-01"
val entity = manager.getEntity("raster-01")
entity?.let {
    println("Found entity: ${it.state.id}")
}
```

---

### hasEntity
Checks if an entity with the specified ID is registered with the manager.

**Signature**
```kotlin
fun hasEntity(id: String): Boolean
```

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `String` | The unique ID of the entity to check for. |

**Returns**
`true` if an entity with the given ID exists, `false` otherwise.

**Example**
```kotlin
val manager = RasterLayerManager<Any>()
// ... register an entity with id "raster-01"
if (manager.hasEntity("raster-01")) {
    println("Manager contains entity with ID raster-01.")
}
```

---

### allEntities
Retrieves a list of all raster layer entities currently registered with the manager.

**Signature**
```kotlin
fun allEntities(): List<RasterLayerEntityInterface<ActualLayer>>
```

**Returns**
A `List` containing all registered `RasterLayerEntityInterface<ActualLayer>` objects. The list will be empty if no entities are registered.

**Example**
```kotlin
val manager = RasterLayerManager<Any>()
// ... register multiple entities
val all = manager.allEntities()
println("Total entities: ${all.size}")
for (entity in all) {
    println("Entity ID: ${entity.state.id}")
}
```

---

### clear
Removes all raster layer entities from the manager, leaving it empty.

**Signature**
```kotlin
fun clear()
```

**Returns**
`Unit` - This method does not return a value.

**Example**
```kotlin
val manager = RasterLayerManager<Any>()
// ... register multiple entities
println("Entities before clear: ${manager.allEntities().size}") // e.g., prints "Entities before clear: 5"
manager.clear()
println("Entities after clear: ${manager.allEntities().size}") // prints "Entities after clear: 0"
```

---

### find
Finds a raster layer entity at a specific geographic position.

**Signature**
```kotlin
fun find(position: GeoPointInterface): RasterLayerEntityInterface<ActualLayer>?
```

**Description**
This method is designed to perform a spatial query to find an entity at the given coordinates.

**Note:** The current implementation is a stub and always returns `null`. This method is intended for future functionality.

**Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| `position` | `GeoPointInterface` | The geographic coordinate to search at. |

**Returns**
Currently, this method always returns `null`. In a future implementation, it would return the found `RasterLayerEntityInterface<ActualLayer>` or `null` if no entity is found at the specified position.

**Example**
```kotlin
val manager = RasterLayerManager<Any>()
val somePosition: GeoPointInterface = // ... create a GeoPointInterface instance
val foundEntity = manager.find(somePosition) // foundEntity will be null with the current implementation
```