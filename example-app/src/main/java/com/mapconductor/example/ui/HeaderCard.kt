package com.mapconductor.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.arcgis.ArcGISMapViewStateImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.googlemaps.GoogleMapViewStateImpl
import com.mapconductor.here.HereViewStateImpl
import com.mapconductor.mapbox.MapboxViewStateImpl

@Composable
fun HeaderCard(
    googleMapState: GoogleMapViewStateImpl,
    mapboxMapState: MapboxViewStateImpl,
    hereMapState: HereViewStateImpl,
    arcGISMapState: ArcGISMapViewStateImpl,
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit,
    onSdkSelectChange: (selected: MapViewState<*>) -> Unit,
) {
}
