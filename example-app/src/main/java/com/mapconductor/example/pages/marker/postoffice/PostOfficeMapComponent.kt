package com.mapconductor.example.pages.marker.postoffice

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.Markers
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.postoffice.PostOffice
import com.mapconductor.postoffice.PostOfficeInfoView
import java.util.Locale
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
    onCameraMoveEnd: ((MapCameraPosition) -> Unit)? = null,
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor by remember {
        mutableStateOf(if (darkTheme) Color.Black else Color.White)
    }
    var cameraPosition by remember { mutableStateOf(mapViewState.cameraPosition) }

    Box(
        modifier = modifier,
    ) {
        MapViewContainer(
            modifier = Modifier.fillMaxSize(),
            state = mapViewState,
            onMapLoaded = onMapLoaded,
            onMapClick = onMapClick,
            onCameraMove = { position ->
                cameraPosition = position
            },
            onCameraMoveEnd = { position ->
                Log.i("MapConductorTiling", "---->zoom=${position.zoom}")
                cameraPosition = position
                onCameraMoveEnd?.invoke(position)
            },
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
        CameraInfoCard(
            modifier = Modifier.wrapContentSize(
                align = Alignment.TopStart,
            ),
            position = cameraPosition,
        )
    }
}

@Composable
private fun CameraInfoCard(
    modifier: Modifier,
    position: MapCameraPosition,
) {
    val fmt = remember { Locale.US }
    Card(modifier = modifier.padding(10.dp)) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = "Lat: ${String.format(fmt, "%.5f", position.position.latitude)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Lng: ${String.format(fmt, "%.5f", position.position.longitude)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Zoom: ${String.format(fmt, "%.2f", position.zoom)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Tilt: ${String.format(fmt, "%.1f°", position.tilt)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Bearing: ${String.format(fmt, "%.1f°", position.bearing)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Alt: ${String.format(fmt, "%.0f m", position.position.altitude ?: 0.0)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
