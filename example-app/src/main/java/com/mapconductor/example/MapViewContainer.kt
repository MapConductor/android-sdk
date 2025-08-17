package com.mapconductor.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.arcgis.ArcGISMapView
import com.mapconductor.arcgis.ArcGISMapViewState
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polyline.OnPolylineEventHandler
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
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onGroundImageClick: OnGroundImageEventHandler? = null,
    content: @Composable MapViewScope.() -> Unit,
) {
    when (state) {
        is GoogleMapViewState ->
            GoogleMapsView(
                modifier = modifier,
                state = state,
                onMapClick = onMapClick,
                onMarkerClick = onMarkerClick,
                onMarkerDragStart = onMarkerDragStart,
                onMarkerDrag = onMarkerDrag,
                onMarkerDragEnd = onMarkerDragEnd,
                onMarkerAnimateStart = onMarkerAnimateStart,
                onMarkerAnimateEnd = onMarkerAnimateEnd,
                onCircleClick = onCircleClick,
                onPolylineClick = onPolylineClick,
                onGroundImageClick = onGroundImageClick,
                content = content,
            )

        is HereMapViewState ->
            HereMapView(
                modifier = modifier,
                state = state,
                onMapClick = onMapClick,
                onMarkerClick = onMarkerClick,
                onMarkerDragStart = onMarkerDragStart,
                onMarkerDrag = onMarkerDrag,
                onMarkerDragEnd = onMarkerDragEnd,
                onMarkerAnimateStart = onMarkerAnimateStart,
                onMarkerAnimateEnd = onMarkerAnimateEnd,
                onGroundImageClick = onGroundImageClick,
                onCircleClick = onCircleClick,
                onPolylineClick = onPolylineClick,
                content = content,
            )

        is MapboxMapViewState ->
            MapboxMapView(
                modifier = modifier,
                state = state,
                onMapClick = onMapClick,
                onMarkerClick = onMarkerClick,
                onMarkerDragStart = onMarkerDragStart,
                onMarkerDrag = onMarkerDrag,
                onMarkerDragEnd = onMarkerDragEnd,
                onMarkerAnimateStart = onMarkerAnimateStart,
                onMarkerAnimateEnd = onMarkerAnimateEnd,
                onCircleClick = onCircleClick,
                onPolylineClick = onPolylineClick,
                onGroundImageClick = onGroundImageClick,
                content = content,
            )

        is ArcGISMapViewState ->
            ArcGISMapView(
                modifier = modifier,
                state = state,
                onMapClick = onMapClick,
                onMarkerClick = onMarkerClick,
                onMarkerDragStart = onMarkerDragStart,
                onMarkerDrag = onMarkerDrag,
                onMarkerDragEnd = onMarkerDragEnd,
                onMarkerAnimateStart = onMarkerAnimateStart,
                onMarkerAnimateEnd = onMarkerAnimateEnd,
                onCircleClick = onCircleClick,
                onGroundImageClick = onGroundImageClick,
                onPolylineClick = onPolylineClick,
                content = content,
            )

        else -> throw IllegalStateException("unknown state")
    }
}
