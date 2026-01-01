package com.mapconductor.core.marker

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.map.MapOverlay
import android.util.Log
import kotlinx.coroutines.flow.StateFlow

val LocalMarkerCollector =
    compositionLocalOf<MarkerCollector> {
        error("Marker must be under the <MapView />")
    }

class MarkerOverlay(
    override val flow: StateFlow<MutableMap<String, MarkerState>>,
) : MapOverlay<MarkerState> {
    override suspend fun render(
        data: MutableMap<String, MarkerState>,
        controller: MapViewController,
    ) {
        (controller as? MarkerCapable)?.let { markerController ->
            Log.d("DEBUG", "----->MarkerOverlay.compositionMarkers data.values.size=${data.values.size}")
            markerController.compositionMarkers(data.values.toList())
        }
    }
}
