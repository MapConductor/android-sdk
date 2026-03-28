Of course! Here is the high-quality SDK documentation for the provided `HexCellRegistry` code snippet.

---

# HexCellRegistry<ActualMarker>

## Table of Contents
- [HexCellRegistry<ActualMarker>](#hexcellregistryactualmarker)
  - [Table of Contents](#table-of-contents)
  - [Class: HexCellRegistry<ActualMarker>](#class-hexcellregistryactualmarker)
    - [Constructor](#constructor)
  - [Methods](#methods)
    - [getCell](#getcell)
    - [setPoint](#setpoint)
    - [contains](#contains)
    - [removePoint](#removepoint)
    - [clear](#clear)
    - [findNearest](#findnearest)
    - [findNearestWithDistance](#findnearestwithdistance)
    - [findNearestKWithDistance](#findnearestkwithdistance)
    - [findWithinRadiusWithDistance](#findwithinradiuswithdistance)
    - [all](#all)
    - [getEntryIDsByHexCell](#getentryidsbyhexcell)
    - [metersPerPixel](#metersperpixel)
    - [findWithinPixelRadius](#findwithinpixelradius)
    - [findByIdPrefix](#findbyidprefix)
    - [getStats](#getstats)
  - [Data Class: RegistryStats](#data-class-registrystats)
    - [Properties](#properties)

## Class: HexCellRegistry<ActualMarker>

A thread-safe class for managing a collection of hexagonal grid cells. It provides efficient spatial indexing and querying using an internal KDTree.

The registry maps entities (like map markers) to the hexagonal cells they occupy at a specific zoom level. It is designed for performance-critical applications, featuring lazy rebuilding of its spatial index to optimize write operations. All public methods are thread-safe.

### Constructor

Initializes a new instance of the `HexCellRegistry`.

**Signature**
```kotlin
class HexCellRegistry<ActualMarker>(
    private val geocell: HexGeocellInterface,
    private val zoom: Double,
)
```

**Parameters**

| Parameter | Type                  | Description                                            |
| :-------- | :-------------------- | :----------------------------------------------------- |
| `geocell` | `HexGeocellInterface` | The hexagonal geocell system used for coordinate logic. |
| `zoom`    | `Double`              | The map zoom level this registry is responsible for.   |

<br/>

## Methods

### getCell

Calculates and returns the `HexCell` that would contain a given entity, without adding the entity to the registry. This is a pure calculation method and does not modify the registry's state.

**Signature**
```kotlin
fun getCell(entity: MarkerEntityInterface<ActualMarker>): HexCell
```

**Parameters**

| Parameter | Type                                  | Description                                  |
| :-------- | :------------------------------------ | :------------------------------------------- |
| `entity`  | `MarkerEntityInterface<ActualMarker>` | The entity for which to calculate the cell. |

**Returns**

`HexCell` - The hexagonal cell corresponding to the entity's position.

**Example**
```kotlin
val entity: MarkerEntityInterface<MyMarker> = /* ... get your entity ... */
val correspondingCell = hexRegistry.getCell(entity)
println("Entity belongs in cell: ${correspondingCell.id}")
```

---

### setPoint

Registers a new entity or updates the position of an existing one. If the entity was previously registered in a different cell, it is automatically moved. This operation marks the internal spatial index as needing a rebuild, which will occur on the next spatial query.

**Signature**
```kotlin
fun setPoint(entity: MarkerEntityInterface<ActualMarker>): HexCell
```

**Parameters**

| Parameter | Type                                  | Description                               |
| :-------- | :------------------------------------ | :---------------------------------------- |
| `entity`  | `MarkerEntityInterface<ActualMarker>` | The entity to add or update in the registry. |

**Returns**

`HexCell` - The hexagonal cell where the entity has been placed.

**Example**
```kotlin
val newEntity: MarkerEntityInterface<MyMarker> = /* ... create your entity ... */
val cell = hexRegistry.setPoint(newEntity)
println("Entity ${newEntity.state.id} was added to cell ${cell.id}")
```

---

### contains

Checks if a hexagonal cell with the specified ID exists in the registry.

**Signature**
```kotlin
fun contains(hexId: String): Boolean
```

**Parameters**

| Parameter | Type     | Description                          |
| :-------- | :------- | :----------------------------------- |
| `hexId`   | `String` | The unique ID of the hexagonal cell. |

**Returns**

`Boolean` - `true` if a cell with the given ID is registered, `false` otherwise.

**Example**
```kotlin
if (hexRegistry.contains("8c2a1072b59ffff")) {
    println("Cell exists in the registry.")
}
```

---

### removePoint

Removes an entity from the registry. If the cell containing the entity becomes empty after its removal, the cell itself is also removed from the registry.

**Signature**
```kotlin
fun removePoint(entity: MarkerEntityInterface<ActualMarker>): Boolean
```

**Parameters**

| Parameter | Type                                  | Description                      |
| :-------- | :------------------------------------ | :------------------------------- |
| `entity`  | `MarkerEntityInterface<ActualMarker>` | The entity to remove. |

**Returns**

`Boolean` - `true` if the entity was found and removed, `false` otherwise.

**Example**
```kotlin
val entityToRemove: MarkerEntityInterface<MyMarker> = /* ... get entity to remove ... */
val wasRemoved = hexRegistry.removePoint(entityToRemove)
if (wasRemoved) {
    println("Entity successfully removed.")
}
```

---

### clear

Removes all entities and cells from the registry, effectively resetting it to an empty state. The internal spatial index is also cleared.

**Signature**
```kotlin
fun clear()
```

**Example**
```kotlin
hexRegistry.clear()
println("Registry has been cleared. Total cells: ${hexRegistry.getStats().totalCells}")
// Output: Registry has been cleared. Total cells: 0
```

---

### findNearest

Finds the single nearest registered `HexCell` to a given geographic point. This method may trigger a rebuild of the spatial index if the registry has been modified since the last query.

**Signature**
```kotlin
fun findNearest(point: GeoPointInterface): HexCell?
```

**Parameters**

| Parameter | Type                | Description                               |
| :-------- | :------------------ | :---------------------------------------- |
| `point`   | `GeoPointInterface` | The geographic point to search from. |

**Returns**

`HexCell?` - The nearest `HexCell`, or `null` if the registry is empty.

**Example**
```kotlin
val searchPoint = GeoPoint(40.7128, -74.0060) // New York City
val nearestCell = hexRegistry.findNearest(searchPoint)
nearestCell?.let {
    println("Nearest cell is ${it.id} at ${it.center.latitude}, ${it.center.longitude}")
}
```

---

### findNearestWithDistance

Finds the single nearest registered `HexCell` to a given point and also returns the distance to it. The distance is calculated in the projected coordinate system (e.g., meters).

**Signature**
```kotlin
fun findNearestWithDistance(point: GeoPointInterface): HexCellWithDistance?
```

**Parameters**

| Parameter | Type                | Description                               |
| :-------- | :------------------ | :---------------------------------------- |
| `point`   | `GeoPointInterface` | The geographic point to search from. |

**Returns**

`HexCellWithDistance?` - An object containing the nearest cell and the distance, or `null` if the registry is empty.

**Example**
```kotlin
val searchPoint = GeoPoint(34.0522, -118.2437) // Los Angeles
val result = hexRegistry.findNearestWithDistance(searchPoint)
result?.let {
    println("Nearest cell is ${it.cell.id}, distance: ${it.distance} meters")
}
```

---

### findNearestKWithDistance

Finds the `k` nearest registered `HexCell` objects to a given point, ordered by distance.

**Signature**
```kotlin
fun findNearestKWithDistance(
    point: GeoPointInterface,
    k: Int,
): List<HexCellWithDistance>
```

**Parameters**

| Parameter | Type                | Description                                  |
| :-------- | :------------------ | :------------------------------------------- |
| `point`   | `GeoPointInterface` | The geographic point to search from.      |
| `k`       | `Int`               | The maximum number of nearest cells to find. |

**Returns**

`List<HexCellWithDistance>` - A list of up to `k` cells with their distances, sorted from nearest to farthest. The list will be empty if the registry is empty.

**Example**
```kotlin
val searchPoint = GeoPoint(48.8566, 2.3522) // Paris
val nearestFive = hexRegistry.findNearestKWithDistance(searchPoint, 5)
println("Found ${nearestFive.size} nearby cells:")
nearestFive.forEach {
    println("- Cell ${it.cell.id} at distance ${it.distance}")
}
```

---

### findWithinRadiusWithDistance

Finds all registered `HexCell` objects within a specified radius of a given point. The radius is interpreted in the units of the projected coordinate system (e.g., meters).

**Signature**
```kotlin
fun findWithinRadiusWithDistance(
    point: GeoPointInterface,
    radius: Double,
): List<HexCellWithDistance>
```

**Parameters**

| Parameter | Type                | Description                                                              |
| :-------- | :------------------ | :----------------------------------------------------------------------- |
| `point`   | `GeoPointInterface` | The center point of the search area.                                     |
| `radius`  | `Double`            | The search radius, in projected units (typically meters). |

**Returns**

`List<HexCellWithDistance>` - A list of all cells found within the radius, along with their respective distances from the center point.

**Example**
```kotlin
val searchPoint = GeoPoint(51.5074, -0.1278) // London
val radiusInMeters = 1000.0
val cellsInRadius = hexRegistry.findWithinRadiusWithDistance(searchPoint, radiusInMeters)
println("Found ${cellsInRadius.size} cells within 1km.")
```

---

### all

Returns a complete list of all `HexCell` objects currently in the registry.

**Signature**
```kotlin
fun all(): List<HexCell>
```

**Returns**

`List<HexCell>` - A list containing all registered hexagonal cells.

**Example**
```kotlin
val allRegisteredCells = hexRegistry.all()
println("Total registered cells: ${allRegisteredCells.size}")
```

---

### getEntryIDsByHexCell

Retrieves the set of all entity IDs associated with a specific `HexCell`.

**Signature**
```kotlin
fun getEntryIDsByHexCell(hexCell: HexCell): Set<String>?
```

**Parameters**

| Parameter | Type      | Description                               |
| :-------- | :-------- | :---------------------------------------- |
| `hexCell` | `HexCell` | The cell for which to retrieve entity IDs. |

**Returns**

`Set<String>?` - An immutable set of entity IDs within the given cell, or `null` if the cell is not registered or contains no entities.

**Example**
```kotlin
val cell: HexCell = /* ... get a cell from a query ... */
val entityIds = hexRegistry.getEntryIDsByHexCell(cell)
entityIds?.let {
    println("Entities in cell ${cell.id}: $it")
}
```

---

### metersPerPixel

A utility function to calculate the approximate real-world distance in meters that corresponds to a given number of pixels on a map display.

**Note:** This calculation assumes the `HexGeocellInterface` projection returns coordinates in meters.

**Signature**
```kotlin
fun metersPerPixel(
    position: GeoPointInterface,
    zoom: Double,
    pixels: Double,
    tileSize: Int = 256,
): Double
```

**Parameters**

| Parameter  | Type                | Description                                                              |
| :--------- | :------------------ | :----------------------------------------------------------------------- |
| `position` | `GeoPointInterface` | The geographic location (latitude/longitude) for the calculation.        |
| `zoom`     | `Double`            | The current map zoom level.                                              |
| `pixels`   | `Double`            | The number of pixels for which to calculate the corresponding distance.  |
| `tileSize` | `Int`               | (Optional) The size of the map tiles in pixels. Defaults to `256`.       |

**Returns**

`Double` - The calculated distance in meters.

**Example**
```kotlin
val centerOfScreen = GeoPoint(35.6895, 139.6917) // Tokyo
val currentZoom = 15.0
val distanceFor100Pixels = hexRegistry.metersPerPixel(centerOfScreen, currentZoom, 100.0)
println("At zoom $currentZoom, 100 pixels is approx. $distanceFor100Pixels meters.")
```

---

### findWithinPixelRadius

A convenience method to find all `HexCell` objects within a given pixel radius of a point on the screen. It internally converts the pixel radius to meters using `metersPerPixel` and then performs a spatial search.

**Signature**
```kotlin
fun findWithinPixelRadius(
    position: GeoPointInterface,
    zoom: Double,
    pixels: Double,
    tileSize: Int = 256,
): List<HexCellWithDistance>
```

**Parameters**

| Parameter  | Type                | Description                                                              |
| :--------- | :------------------ | :----------------------------------------------------------------------- |
| `position` | `GeoPointInterface` | The center point of the search area.                                     |
| `zoom`     | `Double`            | The current map zoom level.                                              |
| `pixels`   | `Double`            | The search radius in pixels.                                             |
| `tileSize` | `Int`               | (Optional) The size of the map tiles in pixels. Defaults to `256`.       |

**Returns**

`List<HexCellWithDistance>` - A list of all cells found within the pixel radius, along with their distances.

**Example**
```kotlin
val tapPoint = GeoPoint(41.9028, 12.4964) // Rome
val currentZoom = 16.0
val searchRadiusInPixels = 50.0
val cellsNearTap = hexRegistry.findWithinPixelRadius(tapPoint, currentZoom, searchRadiusInPixels)
println("Found ${cellsNearTap.size} cells within a 50px radius of the tap.")
```

---

### findByIdPrefix

Finds all registered cells whose IDs start with the given prefix. This is useful for querying groups of cells in hierarchical grid systems (e.g., finding all child cells within a parent region).

**Signature**
```kotlin
fun findByIdPrefix(prefix: String): List<HexCell>
```

**Parameters**

| Parameter | Type     | Description                               |
| :-------- | :------- | :---------------------------------------- |
| `prefix`  | `String` | The cell ID prefix to search for. Must not be empty. |

**Returns**

`List<HexCell>` - A list of all `HexCell` objects matching the prefix.

**Example**
```kotlin
// Assuming a hierarchical cell ID system
val regionPrefix = "8a2a107"
val cellsInRegion = hexRegistry.findByIdPrefix(regionPrefix)
println("Found ${cellsInRegion.size} cells in region $regionPrefix.")
```

---

### getStats

Retrieves diagnostic statistics about the current state of the registry. This is useful for debugging and monitoring performance.

**Signature**
```kotlin
fun getStats(): RegistryStats
```

**Returns**

`RegistryStats` - A data object containing statistics such as total cell and entity counts.

**Example**
```kotlin
val stats = hexRegistry.getStats()
println("Registry Stats:")
println("- Total Cells: ${stats.totalCells}")
println("- Total Entries: ${stats.totalEntries}")
println("- KDTree Built: ${stats.kdTreeBuilt}")
println("- Needs Rebuild: ${stats.needsRebuild}")
```

<br/>

## Data Class: RegistryStats

A data class that holds statistics about the `HexCellRegistry`'s state.

**Signature**
```kotlin
data class RegistryStats(
    val totalCells: Int,
    val totalEntries: Int,
    val kdTreeBuilt: Boolean,
    val needsRebuild: Boolean,
)
```

### Properties

| Property       | Type      | Description                                                              |
| :------------- | :-------- | :----------------------------------------------------------------------- |
| `totalCells`   | `Int`     | The total number of unique hexagonal cells currently in the registry.    |
| `totalEntries` | `Int`     | The total number of entities (e.g., markers) registered across all cells. |
| `kdTreeBuilt`  | `Boolean` | `true` if the KDTree spatial index has been built, `false` otherwise.    |
| `needsRebuild` | `Boolean` | `true` if the registry has been modified and the KDTree needs to be rebuilt on the next spatial query. |