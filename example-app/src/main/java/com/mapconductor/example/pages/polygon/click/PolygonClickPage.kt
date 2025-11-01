package com.mapconductor.example.pages.polygon.click

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard

@Composable
fun PolygonClickPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel = remember { PolygonClickPageViewModelImpl() }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()
        val marker = viewModel.markerState.collectAsState()
        val message = viewModel.message.collectAsState()

        mapViewState.value?.let {
            MapViewContainer(
                state = it,
                onPolygonClick = viewModel::onPolygonClicked,
                onMapClick = viewModel::onMapClicked,
            ) {
                key(california) {
                    california.forEach { points ->
                        val state =
                            PolygonState(
                                points = points,
                                strokeColor = Color.Red.copy(alpha = 0.7f),
                                strokeWidth = 3.dp,
                                fillColor = Color.Blue.copy(alpha = 0.4f),
                            )
                        Polygon(state)
                    }
                }

                marker.value?.let { markerState ->
                    Marker(markerState)

                    InfoBubble(
                        marker = markerState,
                    ) {
                        Text(
                            text = message.value,
                            color = Color.Black,
                        )
                    }
                }
            }
        }

        MessageCard(
            title = "Polygon Example",
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
        ) {
            Text("Tap inside & outside the polygon!")
        }
    }
}
