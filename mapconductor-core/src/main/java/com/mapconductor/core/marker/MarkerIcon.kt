package com.mapconductor.core.marker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import android.graphics.Color
import android.graphics.Path
import android.graphics.drawable.Drawable

class MarkerIcon internal constructor(
    val outsideColor: Int? = Color.RED, // 外側objectの色
    val outsideStrokeColor: Int? = Color.WHITE, // 外周の色
    val insideColor: Int? = Color.WHITE, // 内側objectの色
    val outsideWidth: Float? = 1f,
    val scale: Float? = 2f,
    val label: String? = null,
    val labelTextColor: Int? = Color.BLACK,
    val labelTextSizeLogical: Float? = 10f,
    val fillDrawable: Drawable? = null,
    val iconDrawable: Drawable? = null,
    val anchor: Offset = Offset(0.5f, 1.0f),
    val size: Size = Size(32f, 32f),
    val infoAnchor: Offset = Offset(0.5f, 0.5f),
    val outsidePath: Path, // 外側のPath
    val insidePath: Path? = null, // 内側のPath(nullなら描画しない)
//    val management: Int? = 0,
) {
    companion object
}
//
// fun drawableToBitmap(drawable: Drawable, width: Int = 96, height: Int = 96): Bitmap {
//    val bmpWidth = drawable.intrinsicWidth.takeIf { it > 0 } ?: width
//    val bmpHeight = drawable.intrinsicHeight.takeIf { it > 0 } ?: height
//
//    val bitmap = createBitmap(bmpWidth, bmpHeight)
//    val canvas = Canvas(bitmap)
//    drawable.setBounds(0, 0, canvas.width, canvas.height)
//    drawable.draw(canvas)
//    return bitmap
// }
//
// //
// @Composable
// fun RememberDrawable(@DrawableRes resId: Int): Bitmap {
//    val context = LocalContext.current
//
//    val drawableResId = rememberSaveable { mutableStateOf(resId) }
//
//    return remember(drawableResId.value) {
//        val drawable = ContextCompat.getDrawable(context, drawableResId.value) ?:
//            throw IllegalArgumentException("Resource is not available")
//        drawableToBitmap(drawable)
//    }
// }
