package com.mapconductor.example

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.info.InfoBubbleState
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler

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
    onMarkerDragHandler: OnMarkerEventHandler = {},
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor by remember {
        mutableStateOf(if (darkTheme) Color.Black else Color.White)
    }
    var isMarkerAnimating by remember { mutableStateOf(false) }

    mapViewState?.let { mapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = mapViewState,
            onMapClick = onMapClickHandler,
            onMarkerClick = onMarkerClickHandler,
            onMarkerDrag = onMarkerDragHandler,
            onMarkerAnimateStart = { isMarkerAnimating = true },
            onMarkerAnimateEnd = { isMarkerAnimating = false }
        ) {
            markers.forEach { markerState ->
                key(markerState.id) {
                    Marker(markerState)
                    Circle(
                        center = markerState.position,
                        radius = 1000,
                    )
                }
            }

            selectedMarker?.let {
                if (isMarkerAnimating == false) {
                    InfoBubble(
                        bubbleColor = bubbleColor,
                        state = infoBubbleState,
                    ) {
                        Column {
                            Text(
                                text =
                                    infoBubbleState.marker?.position?.toUrlValue()
                                        ?: "null",
                            )
                            Button(
                                onClick = {
                                    onDirectionButtonClick(it)
                                },
                            ) {
                                Text(
                                    text = "Change Icon Color",
                                )
                            }
                        }
//                        StoreCard(
//                            info = it.extra as Bundle,
//                            onClick = {
//                                onDirectionButtonClick(it)
//                            },
//                        )
                    }
                }
            }
        }
    }
}
