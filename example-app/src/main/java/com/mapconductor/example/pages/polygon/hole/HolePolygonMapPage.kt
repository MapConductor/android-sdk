package com.mapconductor.example.pages.polygon.hole

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.polygon.Polygon
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard

@Composable
fun HolePolygonMapPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel = remember { HolePolygonMapPageViewModel() }

    val polygonState =
        remember {
            PolygonState(
                points =
                    listOf(
                        GeoPoint(85.0, 90.0),
                        GeoPoint(85.0, 0.1),
                        GeoPoint(85.0, -90.0),
                        GeoPoint(85.0, -179.9),
                        GeoPoint(0.0, -179.9),
                        GeoPoint(-85.0, -179.9),
                        GeoPoint(-85.0, -90.0),
                        GeoPoint(-85.0, 0.1),
                        GeoPoint(-85.0, 90.0),
                        GeoPoint(-85.0, 179.9),
                        GeoPoint(0.0, 179.9),
                        GeoPoint(85.0, 179.9),
                    ),
                holes =
                    listOf(
                        listOf(
                            GeoPoint(43.10086924222251, 141.35290903949243),
                            GeoPoint(43.04444342582366, 141.4118953480885),
                            GeoPoint(43.05060149394299, 141.30656265416695),
                        ),
                        listOf(
                            GeoPoint(43.06035050410283, 141.31990479539704),
                            GeoPoint(43.038284739487004, 141.33324693662706),
                            GeoPoint(43.049062034871525, 141.28690055130158),
                        ),
                    ),
                fillColor = Color(0xCC787880),
                strokeColor = Color.Red,
                strokeWidth = 2.dp,
            )
        }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        mapViewState.value?.let {
            MapViewContainer(state = it) {
                Polygon(polygonState)
            }
        }

        MessageCard(
            title = "Hole Polygon Example",
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
        ) {
            Text(
                """
                A world-covering polygon with two triangular holes near Sapporo.
                The grey overlay covers the entire map except the hole areas.
                """.trimIndent(),
            )
        }
    }
}
