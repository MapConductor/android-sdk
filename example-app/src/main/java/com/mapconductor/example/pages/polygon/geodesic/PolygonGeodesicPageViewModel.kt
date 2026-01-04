package com.mapconductor.example.pages.polygon.geodesic

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolygonGeodesicPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    val markerState: StateFlow<MarkerState?>

    fun onMapViewChanged(state: MapViewStateInterface<*>)

    fun onPolygonClicked(event: PolygonEvent)
}

class PolygonGeodesicPageViewModel :
    ViewModel(),
    PolygonGeodesicPageViewModelInterface {
    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    private val _markerState = MutableStateFlow<MarkerState?>(null)
    override val markerState: StateFlow<MarkerState?> = _markerState.asStateFlow()

    override val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(30.0, 0.0),
            zoom = 1.0,
        )

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        _mapViewState.value = state
    }

    override fun onPolygonClicked(event: PolygonEvent) {
        _markerState.value =
            MarkerState(
                id = "clicked",
                position = event.clicked,
                icon =
                    DefaultMarkerIcon(
                        fillColor = event.state.fillColor.copy(alpha = 1.0f),
                    ),
            )
    }
}
