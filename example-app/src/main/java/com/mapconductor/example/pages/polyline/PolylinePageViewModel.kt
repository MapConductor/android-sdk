package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.toast.ToastMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolylinePageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val messages: StateFlow<List<ToastMessage>>

    val wayPointMarkers: List<MarkerState>
    val polylineState: PolylineState

    fun onMapViewChanged(state: MapViewState<*>)

    fun onMarkerClick(clicked: MarkerState)

    fun onMapClick(clicked: GeoPoint)

    fun onPolylineClick(state: PolylineState)

    fun onMarkerDrag(dragged: MarkerState)

    fun showToast(text: String)

    fun removeToast(toastMessage: ToastMessage)
}

class PolylinePageViewModelImpl :
    ViewModel(),
    PolylinePageViewModel {
    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 21.382314,
                    longitude = -157.933097,
                ),
            zoom = 15.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    private val polylinePoints = mutableStateListOf(
            GeoPoint.fromLatLong(21.382314, -157.933097), // Honolulu center
            GeoPoint.fromLatLong(21.385314, -157.930097), // Northeast
            GeoPoint.fromLatLong(21.387314, -157.935097), // Northwest
            GeoPoint.fromLatLong(21.380314, -157.937097), // Southwest
            GeoPoint.fromLatLong(21.378314, -157.930097), // Southeast
            GeoPoint.fromLatLong(21.382314, -157.933097), // Back to center
        )

    private val _wayPointMarkers: MutableState<List<MarkerState>> =
        mutableStateOf(
            polylinePoints.mapIndexed { index, point ->
                MarkerState(
                    id = "waypoint_$index",
                    position = point,
                    icon =
                        DefaultIcon(
                            fillColor =
                                if (index == 0 ||
                                    index == polylinePoints.size - 1
                                ) {
                                    Color.Green
                                } else {
                                    Color.Blue
                                },
                            strokeColor = Color.White,
                            label =
                                if (index ==
                                    0
                                ) {
                                    "S"
                                } else if (index == polylinePoints.size - 1) {
                                    "E"
                                } else {
                                    "$index"
                                },
                        ),
                    draggable = true,
                )
            },
        )

    override val wayPointMarkers: List<MarkerState>
        get() = _wayPointMarkers.value

    private val _polylineState: MutableState<PolylineState> =
        mutableStateOf(
            PolylineState(
                id = "example_polyline",
                points = polylinePoints,
                strokeColor = Color.Red,
                strokeWidth = 4.dp,
                geodesic = true,
            ),
        )

    override val polylineState: PolylineState
        get() = _polylineState.value

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewState<*>) {
        this._mapViewState.value = state
    }

    override fun onMarkerClick(clicked: MarkerState) {
        val markerLabel = (clicked.icon as? DefaultIcon)?.label ?: "Marker"
        showToast("Waypoint $markerLabel clicked")
    }

    override fun onMapClick(clicked: GeoPoint) {
        showToast("Map clicked at: ${clicked.toUrlValue()}")
    }

    override fun onPolylineClick(state: PolylineState) {
        _polylineState.value.strokeColor = Color.Magenta
        showToast("Polyline clicked - ${state.points.size} points")
    }

    override fun onMarkerDrag(dragged: MarkerState) {
        val markerIndex = _wayPointMarkers.value.indexOfFirst { it.id == dragged.id }
        if (markerIndex < 0) return

        // 1. pointsを更新
        polylinePoints[markerIndex].latitude = dragged.position.latitude
        polylinePoints[markerIndex].longitude = dragged.position.longitude
    }

    override fun showToast(text: String) {
        this._messages.value = this._messages.value + ToastMessage(text = text)
    }

    override fun removeToast(toastMessage: ToastMessage) {
        this._messages.value = this._messages.value.filter { it != toastMessage }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
