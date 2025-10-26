package com.mapconductor.example.pages.polygon.click

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolygonClickPageViewModel {
    val initCameraPosition: MapCameraPositionImpl
    val mapViewState: StateFlow<MapViewState<*>?>
    val markerState: StateFlow<MarkerState?>
    val message: StateFlow<String>

    fun onMapViewChanged(state: MapViewState<*>)

    fun onMapClicked(clicked: GeoPointImpl)

    fun onPolygonClicked(event: PolygonEvent)
}

class PolygonClickPageViewModelImpl :
    ViewModel(),
    PolygonClickPageViewModel {
    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    private val _markerState = MutableStateFlow<MarkerState?>(null)
    override val markerState: StateFlow<MarkerState?> = _markerState.asStateFlow()

    private val _message = MutableStateFlow<String>("")
    override val message: StateFlow<String> = _message.asStateFlow()

    override val initCameraPosition =
        MapCameraPositionImpl(
            position = GeoPointImpl(36.73030, -120.24512),
            zoom = 5.0,
        )

    override fun onMapViewChanged(state: MapViewState<*>) {
        _mapViewState.value = state
    }

    override fun onMapClicked(clicked: GeoPointImpl) {
        _message.value = "Outside"

        _markerState.value =
            MarkerState(
                id = "clicked",
                position = clicked,
            )
    }

    override fun onPolygonClicked(event: PolygonEvent) {
        val latLng = GeoPointImpl.from(event.clicked).toUrlValue()
        _message.value = "Inside\n$latLng"

        _markerState.value =
            MarkerState(
                id = "clicked",
                position = event.clicked,
            )
    }
}
