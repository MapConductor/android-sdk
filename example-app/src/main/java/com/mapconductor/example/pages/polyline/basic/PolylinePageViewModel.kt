package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolylinePageViewModel {
    val initCameraPosition: MapCameraPositionImpl
    val mapViewState: StateFlow<MapViewState<*>?>

    val wayPointMarkers: List<MarkerState>
    val polylineState: PolylineState

    fun onMapViewChanged(state: MapViewState<*>)

    fun onMarkerDrag(dragged: MarkerState)
}

class PolylinePageViewModelImpl :
    ViewModel(),
    PolylinePageViewModel {
    override val initCameraPosition =
        MapCameraPositionImpl(
            position =
                GeoPointImpl.fromLatLong(
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
            GeoPointImpl.fromLatLong(21.382314, -157.933097), // Honolulu center
            GeoPointImpl.fromLatLong(21.385314, -157.930097), // Northeast
            GeoPointImpl.fromLatLong(21.387314, -157.935097), // Northwest
            GeoPointImpl.fromLatLong(21.380314, -157.937097), // Southwest
            GeoPointImpl.fromLatLong(21.378314, -157.930097), // Southeast
            GeoPointImpl.fromLatLong(21.382314, -157.933097), // Back to center
        )

    override val wayPointMarkers: List<MarkerState> =
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
                extra = index,
            )
        }

    override val polylineState: PolylineState
        get() =
            PolylineState(
                id = "example_polyline",
                points = polylinePoints,
                strokeColor = Color.Red,
                strokeWidth = 4.dp,
                geodesic = true,
            )

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewState<*>) {
        mapViewState.value?.cameraPosition?.let {
            state.moveCameraTo(it)
        }
        this._mapViewState.value = state
    }

    override fun onMarkerDrag(dragged: MarkerState) {
        (dragged.extra as? Int)?.let { index ->
            if (index >= 0 && index < polylinePoints.size) {
                polylinePoints[index] = GeoPointImpl.from(dragged.position)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
