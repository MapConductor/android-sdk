package com.mapconductor.simplemapapp.docdemo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.mapconductor.compose.marker.Marker
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState

/**
 * /cms/geopoint-bounds — "Where bounds are used".
 *
 * The page's sample builds a GeoRectBounds by extending it one point at a time
 * and hands it to fitBounds. Filmed from a country-wide view so the framing the
 * rectangle produces is the visible result, and reset in the same take so the
 * before and after can be compared without cutting.
 */

private val SPOTS =
    listOf(
        "Tokyo Tower" to GeoPoint.fromLatLong(35.6586, 139.7454),
        "Skytree" to GeoPoint.fromLatLong(35.7101, 139.8107),
        "Imperial Palace" to GeoPoint.fromLatLong(35.6852, 139.7528),
    )

/** Wide enough that all three markers are a single dot — the state to fit from. */
private val WIDE =
    MapCameraPosition(
        position = GeoPoint.fromLatLong(36.5, 138.0),
        zoom = 4.5,
    )

@Composable
fun FitBoundsDemo(modifier: Modifier = Modifier) {
    val mapViewState =
        rememberMapLibreMapViewState(
            mapDesign = MapLibreDesign.OsmBrightEn,
            cameraPosition = WIDE,
        )

    // Start from an empty rectangle and widen it one point at a time.
    val bounds =
        remember {
            GeoRectBounds().apply { SPOTS.forEach { (_, point) -> extend(point) } }
        }
    var fitted by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        DemoCaption(
            "GeoRectBounds → fitBounds",
            "three points, one rectangle, one camera",
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            MapLibreMapView(state = mapViewState, modifier = Modifier.fillMaxSize()) {
                SPOTS.forEachIndexed { i, (_, point) ->
                    Marker(MarkerState(id = "spot-$i", position = point))
                }
            }
            DemoReadout(
                lines =
                    listOf(
                        "SW  %.4f, %.4f".format(
                            bounds.southWest?.latitude ?: 0.0,
                            bounds.southWest?.longitude ?: 0.0,
                        ),
                        "NE  %.4f, %.4f".format(
                            bounds.northEast?.latitude ?: 0.0,
                            bounds.northEast?.longitude ?: 0.0,
                        ),
                        if (fitted) "fitBounds(padding = 64)" else "camera: not fitted",
                    ),
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            )
        }
        DemoControls {
            RowScopeDemoButton("Show all", Modifier.weight(1f)) {
                // padding is screen margin in logical pixels; at 0 the outermost
                // point hugs the edge.
                if (!bounds.isEmpty) {
                    mapViewState.fitBounds(bounds, padding = 64)
                    fitted = true
                }
            }
            RowScopeDemoButton("Back out", Modifier.weight(1f)) {
                mapViewState.moveCameraTo(WIDE, durationMillis = 1200)
                fitted = false
            }
        }
    }
}
