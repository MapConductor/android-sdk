package com.mapconductor.vectortile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Test
import org.junit.runner.RunWith

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
    private class DotIcon(private val bitmap: Bitmap, private val sizePx: Float) :
        MarkerIconInterface {
        override val scale: Float = 1.0f
        override val anchor: Offset = Offset(0.5f, 0.5f)
        override val iconSize: Dp = sizePx.dp
        override val infoAnchor: Offset = Offset(0.5f, 0.0f)
        override val debug: Boolean = false

        // A pre-baked bitmap, so nothing is rasterised per marker: the point of
        // the measurement is the drawing, not icon generation.
        private val icon = BitmapIcon(
            bitmap = bitmap,
            size = Size(sizePx, sizePx),
            anchor = anchor,
        )

        override fun toBitmapIcon(): BitmapIcon = icon
    }

    private fun speciesIcons(count: Int, sizePx: Int): List<DotIcon> {
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

    private class Trees(val manager: MarkerManager<Unit>, val count: Int, val species: Int)

    private fun loadTrees(iconPx: Int): Trees {
        val bytes = InstrumentationRegistry.getInstrumentation().context.assets
            .open("tokyo-trees.bin").use { it.readBytes() }
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        val speciesCount = buffer.int
        repeat(speciesCount) {
            val length = buffer.short.toInt()
            buffer.position(buffer.position() + length)
        }
        val icons = speciesIcons(speciesCount, iconPx)

        val treeCount = buffer.int
        val manager = MarkerManager.defaultManager<Unit>(minMarkerCount = 1)
        repeat(treeCount) {
            val lat = buffer.float.toDouble()
            val lon = buffer.float.toDouble()
            val species = buffer.short.toInt() and 0xFFFF
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
        return Trees(manager, treeCount, speciesCount)
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

        // Tiles covering Tokyo from "the whole city on one tile" down to a
        // street-level view, so the cost can be seen against tile density.
        for ((z, x, y) in listOf(
            Triple(9, 454, 201),
            Triple(11, 1818, 806),
            Triple(12, 3637, 1612),
            Triple(14, 14551, 6451),
        )) {
            var bytes: ByteArray? = null
            val samples = (0 until 3).map {
                // A fresh renderer each round: its cache would answer every
                // call after the first.
                val renderer = MarkerTileRenderer(
                    markerManager = trees.manager,
                    tileSize = tileSize,
                    cacheSizeBytes = 8 * 1024 * 1024,
                    declutterPx = declutterPx,
                )
                val started = System.nanoTime()
                bytes = renderer.renderTile(TileRequest(x = x, y = y, z = z))
                (System.nanoTime() - started) / 1_000_000.0
            }
            val png = bytes
            if (png == null) {
                println("TREES z=%d EMPTY".format(z))
                continue
            }
            val decoded = BitmapFactory.decodeByteArray(png, 0, png.size)
            println(
                "TREES declutter=%d z=%d render=%.0fms png=%dKB tile=%dpx".format(
                    declutterPx, z, median(samples), png.size / 1024, decoded.width,
                ),
            )
            decoded.recycle()
        }
    }
}
