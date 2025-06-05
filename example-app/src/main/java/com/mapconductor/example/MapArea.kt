package com.mapconductor.example

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.info.InfoBubbleState
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapClickHandler
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerClickHandler
import com.mapconductor.example.demo.StoreCard
import android.os.Bundle

@Composable
fun MapArea(
    mapViewState: MapViewState<*>?,
    selectedMarker: MarkerState?,
    infoBubbleState: InfoBubbleState,
    modifier: Modifier = Modifier,
    markers: List<MarkerState> = emptyList<MarkerState>(),
    onCallButtonClick: OnMarkerClickHandler = {},
    onMapClickHandler: OnMapClickHandler = {},
    onMarkerClickHandler: OnMarkerClickHandler = {},
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor by remember { mutableStateOf(if (darkTheme) Color.Black else Color.White) }

    mapViewState?.let { mapViewState ->
        MapViewContainer(
            modifier = modifier,
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
                InfoBubble(
                    bubbleColor = bubbleColor,
                    state = infoBubbleState,
                ) {
                    StoreCard(
                        info = it.extra as Bundle,
                        onClick = {
                            onCallButtonClick(it)
                        },
                    )
                }
            }
        }
    }
}
