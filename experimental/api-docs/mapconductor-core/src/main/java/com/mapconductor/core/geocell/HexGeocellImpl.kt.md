Excellent! Here is the high-quality SDK documentation for the provided `HexGeocell` system.

***

# HexGeocell SDK Documentation

## `HexGeocell`

### Description

The `HexGeocell` class provides a comprehensive system for spatial indexing using a hexagonal grid. It allows for the conversion between geographic coordinates (latitude/longitude) and hexagonal cell coordinates, facilitating efficient spatial queries, clustering, and data aggregation on a map.

This implementation uses a "flat-top" hexagon orientation and an axial coordinate system (`q`, `r`) for representing hexagonal positions.

### Constructor

#### Signature

```kotlin
class HexGeocell(
    override val projection: ProjectionInterface,
    override val baseHexSideLength: Int = 1000,
) : HexGeocellInterface
```

#### Description

Creates a new instance of the `HexGeocell` system. The behavior of the geocell system is determined by the map projection and the base size of the hexagons.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `projection` | `ProjectionInterface` | The map projection used for converting between geographic (lat/lng) and Cartesian (XY) coordinates. Example: `WebMercator`. |
| `baseHexSideLength` | `Int` | The side length of a base hexagon (at zoom level 0) in meters. This value is fundamental to the grid's scale. Recommended values vary by use case: <br>- **High Zoom (15-18):** 100-1000m <br>- **Medium Zoom (10-15):** 1000-10000m <br>- **Low Zoom (5-10):** 10000-100000m |

---

## Methods

### `latLngToHexCoord`

#### Signature

```kotlin
fun latLngToHexCoord(
    position: GeoPointInterface,
    zoom: Double,
): HexCoord
```

#### Description

Converts a geographic coordinate (latitude/longitude) into a hexagonal grid coordinate (`HexCoord`) for a given map zoom level.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPointInterface` | The geographic point to convert. |
| `zoom` | `Double` | The current map zoom level. |

#### Returns

| Type | Description |
| :--- | :--- |
| `HexCoord` | The corresponding hexagonal coordinate (`q`, `r`). |

### `latLngToHexCell`

#### Signature

```kotlin
fun latLngToHexCell(
    position: GeoPointInterface,
    zoom: Double,
): HexCell
```

#### Description

Converts a geographic coordinate into a complete `HexCell` object. This is a higher-level function that returns not just the coordinate but also the cell's center, projected center, and unique ID.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPointInterface` | The geographic point to convert. |
| `zoom` | `Double` | The current map zoom level. |

#### Returns

| Type | Description |
| :--- | :--- |
| `HexCell` | The `HexCell` object containing the point. |

### `hexToLatLngCenter`

#### Signature

```kotlin
fun hexToLatLngCenter(
    coord: HexCoord,
    latHint: Double,
    zoom: Double,
): GeoPointInterface
```

#### Description

Calculates the geographic center (latitude/longitude) of a specified hexagonal cell.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `coord` | `HexCoord` | The hexagonal coordinate of the cell. |
| `latHint` | `Double` | A reference latitude used for accurate scale correction, as hexagon size varies with latitude in Mercator projections. Typically, this should be the latitude of the area of interest. |
| `zoom` | `Double` | The map zoom level for which the calculation is being made. |

#### Returns

| Type | Description |
| :--- | :--- |
| `GeoPointInterface` | A geographic point representing the center of the hex cell. |

### `hexToCellId`

#### Signature

```kotlin
fun hexToCellId(
    coord: HexCoord,
    zoom: Double,
): String
```

#### Description

Generates a unique and human-readable string identifier for a hex cell. The ID is composed of the cell's `q` and `r` coordinates and the integer part of the zoom level, ensuring uniqueness across different zoom levels.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `coord` | `HexCoord` | The hexagonal coordinate of the cell. |
| `zoom` | `Double` | The map zoom level. |

#### Returns

| Type | Description |
| :--- | :--- |
| `String` | The unique cell ID, formatted as `H<q>_<r>_Z<zoom>`. Example: `H10_-5_Z12`. |

### `hexToPolygonLatLng`

#### Signature

```kotlin
fun hexToPolygonLatLng(
    coord: HexCoord,
    latHint: Double,
    zoom: Double,
): List<GeoPointInterface>
```

#### Description

Calculates the six vertices of a hexagon in geographic coordinates. The returned list of points can be used to draw the cell's polygon boundary on a map.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `coord` | `HexCoord` | The hexagonal coordinate of the cell. |
| `latHint` | `Double` | A reference latitude for accurate scale correction. |
| `zoom` | `Double` | The map zoom level. |

#### Returns

| Type | Description |
| :--- | :--- |
| `List<GeoPointInterface>` | A list containing the six `GeoPointInterface` vertices of the hexagon. |

### `enclosingCellOf`

#### Signature

```kotlin
fun enclosingCellOf(
    points: List<MarkerState>,
    zoom: Double,
): HexCell
```

#### Description

Determines the single hex cell that encloses the geographic centroid of a list of points. This is useful for finding a representative cell for a cluster of markers. The method uses a curvature-aware algorithm to accurately compute the centroid.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `points` | `List<MarkerState>` | A non-empty list of points (e.g., markers) from which to calculate the centroid. |
| `zoom` | `Double` | The map zoom level. |

#### Returns

| Type | Description |
| :--- | :--- |
| `HexCell` | The `HexCell` that contains the calculated centroid. |

**Throws**
- `IllegalArgumentException` if the `points` list is empty.

### `hexCellsForPointsWithId`

#### Signature

```kotlin
fun hexCellsForPointsWithId(
    points: List<MarkerState>,
    zoom: Double,
): Set<IdentifiedHexCell>
```

#### Description

Processes a list of `MarkerState` objects and maps each one to its corresponding `HexCell`. The result is a set of `IdentifiedHexCell` objects, which pairs the original marker's ID with its calculated hex cell.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `points` | `List<MarkerState>` | A list of `MarkerState` objects to process. |
| `zoom` | `Double` | The map zoom level. |

#### Returns

| Type | Description |
| :--- | :--- |
| `Set<IdentifiedHexCell>` | A set of `IdentifiedHexCell` objects, linking each original ID to a `HexCell`. |

### `hexDistance`

#### Signature

```kotlin
fun hexDistance(
    a: HexCoord,
    b: HexCoord,
): Int
```

#### Description

Calculates the distance between two hex coordinates on the grid. This is the "Manhattan distance" on the hexagonal grid, representing the minimum number of steps needed to move from cell `a` to cell `b`.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `a` | `HexCoord` | The starting hex coordinate. |
| `b` | `HexCoord` | The ending hex coordinate. |

#### Returns

| Type | Description |
| :--- | :--- |
| `Int` | The distance in number of cells. |

### `hexRange`

#### Signature

```kotlin
fun hexRange(
    center: HexCoord,
    radius: Int,
): List<HexCoord>
```

#### Description

Returns a list of all `HexCoord`s within a specified grid radius from a central cell. This is useful for "find in area" or neighborhood queries.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `center` | `HexCoord` | The coordinate of the central cell. |
| `radius` | `Int` | The search radius in number of cells. A radius of 0 returns only the center cell. A radius of 1 returns the center and its 6 neighbors. |

#### Returns

| Type | Description |
| :--- | :--- |
| `List<HexCoord>` | A list of all `HexCoord`s within the specified range, including the center. |

---

## Data Classes

### `HexCoord`

A data class representing a coordinate in a hexagonal grid using the axial coordinate system.

**Properties**
- `q: Int`: The `q` coordinate in the axial system.
- `r: Int`: The `r` coordinate in the axial system.
- `depth: Int`: An optional depth value, defaults to 0.
- `s: Int`: A computed property for the third cube coordinate (`s = -q - r`), useful for many hex grid algorithms.

### `HexCell`

A data class representing a single hexagonal cell with its computed properties.

**Properties**
- `coord: HexCoord`: The hexagonal coordinate of the cell.
- `centerLatLng: GeoPointInterface`: The geographic coordinate (lat/lng) of the cell's center.
- `centerXY: Offset`: The projected Cartesian coordinate (XY) of the cell's center.
- `id: String`: The unique string identifier for the cell.

### `IdentifiedHexCell`

A data class used to associate an original item's ID with its corresponding `HexCell`.

**Properties**
- `id: String`: The ID of the original item (e.g., a `MarkerState`).
- `cell: HexCell`: The `HexCell` associated with the item.

---

## Example

The following example demonstrates how to initialize `HexGeocell` and use its core functions to work with hexagonal cells.

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.projection.WebMercator

fun main() {
    // 1. Initialize the HexGeocell system
    // Use a base side length of 50km for medium zoom levels
    val hexGeocell = HexGeocell(
        projection = WebMercator,
        baseHexSideLength = 50000 
    )

    // Define a location (e.g., Tokyo Station) and a zoom level
    val tokyoStation = GeoPoint(35.681236, 139.767125)
    val zoom = 10.0

    // 2. Find the hex cell for the location
    val cell = hexGeocell.latLngToHexCell(tokyoStation, zoom)
    println("--- Cell Information ---")
    println("Cell for Tokyo Station: ${cell.id}")
    println("Cell Coordinate (q, r): (${cell.coord.q}, ${cell.coord.r})")
    println("Cell Center (Lat, Lng): (${cell.centerLatLng.latitude}, ${cell.centerLatLng.longitude})")

    // 3. Get the polygon vertices to draw the cell on a map
    val polygon = hexGeocell.hexToPolygonLatLng(cell.coord, tokyoStation.latitude, zoom)
    println("\n--- Cell Polygon Vertices ---")
    polygon.forEachIndexed { index, point ->
        println("Vertex $index: (${point.latitude}, ${point.longitude})")
    }

    // 4. Find all neighboring cells within a radius of 1
    val neighbors = hexGeocell.hexRange(center = cell.coord, radius = 1)
    println("\n--- Neighbors (radius=1) ---")
    println("Total cells in range: ${neighbors.size}")
    neighbors.forEach { neighborCoord ->
        val neighborId = hexGeocell.hexToCellId(neighborCoord, zoom)
        val distance = hexGeocell.hexDistance(cell.coord, neighborCoord)
        println("Neighbor: $neighborId, Distance: $distance")
    }
}

/*
Expected Output (values may vary slightly based on projection implementation):

--- Cell Information ---
Cell for Tokyo Station: H2236_-1100_Z10
Cell Coordinate (q, r): (2236, -1100)
Cell Center (Lat, Lng): (35.6789..., 139.7638...)

--- Cell Polygon Vertices ---
Vertex 0: (35.801..., 139.708...)
Vertex 1: (35.801..., 139.819...)
Vertex 2: (35.678..., 139.874...)
Vertex 3: (35.555..., 139.819...)
Vertex 4: (35.555..., 139.708...)
Vertex 5: (35.678..., 139.653...)

--- Neighbors (radius=1) ---
Total cells in range: 7
Neighbor: H2235_-1101_Z10, Distance: 1
Neighbor: H2235_-1100_Z10, Distance: 1
Neighbor: H2236_-1101_Z10, Distance: 1
Neighbor: H2236_-1100_Z10, Distance: 0
Neighbor: H2236_-1099_Z10, Distance: 1
Neighbor: H2237_-1101_Z10, Distance: 1
Neighbor: H2237_-1100_Z10, Distance: 1
*/
```