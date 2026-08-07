package com.mapconductor.kml

/**
 * フィーチャーの矩形を粗い格子へ入れた索引。
 *
 * タイル 1 枚ごとに全フィーチャーの矩形を見ると、数万件では描画より
 * 探す方が重くなる。格子のセルにフィーチャー番号を入れておき、
 * タイルにかかるセルだけを辿る。
 *
 * android-geojson-layer の `GeoJSONSpatialIndex.kt` と同じ作り。
 */
internal class KMLSpatialIndex(
    private val grid: Array<MutableList<Int>>,
    private val featureCount: Int,
) {
    fun query(
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
    ): List<Int> {
        val cx0 = (x1 * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
        val cx1 = (x2 * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
        val cy0 = (y1 * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
        val cy1 = (y2 * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
        // BitSet uses 1 bit per feature instead of ~32 bytes per entry in HashSet,
        // which prevents OOM when many features fall into the same tile.
        val seen = java.util.BitSet(featureCount)
        val result = ArrayList<Int>()
        for (cy in cy0..cy1) {
            for (cx in cx0..cx1) {
                for (idx in grid[cy * GRID_SIZE + cx]) {
                    if (!seen.get(idx)) {
                        seen.set(idx)
                        result.add(idx)
                    }
                }
            }
        }
        return result
    }

    companion object {
        const val GRID_SIZE = 64

        /** 索引を引く方が高くつく件数では作らない。 */
        const val BUILD_THRESHOLD = 256

        fun build(features: List<RenderFeature>): KMLSpatialIndex {
            val grid = Array(GRID_SIZE * GRID_SIZE) { mutableListOf<Int>() }
            for (i in features.indices) {
                val b = features[i].bounds
                val x0 = (b.minX * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
                val x1 = (b.maxX * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
                val y0 = (b.minY * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
                val y1 = (b.maxY * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
                for (cy in y0..y1) {
                    for (cx in x0..x1) {
                        grid[cy * GRID_SIZE + cx].add(i)
                    }
                }
            }
            return KMLSpatialIndex(grid, features.size)
        }
    }
}
