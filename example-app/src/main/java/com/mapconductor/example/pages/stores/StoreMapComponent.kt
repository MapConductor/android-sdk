package com.mapconductor.example.pages.stores

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.MapViewContainer
import com.mapconductor.icons.CircleIcon
import com.mapconductor.icons.FlagIcon
import com.mapconductor.settings.MarkerIconSize
import android.os.Bundle

@Composable
fun StoreMapComponent(
    mapViewState: MapViewState<*>?,
    selectedMarker: MarkerState?,
    modifier: Modifier = Modifier,
    markers: List<MarkerState> = emptyList<MarkerState>(),
    onDirectionButtonClick: OnMarkerEventHandler = {},
    onMapClickHandler: OnMapEventHandler = {},
    onMarkerClickHandler: OnMarkerEventHandler = {},
    onMarkerDragHandler: OnMarkerEventHandler = {},
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor by remember {
        mutableStateOf(if (darkTheme) Color.Black else Color.White)
    }
    var isMarkerAnimating by remember { mutableStateOf(false) }
    val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan)

    val markerList =
        remember {
            markers.mapIndexed { index, state ->
                val randomColor = colors[index % colors.size]
                state.copy(
                    icon = when (index % 3) {
                        0 -> DefaultIcon(
                            fillColor = randomColor,
                            strokeColor = Color.DarkGray.copy(alpha = 0.4f),
                            strokeWidth = 2.dp,
                            iconSize = MarkerIconSize.Large,
                        )
                        1 -> FlagIcon(
                            fillColor = randomColor,
                            iconSize = MarkerIconSize.Regular,
                        )
                        else -> CircleIcon(
                            fillColor = randomColor.copy(alpha = 0.7f),
                            strokeColor = Color.Blue.copy(alpha = 0.4f),
                            iconSize = MarkerIconSize.Small,
                        )
                    }
                )
            }
        }

    mapViewState?.let { currentMapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = currentMapViewState,
            onMapClick = onMapClickHandler,
            onMarkerClick = onMarkerClickHandler,
            onMarkerDrag = onMarkerDragHandler,
            onMarkerAnimateStart = { isMarkerAnimating = true },
            onMarkerAnimateEnd = { isMarkerAnimating = false },
        ) {
            markerList.forEach { markerState -> Marker(markerState) }

            selectedMarker?.let {
                if (!isMarkerAnimating) {
                    InfoBubble(
                        bubbleColor = bubbleColor,
                        marker = it,
                    ) {
                        StoreInfoView(
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
}
