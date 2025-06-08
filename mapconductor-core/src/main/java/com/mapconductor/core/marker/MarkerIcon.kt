package com.mapconductor.core.marker

import android.graphics.Color
import android.graphics.Path
import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

class MarkerIcon internal constructor(
    val fillColor: Int? = Color.RED,
    val strokeColor: Int? = Color.WHITE,
    val strokeWidth: Float? = 1f,
    val scale: Float? = 2f,
    val label: String? = null,
    val labelTextColor: Int? = Color.BLACK,
    val labelTextSizeLogical: Float? = 10f,
    val fillDrawable: Drawable? = null,
    val iconDrawable: Drawable? = null,
    val anchor: Offset = Offset(0.5f, 1.0f),
    val size: Size = Size(32f, 32f),
    val infoAnchor: Offset = Offset(0.5f, 0.5f),
    val fillPath: Path,
    val strokePath: Path,
) {
    companion object
}
//
//fun drawableToBitmap(drawable: Drawable, width: Int = 96, height: Int = 96): Bitmap {
//    val bmpWidth = drawable.intrinsicWidth.takeIf { it > 0 } ?: width
//    val bmpHeight = drawable.intrinsicHeight.takeIf { it > 0 } ?: height
//
//    val bitmap = createBitmap(bmpWidth, bmpHeight)
//    val canvas = Canvas(bitmap)
//    drawable.setBounds(0, 0, canvas.width, canvas.height)
//    drawable.draw(canvas)
//    return bitmap
//}
//
////
//@Composable
//fun RememberDrawable(@DrawableRes resId: Int): Bitmap {
//    val context = LocalContext.current
//
//    val drawableResId = rememberSaveable { mutableStateOf(resId) }
//
//    return remember(drawableResId.value) {
//        val drawable = ContextCompat.getDrawable(context, drawableResId.value) ?:
//            throw IllegalArgumentException("Resource is not available")
//        drawableToBitmap(drawable)
//    }
//}
