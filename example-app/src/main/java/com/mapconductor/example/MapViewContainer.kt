package com.mapconductor.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.arcgis.map.ArcGISMapView
import com.mapconductor.arcgis.map.ArcGISMapViewStateImpl
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.GoogleMapViewStateImpl
import com.mapconductor.here.HereMapView
import com.mapconductor.here.HereViewStateImpl
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.MapboxViewStateImpl
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.MapLibreViewStateImpl

@Composable
@Suppress("DEPRECATION")
fun MapViewContainer(
    modifier: Modifier = Modifier,
    state: MapViewState<*>? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    content: (@Composable MapViewScope.() -> Unit)? = null,
) {
    @Suppress("UNCHECKED_CAST")
    when (state) {
        is GoogleMapViewStateImpl ->
            GoogleMapView(
                modifier = modifier,
                state = state,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is HereViewStateImpl ->
            HereMapView(
                modifier = modifier,
                state = state,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is MapboxViewStateImpl ->
            MapboxMapView(
                modifier = modifier,
                state = state,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is ArcGISMapViewStateImpl ->
            ArcGISMapView(
                modifier = modifier,
                state = state,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is MapLibreViewStateImpl ->
            MapLibreMapView(
                modifier = modifier,
                state = state,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        else -> throw IllegalStateException("unknown state")
    }
}
