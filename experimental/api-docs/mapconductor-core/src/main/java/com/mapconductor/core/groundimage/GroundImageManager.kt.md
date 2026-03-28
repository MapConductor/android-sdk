# GroundImageManager

The `GroundImageManager` class is a concrete implementation of the `GroundImageManagerInterface`. It provides a comprehensive solution for managing a collection of ground image entities on a map. It handles the lifecycle of ground images, including their registration, retrieval, removal, and querying.

This manager uses a map to store entities, keyed by their unique string identifiers, allowing for efficient lookups.

## Type Parameters

| Name              | Description                                                              |
| ----------------- | ------------------------------------------------------------------------ |
| `ActualGroundImage` | A generic type representing the platform-specific ground image object. |

---

## Methods

### registerEntity

Adds a new ground image entity to the manager. If an entity with the same ID already exists, it will be replaced.

**Signature**
```kotlin
fun registerEntity(entity: GroundImageEntityInterface<ActualGroundImage>)
```

**Parameters**

| Parameter | Type                                                | Description                               |
| --------- | --------------------------------------------------- | ----------------------------------------- |
| `entity`  | `GroundImageEntityInterface<ActualGroundImage>` | The ground image entity to be registered. |

**Example**
```kotlin
val manager = GroundImageManager<Any>()
val groundImageEntity = // ... create or obtain a GroundImageEntityInterface instance
manager.registerEntity(groundImageEntity)
```

---

### removeEntity

Removes a ground image entity from the manager using its unique ID.

**Signature**
```kotlin
fun removeEntity(id: String): GroundImageEntityInterface<ActualGroundImage>?
```

**Parameters**

| Parameter | Type     | Description                                    |
| --------- | -------- | ---------------------------------------------- |
| `id`      | `String` | The unique identifier of the entity to remove. |

**Returns**

| Type                                                  | Description                                                              |
| ----------------------------------------------------- | ------------------------------------------------------------------------ |
| `GroundImageEntityInterface<ActualGroundImage>?` | The removed entity if it was found, or `null` if no entity with the given ID exists. |

**Example**
```kotlin
val removedEntity = manager.removeEntity("ground-image-id-123")
if (removedEntity != null) {
    println("Successfully removed entity.")
} else {
    println("Entity not found.")
}
```

---

### getEntity

Retrieves a specific ground image entity from the manager by its unique ID.

**Signature**
```kotlin
fun getEntity(id: String): GroundImageEntityInterface<ActualGroundImage>?
```

**Parameters**

| Parameter | Type     | Description                                      |
| --------- | -------- | ------------------------------------------------ |
| `id`      | `String` | The unique identifier of the entity to retrieve. |

**Returns**

| Type                                                  | Description                                                              |
| ----------------------------------------------------- | ------------------------------------------------------------------------ |
| `GroundImageEntityInterface<ActualGroundImage>?` | The entity corresponding to the given ID, or `null` if it is not found. |

**Example**
```kotlin
val entity = manager.getEntity("ground-image-id-123")
entity?.let {
    // Do something with the entity
    println("Found entity with ID: ${it.state.id}")
}
```

---

### hasEntity

Checks if an entity with the specified ID is currently registered in the manager.

**Signature**
```kotlin
fun hasEntity(id: String): Boolean
```

**Parameters**

| Parameter | Type     | Description                                    |
| --------- | -------- | ---------------------------------------------- |
| `id`      | `String` | The unique identifier to check for existence. |

**Returns**

| Type      | Description                                      |
| --------- | ------------------------------------------------ |
| `Boolean` | `true` if an entity with the specified ID exists, `false` otherwise. |

**Example**
```kotlin
if (manager.hasEntity("ground-image-id-123")) {
    println("The manager contains this entity.")
} else {
    println("The manager does not contain this entity.")
}
```

---

### allEntities

Retrieves a list of all ground image entities currently managed.

**Signature**
```kotlin
fun allEntities(): List<GroundImageEntityInterface<ActualGroundImage>>
```

**Returns**

| Type                                                          | Description                                                                                             |
| ------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `List<GroundImageEntityInterface<ActualGroundImage>>` | A `List` containing all registered ground image entities. The list is a snapshot; modifying it will not affect the manager's internal collection. |

**Example**
```kotlin
val allImages = manager.allEntities()
println("Total ground images: ${allImages.size}")
allImages.forEach { entity ->
    println("Entity ID: ${entity.state.id}")
}
```

---

### clear

Removes all ground image entities from the manager, leaving it empty.

**Signature**
```kotlin
fun clear()
```

**Example**
```kotlin
manager.registerEntity(someEntity)
println("Entities before clear: ${manager.allEntities().size}") // > 0
manager.clear()
println("Entities after clear: ${manager.allEntities().size}") // 0
```

---

### find

Finds the first ground image entity whose geographical bounds contain a given geographical position. The order in which entities are checked is not guaranteed.

**Signature**
```kotlin
fun find(position: GeoPointInterface): GroundImageEntityInterface<ActualGroundImage>?
```

**Parameters**

| Parameter  | Type                | Description                                                              |
| ---------- | ------------------- | ------------------------------------------------------------------------ |
| `position` | `GeoPointInterface` | The geographical point used to search for a containing ground image. |

**Returns**

| Type                                                  | Description                                                                                             |
| ----------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `GroundImageEntityInterface<ActualGroundImage>?` | The first matching ground image entity found, or `null` if no entity's bounds contain the specified position. |

**Example**
```kotlin
val someGeoPoint = // ... create a GeoPointInterface instance
val foundEntity = manager.find(someGeoPoint)
foundEntity?.let {
    println("Found an entity at the given position: ${it.state.id}")
}
```