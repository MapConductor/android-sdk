package com.mapconductor.simplemapapp.docdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState

/**
 * /cms/reading-camera — reading values while the map moves.
 *
 * The page's sample keeps the last MapCameraPosition from onCameraMove and reads
 * latitude, longitude, zoom and the visible region straight off it. Filmed by
 * dragging and pinching so the readout is visibly chasing the gesture, and the
 * onCameraMoveEnd counter only ticks when the map settles — that difference is
 * the whole section.
 */

@Composable
fun ReadingCameraDemo(modifier: Modifier = Modifier) {
    val mapViewState =
        rememberMapLibreMapViewState(
            mapDesign = MapLibreDesign.OsmBrightEn,
            cameraPosition =
                MapCameraPosition(
                    position = GeoPoint.fromLatLong(35.6812, 139.7671),
                    zoom = 13.0,
                ),
        )

    var camera by remember { mutableStateOf<MapCameraPosition?>(null) }
    var moves by remember { mutableIntStateOf(0) }
    var settles by remember { mutableIntStateOf(0) }

    Column(modifier.fillMaxSize()) {
        DemoCaption(
            "onCameraMove vs onCameraMoveEnd",
            "one fires throughout the gesture, one when it settles",
        )
        MapLibreMapView(
            state = mapViewState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            // Fires throughout the gesture — keep this handler cheap.
            onCameraMove = { position ->
                camera = position
                moves++
            },
            // Once, when the map settles. Fetching and redrawing belong here.
            onCameraMoveEnd = { position ->
                camera = position
                settles++
            },
        )

        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xE6101418))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            val position = camera
            DemoReadout(
                lines =
                    if (position == null) {
                        listOf("drag the map…")
                    } else {
                        listOf(
                            "lat   %.5f".format(position.position.latitude),
                            "lng   %.5f".format(position.position.longitude),
                            "zoom  %.2f".format(position.zoom),
                            // Read the visible region off the value you were
                            // handed; do not rebuild it.
                            position.visibleRegion?.bounds?.let { bounds ->
                                "SW %.3f,%.3f  NE %.3f,%.3f".format(
                                    bounds.southWest?.latitude ?: 0.0,
                                    bounds.southWest?.longitude ?: 0.0,
                                    bounds.northEast?.latitude ?: 0.0,
                                    bounds.northEast?.longitude ?: 0.0,
                                )
                            } ?: "visibleRegion  —",
                            "onCameraMove $moves   onCameraMoveEnd $settles",
                        )
                    },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
