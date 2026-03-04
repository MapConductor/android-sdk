package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineEvent
import com.mapconductor.core.polyline.PolylineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolylineClickPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>

    val markers: StateFlow<List<MarkerState>>
    val polylineState: PolylineState

    fun onMapViewChanged(state: MapViewStateInterface<*>)

    fun onPolylineClicked(clicked: PolylineEvent)
}

class PolylineClickPageViewModel :
    ViewModel(),
    PolylineClickPageViewModelInterface {
    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(35.548852, 139.784086),
            zoom = 4.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    private val polylinePoints =
        mutableStateListOf(
            GeoPoint.fromLatLong(35.548852, 139.784086), // HND_AIR_PORT
            GeoPoint.fromLatLong(37.615223, -122.389979), // SFO_AIR_PORT
            GeoPoint.fromLatLong(21.324513, -157.925074), // HNL_AIR_PORT
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

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
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
