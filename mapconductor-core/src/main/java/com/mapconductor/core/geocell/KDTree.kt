package com.mapconductor.core.geocell

import com.mapconductor.core.Offset
import java.util.PriorityQueue
import kotlin.math.pow
import kotlin.math.sqrt

// KDTree 本体（nearest, k-NN, radius検索対応）

class KDTree(private val points: List<HexCell>) {
    private val root = build(points, 0)

    private class Node(val cell: HexCell, val left: Node?, val right: Node?, val axis: Int)

    private fun build(items: List<HexCell>, depth: Int): Node? {
        if (items.isEmpty()) return null
        val axis = depth % 2
        val sorted = items.sortedBy { if (axis == 0) it.centerXY.x else it.centerXY.y }
        val mid = sorted.size / 2
        return Node(sorted[mid], build(sorted.subList(0, mid), depth + 1), build(sorted.subList(mid + 1, sorted.size), depth + 1), axis)
    }

    fun nearest(query: Offset): HexCell? = nearest(root, query, null, Double.MAX_VALUE)

    private fun nearest(node: Node?, query: Offset, best: HexCell?, bestDist: Double): HexCell? {
        if (node == null) return best
        val axis = node.axis
        val queryVal = if (axis == 0) query.x else query.y
        val nodeVal = if (axis == 0) node.cell.centerXY.x else node.cell.centerXY.y
        val distSq = squaredDistance(query, node.cell.centerXY)

        var newBest = best
        var newBestDist = bestDist
        if (distSq < newBestDist) {
            newBest = node.cell
            newBestDist = distSq
        }

        val (near, far) = if (queryVal < nodeVal) node.left to node.right else node.right to node.left
        newBest = nearest(near, query, newBest, newBestDist)
        val axisDist = (queryVal - nodeVal).pow(2)
        if (axisDist < newBestDist) {
            newBest = nearest(far, query, newBest, newBestDist)
        }
        return newBest
    }

    fun nearestWithDistance(query: Offset): HexCellWithDistance? {
        val cell = nearest(query) ?: return null
        return HexCellWithDistance(cell, distanceMeters(query, cell.centerXY))
    }

    fun nearestKWithDistance(query: Offset, k: Int): List<HexCellWithDistance> {
        val queue = PriorityQueue<Pair<Double, HexCell>>(compareByDescending { it.first })
        nearestK(root, query, k, queue)
        return queue.map { HexCellWithDistance(it.second, sqrt(it.first)) }
    }

    private fun nearestK(node: Node?, query: Offset, k: Int, queue: PriorityQueue<Pair<Double, HexCell>>) {
        if (node == null) return
        val distSq = squaredDistance(query, node.cell.centerXY)
        if (queue.size < k) {
            queue.offer(distSq to node.cell)
        } else if (distSq < queue.peek().first) {
            queue.poll()
            queue.offer(distSq to node.cell)
        }
        val axis = node.axis
        val queryVal = if (axis == 0) query.x else query.y
        val nodeVal = if (axis == 0) node.cell.centerXY.x else node.cell.centerXY.y
        val (near, far) = if (queryVal < nodeVal) node.left to node.right else node.right to node.left
        nearestK(near, query, k, queue)
        val axisDist = (queryVal - nodeVal).pow(2)
        if (queue.size < k || axisDist < queue.peek().first) {
            nearestK(far, query, k, queue)
        }
    }

    fun withinRadiusWithDistance(query: Offset, radius: Double): List<HexCellWithDistance> {
        val radiusSq = radius * radius
        val result = mutableListOf<HexCellWithDistance>()
        withinRadius(root, query, radiusSq, result)
        return result
    }

    private fun withinRadius(node: Node?, query: Offset, radiusSq: Double, result: MutableList<HexCellWithDistance>) {
        if (node == null) return
        val distSq = squaredDistance(query, node.cell.centerXY)
        if (distSq <= radiusSq) {
            result.add(HexCellWithDistance(node.cell, sqrt(distSq)))
        }
        val axis = node.axis
        val queryVal = if (axis == 0) query.x else query.y
        val nodeVal = if (axis == 0) node.cell.centerXY.x else node.cell.centerXY.y
        val (near, far) = if (queryVal < nodeVal) node.left to node.right else node.right to node.left
        withinRadius(near, query, radiusSq, result)
        val axisDist = (queryVal - nodeVal).pow(2)
        if (axisDist <= radiusSq) {
            withinRadius(far, query, radiusSq, result)
        }
    }

    private fun squaredDistance(a: Offset, b: Offset): Double {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return dx * dx + dy * dy
    }

    private fun distanceMeters(a: Offset, b: Offset): Double = sqrt(squaredDistance(a, b))
}
