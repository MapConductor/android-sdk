package com.mapconductor.core.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.marker.MarkerIcon
import android.graphics.Color
import android.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mapconductor.core.R

fun MarkerIcon.Companion.ImgInCircle(
    outsideColor: Int? = 0xFF008000.toInt(),  // 円の塗りつぶし色（緑）
    strokeWidth: Float? = 2f,
    triangleHeight: Float? = 24f,
    triangleWidth: Float? = 24f,
): MarkerIcon {
    val path = Path().apply {}
/*
    @Composable
    fun ClippedImageWithCanvas() {
        val context = LocalContext.current
        val imageBitmap = remember {
            val drawable = ContextCompat.getDrawable(context, R.drawable.sample)!!
            val bitmap = Bitmap.createBitmap(28, 28, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, 28, 28)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        }

        Canvas(modifier = Modifier.size(28.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Compose Path（中心14,14 半径12）
            val clipPath = Path().apply {
                addOval(
                    Rect(
                        offset = Offset(2f, 2f),
                        size = Size(24f, 24f) // 半径12fの円
                    )
                )
            }

            // クリッピング処理
            clipPath(clipPath) {
                drawImage(
                    image = imageBitmap,
                    dstSize = IntSize(canvasWidth.toInt(), canvasHeight.toInt())
                )
            }
        }
    }
*/
    return MarkerIcon(
        outsideColor = outsideColor,      // 塗りつぶし色（黄色）
        outsideWidth = strokeWidth,
        scale = 1.5f,
        anchor = Offset(0.5f, 0.5f),
        size = Size(32f, 32f),
        infoAnchor = Offset(0.5f, 0.5f),
        outsidePath = path,
    )
}
