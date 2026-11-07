package com.mapconductor.example.pages.polygon.boston

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.compose.info.InfoBubble
import com.mapconductor.compose.polygon.Polygons
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import android.app.Activity

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
        val progress = viewModel.progress.collectAsState()

        // Two frames after the list reaches Polygons(), for the stats. The loading overlay waits for onAdded.
        LaunchedEffect(states.value) {
            val shown = states.value
            withFrameNanos {}
            withFrameNanos {}
            viewModel.onDrawn(shown)
        }

        mapViewState.value?.let {
            MapViewContainer(
                state = it,
                modifier = modifier.padding(paddingValues),
                onMapClick = viewModel::onMapClick,
            ) {
                Polygons(states.value)
                val shownTap = tap.value
                val building = shownTap?.building
                val point = shownTap?.point
                if (building != null && point != null) {
                    InfoBubble(position = point, borderWidth = 1.dp, borderColor = Color(0x33000000)) {
                        PolygonInfo(building)
                    }
                }
            }
        }

        progress.value?.let { LoadingOverlay(it, modifier = Modifier.padding(paddingValues)) }

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
            maxHeight = 260.dp,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    // On a tablet the card would otherwise run under the whole map.
                    .widthIn(max = PANEL_MAX_WIDTH)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
        ) {
            loadError.value?.let { Text(it) }
            if (dataset.value == Dataset.CITY_PARCELS) LandUseLegend(modifier = Modifier.padding(vertical = 4.dp))
            stats.value?.let {
                val frame = it.frameMillis?.let { ms -> ", next frame $ms ms" } ?: ""
                val added = it.addedMillis?.let { ms -> ", on the map $ms ms" } ?: ""
                Text("Load ${it.loadMillis} ms, states ${it.buildMillis} ms$frame$added")
            }
            tap.value?.let { tap ->
                val building = tap.building
                val use = building?.use
                Text(
                    when {
                        building == null -> "No building here"
                        use != null -> "${building.name} · ${use.label}"
                        else -> {
                            val height = building.height?.let { "$it m" } ?: "height unknown"
                            "${building.name ?: "Building"} · $height · ${building.id.take(8)}"
                        }
                    },
                )
            }
            Text(attribution.value, fontSize = 11.sp)
        }
    }
}

/** Wide enough for the title and two legend columns without wrapping. */
private val PANEL_MAX_WIDTH = 480.dp
