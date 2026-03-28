Of course! Here is the high-quality SDK documentation for the provided `MarkerManager` class.

# MarkerManager SDK Documentation

## `MarkerManagerStats` Data Class

Provides memory usage statistics for a `MarkerManager` instance, useful for debugging and performance optimization.

### Signature
```kotlin
data class MarkerManagerStats(
    val entityCount: Int,
    val hasSpatialIndex: Boolean,
    val spatialIndexInitialized: Boolean,
    val estimatedMemoryKB: Long,
)
```

### Parameters

| Parameter                 | Type    | Description                                                                 |
| ------------------------- | ------- | --------------------------------------------------------------------------- |
| `entityCount`             | `Int`   | The total number of marker entities currently stored in the manager.        |
| `hasSpatialIndex`         | `Boolean` | `true` if the spatial index has been created, `false` otherwise.            |
| `spatialIndexInitialized` | `Boolean` | An alias for `hasSpatialIndex`.                                             |
| `estimatedMemoryKB`       | `Long`  | A rough estimation of the memory consumed by the manager in kilobytes (KB). |

---

## `MarkerManager<ActualMarker>` Class

A generic, thread-safe manager for collections of marker entities. It provides efficient storage and spatial querying capabilities.

### Description

`MarkerManager` is designed to handle a large number of markers on a map. It employs a performance optimization strategy: for small datasets (fewer than `minMarkerCount`), it uses simple brute-force searches. For larger datasets, it automatically creates and uses a spatial index (`HexCellRegistry`) to perform fast spatial queries like finding the nearest marker or markers within a specific area.

This lazy initialization of the spatial index saves memory for use cases with a small number of markers. All operations that modify or access the internal collections are thread-safe.

The class is generic, where `ActualMarker` represents the concrete marker type of the specific map provider (e.g., `GoogleMap.Marker`, `Mapbox.Marker`).

### Signature

```kotlin
open class MarkerManager<ActualMarker>(
    protected val geocell: HexGeocellInterface,
    val minMarkerCount: Int,
)
```

### Parameters

| Parameter        | Type                  | Description                                                                                                                            |
| ---------------- | --------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `geocell`        | `HexGeocellInterface` | The hexagonal geocelling system to use for the spatial index.                                                                          |
| `minMarkerCount` | `Int`                 | The threshold for creating the spatial index. Spatial queries will use the index only if the number of entities exceeds this value. |

---

## Methods

### `defaultManager`
A static factory method to create a `MarkerManager` instance with sensible default settings.

#### Signature
```kotlin
companion object {
    fun <ActualMarker> defaultManager(
        geocell: HexGeocellInterface? = null,
        minMarkerCount: Int = 2000,
    ): MarkerManager<ActualMarker>
}
```

#### Parameters
| Parameter        | Type                  | Description                                                                                                                            |
| ---------------- | --------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `geocell`        | `HexGeocellInterface?` | The geocelling system to use. If `null`, `HexGeocell.defaultGeocell()` is used.                                                        |
| `minMarkerCount` | `Int`                 | The entity count threshold to trigger the use of the spatial index. Defaults to `2000`.                                                |

#### Returns
A new `MarkerManager<ActualMarker>` instance.

#### Example
```kotlin
// Create a manager with default settings
val markerManager = MarkerManager.defaultManager<MyMapMarker>()
```

### `lock`
Acquires a write lock on the manager, preventing other threads from reading or writing. This is useful for performing bulk operations atomically.

> **Note:** You must call `unlock()` to release the lock. Failing to do so will result in a deadlock.

#### Signature
```kotlin
fun lock()
```

### `unlock`
Releases the write lock acquired by `lock()`.

#### Signature
```kotlin
fun unlock()
```

### `registerEntity`
Adds a new marker entity to the manager. The operation is thread-safe. If the spatial index has been initialized, the new entity is also added to it.

#### Signature
```kotlin
open fun registerEntity(entity: MarkerEntityInterface<ActualMarker>)
```

#### Parameters
| Parameter | Type                                | Description                               |
| --------- | ----------------------------------- | ----------------------------------------- |
| `entity`  | `MarkerEntityInterface<ActualMarker>` | The marker entity to add to the manager. |

### `updateEntity`
Updates an existing entity in the manager. If the entity does not exist, it will be added. This operation is thread-safe.

#### Signature
```kotlin
open fun updateEntity(entity: MarkerEntityInterface<ActualMarker>)
```

#### Parameters
| Parameter | Type                                | Description                               |
| --------- | ----------------------------------- | ----------------------------------------- |
| `entity`  | `MarkerEntityInterface<ActualMarker>` | The marker entity to update or register. |

### `removeEntity`
Removes a marker entity from the manager by its unique ID.

#### Signature
```kotlin
open fun removeEntity(id: String): MarkerEntityInterface<ActualMarker>?
```

#### Parameters
| Parameter | Type     | Description                          |
| --------- | -------- | ------------------------------------ |
| `id`      | `String` | The unique ID of the entity to remove. |

#### Returns
The removed `MarkerEntityInterface<ActualMarker>` if it was found, otherwise `null`.

### `getEntity`
Retrieves a marker entity by its unique ID.

#### Signature
```kotlin
open fun getEntity(id: String): MarkerEntityInterface<ActualMarker>?
```

#### Parameters
| Parameter | Type     | Description                           |
| --------- | -------- | ------------------------------------- |
| `id`      | `String` | The unique ID of the entity to retrieve. |

#### Returns
The `MarkerEntityInterface<ActualMarker>` if found, otherwise `null`.

### `hasEntity`
Checks if an entity with the specified ID exists in the manager.

#### Signature
```kotlin
open fun hasEntity(id: String): Boolean
```

#### Parameters
| Parameter | Type     | Description                               |
| --------- | -------- | ----------------------------------------- |
| `id`      | `String` | The unique ID of the entity to check for. |

#### Returns
`true` if the entity exists, `false` otherwise.

### `findNearest`
Finds the marker entity closest to a given geographic position. This method automatically chooses the most efficient search strategy.

- **Spatial Index:** If the number of entities is greater than `minMarkerCount`, a spatial index is used for a fast search.
- **Brute Force:** If the number of entities is small, a simple and efficient brute-force search is performed.

#### Signature
```kotlin
open fun findNearest(position: GeoPointInterface): MarkerEntityInterface<ActualMarker>?
```

#### Parameters
| Parameter  | Type                | Description                               |
| ---------- | ------------------- | ----------------------------------------- |
| `position` | `GeoPointInterface` | The geographic point to search around. |

#### Returns
The nearest `MarkerEntityInterface<ActualMarker>` or `null` if the manager is empty.

### `findMarkersInBounds`
Finds all marker entities that fall within a given geographic bounding box. This method also uses the spatial index for large datasets and brute-force for smaller ones to ensure optimal performance.

#### Signature
```kotlin
fun findMarkersInBounds(
    bounds: com.mapconductor.core.features.GeoRectBounds,
): List<MarkerEntityInterface<ActualMarker>>
```

#### Parameters
| Parameter | Type                                         | Description                               |
| --------- | -------------------------------------------- | ----------------------------------------- |
| `bounds`  | `com.mapconductor.core.features.GeoRectBounds` | The rectangular geographic area to search within. |

#### Returns
A `List` of all `MarkerEntityInterface<ActualMarker>` instances found inside the bounds.

### `allEntities`
Returns a snapshot of all marker entities currently in the manager.

#### Signature
```kotlin
open fun allEntities(): List<MarkerEntityInterface<ActualMarker>>
```

#### Returns
A `List` containing all `MarkerEntityInterface<ActualMarker>` instances.

### `clear`
Removes all entities from the manager and clears the spatial index if it exists.

#### Signature
```kotlin
open fun clear()
```

### `destroy`
Releases all resources held by the `MarkerManager`, including clearing all entities and destroying the spatial index.

> **CRITICAL:** This method **must** be called when the `MarkerManager` is no longer needed to prevent memory leaks. After `destroy()` is called, any other method call will throw an `IllegalStateException`.

#### Signature
```kotlin
open fun destroy()
```

### `getMemoryStats`
Retrieves statistics about the current memory usage and state of the manager.

#### Signature
```kotlin
fun getMemoryStats(): MarkerManagerStats
```

#### Returns
A `MarkerManagerStats` object containing details about entity count, spatial index state, and estimated memory usage.

### `metersPerPixel`
A utility function to calculate the approximate distance in meters that one pixel represents on the map at a given latitude, zoom level, and screen density.

#### Signature
```kotlin
open fun metersPerPixel(
    position: GeoPointInterface,
    zoom: Double,
    pixels: Double,
    tileSize: Int = 256,
): Double
```

#### Parameters
| Parameter  | Type                | Description                                                              |
| ---------- | ------------------- | ------------------------------------------------------------------------ |
| `position` | `GeoPointInterface` | The geographic position (latitude is used for cosine correction).        |
| `zoom`     | `Double`            | The current zoom level of the map.                                       |
| `pixels`   | `Double`            | The number of pixels for which to calculate the distance.                |
| `tileSize` | `Int`               | The size of the map tiles in pixels (usually 256 or 512). Defaults to 256. |

#### Returns
The calculated distance in meters.

### `findByIdPrefix`
Searches the spatial index for `HexCell`s that match a given ID prefix.

> **Note:** This method will only return results if the spatial index has been initialized (i.e., when the entity count has exceeded `minMarkerCount`). Otherwise, it returns an empty list.

#### Signature
```kotlin
open fun findByIdPrefix(prefix: String): List<HexCell>
```

#### Parameters
| Parameter | Type     | Description                               |
| --------- | -------- | ----------------------------------------- |
| `prefix`  | `String` | The `HexCell` ID prefix to search for. |

#### Returns
A `List` of matching `HexCell`s, or an empty list if the index is not active or no matches are found.