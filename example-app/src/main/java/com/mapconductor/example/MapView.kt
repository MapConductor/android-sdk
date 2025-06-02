package com.mapconductor.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.arcgis.ArcGISMapView
import com.mapconductor.arcgis.ArcGISMapViewState
import com.mapconductor.core.map.OnMapClickHandler
import com.mapconductor.core.map.MapViewScope
import com.mapconductor.core.map.MapViewState
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.googlemaps.GoogleMapsView
import com.mapconductor.here.HereMapView
import com.mapconductor.here.HereMapViewState
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.MapboxMapViewState

@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,
    state: MapViewState<*>? = null,
    onMapClick: OnMapClickHandler,
    content: @Composable MapViewScope.() -> Unit,
) {
    when (state) {
        is GoogleMapViewState -> GoogleMapsView(
            modifier = modifier,
            state = state,
            onMapClick = onMapClick,
            content = content,
        )
        is HereMapViewState -> HereMapView(
            modifier = modifier,
            state = state,
            onMapClick = onMapClick,
            content = content,
        )
        is MapboxMapViewState -> MapboxMapView(
            modifier = modifier,
            state = state,
            onMapClick = onMapClick,
            content = content,
        )
        is ArcGISMapViewState -> ArcGISMapView(
            modifier = modifier,
            state = state,
            onMapClick = onMapClick,
            content = content,
        )
        else -> throw IllegalStateException("unknown state")
    }
}