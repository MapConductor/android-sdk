package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolylinePageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>

    val wayPointMarkers: List<MarkerState>
    val polylineState: PolylineState

    fun onMapViewChanged(state: MapViewStateInterface<*>)

    fun onMarkerDrag(dragged: MarkerState)
}

class PolylinePageViewModel :
    ViewModel(),
    PolylinePageViewModelInterface {
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
                    DefaultMarkerIcon(
                        fillColor = markerColor.toArgb(),
                        strokeColor = Color.Black.toArgb(),
                        label = label,
                    ),
                draggable = true,
                extra = index,
                onDragStart = this::onMarkerDrag,
                onDrag = this::onMarkerDrag,
                onDragEnd = this::onMarkerDrag,
            )
        }

    override val polylineState: PolylineState
        get() =
            PolylineState(
                id = "example_polyline",
                points = polylinePoints,
                strokeColor = Color.Red.toArgb(),
                strokeWidth = 4f,
                geodesic = true,
            )

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        mapViewState.value?.cameraPosition?.let {
            state.moveCameraTo(it)
        }
        this._mapViewState.value = state
    }

    override fun onMarkerDrag(dragged: MarkerState) {
        (dragged.extra as? Int)?.let { index ->
            if (index >= 0 && index < polylinePoints.size) {
                polylinePoints[index] = GeoPoint.from(dragged.position)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
