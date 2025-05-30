package com.mapconductor.example

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerClickHandler
import com.mapconductor.core.marker.MarkerState

@Composable
fun MapArea(
    state: MapViewState<*>?,
    modifier: Modifier = Modifier,
    markers: List<MarkerState> = emptyList<MarkerState>(),
    onMarkerClickHandler: MarkerClickHandler = {},
    onCallButtonClick: MarkerClickHandler = {},
) {
    val camera = state?.mapCameraPosition?.collectAsStateWithLifecycle()?.value
    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor = if (darkTheme) Color.Black else Color.White
    val context = LocalContext.current

    Box(
        modifier = modifier,
    ) {
        if (state != null) {
            MapViewContainer(
                state = state,
            ) {

                markers.forEach { state ->
                    Marker(
                        state = state,
                        onClick = onMarkerClickHandler,
                    ) {
//                        InfoBubble(
//                            markerState = state,
//                            bubbleColor = bubbleColor,
//                            contentPadding = 0.dp,
//                        ) {
//                            StoreCard(
//                                onClick = {
//                                    onCallButtonClick(props)
//                                }
//                            )
//                        }
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

        } else {
            Text(
                text = "Loading...",
            )
        }
    }
}
