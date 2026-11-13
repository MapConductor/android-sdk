package com.mapconductor.example.pages.polygon.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.polygon.Polygons
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard

@Composable
fun PolygonsMapPage(
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit = {},
) {
    val viewModel = remember { PolygonsMapPageViewModel() }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()
        val states = viewModel.polygonStates.collectAsState()

        mapViewState.value?.let {
            MapViewContainer(
                state = it,
                modifier = modifier.padding(paddingValues),
            ) {
                Polygons(states.value)
            }
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(top = paddingValues.calculateTopPadding() + 16.dp, start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = viewModel::removeRow) { Text("−") }
            Button(onClick = viewModel::addRow) { Text("+") }
            Button(onClick = viewModel::recolor) { Text("Recolor") }
        }

        MessageCard(
            title = "Polygons (${states.value.size})",
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
                A grid of hexagons registered with a single Polygons(states) call.
                + / − add or remove a row, Recolor replaces every state with a new fill,
                and tapping a hexagon changes its own fill in place.
                """.trimIndent(),
            )
        }
    }
}
