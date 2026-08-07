package com.mapconductor.simplemapapp.docdemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState

/**
 * /cms/projection-zoom — the same zoom value across providers.
 *
 * The page's sample hands one MapCameraPosition to a MapLibre state and a Google
 * Maps state and keeps them in step from onCameraMoveEnd. Filmed by dragging and
 * pinching one half and watching the other follow at a matching scale, which is
 * what the per-provider ZoomAltitudeConverter buys you.
 *
 * One thing the snippet leaves out because it would only obscure the point: a
 * plain echo between two maps re-enters itself, since moveCameraTo makes the
 * other side emit onCameraMoveEnd right back. `syncing` is that latch.
 */

private val SHARED =
    MapCameraPosition(
        position = GeoPoint.fromLatLong(35.6812, 139.7671),
        zoom = 14.0,
    )

@Composable
fun ZoomCompareDemo(modifier: Modifier = Modifier) {
    val maplibre =
        rememberMapLibreMapViewState(
            mapDesign = MapLibreDesign.OsmBrightEn,
            cameraPosition = SHARED,
        )
    val googlemaps = rememberGoogleMapViewState(cameraPosition = SHARED)

    var syncing by remember { mutableStateOf(false) }
    var zoomLeft by remember { mutableStateOf(SHARED.zoom) }
    var zoomRight by remember { mutableStateOf(SHARED.zoom) }

    Column(modifier.fillMaxSize()) {
        DemoCaption(
            "One zoom value, two providers",
            "drag or pinch either half — the other keeps the same scale",
        )
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(Modifier.weight(1f).fillMaxSize()) {
                MapLibreMapView(
                    state = maplibre,
                    modifier = Modifier.fillMaxSize(),
                    onCameraMove = { zoomLeft = it.zoom },
                    onCameraMoveEnd = { position ->
                        zoomLeft = position.zoom
                        if (!syncing) {
                            syncing = true
                            googlemaps.moveCameraTo(position)
                            syncing = false
                        }
                    },
                )
                MapBadge("MapLibre", Modifier.align(Alignment.TopStart).padding(8.dp))
                DemoReadout(
                    listOf("zoom %.2f".format(zoomLeft)),
                    Modifier.align(Alignment.BottomStart).padding(8.dp),
                )
            }
            Box(Modifier.weight(1f).fillMaxSize()) {
                GoogleMapView(
                    state = googlemaps,
                    modifier = Modifier.fillMaxSize(),
                    onCameraMove = { zoomRight = it.zoom },
                    onCameraMoveEnd = { position ->
                        zoomRight = position.zoom
                        if (!syncing) {
                            syncing = true
                            maplibre.moveCameraTo(position)
                            syncing = false
                        }
                    },
                )
                MapBadge("Google Maps", Modifier.align(Alignment.TopStart).padding(8.dp))
                DemoReadout(
                    listOf("zoom %.2f".format(zoomRight)),
                    Modifier.align(Alignment.BottomStart).padding(8.dp),
                )
            }
        }
        DemoControls {
            RowScopeDemoButton("z 6", Modifier.weight(1f)) { both(maplibre, googlemaps, 6.0) }
            RowScopeDemoButton("z 11", Modifier.weight(1f)) { both(maplibre, googlemaps, 11.0) }
            RowScopeDemoButton("z 16", Modifier.weight(1f)) { both(maplibre, googlemaps, 16.0) }
        }
    }
}

/** Jump both sides to one zoom level, so the comparison is not left to a gesture. */
private fun both(
    maplibre: com.mapconductor.maplibre.MapLibreViewState,
    googlemaps: com.mapconductor.googlemaps.GoogleMapViewState,
    zoom: Double,
) {
    val target = MapCameraPosition(position = SHARED.position, zoom = zoom)
    maplibre.moveCameraTo(target, durationMillis = 900)
    googlemaps.moveCameraTo(target, durationMillis = 900)
}
