package com.mapconductor.vectortile

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerIconInterface
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.MarkerTileRenderer
import com.mapconductor.core.tileserver.TileRequest
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Tokyo's street trees: 144,183 of them, 407 species, inside a box roughly
 * 0.34 by 0.27 degrees.
 *
 * A harder shape than the post office set this suite already measures. There
 * are six times as many markers, they are packed into one metropolitan area
 * rather than spread over a country, and each species draws its own icon — so
 * the renderer cannot collapse two trees of different species that land on the
 * same pixel.
 */
@RunWith(AndroidJUnit4::class)
class StreetTreeCostTest {
    private val tileSize = 512

    /** A small coloured dot per species, of the size a tree marker would use. */
    private class DotIcon(
        private val bitmap: Bitmap,
        private val sizePx: Float,
    ) : MarkerIconInterface {
        override val scale: Float = 1.0f
        override val anchor: Offset = Offset(0.5f, 0.5f)
        override val iconSize: Dp = sizePx.dp
        override val infoAnchor: Offset = Offset(0.5f, 0.0f)
        override val debug: Boolean = false

        // A pre-baked bitmap, so nothing is rasterised per marker: the point of
        // the measurement is the drawing, not icon generation.
        private val icon =
            BitmapIcon(
                bitmap = bitmap,
                size = Size(sizePx, sizePx),
                anchor = anchor,
            )

        override fun toBitmapIcon(): BitmapIcon = icon
    }

    private fun speciesIcons(
        count: Int,
        sizePx: Int,
    ): List<DotIcon> {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        return (0 until count).map { index ->
            // Golden-angle hue rotation, so neighbouring species indices do not
            // come out as neighbouring colours.
            val hue = (index * 137.508f) % 360f
            val colour = Color.HSVToColor(floatArrayOf(hue, 0.72f, 0.85f))
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            Canvas(bitmap).apply {
                paint.color = colour
                drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 1f, paint)
                paint.color = Color.argb(90, 0, 0, 0)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 1f, paint)
                paint.style = Paint.Style.FILL
            }
            DotIcon(bitmap, sizePx.toFloat())
        }
    }

    private class Trees(
        val manager: MarkerManager<Unit>,
        val count: Int,
        val species: Int,
    )

    private fun loadTrees(
        iconPx: Int,
        minMarkerCount: Int = 1,
        keepEvery: Int = 1,
    ): Trees {
        val bytes =
            InstrumentationRegistry
                .getInstrumentation()
                .context.assets
                .open("tokyo-trees.bin")
                .use { it.readBytes() }
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        // The magic is checked, not skipped. Read past it and the header
        // becomes a species count of over a billion, and the icon palette
        // allocates until the process aborts — which is what happened, twice,
        // after the asset gained its attribute columns.
        val magic = ByteArray(5)
        buffer.get(magic)
        check(String(magic, Charsets.US_ASCII) == "TREE\u0002") {
            "unexpected street tree asset format"
        }

        // The length has to be read into a local first: buffer.short advances
        // the position itself, so reading it inside position(position() + ...)
        // captures the offset from before the read and loses two bytes a name.
        fun skipName() {
            val length = buffer.short.toInt()
            buffer.position(buffer.position() + length)
        }

        fun skipTable() = repeat(buffer.int) { skipName() }

        val speciesCount = buffer.int
        repeat(speciesCount) { skipName() }
        skipTable() // wards
        skipTable() // roads
        val icons = speciesIcons(speciesCount, iconPx)

        val treeCount = buffer.int
        val manager = MarkerManager.defaultManager<Unit>(minMarkerCount = minMarkerCount)
        var kept = 0
        repeat(treeCount) { index ->
            // Every field is read even when the tree is skipped: the record is
            // fixed width, and leaving four of them unread walks the buffer
            // ten bytes off per tree until the coordinates are noise.
            val lat = buffer.float.toDouble()
            val lon = buffer.float.toDouble()
            val species = buffer.short.toInt() and 0xFFFF
            buffer.float // height
            buffer.short // girth
            buffer.short // ward
            buffer.short // road
            if (index % keepEvery != 0) return@repeat
            kept++
            manager.registerEntity(
                MarkerEntity(
                    marker = null,
                    state = MarkerState(position = GeoPoint(lat, lon), icon = icons[species]),
                    visible = true,
                    isRendered = true,
                    tiling = true,
                ),
            )
        }
        return Trees(manager, kept, speciesCount)
    }

    private fun median(values: List<Double>): Double = values.sorted()[values.size / 2]

    @Test
    fun rendersTokyoStreetTrees() {
        val loadStarted = System.nanoTime()
        val trees = loadTrees(iconPx = 14)
        val loadMs = (System.nanoTime() - loadStarted) / 1_000_000.0
        println("TREES loaded=%d species=%d load=%.0fms".format(trees.count, trees.species, loadMs))
        measure(trees, declutterPx = 0)
        measure(trees, declutterPx = 14)
    }

    /** Reports per-tile cost across zooms, with and without decluttering. */
    private fun measure(
        trees: Trees,
        declutterPx: Int,
    ) {
        // One renderer for the sweep. Each zoom asks for a different tile and
        // the cache is keyed by z/x/y, so it never answers from cache — and a
        // renderer per round kept a tile-sized bitmap and its own pool alive
        // until native allocation failed inside Canvas::create_canvas.
        val renderer =
            MarkerTileRenderer(
                markerManager = trees.manager,
                tileSize = tileSize,
                cacheSizeBytes = 8 * 1024 * 1024,
                declutterPx = declutterPx,
            )

        // Tiles covering Tokyo from "the whole city on one tile" down to a
        // street-level view, so the cost can be seen against tile density.
        for ((z, x, y) in listOf(
            Triple(9, 454, 201),
            Triple(11, 1818, 806),
            Triple(12, 3637, 1612),
            Triple(14, 14551, 6451),
        )) {
            val started = System.nanoTime()
            val bytes = renderer.renderTile(TileRequest(x = x, y = y, z = z))
            val elapsed = (System.nanoTime() - started) / 1_000_000.0

            val png = bytes
            if (png == null) {
                println("TREES z=%d EMPTY".format(z))
                continue
            }
            val decoded = BitmapFactory.decodeByteArray(png, 0, png.size)
            println(
                "TREES declutter=%d z=%d render=%.0fms png=%dKB tile=%dpx".format(
                    declutterPx, z, elapsed, png.size / 1024, decoded.width,
                ),
            )
            decoded.recycle()
        }
    }
}
