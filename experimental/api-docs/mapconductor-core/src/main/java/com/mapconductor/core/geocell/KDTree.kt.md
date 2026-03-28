Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# KDTree

## `KDTree` Class

A K-D Tree (K-dimensional tree) implementation optimized for efficient 2D spatial queries on `HexCell` objects. This class partitions a set of points (the centers of hex cells) in a 2-dimensional space, allowing for fast searches.

It supports the following query types:
*   **Nearest Neighbor:** Find the single closest point to a given location.
*   **K-Nearest Neighbors (k-NN):** Find the `k` closest points to a given location.
*   **Radius Search:** Find all points within a certain distance of a given location.

### Signature

```kotlin
class KDTree(points: List<HexCell>)
```

### Description

Constructs a `KDTree` by recursively partitioning the provided list of `HexCell` points. The tree is balanced by alternating the splitting axis (X and Y) at each level of depth. If the input list is empty, an empty tree is created.

### Parameters

| Parameter | Type             | Description                               |
| :-------- | :--------------- | :---------------------------------------- |
| `points`  | `List<HexCell>`  | A list of `HexCell` objects to build the tree from. |

### Example

```kotlin
// Assuming HexCell and Offset are defined as:
// data class HexCell(val id: String, val centerXY: Offset)
// import androidx.compose.ui.geometry.Offset

val hexCells = listOf(
    HexCell("A", Offset(2f, 3f)),
    HexCell("B", Offset(5f, 4f)),
    HexCell("C", Offset(9f, 6f)),
    HexCell("D", Offset(4f, 7f)),
    HexCell("E", Offset(8f, 1f)),
    HexCell("F", Offset(7f, 2f))
)

// Create a new KDTree instance
val kdTree = KDTree(hexCells)
```

---

## Methods

### `nearest`

Finds the single nearest `HexCell` to a given query point.

#### Signature

```kotlin
fun nearest(query: Offset): HexCell?
```

#### Description

Performs a nearest neighbor search to find the `HexCell` in the tree whose center is closest to the specified `query` point.

#### Parameters

| Parameter | Type     | Description                                  |
| :-------- | :------- | :------------------------------------------- |
| `query`   | `Offset` | The coordinate point to search from.         |

#### Returns

The nearest `HexCell` object, or `null` if the tree is empty.

#### Example

```kotlin
val queryPoint = Offset(5f, 5f)
val nearestCell = kdTree.nearest(queryPoint)

if (nearestCell != null) {
    println("Nearest cell is ${nearestCell.id} at ${nearestCell.centerXY}")
    // Expected output: Nearest cell is B at Offset(5.0, 4.0)
}
```

---

### `nearestWithDistance`

Finds the single nearest `HexCell` to a query point and returns it along with the calculated distance.

#### Signature

```kotlin
fun nearestWithDistance(query: Offset): HexCellWithDistance?
```

#### Description

This method extends the `nearest` search by packaging the resulting `HexCell` and its Euclidean distance from the `query` point into a `HexCellWithDistance` object.

#### Parameters

| Parameter | Type     | Description                                  |
| :-------- | :------- | :------------------------------------------- |
| `query`   | `Offset` | The coordinate point to search from.         |

#### Returns

A `HexCellWithDistance` object containing the nearest cell and the distance, or `null` if the tree is empty.

#### Example

```kotlin
// Assuming HexCellWithDistance is defined as:
// data class HexCellWithDistance(val cell: HexCell, val distanceMeters: Double)

val queryPoint = Offset(5.1f, 4.2f)
val result = kdTree.nearestWithDistance(queryPoint)

result?.let {
    println("Nearest cell is ${it.cell.id} with a distance of ${it.distanceMeters}")
    // Expected output: Nearest cell is B with a distance of 0.2236...
}
```

---

### `nearestKWithDistance`

Finds the `k` nearest `HexCell`s to a query point, along with their respective distances.

#### Signature

```kotlin
fun nearestKWithDistance(query: Offset, k: Int): List<HexCellWithDistance>
```

#### Description

Performs a k-nearest neighbors (k-NN) search. It returns a list of the `k` closest `HexCell`s to the `query` point, sorted by distance in ascending order.

#### Parameters

| Parameter | Type     | Description                                  |
| :-------- | :------- | :------------------------------------------- |
| `query`   | `Offset` | The coordinate point to search from.         |
| `k`       | `Int`    | The number of nearest neighbors to find. Must be a positive integer. |

#### Returns

A `List<HexCellWithDistance>` containing the `k` nearest cells and their distances, sorted from nearest to farthest. Returns an empty list if the tree is empty.

#### Example

```kotlin
val queryPoint = Offset(6f, 3f)
val k = 3
val nearestThree = kdTree.nearestKWithDistance(queryPoint, k)

println("Found ${nearestThree.size} neighbors:")
nearestThree.forEach {
    println("- Cell ${it.cell.id} at distance ${it.distanceMeters}")
}
// Expected output:
// Found 3 neighbors:
// - Cell F at distance 1.414...
// - Cell B at distance 1.414...
// - Cell A at distance 4.0...
```

---

### `withinRadiusWithDistance`

Finds all `HexCell`s within a specified radius of a query point.

#### Signature

```kotlin
fun withinRadiusWithDistance(query: Offset, radius: Double): List<HexCellWithDistance>
```

#### Description

Performs a radius search to find all `HexCell`s whose centers fall within the given `radius` of the `query` point. The results include the distance to each cell and are sorted by distance.

#### Parameters

| Parameter | Type     | Description                                  |
| :-------- | :------- | :------------------------------------------- |
| `query`   | `Offset` | The center point of the search circle.       |
| `radius`  | `Double` | The radius of the search area. Must be non-negative. |

#### Returns

A `List<HexCellWithDistance>` of all cells found within the radius, sorted by distance. Returns an empty list if no cells are within the radius or if the tree is empty.

#### Example

```kotlin
val queryPoint = Offset(7.5f, 2.5f)
val searchRadius = 2.0
val cellsInRadius = kdTree.withinRadiusWithDistance(queryPoint, searchRadius)

println("Found ${cellsInRadius.size} cells within a radius of $searchRadius:")
cellsInRadius.forEach {
    println("- Cell ${it.cell.id} at distance ${it.distanceMeters}")
}
// Expected output:
// Found 2 cells within a radius of 2.0:
// - Cell F at distance 0.707...
// - Cell E at distance 1.581...
```

---

### `getStats`

Retrieves statistics about the internal structure of the `KDTree`.

#### Signature

```kotlin
fun getStats(): KDTreeStats
```

#### Description

Provides metrics about the constructed tree, which can be useful for debugging or performance analysis. It returns the total number of nodes, the maximum depth of the tree, and a flag indicating if the tree is empty.

#### Returns

A `KDTreeStats` object containing the tree's structural metrics.

#### Example

```kotlin
val stats = kdTree.getStats()

println("Tree is empty: ${stats.isEmpty}")
println("Node count: ${stats.nodeCount}")
println("Max depth: ${stats.maxDepth}")

// Example output for the tree created earlier:
// Tree is empty: false
// Node count: 6
// Max depth: 4
```

---

## `KDTreeStats` Data Class

A data class that holds statistics about a `KDTree`'s structure.

### Signature

```kotlin
data class KDTreeStats(
    val nodeCount: Int,
    val maxDepth: Int,
    val isEmpty: Boolean
)
```

### Properties

| Property    | Type      | Description                                       |
| :---------- | :-------- | :------------------------------------------------ |
| `nodeCount` | `Int`     | The total number of nodes (cells) in the tree.    |
| `maxDepth`  | `Int`     | The maximum depth from the root to any leaf node. |
| `isEmpty`   | `Boolean` | `true` if the tree contains no nodes, otherwise `false`. |