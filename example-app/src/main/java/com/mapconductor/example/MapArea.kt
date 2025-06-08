package com.mapconductor.example

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.info.InfoBubbleState
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.demo.StoreCard
import android.os.Bundle

@Composable
fun MapArea(
    mapViewState: MapViewState<*>?,
    selectedMarker: MarkerState?,
    infoBubbleState: InfoBubbleState,
    modifier: Modifier = Modifier,
    markers: List<MarkerState> = emptyList<MarkerState>(),
    onDirectionButtonClick: OnMarkerEventHandler = {},
    onMapClickHandler: OnMapEventHandler = {},
    onMarkerClickHandler: OnMarkerEventHandler = {},
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor by remember { mutableStateOf(if (darkTheme) Color.Black else Color.White) }

    mapViewState?.let { mapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = mapViewState,
            onMapClick = onMapClickHandler,
            onMarkerClick = onMarkerClickHandler,
        ) {
            markers.forEach { markerState -> Marker(markerState) }

            selectedMarker?.let {
                InfoBubble(
                    bubbleColor = bubbleColor,
                    state = infoBubbleState,
                ) {
                    StoreCard(
                        info = it.extra as Bundle,
                        onClick = {
                            onDirectionButtonClick(it)
                        },
                    )
                }
            }
        }
    }
}
