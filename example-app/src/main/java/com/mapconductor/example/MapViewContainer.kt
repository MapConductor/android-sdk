package com.mapconductor.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.arcgis.map.ArcGISMapView
import com.mapconductor.arcgis.map.ArcGISMapViewState
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapView
import com.mapconductor.here.HereViewState
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.MapboxViewState
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.MapLibreViewState

@Composable
@Suppress("DEPRECATION")
fun MapViewContainer(
    modifier: Modifier = Modifier,
    state: MapViewStateInterface<*>? = null,
    markerTiling: MarkerTilingOptions? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMapLongClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    content: (@Composable MapViewScope.() -> Unit)? = null,
) {
    @Suppress("UNCHECKED_CAST")
    when (state) {
        is GoogleMapViewState ->
            GoogleMapView(
                modifier = modifier,
                state = state,
                markerTiling = markerTiling,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is HereViewState ->
            HereMapView(
                modifier = modifier,
                state = state,
                markerTiling = markerTiling,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is MapboxViewState ->
            MapboxMapView(
                modifier = modifier,
                state = state,
                markerTiling = markerTiling,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is ArcGISMapViewState ->
            ArcGISMapView(
                modifier = modifier,
                state = state,
                markerTiling = markerTiling,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is MapLibreViewState ->
            MapLibreMapView(
                modifier = modifier,
                state = state,
                markerTiling = markerTiling,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        else -> throw IllegalStateException("unknown state")
    }
}
