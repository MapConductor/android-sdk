package com.mapconductor.example.pages.marker.postoffice

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.MapViewContainer
import android.util.Log

@Composable
fun PostOfficeMapComponent(
    modifier: Modifier = Modifier,
    mapViewState: MapViewState<*>,
    renderingStrategy: MarkerRenderingStrategy<*>?,
    selectedMarker: MarkerState?,
    markers: List<MarkerState> = emptyList<MarkerState>(),
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onCameraChanged: ((com.mapconductor.core.map.MapCameraPosition) -> Unit)? = null,
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor by remember {
        mutableStateOf(if (darkTheme) Color.Black else Color.White)
    }

    // Observe camera position changes and notify the callback
//    val cameraPosition by mapViewState.cameraPosition.collectAsState()
//    LaunchedEffect(cameraPosition) {
//        cameraPosition?.let { position ->
//            onCameraChanged?.invoke(position)
//        }
//    }

    MapViewContainer(
        modifier = modifier,
        renderingStrategy = renderingStrategy,
        state = mapViewState,
        onMapLoaded = onMapLoaded,
        onMapClick = onMapClick,
        onMarkerClick = onMarkerClick,
    ) {
        val startTime = System.currentTimeMillis()
        markers.forEach { markerState -> Marker(markerState) }
        val endTime = System.currentTimeMillis()
        val elapsedTime = endTime - startTime // 処理時間を計算
        Log.d("debug", "PostOfficeMapComponent: $elapsedTime ms")

//        selectedMarker?.let {
//            InfoBubble(
//                bubbleColor = bubbleColor,
//                marker = it,
//            ) {
//                PostOfficeInfoView(
//                    info = it.extra as PostOffice,
//                )
//            }
//        }
    }
}
