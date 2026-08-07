package com.mapconductor.simplemapapp.docdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.marker.Marker
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import android.os.SystemClock

/**
 * /cms/mapview — initialisation proceeding.
 *
 * The page's sample flips a `ready` flag in onMapLoaded, puts its own overlay up
 * until then, and declares a Marker that survives being declared before the map
 * exists. All three are what this films.
 *
 * The stage list is built from what an application can actually observe: the SDK
 * keeps InitState inside MapViewBase, so what is timestamped here is composition,
 * the map view appearing (getMapViewHolder stops returning null) and onMapLoaded.
 * A "Reload" button remounts the map so the sequence can be watched twice in one
 * take — the first run has a cold tile cache, the second a warm one, which is
 * itself worth seeing.
 */

private val ROUTE_BOUNDS =
    GeoRectBounds().apply {
        extend(GeoPoint.fromLatLong(35.6586, 139.7454))
        extend(GeoPoint.fromLatLong(35.7101, 139.8107))
    }

@Composable
fun LifecycleDemo(modifier: Modifier = Modifier) {
    var generation by remember { mutableStateOf(0) }

    Column(modifier.fillMaxSize()) {
        DemoCaption(
            "MapView initialisation",
            "declared → map view created → loaded",
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            // A new key tears the whole map down, so the next run starts from
            // NotStarted rather than from whatever the last one left behind.
            key(generation) { LifecycleMap() }
        }
        DemoControls {
            RowScopeDemoButton("Reload map", Modifier.weight(1f)) { generation++ }
        }
    }
}

@Composable
private fun LifecycleMap() {
    val mapViewState =
        rememberMapLibreMapViewState(
            mapDesign = MapLibreDesign.OsmBrightEn,
            cameraPosition =
                MapCameraPosition(
                    position = GeoPoint.fromLatLong(35.6812, 139.7671),
                    zoom = 11.0,
                ),
        )

    val startedAt = remember { SystemClock.elapsedRealtime() }
    val stages = remember { mutableStateListOf<String>() }

    fun mark(name: String) {
        stages.add("%5d ms  %s".format(SystemClock.elapsedRealtime() - startedAt, name))
    }

    var ready by remember { mutableStateOf(false) }
    val markerState = remember { MarkerState(id = "tower", position = GeoPoint.fromLatLong(35.6586, 139.7454)) }

    LaunchedEffect(Unit) {
        mark("composed — Marker already declared")
        // Poll for the holder: it is the first moment the provider's view exists,
        // and the only part of the SDK's staging an app can see from outside.
        while (mapViewState.getMapViewHolder() == null) {
            kotlinx.coroutines.delay(16)
        }
        mark("map view created")
    }

    Box(Modifier.fillMaxSize()) {
        MapLibreMapView(
            state = mapViewState,
            modifier = Modifier.fillMaxSize(),
            // Called once, when the tiles have finished drawing.
            onMapLoaded = { state ->
                ready = true
                mark("onMapLoaded — camera fitted")
                state.fitBounds(ROUTE_BOUNDS, padding = 48)
            },
        ) {
            // Declarations here are not lost before MapCreated — they are queued
            // internally.
            Marker(markerState)
        }

        if (!ready) {
            // Your own loading overlay, if you want one. The map does not need
            // blocking.
            Box(
                Modifier.fillMaxSize().background(Color(0xCC0B1016)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        DemoReadout(
            lines = if (stages.isEmpty()) listOf("starting…") else stages.toList(),
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
        )
    }
}
