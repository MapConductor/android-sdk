package com.mapconductor.example.pages.polygon.geodesic

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolygonGeodesicPageViewModel {
    val initCameraPosition: MapCameraPositionImpl
    val mapViewState: StateFlow<MapViewState<*>?>
    val markerState: StateFlow<MarkerState?>

    fun onMapViewChanged(state: MapViewState<*>)

    fun onPolygonClicked(event: PolygonEvent)
}

class PolygonGeodesicPageViewModelImpl :
    ViewModel(),
    PolygonGeodesicPageViewModel {
    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    private val _markerState = MutableStateFlow<MarkerState?>(null)
    override val markerState: StateFlow<MarkerState?> = _markerState.asStateFlow()

    override val initCameraPosition =
        MapCameraPositionImpl(
            position = GeoPointImpl(30.0, 0.0),
            zoom = 1.0,
        )

    override fun onMapViewChanged(state: MapViewState<*>) {
        _mapViewState.value = state
    }

    override fun onPolygonClicked(event: PolygonEvent) {
        _markerState.value =
            MarkerState(
                id = "clicked",
                position = event.clicked,
                icon =
                    DefaultIcon(
                        fillColor = event.state.fillColor.copy(alpha = 1.0f),
                    ),
            )
    }
}
