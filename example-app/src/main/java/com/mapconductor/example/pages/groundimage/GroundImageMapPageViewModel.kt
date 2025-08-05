package com.mapconductor.example.pages.groundimage

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.circle.CircleClickEvent
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.spherical.calculatePositionAtDistance
import com.mapconductor.core.spherical.haversineDistance
import com.mapconductor.example.toast.ToastMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface GroundImageMapPageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val messages: StateFlow<List<ToastMessage>>

    val circleCenter: GeoPoint
    val radiusMeters: Double
    val centerMarker: MarkerState
    val edgeMarker: MarkerState
    val groundImageState: GroundImageState

    fun onMapViewChanged(state: MapViewState<*>)

    fun cameraReset(listener: MapViewState.MoveCameraCallback? = null)

    fun onMapClick(clicked: GeoPoint)

    fun onGroundImageClick(event: GroundImageClickEvent)

    fun onGroundImageChange(event: GroundImageClickEvent)

    fun showToast(text: String)

    fun removeToast(toastMessage: ToastMessage)
}

class GroundImageMapPageViewModelImpl :
    ViewModel(),
    GroundImageMapPageViewModel {
    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 21.382314,
                    longitude = -157.933097,
                ),
            zoom = 12.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

//    override val circleCenter = GeoPoint.fromLatLong(21.382314, -157.933097)
//
//    override val centerMarker =
//        MarkerState(
//            id = "center_marker",
//            position = circleCenter,
//            icon =
//                DefaultIcon(
//                    fillColor = Color.Red,
//                    strokeColor = Color.White,
//                    label = "C",
//                ),
//            draggable = false,
//        )
//
    private val _edgeMarker: MutableState<MarkerState> =
        mutableStateOf(
            MarkerState(
                id = "edge_marker",
                position =
                    calculatePositionAtDistance(
                        center = circleCenter,
                        distanceMeters = 1000.0,
                        bearingDegrees = 90.0, // East
                    ),
                icon =
                    DefaultIcon(
                        fillColor = Color.Green,
                        strokeColor = Color.White,
                        label = "E",
                    ),
                draggable = true,
            ),
        )
    override val edgeMarker: MarkerState
        get() = _edgeMarker.value
//
//    override val radiusMeters by derivedStateOf {
//        haversineDistance(circleCenter, _edgeMarker.value.position)
//    }
//
    private val _groundImageState: MutableState<GroundImageState> =
        mutableStateOf(
            GroundImageState(
                id = "groundImage",
            ),
        )
    override val groundImageState: GroundImageState
        get() = _groundImageState.value
//
//    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
//    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()
//
//    override fun onMapViewChanged(state: MapViewState<*>) {
//        this._mapViewState.value = state
//    }
//
//    override fun cameraReset(listener: MapViewState.MoveCameraCallback?) {
//        this.mapViewState.value?.moveCameraTo(
//            cameraPosition = initCameraPosition,
//            durationMs = 3000,
//            listener = listener,
//        )
//    }

    override fun onMapClick(clicked: GeoPoint) {
        showToast("Map clicked at: ${clicked.toUrlValue()}")
    }

    override fun onGroundImageClick(event: GroundImageClickEvent) {
        event.state.fillColor = Color.Blue.copy(alpha = 0.5f)
        showToast("GroundImage clicked - Radius: ${radiusMeters.toInt()}m")
    }

    override fun onGroundImageChange(event: GroundImageClickEvent) {
        event.state.fillColor = Color.Blue.copy(alpha = 0.5f)
        showToast("GroundImage clicked - Radius: ${radiusMeters.toInt()}m")
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
