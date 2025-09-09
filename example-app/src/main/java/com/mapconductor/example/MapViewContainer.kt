package com.mapconductor.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.arcgis.ArcGISMapView
import com.mapconductor.arcgis.ArcGISMapViewStateImpl
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.googlemaps.GoogleMapViewStateImpl
import com.mapconductor.googlemaps.GoogleMapsView
import com.mapconductor.here.HereMapView
import com.mapconductor.here.HereViewStateImpl
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.MapboxViewStateImpl

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
    onPolygonClick: OnPolygonEventHandler? = null,
    onGroundImageClick: OnGroundImageEventHandler? = null,
    content: @Composable MapViewScope.() -> Unit,
) {
    when (state) {
        is GoogleMapViewStateImpl ->
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
                onPolygonClick = onPolygonClick,
                onGroundImageClick = onGroundImageClick,
                content = content,
            )

        is HereViewStateImpl ->
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
                onCircleClick = onCircleClick,
                onPolylineClick = onPolylineClick,
                onPolygonClick = onPolygonClick,
                content = content,
            )

        is MapboxViewStateImpl ->
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
                onPolygonClick = onPolygonClick,
                content = content,
            )

        is ArcGISMapViewStateImpl ->
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
                onPolylineClick = onPolylineClick,
                onPolygonClick = onPolygonClick,
                content = content,
            )

        else -> throw IllegalStateException("unknown state")
    }
}
