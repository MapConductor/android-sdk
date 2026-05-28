package com.mapconductor.example.pages.map.tilt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.Markers
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TiltMapPage(onToggleSidebar: () -> Unit = {}) {

    val coroutineScope = rememberCoroutineScope()
    val debounceMs = 80L
    val viewModel = remember {
        TiltMapPageViewModel(
        listOf(
            DistanceColorPair(
                distance = 24000.0,
                color = Color(red = 234, green = 51, blue = 247),
            ),
            DistanceColorPair(
                distance = 12000.0,
                color = Color(red = 108, green = 34, blue = 119),
            ),
            DistanceColorPair(
                distance = 6400.0,
                color = Color(red = 0, green = 0, blue = 244),
            ),
            DistanceColorPair(
                distance = 3200.0,
                color = Color(red = 82, green = 181, blue = 203),
            ),
            DistanceColorPair(
                distance = 1600.0,
                color = Color(red = 0, green = 255, blue = 0),
            ),
            DistanceColorPair(
                distance = 800.0,
                color = Color(red = 56, green = 127, blue = 34),
            ),
            DistanceColorPair(
                distance = 400.0,
                color = Color(red = 255, green = 255, blue = 85),
            ),
            DistanceColorPair(
                distance = 200.0,
                color = Color(red = 240, green = 146, blue = 54),
            ),
            DistanceColorPair(
                distance = 0.0,
                color = Color(red = 235, green = 72, blue = 64),
            ),
        )
    ) }

    val tilt = viewModel.tilt

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState().value

        mapViewState?.let {
            MapViewContainer(
                state = mapViewState,
            ) {
                viewModel.polygons.forEach { polygonState ->
                    Polygon(polygonState)
                }
            }
        }

        // Message Card
        MessageCard(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
            title = "Camera tilt angle",
        ) {
            Column {
                Text("tilt: ${"%.2f".format(tilt)}")
                Slider(
                    value = viewModel.tilt.toFloat(),
                    onValueChange = { newValue ->
                            coroutineScope.launch {
                                delay(debounceMs)
                                viewModel.tilt = newValue.toDouble()
                            }
                    },
                    onValueChangeFinished = {
                    },
                    valueRange = -90.0f..90.0f, // スライダー範囲
                    steps = 0,
                )
            }
        }
    }
}
