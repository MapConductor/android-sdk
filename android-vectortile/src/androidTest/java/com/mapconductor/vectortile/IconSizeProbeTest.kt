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
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerIconInterface
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.MarkerTileRenderer
import com.mapconductor.core.tileserver.TileRequest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * アイコンが実際に何ピクセルで描かれるかを、出来上がったタイルから数える。
 *
 * ios-sdk-core の `IconSizeProbeTests` と対になっていて、同じ 14 単位の円を
 * 同じ zoom・同じ iconScaleCallback で描いて比べる。計算で追うと前提を一つ
 * 間違えるだけで答えが変わるので、描いた結果を測る。
 */
@RunWith(AndroidJUnit4::class)
class IconSizeProbeTest {

    /** サンプルの TreeDotIcon と同じ形。 */
    private class DotIcon(private val bitmap: Bitmap, private val sizePx: Float) : MarkerIconInterface {
        override val scale: Float = 1.0f
        override val anchor: Offset = Offset(0.5f, 0.5f)
        override val iconSize: Dp = sizePx.dp
        override val infoAnchor: Offset = Offset(0.5f, 0.0f)
        override val debug: Boolean = false

        override fun toBitmapIcon(): BitmapIcon =
            BitmapIcon(bitmap = bitmap, size = Size(sizePx, sizePx), anchor = anchor)
    }

    @Test
    fun drawnIconSize() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val density = context.resources.displayMetrics.density
        val callbackScale = 1.4 // サンプルの zoom > 15 の帯
        val tileSize = 256      // プロバイダが渡す値（GoogleMapMarkerController）

        // サンプルと同じ作り: 14 **dp** の円。密度倍したピクセルでビットマップを作る。
        // core の AbstractDefaultIcon が dpToPxForBitmap(iconSize) でそうしているのと
        // 同じ流儀。ここを生ピクセルにすると端末が精細になるほど小さく見える。
        val sizeDp = 10f
        val sizePx = ResourceProvider.dpToPxForBitmap(sizeDp).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawCircle(
            sizePx / 2f, sizePx / 2f, sizePx / 2f - 1f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.RED },
        )

        val zoom = 16
        val n = (1 shl zoom).toDouble()
        val lat = 35.68
        val lon = 139.75
        val latRad = Math.toRadians(lat)
        val tx = ((lon + 180.0) / 360.0 * n).toInt()
        val ty = ((1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * n).toInt()

        val manager = MarkerManager.defaultManager<Unit>(minMarkerCount = 1)
        manager.registerEntity(
            MarkerEntity(
                marker = null,
                state = MarkerState(position = GeoPoint(lat, lon), icon = DotIcon(bitmap, sizePx.toFloat())),
                visible = true,
                isRendered = true,
                tiling = true,
            ),
        )
        val renderer =
            MarkerTileRenderer(
                markerManager = manager,
                tileSize = tileSize,
                cacheSizeBytes = 1 shl 20,
                iconScaleCallback = { _, _ -> callbackScale },
            )

        val png = renderer.renderTile(TileRequest(tx, ty, zoom))
        assertTrue("タイルが描けていない", png != null)
        val tile = BitmapFactory.decodeByteArray(png, 0, png!!.size)

        var minX = tile.width
        var maxX = -1
        for (y in 0 until tile.height) {
            for (x in 0 until tile.width) {
                if (Color.alpha(tile.getPixel(x, y)) > 8) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                }
            }
        }
        val drawnPx = maxX - minX + 1
        // タイルは 256dp として表示されるので、見かけの大きさはこの比。
        val apparentDp = drawnPx.toDouble() / tile.width * 256.0
        println(
            "ICONPROBE android density=%.2f tile=%d canvas=%d sizeDp=%.0f sizePx=%d drawnPx=%d apparentDp=%.2f"
                .format(density, tileSize, tile.width, sizeDp, sizePx, drawnPx, apparentDp),
        )
        assertTrue(drawnPx > 0)
    }
}
