package com.mapconductor.example.pages.map.basic

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.mapconductor.compose.info.InfoBubble
import com.mapconductor.compose.marker.Marker
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.OnMapEventHandler
import com.mapconductor.core.marker.DrawableDefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.R

@Composable
fun StoreMapComponent(
    modifier: Modifier = Modifier,
    mapViewState: MapViewStateInterface<*>?,
    selectedMarker: MarkerState?,
    markers: List<MarkerState> = emptyList<MarkerState>(),
    onDirectionButtonClick: OnMarkerEventHandler = {},
    onMapClick: OnMapEventHandler = {},
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor by remember {
        mutableStateOf(if (darkTheme) Color.Black else Color.White)
    }
    val context = LocalContext.current

    val icons =
        mapOf(
            "coffee_bean" to
                DrawableDefaultIcon(
                    backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.coffee_bean)!!,
                ),
            "honolulu_coffee" to
                DrawableDefaultIcon(
                    backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.honolulu_coffee)!!,
                ),
            "coffee_extra" to
                DrawableDefaultIcon(
                    backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.coffee_extra)!!,
                ),
            "starbucks" to
                DrawableDefaultIcon(
                    backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.starbucks)!!,
                ),
        )
    val markerList =
        remember {
            markers.map { state ->
                (state.extra as StoreInfo).let { info ->
                    val storeIcon = info.store
                    state.copy(
                        icon = icons[storeIcon],
                    )
                }
            }
        }

    mapViewState?.let { currentMapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = currentMapViewState,
            onMapClick = onMapClick,
        ) {
            markerList.forEach { markerState -> Marker(markerState) }

            selectedMarker?.let {
                InfoBubble(
                    bubbleColor = bubbleColor,
                    marker = it,
                ) {
                    StoreInfoView(
                        info = it.extra as StoreInfo,
                        onClick = {
                            onDirectionButtonClick(it)
                        },
                    )
                }
            }
        }
    }
}
