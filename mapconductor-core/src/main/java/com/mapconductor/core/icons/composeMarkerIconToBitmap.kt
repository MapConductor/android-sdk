package com.mapconductor.core.icons

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.marker.MarkerIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap


//@Composable
//fun MarkerIconPreview(icon: MarkerIcon) {
//    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
//
//        // ① 背景のシェイプ（ベース形状）を背景色で描画
//        Image(
//            painter = rememberVectorPainter(icon.baseShape.imageVector),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            colorFilter = ColorFilter.tint(icon.background)
//        )
//
//        // ② メインのアイコン（星など）
//        icon.icon?.let {
//            Image(
//                painter = rememberVectorPainter(it),
//                contentDescription = null,
//                modifier = Modifier.size(18.dp),
//                colorFilter = ColorFilter.tint(Color.White)
//            )
//        }
//    }
//}

//suspend fun composeMarkerIconToBitmap(context: Context, markerIcon: MarkerIcon): Bitmap {
//    val composeView = ComposeView(context)
//    val density = context.resources.displayMetrics.density
//    fun roundToPx(value: Dp) : Int {
//        return (value.value * density).roundToInt()
//    }
//
//    return withContext(Dispatchers.Main) {
//        val iconSize = roundToPx(markerIcon.size)
//        composeView.setContent {
//            MarkerIconPreview(markerIcon)
//        }
//        composeView.measure(
//            View.MeasureSpec.makeMeasureSpec(iconSize, View.MeasureSpec.EXACTLY),
//            View.MeasureSpec.makeMeasureSpec(iconSize, View.MeasureSpec.EXACTLY)
//        )
//        composeView.layout(0, 0, iconSize, iconSize)
//        val bitmap = createBitmap(iconSize, iconSize)
//        val canvas = Canvas(bitmap)
//        composeView.draw(canvas)
//        return@withContext bitmap
//    }
//}
