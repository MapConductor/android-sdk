package com.mapconductor.example

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.info.InfoBubbleState
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.OnMarkerClickHandler
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.demo.StoreCard

@Composable
fun MapArea(
    state: MapViewState<*>?,
    modifier: Modifier = Modifier,
    markers: List<MarkerState> = emptyList<MarkerState>(),
//    onMarkerClickHandler: MarkerClickHandler = {},
    onCallButtonClick: OnMarkerClickHandler = {},
) {
    val camera = state?.mapCameraPosition?.collectAsStateWithLifecycle()?.value
    val darkTheme: Boolean = isSystemInDarkTheme()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }
    val infoBubbleState by remember { mutableStateOf(InfoBubbleState()) }
    val bubbleColor by remember { mutableStateOf(if (darkTheme) Color.Black else Color.White) }
//    infoBubbleState.bubbleColor = bubbleColor

    val onMapClickHandler = { _: GeoPoint ->
        selectedMarker = null
        infoBubbleState.close()
    }
    val onMarkerClickHandler = { state: MarkerState -> selectedMarker = state }

    state?.let { mapViewState ->
        Box(
            modifier = modifier,
        ) {
            MapViewContainer(
                state = mapViewState,
                onMapClick = onMapClickHandler,
            ) {
                markers.forEach { markerState ->
                    Marker(
                        state = markerState,
                        onClick = onMarkerClickHandler,
                    )
                }

                selectedMarker?.let {
                    infoBubbleState.open(it)

                    InfoBubble(
                        bubbleColor = bubbleColor,
                        state = infoBubbleState
                    ) {
                        StoreCard(
                            info = it.extra as Bundle,
                            onClick = {
                                onCallButtonClick(it)
                            }
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color(
                        red = 0.9f,
                        green = 0.9f,
                        blue = 0.9f,
                        alpha = 0.75f,
                    ))
                    .wrapContentHeight()
            ) {
                Text("LatLng: (${camera?.position?.latitude}, ${camera?.position?.longitude})", color = Color.Black)
                Text("Zoom: ${camera?.zoom}", color = Color.Black)
                Text("bearing: ${camera?.bearing}", color = Color.Black)
                Text("tilt: ${camera?.tilt}", color = Color.Black)
            }
        }
    }
}
