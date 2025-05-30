package com.mapconductor.core.marker

import android.graphics.Bitmap
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.MapOverlay
import com.mapconductor.core.Offset
import com.mapconductor.core.controller.MapViewController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.ByteArrayOutputStream

data class BitmapIcon(
    val bitmap: Bitmap,
    val anchor: Offset,
    val size: Size,
) {
    fun toByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}

class MarkerOverlay(
    override val flow: StateFlow<List<MarkerEntry>>,
) : MapOverlay<MarkerEntry> {

    override suspend fun render(data: List<MarkerEntry>, controller: MapViewController) {
        controller.addMarkers(data)
    }
}

val LocalMarkerCollector = compositionLocalOf<MutableStateFlow<List<MarkerEntry>>> {
    error("Marker must be under the <MapView />")
}
