package com.mapconductor.streettree

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toDrawable
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.ImageIcon
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.ceil
import kotlin.math.max
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
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

/** 点のどちら側にラベルを描くか。 */
enum class LabelSide { RIGHT, LEFT, TOP, BOTTOM }

object StreetTreeIcons {
    /** ラベルの文字の大きさ（sp）。ラベルの重なり判定もこの値で測る。 */
    const val LABEL_TEXT_SP = 11f

    /** 点とラベルのすき間（dp）。SDK の配置（縁取りの太さ 1dp）と同じ値。 */
    const val LABEL_GAP_DP = 1f

    /** 縁取りの太さ（dp）。SDK はラベルの幅と高さにこの 2 倍を足す。 */
    private const val LABEL_OUTLINE_DP = 1f

    private val labelTypeface: Typeface = Typeface.DEFAULT_BOLD

    /** 点の上下に置くラベルは縦書きにする。横の通りでは点が横に詰まって並ぶので、横書きだと数本に 1 つしか名前を付けられない。 */
    fun isVertical(side: LabelSide): Boolean = side == LabelSide.TOP || side == LabelSide.BOTTOM

    private val labelSizes = java.util.concurrent.ConcurrentHashMap<String, Pair<Float, Float>>()

    /**
     * ラベルの幅と高さ（dp、縁取りを含む）。樹種名と向きごとに 1 回だけ測る。
     * SDK と同じ測り方（縦書きは縦書き用の字形で 1 文字 1em に積む）。
     */
    fun labelSizeDp(
        text: String,
        vertical: Boolean,
    ): Pair<Float, Float> =
        labelSizes.getOrPut(if (vertical) "v/$text" else "h/$text") {
            val density = ResourceProvider.dpToPxForBitmap(1f).toFloat()
            val paint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = ResourceProvider.spToPx(LABEL_TEXT_SP).toFloat()
                    typeface = labelTypeface
                    if (vertical) fontFeatureSettings = "'vert' 1"
                }
            val outline = LABEL_OUTLINE_DP * 2
            if (vertical) {
                var column = paint.textSize
                text.forEach { column = max(column, paint.measureText(it.toString())) }
                ceil(column) / density + outline to text.length * paint.textSize / density + outline
            } else {
                val metrics = paint.fontMetrics
                paint.measureText(text) / density + outline to (metrics.descent - metrics.ascent) / density + outline
            }
        }

    /**
     * 点の [side] 側に樹種名を添えたアイコン。
     *
     * SDK の [ImageIcon] のラベルを使う。画像は作らない -- タイル描画は点の画像と
     * 文字を別々に描くので、ラベル付きのアイコンをいくつ作っても点の画像は 1 枚のまま。
     */
    fun labelled(
        dot: ImageIcon,
        text: String,
        side: LabelSide,
    ): ImageIcon =
        dot.copy(
            label = text,
            labelTextColor = ComposeColor(0xFF282828),
            labelTextSize = LABEL_TEXT_SP.sp,
            labelTypeFace = labelTypeface,
            labelStrokeColor = ComposeColor.White,
            labelPosition =
                when (side) {
                    LabelSide.RIGHT -> "right"
                    LabelSide.LEFT -> "left"
                    LabelSide.TOP -> "top"
                    LabelSide.BOTTOM -> "bottom"
                },
            labelWritingMode = if (isVertical(side)) "vertical-rl" else "horizontal-tb",
        )

    /**
     * 種ごとの色分けドット。
     *
     * [sizeDp] は **dp**。ビットマップは密度倍したピクセルで作る -- core の
     * [com.mapconductor.core.marker.AbstractDefaultIcon] が
     * `dpToPxForBitmap(iconSize)` でそうしているのと同じ流儀で、ここだけ生ピクセル
     * で作ると端末が精細になるほど小さく見える。
     *
     * 実測（density 2.13 の端末、zoom > 15）: 生ピクセルだと見かけ 8.47dp、
     * dp 基準だと 18dp。ios-sdk は既定アイコンもサンプルもポイント基準なので、
     * dp 基準にして初めて 3 プラットフォームが揃う。
     */
    fun palette(
        count: Int,
        sizeDp: Float,
    ): List<ImageIcon> {
        val resources = ResourceProvider.getAppContext().resources
        val sizePx = ResourceProvider.dpToPxForBitmap(sizeDp).toInt().coerceAtLeast(1)
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
            ImageIcon(
                image = bitmap.toDrawable(resources),
                iconSize = sizeDp.dp,
                // 吹き出しは点の上端に出す。
                infoAnchor = Offset(0.5f, 0.0f),
            )
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
