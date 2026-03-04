package com.mapconductor.example.pages.polygon.click

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolygonClickPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    val markerState: StateFlow<MarkerState?>
    val message: StateFlow<String>

    fun onMapViewChanged(state: MapViewStateInterface<*>)

    fun onMapClicked(clicked: GeoPoint)

    fun onPolygonClicked(event: PolygonEvent)
}

class PolygonClickPageViewModel :
    ViewModel(),
    PolygonClickPageViewModelInterface {
    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    private val _markerState = MutableStateFlow<MarkerState?>(null)
    override val markerState: StateFlow<MarkerState?> = _markerState.asStateFlow()

    private val _message = MutableStateFlow<String>("")
    override val message: StateFlow<String> = _message.asStateFlow()

    override val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(36.73030, -120.24512),
            zoom = 5.0,
        )

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        _mapViewState.value = state
    }

    override fun onMapClicked(clicked: GeoPoint) {
        _message.value = "Outside"

        _markerState.value =
            MarkerState(
                id = "clicked",
                position = clicked,
            )
    }

    override fun onPolygonClicked(event: PolygonEvent) {
        val latLng = GeoPoint.from(event.clicked).toUrlValue()
        _message.value = "Inside\n$latLng"

        _markerState.value =
            MarkerState(
                id = "clicked",
                position = event.clicked,
            )
    }
}
