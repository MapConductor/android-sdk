package com.mapconductor.vectortile

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays

/**
 * A flat grid index against the hex-cell-plus-kd-tree one the SDK ships.
 *
 * Written outside the SDK on purpose: this is a question, not a change. What it
 * asks is whether the cost of the current index is its arithmetic or its
 * allocation. The hex index builds a HexCell object with a String id per
 * marker, puts them in a ConcurrentHashMap and sorts a kd-tree over them; this
 * builds one Long per marker and sorts a primitive array.
 *
 * The web SDK already indexes this way — GeoGridIndex, 0.02-degree cells — so
 * the platforms have diverged and only one of them has been measured.
 */
@RunWith(AndroidJUnit4::class)
class GridIndexPrototypeTest {
    /**
     * Positions only. What is being compared is the index, not the marker
     * objects around it — and 144k MarkerState objects are what makes the hex
     * index run out of memory in the first place.
     */
    private class Positions(
        val lat: DoubleArray,
        val lon: DoubleArray,
    )

    private fun loadPositions(): Positions {
        val bytes =
            InstrumentationRegistry
                .getInstrumentation()
                .context.assets
                .open("tokyo-trees.bin")
                .use { it.readBytes() }
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val magic = ByteArray(5)
        buffer.get(magic)
        check(String(magic, Charsets.US_ASCII).startsWith("TREE"))

        fun skipNames() {
            repeat(buffer.int) {
                val length = buffer.short.toInt()
                buffer.position(buffer.position() + length)
            }
        }
        skipNames() // species
        skipNames() // wards
        skipNames() // roads

        val count = buffer.int
        val lat = DoubleArray(count)
        val lon = DoubleArray(count)
        for (index in 0 until count) {
            lat[index] = buffer.float.toDouble()
            lon[index] = buffer.float.toDouble()
            buffer.short
            buffer.float
            buffer.short
            buffer.short
            buffer.short
        }
        return Positions(lat, lon)
    }

    /**
     * A uniform lat/lng grid held as one sorted primitive array.
     *
     * Each entry packs its cell key and the marker's index into a single Long,
     * so building the index is an arithmetic pass and a primitive sort: no
     * objects, no hashing, no strings. A bounds query walks the cells it covers
     * and binary-searches each one's run.
     */
    private class GridIndex(
        positions: Positions,
        private val cellDegrees: Double,
    ) {
        private val packed: LongArray

        private fun cellKey(
            lat: Double,
            lon: Double,
        ): Long {
            // Offsets keep the keys positive so their ordering matches the
            // numeric ordering the sort and the binary search rely on.
            val latCell = Math.floor(lat / cellDegrees).toLong() + 262144
            val lonCell = Math.floor(lon / cellDegrees).toLong() + 524288
            return (latCell shl 20) or lonCell
        }

        init {
            packed =
                LongArray(positions.lat.size) { index ->
                    (cellKey(positions.lat[index], positions.lon[index]) shl INDEX_BITS) or
                        index.toLong()
                }
            Arrays.sort(packed)
        }

        /** Indices of every marker inside the box. */
        fun query(
            south: Double,
            north: Double,
            west: Double,
            east: Double,
        ): IntArray {
            var result = IntArray(64)
            var found = 0
            val latFrom = Math.floor(south / cellDegrees).toLong()
            val latTo = Math.floor(north / cellDegrees).toLong()
            val lonFrom = Math.floor(west / cellDegrees).toLong()
            val lonTo = Math.floor(east / cellDegrees).toLong()

            for (latCell in latFrom..latTo) {
                for (lonCell in lonFrom..lonTo) {
                    val key = ((latCell + 262144) shl 20) or (lonCell + 524288)
                    var at = lowerBound(key shl INDEX_BITS)
                    val limit = (key + 1) shl INDEX_BITS
                    while (at < packed.size && packed[at] < limit) {
                        if (found == result.size) result = result.copyOf(result.size * 2)
                        result[found++] = (packed[at] and INDEX_MASK).toInt()
                        at++
                    }
                }
            }
            return result.copyOf(found)
        }

        private fun lowerBound(target: Long): Int {
            var low = 0
            var high = packed.size
            while (low < high) {
                val mid = (low + high) ushr 1
                if (packed[mid] < target) low = mid + 1 else high = mid
            }
            return low
        }

        companion object {
            // 24 bits of index, enough for 16.7M markers, under the cell key.
            private const val INDEX_BITS = 24
            private const val INDEX_MASK = (1L shl INDEX_BITS) - 1
        }
    }

    private fun median(values: List<Double>): Double = values.sorted()[values.size / 2]

    @Test
    fun gridIndexAgainstBruteForce() {
        val positions = loadPositions()
        val count = positions.lat.size

        for (cellDegrees in listOf(0.02, 0.005, 0.001)) {
            var index: GridIndex? = null
            val build =
                median(
                    (0 until 3).map {
                        val started = System.nanoTime()
                        index = GridIndex(positions, cellDegrees)
                        (System.nanoTime() - started) / 1_000_000.0
                    },
                )

            // A z14-sized box near Shinjuku, and a z9-sized one over all Tokyo.
            for ((label, box) in listOf(
                "tile-z14" to doubleArrayOf(35.688, 35.694, 139.695, 139.703),
                "all-tokyo" to doubleArrayOf(35.50, 35.85, 139.55, 139.95),
            )) {
                var found = 0
                val query =
                    median(
                        (0 until 7).map {
                            val started = System.nanoTime()
                            found = index!!.query(box[0], box[1], box[2], box[3]).size
                            (System.nanoTime() - started) / 1_000_000.0
                        },
                    )
                var scanFound = 0
                val scan =
                    median(
                        (0 until 7).map {
                            val started = System.nanoTime()
                            var hits = 0
                            for (i in 0 until count) {
                                val la = positions.lat[i]
                                val lo = positions.lon[i]
                                if (la >= box[0] && la <= box[1] && lo >= box[2] && lo <= box[3]) {
                                    hits++
                                }
                            }
                            scanFound = hits
                            (System.nanoTime() - started) / 1_000_000.0
                        },
                    )
                println(
                    "GRID n=%d cell=%.3f build=%.0fms %s query=%.2fms/%d scan=%.2fms/%d".format(
                        count, cellDegrees, build, label, query, found, scan, scanFound,
                    ),
                )
            }
            index = null
        }
    }
}
