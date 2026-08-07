package com.mapconductor.simplemapapp.docdemo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.MapViewScope
import com.mapconductor.compose.marker.Marker
import com.mapconductor.compose.polyline.Polyline
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState

/**
 * /cms/switching-providers — switching providers at runtime.
 *
 * The page's sample keeps one state per provider, declares its overlays once
 * outside the branch, and hands the camera across on the way over. What the
 * video has to show is that the marker and the polyline are in the same place
 * before and after the swap, so both are put somewhere unmistakable — the
 * Imperial Palace moat — and the camera is left where the viewer put it.
 */

private val START =
    MapCameraPosition(
        position = GeoPoint.fromLatLong(35.6812, 139.7671),
        zoom = 12.0,
    )

private val ROUTE =
    listOf(
        GeoPoint.fromLatLong(35.6586, 139.7454),
        GeoPoint.fromLatLong(35.6762, 139.7633),
        GeoPoint.fromLatLong(35.6852, 139.7528),
        GeoPoint.fromLatLong(35.7101, 139.8107),
    )

@Composable
fun SwitchingProvidersDemo(modifier: Modifier = Modifier) {
    // One state per provider.
    val maplibre =
        rememberMapLibreMapViewState(
            mapDesign = MapLibreDesign.OsmBrightEn,
            cameraPosition = START,
        )
    val googlemaps = rememberGoogleMapViewState(cameraPosition = START)
    var useMapLibre by remember { mutableStateOf(true) }

    val markerState = remember { MarkerState(id = "palace", position = ROUTE[2]) }
    val routeState =
        remember {
            PolylineState(
                id = "route",
                points = ROUTE,
                strokeColor = Color(0xFFE5484D),
                strokeWidth = 5.dp,
            )
        }

    // The overlays are declared outside the branch, so swapping does not touch
    // them.
    val overlays: @Composable MapViewScope.() -> Unit = {
        Marker(markerState)
        Polyline(routeState)
    }

    // Hand the camera you were just looking at to the side you are switching to.
    LaunchedEffect(useMapLibre) {
        if (useMapLibre) {
            maplibre.moveCameraTo(googlemaps.cameraPosition)
        } else {
            googlemaps.moveCameraTo(maplibre.cameraPosition)
        }
    }

    Column(modifier.fillMaxSize()) {
        DemoCaption(
            "Swapping the map view at runtime",
            "the overlays and the camera are declared once",
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (useMapLibre) {
                MapLibreMapView(
                    state = maplibre,
                    modifier = Modifier.fillMaxSize(),
                    content = overlays,
                )
            } else {
                GoogleMapView(
                    state = googlemaps,
                    modifier = Modifier.fillMaxSize(),
                    content = overlays,
                )
            }
            MapBadge(
                if (useMapLibre) "MapLibre" else "Google Maps",
                Modifier.align(Alignment.TopStart).padding(12.dp),
            )
        }
        DemoControls {
            RowScopeDemoButton(
                if (useMapLibre) "Switch to Google Maps" else "Switch to MapLibre",
                Modifier.weight(1f),
            ) { useMapLibre = !useMapLibre }
        }
    }
}
