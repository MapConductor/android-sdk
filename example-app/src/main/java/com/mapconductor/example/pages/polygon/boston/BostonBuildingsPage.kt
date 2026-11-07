package com.mapconductor.example.pages.polygon.boston

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    // `adb shell am start ... --es camera "42.35545,-71.0605,17" --es area 2` opens
    // a fixed view, so a measurement sees the same thing on every provider.
    val viewModel =
        remember {
            val intent = (context as? Activity)?.intent
            BostonBuildingsViewModel(
                context = context.applicationContext,
                initCamera = intent?.getStringExtra("camera"),
                initArea = intent?.getStringExtra("area")?.toIntOrNull(),
                initDataset =
                    when (intent?.getStringExtra("data")) {
                        "buildings" -> Dataset.CITY_BUILDINGS
                        "parcels" -> Dataset.CITY_PARCELS
                        else -> null
                    },
            )
        }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()
        val states = viewModel.polygonStates.collectAsState()
        val area = viewModel.area.collectAsState()
        val dataset = viewModel.dataset.collectAsState()
        val loadError = viewModel.loadError.collectAsState()
        val stats = viewModel.stats.collectAsState()
        val attribution = viewModel.attribution.collectAsState()
        val tap = viewModel.tap.collectAsState()

        mapViewState.value?.let {
            MapViewContainer(
                state = it,
                modifier = modifier.padding(paddingValues),
                onMapClick = viewModel::onMapClick,
            ) {
                Polygons(states.value)
            }
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(top = paddingValues.calculateTopPadding() + 16.dp, start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Dataset.entries.forEach { entry ->
                    if (entry == dataset.value) {
                        Button(onClick = {}) { Text(entry.label) }
                    } else {
                        FilledTonalButton(onClick = { viewModel.setDataset(entry) }) { Text(entry.label) }
                    }
                }
            }
            if (dataset.value == Dataset.DOWNTOWN) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AREAS.forEachIndexed { level, label ->
                        if (level == area.value) {
                            Button(onClick = {}) { Text(label) }
                        } else {
                            FilledTonalButton(onClick = { viewModel.setArea(level) }) { Text(label) }
                        }
                    }
                }
            }
        }

        MessageCard(
            title =
                stats.value?.let { "Boston ${dataset.value.label}: ${it.polygons} polygons, ${it.vertices} vertices" }
                    ?: "Boston ${dataset.value.label}",
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
            loadError.value?.let { Text(it) }
            stats.value?.let { Text("Loaded in ${it.loadMillis} ms, states built in ${it.buildMillis} ms") }
            tap.value?.let { tap ->
                val building = tap.building
                Text(
                    if (building == null) {
                        "No building here"
                    } else {
                        val height = building.height?.let { "$it m" } ?: "height unknown"
                        "${building.name ?: "Building"} · $height · ${building.id.take(8)}"
                    },
                )
            }
            Text(attribution.value, fontSize = 11.sp)
        }
    }
}
