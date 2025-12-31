package com.mapconductor.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.map.ArcGISMapView
import com.mapconductor.arcgis.map.ArcGISMapViewStateImpl
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.GoogleMapViewStateImpl
import com.mapconductor.here.HereActualMarker
import com.mapconductor.here.HereMapView
import com.mapconductor.here.HereViewStateImpl
import com.mapconductor.mapbox.MapboxActualMarker
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.MapboxViewStateImpl
import com.mapconductor.maplibre.MapLibreActualMarker
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.MapLibreViewStateImpl

@Composable
@Suppress("DEPRECATION")
fun MapViewContainer(
    modifier: Modifier = Modifier,
    renderingStrategy: MarkerRenderingStrategy<*>? = null,
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
                markerRenderingStrategy = renderingStrategy as? MarkerRenderingStrategy<GoogleMapActualMarker>?,
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
                markerRenderingStrategy = renderingStrategy as? MarkerRenderingStrategy<HereActualMarker>?,
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
                markerRenderingStrategy = renderingStrategy as? MarkerRenderingStrategy<MapboxActualMarker>?,
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
                markerRenderingStrategy = renderingStrategy as? MarkerRenderingStrategy<ArcGISActualMarker>?,
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
                markerRenderingStrategy = renderingStrategy as? MarkerRenderingStrategy<MapLibreActualMarker>?,
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
