package com.mapconductor.streettree

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerIconInterface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Tokyo's street trees: 144,183 of them, from the metropolitan government's
 * open data.
 *
 * A harder shape than the post office set. There are six times as many, they
 * sit inside one metropolitan area rather than spread over a country, and each
 * species draws its own colour — so the renderer cannot collapse two trees of
 * different species that land on the same pixel.
 *
 * Species with fewer than a hundred trees are folded into one bucket. The
 * distribution has a long tail: a hundred species cover 96% of the trees, and
 * the remaining three hundred would be indistinguishable colours nobody could
 * read off a legend. The bucket is still the fifth largest group, so it is not
 * a rounding error being hidden.
 */
data class StreetTree(
    val position: GeoPoint,
    val species: String,
    val speciesIndex: Int,
    /** Height in metres, as recorded in the survey. */
    val heightM: Float,
    /** Trunk circumference in centimetres. */
    val girthCm: Int,
    val ward: String,
    val roadName: String,
    // Serializable because MarkerState.extra is: the SDK carries this across
    // the marker abstraction without knowing what it is.
) : java.io.Serializable

/** A small coloured dot. One per species, shared by every tree of that species. */
class TreeDotIcon(
    bitmap: Bitmap,
    sizePx: Float,
) : MarkerIconInterface {
    override val scale: Float = 1.0f
    override val anchor: Offset = Offset(0.5f, 0.5f)
    override val iconSize: Dp = sizePx.dp
    override val infoAnchor: Offset = Offset(0.5f, 0.0f)
    override val debug: Boolean = false

    // Baked once. Building it per marker would measure icon generation rather
    // than drawing.
    private val icon =
        BitmapIcon(bitmap = bitmap, size = Size(sizePx, sizePx), anchor = anchor)

    override fun toBitmapIcon(): BitmapIcon = icon
}

object StreetTreeIcons {
    fun palette(
        count: Int,
        sizePx: Int,
    ): List<TreeDotIcon> {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        return (0 until count).map { index ->
            // Golden-angle hue rotation, so neighbouring species indices do not
            // come out as neighbouring colours.
            val hue = (index * 137.508f) % 360f
            val colour =
                if (index == count - 1) {
                    // The "other" bucket reads as grey rather than competing
                    // with a named species for a hue.
                    Color.rgb(150, 150, 155)
                } else {
                    Color.HSVToColor(floatArrayOf(hue, 0.70f, 0.80f))
                }
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            Canvas(bitmap).apply {
                val radius = sizePx / 2f - 1f
                paint.color = colour
                paint.style = Paint.Style.FILL
                drawCircle(sizePx / 2f, sizePx / 2f, radius, paint)
                paint.color = Color.argb(110, 0, 0, 0)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                drawCircle(sizePx / 2f, sizePx / 2f, radius, paint)
            }
            TreeDotIcon(bitmap, sizePx.toFloat())
        }
    }
}

class StreetTreeDataLoader(
    private val context: Context,
) {
    /**
     * Reads the packed asset: string tables for species, ward and road, then a
     * fixed record per tree. The source is a 12 MB Shift-JIS CSV; parsing that
     * on the device would measure CSV parsing rather than the map.
     */
    suspend fun load(): StreetTreeData =
        withContext(Dispatchers.IO) {
            val bytes = context.assets.open("tokyo-trees.bin").use { it.readBytes() }
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

            val magic = ByteArray(5)
            buffer.get(magic)
            require(String(magic, Charsets.US_ASCII) == "TREE\u0002") {
                "unexpected street tree asset format"
            }

            fun table(): List<String> =
                (0 until buffer.int).map {
                    val length = buffer.short.toInt()
                    val name = ByteArray(length)
                    buffer.get(name)
                    String(name, Charsets.UTF_8)
                }

            val species = table()
            val wards = table()
            val roads = table()

            val count = buffer.int
            val trees = ArrayList<StreetTree>(count)
            repeat(count) {
                val lat = buffer.float.toDouble()
                val lon = buffer.float.toDouble()
                val speciesIndex = buffer.short.toInt() and 0xFFFF
                val height = buffer.float
                val girth = buffer.short.toInt() and 0xFFFF
                val ward = buffer.short.toInt() and 0xFFFF
                val road = buffer.short.toInt() and 0xFFFF
                trees.add(
                    StreetTree(
                        position = GeoPoint(lat, lon),
                        species = species[speciesIndex],
                        speciesIndex = speciesIndex,
                        heightM = height,
                        girthCm = girth,
                        ward = wards[ward],
                        roadName = roads[road],
                    ),
                )
            }
            StreetTreeData(trees = trees, species = species)
        }
}

/** The trees, and the species table their colours are indexed by. */
data class StreetTreeData(
    val trees: List<StreetTree>,
    val species: List<String>,
)
