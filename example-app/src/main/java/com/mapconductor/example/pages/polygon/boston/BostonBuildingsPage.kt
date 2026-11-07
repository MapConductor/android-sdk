package com.mapconductor.example.pages.polygon.boston

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.compose.polygon.Polygons
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard

@Composable
fun BostonBuildingsPage(
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit = {},
) {
    val context = LocalContext.current
    // `adb shell am start ... --es camera "42.3406,-71.0458,14.3" --es grid 4` opens
    // a fixed view, so a measurement sees the same thing on every provider.
    val viewModel =
        remember {
            val intent = (context as? Activity)?.intent
            BostonBuildingsViewModel(
                assets = context.assets,
                initCamera = intent?.getStringExtra("camera"),
                initGrid = intent?.getStringExtra("grid")?.toIntOrNull(),
            )
        }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()
        val states = viewModel.polygonStates.collectAsState()
        val grid = viewModel.grid.collectAsState()
        val stats = viewModel.stats.collectAsState()
        val attribution = viewModel.attribution.collectAsState()
        val tapped = viewModel.tapped.collectAsState()

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
            GRID_SIZES.forEach { size ->
                val label = if (size == 1) "×1" else "$size×$size"
                if (size == grid.value) {
                    Button(onClick = {}) { Text(label) }
                } else {
                    FilledTonalButton(onClick = { viewModel.setGrid(size) }) { Text(label) }
                }
            }
        }

        MessageCard(
            title =
                stats.value?.let { "Boston buildings: ${it.polygons} polygons, ${it.vertices} vertices" }
                    ?: "Boston buildings",
            isCardOpen = true,
            maxHeight = 160.dp,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
        ) {
            stats.value?.let { Text("States built in ${it.buildMillis} ms") }
            tapped.value?.let { Text(if (it.height == null) "Height unknown" else "Height ${it.height} m") }
            Text(attribution.value, fontSize = 11.sp)
        }
    }
}
