package com.mapconductor.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mapconductor.arcgis.ArcGISMapViewState
import com.mapconductor.core.map.MapViewState
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapViewState
import com.mapconductor.mapbox.MapboxMapViewState

@Composable
fun HeaderCard(
    googleMapState: GoogleMapViewState,
    mapboxMapState: MapboxMapViewState,
    hereMapState: HereMapViewState,
    arcGISMapState: ArcGISMapViewState,
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit,
    onSdkSelectChange: (selected: MapViewState<*>) -> Unit,
) {
}
