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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolylinePageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>

    val wayPointMarkers: List<MarkerState>
    val polylineState: PolylineState

    fun onMapViewChanged(state: MapViewState<*>)

    fun onPolylineClick(state: PolylineState)

    fun onMarkerDrag(dragged: MarkerState)
}

class PolylinePageViewModelImpl :
    ViewModel(),
    PolylinePageViewModel {
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

    private val polylinePoints =
        mutableStateListOf(
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
                val markerColor =
                    when {
                        index == 0 -> Color.Green
                        index == polylinePoints.size - 1 -> Color.Green
                        else -> Color.Yellow
                    }
                val label =
                    when {
                        index == 0 -> "S"
                        index == polylinePoints.size - 1 -> "E"
                        else -> "$index"
                    }
                MarkerState(
                    id = "waypoint_$index",
                    position = point,
                    icon =
                        DefaultIcon(
                            fillColor = markerColor,
                            strokeColor = Color.Black,
                            label = label,
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

    override fun onPolylineClick(state: PolylineState) {
        _polylineState.value.strokeColor = Color.Magenta
    }

    override fun onMarkerDrag(dragged: MarkerState) {
        val markerIndex = _wayPointMarkers.value.indexOfFirst { it.id == dragged.id }
        if (markerIndex < 0) return

        // 1. pointsを更新
        polylinePoints[markerIndex].latitude = dragged.position.latitude
        polylinePoints[markerIndex].longitude = dragged.position.longitude
    }

    override fun onCleared() {
        super.onCleared()
    }
}
