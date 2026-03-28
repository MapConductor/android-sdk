# HexGeocellInterface

The `HexGeocellInterface` defines the contract for a hexagonal geocelling system. It provides a comprehensive set of methods for converting between geographic coordinates (latitude/longitude) and hexagonal grid coordinates. This interface is fundamental for spatial indexing, data aggregation, and performing grid-based analysis on a map.

Implementations of this interface manage the projection logic and grid calculations required to partition a map into a discrete hexagonal grid at various zoom levels.

## Properties

### projection
The map projection used for converting between geographic and world coordinates.

**Signature**
```kotlin
val projection: ProjectionInterface
```

### baseHexSideLength
The side length, in projected units, of a base hexagon at a reference zoom level. This value is used as a basis for scaling hexagons at different zoom levels.

**Signature**
```kotlin
val baseHexSideLength: Int
```

---

## Methods

### latLngToHexCoord
Converts a geographic coordinate (latitude/longitude) to its corresponding hexagonal grid coordinate at a given zoom level.

**Signature**
```kotlin
fun latLngToHexCoord(
    position: GeoPointInterface,
    zoom: Double,
): HexCoord
```

**Parameters**

| Parameter  | Type                | Description                                    |
|------------|---------------------|------------------------------------------------|
| `position` | `GeoPointInterface` | The geographic point to convert.               |
| `zoom`     | `Double`            | The map zoom level for the conversion.         |

**Returns**

`HexCoord` - The hexagonal coordinate corresponding to the input position.

---

### latLngToHexCell
Converts a geographic coordinate to a `HexCell` object, which encapsulates the hexagonal coordinate and its associated zoom level.

**Signature**
```kotlin
fun latLngToHexCell(
    position: GeoPointInterface,
    zoom: Double,
): HexCell
```

**Parameters**

| Parameter  | Type                | Description                                    |
|------------|---------------------|------------------------------------------------|
| `position` | `GeoPointInterface` | The geographic point to convert.               |
| `zoom`     | `Double`            | The map zoom level for the conversion.         |

**Returns**

`HexCell` - The `HexCell` containing the input position.

---

### hexToLatLngCenter
Calculates the geographic coordinate (latitude/longitude) of the center of a given hexagonal cell.

**Signature**
```kotlin
fun hexToLatLngCenter(
    coord: HexCoord,
    latHint: Double,
    zoom: Double,
): GeoPointInterface
```

**Parameters**

| Parameter | Type       | Description                                                                    |
|-----------|------------|--------------------------------------------------------------------------------|
| `coord`   | `HexCoord` | The hexagonal coordinate to convert.                                           |
| `latHint` | `Double`   | A latitude hint to improve the accuracy of the inverse projection.             |
| `zoom`    | `Double`   | The map zoom level at which the `HexCoord` was originally calculated.          |

**Returns**

`GeoPointInterface` - The geographic coordinate of the hex cell's center.

---

### hexToCellId
Generates a unique, stable string identifier for a hexagonal cell based on its coordinate and zoom level. This ID is suitable for use as a key in hashmaps or for database storage.

**Signature**
```kotlin
fun hexToCellId(
    coord: HexCoord,
    zoom: Double,
): String
```

**Parameters**

| Parameter | Type       | Description                        |
|-----------|------------|------------------------------------|
| `coord`   | `HexCoord` | The hexagonal coordinate of the cell. |
| `zoom`    | `Double`   | The zoom level of the cell.        |

**Returns**

`String` - A unique string identifier for the cell.

---

### hexToPolygonLatLng
Calculates the geographic coordinates of the six vertices that form the boundary of a hexagonal cell. The vertices are returned in a list, which can be used to draw the hexagon on a map.

**Signature**
```kotlin
fun hexToPolygonLatLng(
    coord: HexCoord,
    latHint: Double,
    zoom: Double,
): List<GeoPointInterface>
```

**Parameters**

| Parameter | Type       | Description                                                                    |
|-----------|------------|--------------------------------------------------------------------------------|
| `coord`   | `HexCoord` | The hexagonal coordinate of the cell.                                          |
| `latHint` | `Double`   | A latitude hint for improving the accuracy of the inverse projection.          |
| `zoom`    | `Double`   | The map zoom level of the cell.                                                |

**Returns**

`List<GeoPointInterface>` - A list of six `GeoPointInterface` objects representing the vertices of the hexagon.

---

### enclosingCellOf
Determines the single hexagonal cell at a specified zoom level that completely encloses all the given points. This is useful for finding a common parent cell for a cluster of markers.

**Signature**
```kotlin
fun enclosingCellOf(
    points: List<MarkerState>,
    zoom: Double,
): HexCell
```

**Parameters**

| Parameter | Type                | Description                                                              |
|-----------|---------------------|--------------------------------------------------------------------------|
| `points`  | `List<MarkerState>` | A list of marker states, each containing a geographic position.          |
| `zoom`    | `Double`            | The target zoom level for the enclosing cell.                            |

**Returns**

`HexCell` - The single `HexCell` that encloses all input points.

---

### hexCellsForPointsWithId
Converts a list of points (represented as `MarkerState` objects) into a set of unique hexagonal cells at a given zoom level. Each cell in the returned set is an `IdentifiedHexCell`, which includes its coordinate, zoom level, and a unique ID.

**Signature**
```kotlin
fun hexCellsForPointsWithId(
    points: List<MarkerState>,
    zoom: Double,
): Set<IdentifiedHexCell>
```

**Parameters**

| Parameter | Type                | Description                                                              |
|-----------|---------------------|--------------------------------------------------------------------------|
| `points`  | `List<MarkerState>` | A list of marker states to be placed into hex cells.                     |
| `zoom`    | `Double`            | The map zoom level to use for creating the cells.                        |

**Returns**

`Set<IdentifiedHexCell>` - A set of unique `IdentifiedHexCell` objects corresponding to the locations of the input points.

---

### hexDistance
Calculates the grid distance between two hexagonal coordinates. The distance is defined as the minimum number of cells one must traverse to get from cell `a` to cell `b`.

**Signature**
```kotlin
fun hexDistance(
    a: HexCoord,
    b: HexCoord,
): Int
```

**Parameters**

| Parameter | Type       | Description                      |
|-----------|------------|----------------------------------|
| `a`       | `HexCoord` | The starting hexagonal coordinate. |
| `b`       | `HexCoord` | The ending hexagonal coordinate.   |

**Returns**

`Int` - The distance in number of cells.

---

### hexRange
Finds all hexagonal coordinates within a specified radius from a central coordinate, forming a larger hexagonal area.

**Signature**
```kotlin
fun hexRange(
    center: HexCoord,
    radius: Int,
): List<HexCoord>
```

**Parameters**

| Parameter | Type       | Description                                                                                             |
|-----------|------------|---------------------------------------------------------------------------------------------------------|
| `center`  | `HexCoord` | The central hexagonal coordinate.                                                                       |
| `radius`  | `Int`      | The radius in number of cells. A radius of `0` returns just the center cell. A radius of `1` returns the center and its 6 immediate neighbors. |

**Returns**

`List<HexCoord>` - A list of all `HexCoord` objects within the specified range, including the center.

---

## Example

The following conceptual example demonstrates a common workflow using an implementation of `HexGeocellInterface`.

```kotlin
// Assume 'hexGeocell' is an initialized instance of a class
// that implements HexGeocellInterface.
// Assume 'GeoPoint' is a class that implements GeoPointInterface.

val zoomLevel = 10.0
val myPosition = GeoPoint(latitude = 40.7128, longitude = -74.0060) // New York City

// 1. Convert a geographic position to a hex coordinate
val hexCoord: HexCoord = hexGeocell.latLngToHexCoord(myPosition, zoomLevel)
println("Hex coordinate for my position: $hexCoord")

// 2. Find all neighboring hex cells within a radius of 1
val nearbyHexes: List<HexCoord> = hexGeocell.hexRange(hexCoord, 1)
println("Found ${nearbyHexes.size} hexes within radius 1.") // Should print 7

// 3. Get the center lat/lng of the first neighbor
val firstNeighborCoord = nearbyHexes.first { it != hexCoord }
val neighborCenter: GeoPointInterface = hexGeocell.hexToLatLngCenter(
    coord = firstNeighborCoord,
    latHint = myPosition.latitude,
    zoom = zoomLevel
)
println("Center of a neighboring cell: ${neighborCenter.latitude}, ${neighborCenter.longitude}")

// 4. Get the polygon vertices for the original hex cell to draw it on a map
val polygonVertices: List<GeoPointInterface> = hexGeocell.hexToPolygonLatLng(
    coord = hexCoord,
    latHint = myPosition.latitude,
    zoom = zoomLevel
)
println("Polygon has ${polygonVertices.size} vertices.") // Should print 6
```