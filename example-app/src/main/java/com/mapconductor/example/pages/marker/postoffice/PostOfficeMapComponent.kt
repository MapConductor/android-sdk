package com.mapconductor.example.pages.marker.postoffice

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.Markers
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.postoffice.PostOffice
import com.mapconductor.postoffice.PostOfficeInfoView
import android.util.Log

@Composable
fun PostOfficeMapComponent(
    modifier: Modifier = Modifier,
    mapViewState: MapViewStateInterface<*>,
    selectedMarker: MarkerState?,
    markers: List<MarkerState> = emptyList<MarkerState>(),
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onInfoWndClick: ((PostOffice) -> Unit)? = null,
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor by remember {
        mutableStateOf(if (darkTheme) Color.Black else Color.White)
    }

    MapViewContainer(
        modifier = modifier,
        state = mapViewState,
        onMapLoaded = onMapLoaded,
        onMapClick = onMapClick,
        onCameraMoveEnd = {
            Log.i("MapConductorTiling", "---->zoom=${mapViewState.cameraPosition.zoom}")
        }
    ) {
        Markers(markers)

        selectedMarker?.let {
            InfoBubble(
                bubbleColor = bubbleColor,
                marker = it,
            ) {
                PostOfficeInfoView(
                    info = it.extra as PostOffice,
                    onClick = onInfoWndClick,
                )
            }
        }
    }
}
