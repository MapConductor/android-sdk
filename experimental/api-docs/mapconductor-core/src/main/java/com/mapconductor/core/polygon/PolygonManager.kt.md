# PolygonManager&lt;ActualPolygon&gt;

## Description

The `PolygonManager` class provides a comprehensive solution for managing a collection of polygon entities on a map. It offers standard CRUD (Create, Read, Update, Delete) operations and a powerful spatial query method, `find`, to determine which polygon contains a specific geographic coordinate.

This manager is designed for performance and accuracy, with built-in support for:
- **Z-indexing**: Prioritizes polygons with a higher `zIndex` when multiple polygons overlap.
- **Geodesic correctness**: Accurately performs hit-testing on geodesic polygons by densifying their edges to follow the curvature of the Earth.
- **Complex polygons**: Correctly handles polygons with holes and those that cross the antimeridian.

The class is generic, allowing it to work with any underlying platform-specific polygon implementation (e.g., Google Maps Polygons, Mapbox Polygons) by specifying the `ActualPolygon` type.

## Type Parameters

| Name | Description |
|---|---|
| `<ActualPolygon>` | The concrete type of the underlying polygon object managed by the entities (e.g., `com.google.android.gms.maps.model.Polygon`). |

## Constructor

### PolygonManager()

Creates a new, empty instance of the `PolygonManager`.

#### Signature
```kotlin
PolygonManager<ActualPolygon>()
```

## Methods

### registerEntity
Registers a polygon entity with the manager. If an entity with the same ID already exists, it will be overwritten.

#### Signature
```kotlin
fun registerEntity(entity: PolygonEntityInterface<ActualPolygon>)
```

#### Parameters
| Parameter | Type | Description |
|---|---|---|
| `entity` | `PolygonEntityInterface<ActualPolygon>` | The polygon entity to register. The entity's ID is used as the key for storage. |

---

### removeEntity
Removes a polygon entity from the manager based on its unique ID.

#### Signature
```kotlin
fun removeEntity(id: String): PolygonEntityInterface<ActualPolygon>?
```

#### Parameters
| Parameter | Type | Description |
|---|---|---|
| `id` | `String` | The unique identifier of the polygon entity to remove. |

#### Returns
`PolygonEntityInterface<ActualPolygon>?`: The removed entity instance, or `null` if no entity with the specified ID was found.

---

### getEntity
Retrieves a polygon entity by its unique ID.

#### Signature
```kotlin
fun getEntity(id: String): PolygonEntityInterface<ActualPolygon>?
```

#### Parameters
| Parameter | Type | Description |
|---|---|---|
| `id` | `String` | The unique identifier of the polygon entity to retrieve. |

#### Returns
`PolygonEntityInterface<ActualPolygon>?`: The entity instance corresponding to the given ID, or `null` if not found.

---

### hasEntity
Checks if a polygon entity with the specified ID is registered with the manager.

#### Signature
```kotlin
fun hasEntity(id: String): Boolean
```

#### Parameters
| Parameter | Type | Description |
|---|---|---|
| `id` | `String` | The unique identifier to check for. |

#### Returns
`Boolean`: Returns `true` if an entity with the given ID exists, `false` otherwise.

---

### allEntities
Returns a list of all polygon entities currently registered with the manager.

#### Signature
```kotlin
fun allEntities(): List<PolygonEntityInterface<ActualPolygon>>
```

#### Returns
`List<PolygonEntityInterface<ActualPolygon>>`: A list containing all registered entity instances. The order of entities in the list is not guaranteed.

---

### clear
Removes all polygon entities from the manager, leaving it in an empty state.

#### Signature
```kotlin
fun clear()
```

---

### find
Finds the top-most polygon entity that contains the given geographic coordinate.

The search algorithm iterates through all registered polygons, sorted by their `zIndex` in descending order. This ensures that if multiple polygons overlap at the given position, the one with the highest `zIndex` (visually "on top") is returned.

The method uses the winding number algorithm for a robust point-in-polygon test. It correctly handles complex cases, including:
- **Holes:** A point located within a polygon's hole is considered outside the polygon.
- **Antimeridian:** Polygons that cross the 180° longitude line are handled correctly.
- **Geodesic Edges:** If an entity's `geodesic` property is `true`, its edges are densified to approximate great-circle paths on the globe, leading to a more accurate hit test.
- **On-Edge Points:** A point lying on the boundary of a polygon is considered to be inside it.

#### Signature
```kotlin
fun find(position: GeoPointInterface): PolygonEntityInterface<ActualPolygon>?
```

#### Parameters
| Parameter | Type | Description |
|---|---|---|
| `position` | `GeoPointInterface` | The geographic coordinate (latitude and longitude) to test. |

#### Returns
`PolygonEntityInterface<ActualPolygon>?`: The entity of the containing polygon with the highest `zIndex`, or `null` if the point is not inside any registered polygon.

## Example

```kotlin
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonEntityInterface

// Helper classes and interfaces for a self-contained example.
// In a real application, these would be part of your project structure.
data class GeoPoint(override val latitude: Double, override val longitude: Double) : GeoPointInterface

interface PolygonStateInterface<T> {
    val id: String
    val points: List<GeoPointInterface>
    val holes: List<List<GeoPointInterface>>
    val geodesic: Boolean
    val zIndex: Int
}

data class MyPolygonState(
    override val id: String,
    override val points: List<GeoPointInterface>,
    override val holes: List<List<GeoPointInterface>> = emptyList(),
    override val geodesic: Boolean = false,
    override val zIndex: Int = 0
) : PolygonStateInterface<Unit>

class MyPolygonEntity(override val state: MyPolygonState) : PolygonEntityInterface<Unit> {
    override val actualPolygon: Unit? = null // Represents a platform-specific polygon object
}

fun main() {
    // 1. Initialize the manager. The type parameter <Unit> is used for this example.
    val polygonManager = PolygonManager<Unit>()

    // 2. Define and register a polygon entity (a square around Denver, CO).
    val denverZonePoints = listOf(
        GeoPoint(40.0, -105.2),
        GeoPoint(40.0, -104.8),
        GeoPoint(39.5, -104.8),
        GeoPoint(39.5, -105.2)
    )
    val denverZoneState = MyPolygonState(id = "denver-zone", points = denverZonePoints, zIndex = 1)
    val denverZoneEntity = MyPolygonEntity(denverZoneState)

    polygonManager.registerEntity(denverZoneEntity)
    println("Registered entity with ID: ${denverZoneEntity.state.id}")

    // 3. Use the find() method to check for points.
    val pointInside = GeoPoint(39.7, -105.0) // A point inside the square
    val pointOutside = GeoPoint(41.0, -105.0) // A point north of the square

    val foundEntity = polygonManager.find(pointInside)
    println("Find inside point: Found entity with ID '${foundEntity?.state?.id}'")

    val notFoundEntity = polygonManager.find(pointOutside)
    println("Find outside point: Found entity with ID '${notFoundEntity?.state?.id}'")

    // 4. Use other management methods.
    val retrievedEntity = polygonManager.getEntity("denver-zone")
    println("Get entity 'denver-zone': Success = ${retrievedEntity != null}")

    val hasEntity = polygonManager.hasEntity("denver-zone")
    println("Manager has 'denver-zone': $hasEntity")

    // 5. Remove the entity and verify.
    polygonManager.removeEntity("denver-zone")
    val hasEntityAfterRemove = polygonManager.hasEntity("denver-zone")
    println("Manager has 'denver-zone' after removal: $hasEntityAfterRemove")
}

// Expected Output:
// Registered entity with ID: denver-zone
// Find inside point: Found entity with ID 'denver-zone'
// Find outside point: Found entity with ID 'null'
// Get entity 'denver-zone': Success = true
// Manager has 'denver-zone': true
// Manager has 'denver-zone' after removal: false
```