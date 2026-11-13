package com.mapconductor.example.pages.polyline.boston

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.compose.MapViewScope
import com.mapconductor.compose.circle.Circle
import com.mapconductor.compose.info.InfoBubble
import com.mapconductor.compose.marker.Marker
import com.mapconductor.compose.polygon.Polygons
import com.mapconductor.compose.polyline.PolylineTiling
import com.mapconductor.compose.polyline.Polylines
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.pages.polygon.boston.LoadingOverlay
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import java.util.Locale
import android.app.Activity

@Composable
fun BostonRoadsPage(
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit = {},
) {
    val context = LocalContext.current
    // `adb shell am start ... --es page boston-roads --es camera "42.3555,-71.0605,15" --es tiling off`
    // opens a fixed view, so a measurement sees the same thing on every provider.
    val viewModel =
        remember {
            val intent = (context as? Activity)?.intent
            BostonRoadsViewModel(
                context = context.applicationContext,
                initCamera = intent?.getStringExtra("camera"),
                initTiling =
                    when (intent?.getStringExtra("tiling")) {
                        "off" -> false
                        "on" -> true
                        else -> null
                    },
                initOverlap = intent?.getStringExtra("overlap") == "on",
            )
        }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()
        val states = viewModel.polylineStates.collectAsState()
        val tiling = viewModel.tiling.collectAsState()
        val overlap = viewModel.overlap.collectAsState()
        val loadError = viewModel.loadError.collectAsState()
        val stats = viewModel.stats.collectAsState()
        val tap = viewModel.tap.collectAsState()
        val progress = viewModel.progress.collectAsState()

        // Two frames after the list reaches Polylines(), for the stats. The loading overlay waits for onAdded.
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
                // Before Polylines(): read when the roads are handed over.
                PolylineTiling(viewModel.tilingOptions(tiling.value))
                Polylines(states.value)
                if (overlap.value) OverlapCheck()
                val shownTap = tap.value
                val road = shownTap?.road
                val point = shownTap?.point
                if (road != null && point != null) {
                    InfoBubble(position = point, borderWidth = 1.dp, borderColor = Color(0x33000000)) {
                        RoadInfo(road)
                    }
                }
            }
        }

        progress.value?.let { LoadingOverlay(it, modifier = Modifier.padding(paddingValues), shapes = "roads") }

        Column(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(top = paddingValues.calculateTopPadding() + 16.dp, start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(true to "Tiles", false to "Native").forEach { (tiled, label) ->
                    if (tiling.value == tiled) {
                        Button(onClick = {}) { Text(label) }
                    } else {
                        FilledTonalButton(onClick = { viewModel.setTiling(tiled) }) { Text(label) }
                    }
                }
                if (overlap.value) {
                    Button(onClick = { viewModel.setOverlap(false) }) { Text("Overlap check") }
                } else {
                    FilledTonalButton(onClick = { viewModel.setOverlap(true) }) { Text("Overlap check") }
                }
            }
        }

        MessageCard(
            title =
                stats.value?.let { "Boston roads: ${it.roads} polylines, ${it.points} points" }
                    ?: "Boston roads",
            isCardOpen = true,
            maxHeight = 300.dp,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .widthIn(max = 480.dp)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
        ) {
            loadError.value?.let { Text(it) }
            RoadClassLegend(modifier = Modifier.padding(vertical = 4.dp))
            stats.value?.let {
                val how = if (it.tiled) "tiles" else "native"
                val frame = it.frameMillis?.let { ms -> ", next frame $ms ms" } ?: ""
                val added = it.addedMillis?.let { ms -> ", on the map $ms ms" } ?: ""
                Text("[$how] Load ${it.loadMillis} ms, states ${it.buildMillis} ms$frame$added")
            }
            tap.value?.let { tap ->
                val road = tap.road
                Text(
                    if (road == null) {
                        "No road here"
                    } else {
                        "${road.name ?: road.roadClass.label} · ${road.roadClass.label} · ${
                            formatLength(
                                lengthMetres(
                                    road
                                )
                            )
                        }"
                    },
                )
            }
            Text(ROADS_ATTRIBUTION, fontSize = 11.sp)
        }
    }
}

/** The road classes: a line as wide and as coloured as on the map, two to a row. */
@Composable
fun RoadClassLegend(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        RoadClass.entries.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { roadClass ->
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier
                                    .width(18.dp)
                                    .height(roadClass.width.dp)
                                    .background(roadClass.color, RoadedEnds),
                        )
                        Text(roadClass.label, fontSize = 11.sp, modifier = Modifier.padding(start = 6.dp))
                    }
                }
                if (row.size == 1) Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

private val RoadedEnds = RoundedCornerShape(50)

/** What a tapped road carries, shown in an InfoBubble at the point on the road nearest the tap. */
@Composable
fun RoadInfo(road: Road) {
    val rows = mutableListOf<Pair<String, String>>()
    rows += "Class" to road.roadClass.label
    road.name?.let { rows += "Name" to it }
    rows += "Length" to formatLength(lengthMetres(road))
    rows += "Points" to "${road.path.size / 2}"
    Column {
        rows.forEach { (key, value) ->
            Row {
                Text(
                    key,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569),
                    modifier = Modifier.width(64.dp),
                )
                Text(value, fontSize = 12.sp, color = Color(0xFF0F172A))
            }
        }
    }
}

/** "123 m", or kilometres from 1,000 m up. */
fun formatLength(metres: Double): String =
    if (metres >= 1000) {
        String.format(Locale.US, "%.2f km", metres / 1000)
    } else {
        String.format(Locale.US, "%,.0f m", metres)
    }

/**
 * The stacking check: over the roads downtown, a grid of 2,500 small polygons
 * (enough to be drawn as polygon tiles), a circle and a marker. From the bottom
 * the map shows polygon tiles, the circle, the road tiles, then the marker.
 */
@Composable
private fun MapViewScope.OverlapCheck() {
    val polygons =
        remember {
            List(2500) { i ->
                val lat = 42.3500 + (i / 50) * 0.0002
                val lng = -71.0660 + (i % 50) * 0.0002
                PolygonState(
                    points =
                        listOf(
                            GeoPoint(lat, lng),
                            GeoPoint(lat, lng + 0.00016),
                            GeoPoint(lat + 0.00016, lng + 0.00016),
                            GeoPoint(lat + 0.00016, lng),
                        ),
                    id = "overlap-$i",
                    fillColor = Color(0xCC7C3AED),
                    strokeColor = Color(0xFF4C1D95),
                    strokeWidth = 1.dp,
                )
            }
        }
    Polygons(polygons)
    Circle(
        center = GeoPoint(42.3550, -71.0610),
        radiusMeters = 250.0,
        id = "overlap-circle",
        fillColor = Color(0xCC0EA5E9),
        strokeColor = Color(0xFF0369A1),
    )
    Marker(position = GeoPoint(42.3550, -71.0610), id = "overlap-marker")
}
