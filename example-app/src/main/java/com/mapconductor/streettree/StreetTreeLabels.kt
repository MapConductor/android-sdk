package com.mapconductor.streettree

import com.mapconductor.core.features.GeoRectBounds
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan

/**
 * 画面内の街路樹のうち、どれにどちら向きのラベルを付けるか。
 *
 * - 向きは**道路ごとに 1 つ**。道路の木の並びが横向きなら上か下、縦向きなら右か左
 *   （道路に直交する向き）。同じ通りのラベルが揃って並ぶ。
 * - 2 つの候補のうち、置けるラベルが多い方を選ぶ。木の多い道路から決める。
 *   ただし前回その道路で選んだ向き（[previousSides]）は、もう一方の 2/3 以上置けるなら
 *   そのまま使う。見えている範囲が変わるたびに数の多い方へ乗り換えると、パンするたびに
 *   ラベルが上下や左右で入れ替わる。
 * - すでに置いたラベルか、ほかの木の点の中心を覆うラベルは付けない（その木は点のまま）。
 *   ラベルは点の後に描かれるので、端が隣の点に少しかかるのは許す。点の箱ごと避けると、
 *   木の間隔が詰まった斜めの通りでは隣の点がどの向きの箱にもかかり、ラベルが 1 つも置けない。
 * - 上下に置くラベルは縦書き（[StreetTreeIcons.isVertical]）なので、縦書きの寸法で測る。
 *
 * 座標はカメラのズームでの dp。
 */
object StreetTreeLabels {
    /** [trees] の添字 -> 向き。 */
    fun choose(
        trees: List<StreetTree>,
        bounds: GeoRectBounds,
        zoom: Double,
        /** ラベルのアイコンに掛かる倍率（タイルの iconScaleCallback と同じ値）。 */
        scale: Float,
        dotSizeDp: Float,
        labelSizeDp: (String, Boolean) -> Pair<Float, Float>,
        /** 前回の道路名 -> 向き。 */
        previousSides: Map<String, LabelSide> = emptyMap(),
    ): Map<Int, LabelSide> {
        val southWest = bounds.southWest ?: return emptyMap()
        val northEast = bounds.northEast ?: return emptyMap()
        val world = 256.0 * 2.0.pow(zoom)

        fun x(longitude: Double) = (longitude + 180.0) / 360.0 * world

        fun y(latitude: Double): Double {
            val phi = latitude * PI / 180.0
            return (1.0 - ln(tan(phi) + 1.0 / cos(phi)) / PI) / 2.0 * world
        }

        // 道路ごとの、画面内の木
        val byRoad = HashMap<String, MutableList<Int>>()
        for ((index, tree) in trees.withIndex()) {
            val p = tree.position
            if (p.latitude < southWest.latitude || p.latitude > northEast.latitude) continue
            if (p.longitude < southWest.longitude || p.longitude > northEast.longitude) continue
            byRoad.getOrPut(tree.roadName) { ArrayList() }.add(index)
        }

        val placed = PlacedRects()
        val sides = HashMap<Int, LabelSide>()
        val radius = dotSizeDp * scale / 2.0
        val gap = StreetTreeIcons.LABEL_GAP_DP * scale.toDouble()

        // 画面内のすべての点の中心。ラベルがほかの木の点の中心を覆わないかを見る
        val dots = PlacedRects()
        for (members in byRoad.values) {
            for (index in members) {
                val cx = x(trees[index].position.longitude)
                val cy = y(trees[index].position.latitude)
                dots.add(Rect(cx - 0.5, cy - 0.5, cx + 0.5, cy + 0.5))
            }
        }

        for ((road, members) in byRoad.entries.sortedByDescending { it.value.size }) {
            val xs = DoubleArray(members.size) { x(trees[members[it]].position.longitude) }
            val ys = DoubleArray(members.size) { y(trees[members[it]].position.latitude) }

            // 並びの向き（主成分）
            val mx = xs.average()
            val my = ys.average()
            var sxx = 0.0
            var syy = 0.0
            var sxy = 0.0
            for (i in xs.indices) {
                val dx = xs[i] - mx
                val dy = ys[i] - my
                sxx += dx * dx
                syy += dy * dy
                sxy += dx * dy
            }
            val angle = 0.5 * atan2(2 * sxy, sxx - syy)
            val horizontal = abs(cos(angle)) >= abs(sin(angle))
            val candidates =
                if (horizontal) listOf(LabelSide.TOP, LabelSide.BOTTOM) else listOf(LabelSide.RIGHT, LabelSide.LEFT)

            // 道路に沿って並べてから置いていく
            val order = xs.indices.sortedBy { xs[it] * cos(angle) + ys[it] * sin(angle) }

            fun rect(
                i: Int,
                side: LabelSide,
            ): Rect {
                val (w0, h0) = labelSizeDp(trees[members[i]].species, StreetTreeIcons.isVertical(side))
                val w = w0 * scale
                val h = h0 * scale
                val cx = xs[i]
                val cy = ys[i]
                return when (side) {
                    LabelSide.RIGHT -> Rect(cx + radius + gap, cy - h / 2, cx + radius + gap + w, cy + h / 2)
                    LabelSide.LEFT -> Rect(cx - radius - gap - w, cy - h / 2, cx - radius - gap, cy + h / 2)
                    LabelSide.TOP -> Rect(cx - w / 2, cy - radius - gap - h, cx + w / 2, cy - radius - gap)
                    LabelSide.BOTTOM -> Rect(cx - w / 2, cy + radius + gap, cx + w / 2, cy + radius + gap + h)
                }
            }

            fun attempt(side: LabelSide): List<Pair<Int, Rect>> {
                val own = PlacedRects()
                val accepted = ArrayList<Pair<Int, Rect>>()
                for (i in order) {
                    val r = rect(i, side)
                    if (placed.overlaps(r) || own.overlaps(r) || dots.overlaps(r)) continue
                    own.add(r)
                    accepted.add(i to r)
                }
                return accepted
            }

            val attempts = candidates.map { it to attempt(it) }
            val most = attempts.maxBy { it.second.size }
            val kept = attempts.firstOrNull { it.first == previousSides[road] }
            val best = if (kept != null && kept.second.size * 3 >= most.second.size * 2) kept else most
            for ((i, r) in best.second) {
                placed.add(r)
                sides[members[i]] = best.first
            }
        }
        return sides
    }

    private class Rect(
        val left: Double,
        val top: Double,
        val right: Double,
        val bottom: Double,
    ) {
        fun overlaps(other: Rect) = left < other.right && other.left < right && top < other.bottom && other.top < bottom
    }

    /** 置いたラベルの箱。64dp のマス目に振り分けて、近くだけを見る。 */
    private class PlacedRects {
        private val cells = HashMap<Long, MutableList<Rect>>()

        private fun cellsOf(r: Rect): List<Long> {
            val out = ArrayList<Long>(4)
            for (cx in floor(r.left / CELL).toLong()..floor(r.right / CELL).toLong()) {
                for (cy in floor(r.top / CELL).toLong()..floor(r.bottom / CELL).toLong()) {
                    out.add((cx shl 32) xor (cy and 0xFFFFFFFFL))
                }
            }
            return out
        }

        fun overlaps(r: Rect): Boolean = cellsOf(r).any { key -> cells[key]?.any { it.overlaps(r) } == true }

        fun add(r: Rect) {
            for (key in cellsOf(r)) cells.getOrPut(key) { ArrayList() }.add(r)
        }

        companion object {
            const val CELL = 64.0
        }
    }
}
