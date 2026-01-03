package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineEvent
import com.mapconductor.core.polyline.PolylineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolylineClickPageViewModel {
    val initCameraPosition: MapCameraPositionImpl
    val mapViewState: StateFlow<MapViewState<*>?>

    val markers: StateFlow<List<MarkerState>>
    val polylineState: PolylineState

    fun onMapViewChanged(state: MapViewState<*>)

    fun onPolylineClicked(clicked: PolylineEvent)
}

class PolylineClickPageViewModelImpl :
    ViewModel(),
    PolylineClickPageViewModel {
    override val initCameraPosition =
        MapCameraPositionImpl(
            position =
                GeoPointImpl.fromLatLong(35.548852, 139.784086),
            zoom = 4.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    private val polylinePoints =
        mutableStateListOf(
            GeoPointImpl.fromLatLong(35.548852, 139.784086), // HND_AIR_PORT
            GeoPointImpl.fromLatLong(37.615223, -122.389979), // SFO_AIR_PORT
            GeoPointImpl.fromLatLong(21.324513, -157.925074), // HNL_AIR_PORT
        )

    private var _markers: MutableStateFlow<List<MarkerState>> = MutableStateFlow(emptyList())

    override val markers: StateFlow<List<MarkerState>> = _markers.asStateFlow()

    override val polylineState: PolylineState
        get() =
            PolylineState(
                id = "example_polyline",
                points = polylinePoints,
                strokeColor = Color.Red,
                strokeWidth = 4.dp,
                geodesic = true,
                onClick = this::onPolylineClicked,
            )

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewState<*>) {
        _markers.value = emptyList<MarkerState>()
        mapViewState.value?.cameraPosition?.let {
            state.moveCameraTo(it)
        }
        this._mapViewState.value = state
    }

    override fun onPolylineClicked(clicked: PolylineEvent) {
        _markers.value = _markers.value +
            MarkerState(
                position = clicked.clicked,
                animation = MarkerAnimation.Drop,
                icon =
                    DefaultMarkerIcon(
                        fillColor = clicked.state.strokeColor,
                    ),
            )
    }

    override fun onCleared() {
        super.onCleared()
    }
}
