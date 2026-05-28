package com.mapconductor.example.pages.map.tilt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.spherical.Spherical
import kotlin.collections.flatten
import kotlin.concurrent.timer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TiltMapPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val cameraPosition: StateFlow<MapCameraPosition>
    val disableSlider: StateFlow<Boolean>
    var tilt: Double
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    val polygons: List<PolygonState>

    fun onMapCameraMoveStart(point: GeoPoint)
    fun onMapCameraMoveEnd(point: GeoPoint)
    fun onMapViewChanged(state: MapViewStateInterface<*>)
}

class DistanceColorPair(
    val distance: Double,
    val color: Color,
)

class TiltMapPageViewModel(
    positions: List<DistanceColorPair>,
) : ViewModel(), TiltMapPageViewModelInterface {

    override val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(0.0, 1.0),
            zoom = 13.0,
        )
    private var currentPosition: MapCameraPosition = initCameraPosition

    override val polygons: List<PolygonState> = positions.map {
        val points = mutableListOf<GeoPoint>()
        for (heading in 0 .. 360 step 45) {
            points.add(
                Spherical.computeOffset(
                    origin = initCameraPosition.position,
                    distance = it.distance,
                    heading = heading.toDouble(),
                )
            )
        }
        PolygonState(
            points = points,
            fillColor = it.color.copy(alpha = 0.5f),
            strokeColor = Color.White,
            strokeWidth = 5.dp,
        )
    }

    private var _disableSlider: MutableStateFlow<Boolean> = MutableStateFlow<Boolean>(false)

    override val disableSlider: StateFlow<Boolean> = _disableSlider.asStateFlow()

    private var _cameraPosition: MutableStateFlow<MapCameraPosition> = MutableStateFlow(currentPosition)

    override val cameraPosition: StateFlow<MapCameraPosition> = _cameraPosition.asStateFlow()

    private var _mapViewState: MutableStateFlow<MapViewStateInterface<*>?> = MutableStateFlow(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        mapViewState.value?.cameraPosition?.let {
            state.moveCameraTo(it)
        }
        this._mapViewState.value = state
    }

    private var _tilt by mutableStateOf(initCameraPosition.tilt)

    override var tilt: Double
        get(): Double = _tilt

        set(angle) {
            if (_disableSlider.value) return
            _tilt = angle

            currentPosition = currentPosition.copy(
                tilt = angle,
            )
            _cameraPosition.value = currentPosition

            mapViewState.value?.moveCameraTo(currentPosition)
        }


    override fun onMapCameraMoveStart(point: GeoPoint) {
        _disableSlider.value = true
    }

    override fun onMapCameraMoveEnd(point: GeoPoint) {
        _disableSlider.value = false
        _mapViewState.value?.let {
            currentPosition = it.cameraPosition
        }
    }
}
