package com.mapconductor.core.geocell

import androidx.compose.ui.geometry.Offset
import java.util.PriorityQueue
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * K-D Tree implementation for efficient spatial queries on hex cells
 * Supports nearest neighbor, k-NN, and radius searches
 */
class KDTree(
    points: List<HexCell>,
) {
    private val root = if (points.isNotEmpty()) build(points, 0) else null

    private class Node(
        val cell: HexCell,
        val left: Node?,
        val right: Node?,
        val axis: Int, // 0 for x-axis, 1 for y-axis
    )

    /**
     * Build the K-D tree recursively
     */
    private fun build(
        items: List<HexCell>,
        depth: Int,
    ): Node? {
        if (items.isEmpty()) return null

        val axis = depth % 2
        val sorted =
            items.sortedBy {
                if (axis == 0) it.centerXY.x else it.centerXY.y
            }
        val mid = sorted.size / 2

        return Node(
            cell = sorted[mid],
            left = build(sorted.subList(0, mid), depth + 1),
            right = build(sorted.subList(mid + 1, sorted.size), depth + 1),
            axis = axis,
        )
    }

    /**
     * Find the nearest hex cell to a query point
     */
    fun nearest(query: Offset): HexCell? = root?.let { nearest(it, query, null, Double.MAX_VALUE) }

    /**
     * Recursive nearest neighbor search
     */
    private fun nearest(
        node: Node,
        query: Offset,
        best: HexCell?,
        bestDistSq: Double,
    ): HexCell? {
        val axis = node.axis
        val queryVal = if (axis == 0) query.x else query.y
        val nodeVal = if (axis == 0) node.cell.centerXY.x else node.cell.centerXY.y

        val distSq = squaredDistance(query, node.cell.centerXY).toDouble()

        var currentBest = best
        var currentBestDistSq = bestDistSq

        // Update best if current node is closer
        if (distSq < currentBestDistSq) {
            currentBest = node.cell
            currentBestDistSq = distSq
        }

        // Determine which subtree to search first
        val (nearChild, farChild) =
            if (queryVal < nodeVal) {
                node.left to node.right
            } else {
                node.right to node.left
            }

        // Search near subtree
        nearChild?.let { near ->
            currentBest = nearest(near, query, currentBest, currentBestDistSq)
            currentBestDistSq = currentBest?.let {
                squaredDistance(query, it.centerXY).toDouble()
            } ?: currentBestDistSq
        }

        // Search far subtree if it might contain closer points
        farChild?.let { far ->
            val axisDist = (queryVal - nodeVal).pow(2).toDouble()
            if (axisDist < currentBestDistSq) {
                currentBest = nearest(far, query, currentBest, currentBestDistSq)
            }
        }

        return currentBest
    }

    /**
     * Find the nearest hex cell with distance
     */
    fun nearestWithDistance(query: Offset): HexCellWithDistance? {
        val cell = nearest(query) ?: return null
        val distance = distance(query, cell.centerXY).toDouble()
        return HexCellWithDistance(cell, distance)
    }

    /**
     * Find k nearest hex cells with distances
     */
    fun nearestKWithDistance(
        query: Offset,
        k: Int,
    ): List<HexCellWithDistance> {
        require(k > 0) { "k must be positive" }

        if (root == null) return emptyList()

        val queue =
            PriorityQueue<Pair<Double, HexCell>>(
                compareByDescending { it.first },
            )

        nearestK(root, query, k, queue)

        return queue
            .map { (distSq, cell) ->
                HexCellWithDistance(cell, sqrt(distSq))
            }.sortedBy { it.distanceMeters }
    }

    /**
     * Recursive k-nearest neighbor search
     */
    private fun nearestK(
        node: Node,
        query: Offset,
        k: Int,
        queue: PriorityQueue<Pair<Double, HexCell>>,
    ) {
        val distSq = squaredDistance(query, node.cell.centerXY).toDouble()

        if (queue.size < k) {
            queue.offer(distSq to node.cell)
        } else if (distSq < queue.peek()!!.first) {
            queue.poll()
            queue.offer(distSq to node.cell)
        }

        val axis = node.axis
        val queryVal = if (axis == 0) query.x else query.y
        val nodeVal = if (axis == 0) node.cell.centerXY.x else node.cell.centerXY.y

        val (nearChild, farChild) =
            if (queryVal < nodeVal) {
                node.left to node.right
            } else {
                node.right to node.left
            }

        // Search near subtree
        nearChild?.let { nearestK(it, query, k, queue) }

        // Search far subtree if it might contain closer points
        farChild?.let {
            val axisDist = (queryVal - nodeVal).pow(2).toDouble()
            if (queue.size < k || axisDist < queue.peek()!!.first) {
                nearestK(it, query, k, queue)
            }
        }
    }

    /**
     * Find all hex cells within a radius with distances
     */
    fun withinRadiusWithDistance(
        query: Offset,
        radius: Double,
    ): List<HexCellWithDistance> {
        require(radius >= 0) { "Radius must be non-negative" }

        if (root == null) return emptyList()

        val radiusSq = radius * radius
        val result = mutableListOf<HexCellWithDistance>()
        withinRadius(root, query, radiusSq, result)
        return result.sortedBy { it.distanceMeters }
    }

    /**
     * Recursive radius search
     */
    private fun withinRadius(
        node: Node,
        query: Offset,
        radiusSq: Double,
        result: MutableList<HexCellWithDistance>,
    ) {
        val distSq = squaredDistance(query, node.cell.centerXY).toDouble()

        if (distSq <= radiusSq) {
            result.add(HexCellWithDistance(node.cell, sqrt(distSq)))
        }

        val axis = node.axis
        val queryVal = if (axis == 0) query.x else query.y
        val nodeVal = if (axis == 0) node.cell.centerXY.x else node.cell.centerXY.y

        val (nearChild, farChild) =
            if (queryVal < nodeVal) {
                node.left to node.right
            } else {
                node.right to node.left
            }

        // Search near subtree
        nearChild?.let { withinRadius(it, query, radiusSq, result) }

        // Search far subtree if it intersects with query circle
        farChild?.let {
            val axisDist = (queryVal - nodeVal).pow(2).toDouble()
            if (axisDist <= radiusSq) {
                withinRadius(it, query, radiusSq, result)
            }
        }
    }

    /**
     * Calculate squared Euclidean distance between two points
     */
    private fun squaredDistance(
        a: Offset,
        b: Offset,
    ): Float {
        val deltaX = a.x - b.x
        val deltaY = a.y - b.y
        return deltaX * deltaX + deltaY * deltaY
    }

    /**
     * Calculate Euclidean distance between two points
     */
    private fun distance(
        a: Offset,
        b: Offset,
    ): Float = sqrt(squaredDistance(a, b))

    /**
     * Get statistics about the tree structure
     */
    fun getStats(): KDTreeStats =
        KDTreeStats(
            nodeCount = countNodes(root),
            maxDepth = maxDepth(root),
            isEmpty = root == null,
        )

    private fun countNodes(node: Node?): Int =
        if (node == null) 0 else 1 + countNodes(node.left) + countNodes(node.right)

    private fun maxDepth(node: Node?): Int =
        if (node == null) 0 else 1 + maxOf(maxDepth(node.left), maxDepth(node.right))
}

/**
 * Statistics about the K-D tree structure
 */
data class KDTreeStats(
    val nodeCount: Int,
    val maxDepth: Int,
    val isEmpty: Boolean,
)
