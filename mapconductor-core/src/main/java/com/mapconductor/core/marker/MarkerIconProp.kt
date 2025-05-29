package com.mapconductor.core.marker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.mapconductor.core.Offset

data class MarkerIconProp(
    val fillColor: Int? = Color.RED,
    val strokeColor: Int? = Color.WHITE,
    val strokeWidth: Float? = 1f,
    val scale: Float? = 2f,
    val label: String? = null,
    val labelTextColor: Int? = Color.BLACK,
    val labelTextSizeLogical: Float? = 10f,
    val fillDrawable: Drawable? = null,
    val iconDrawable: Drawable? = null,
    val anchor: Offset = Offset(0.5, 1.0),
    val size: Size = Size(32f, 32f),
    val infoAnchor: Offset = Offset(0.5, 0.5)
)

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