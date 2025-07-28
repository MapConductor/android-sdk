package com.mapconductor.example.pages.circle

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
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.toast.ToastMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

interface CirclePageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val messages: StateFlow<List<ToastMessage>>

    val circleCenter: GeoPoint
    val radiusMeters: Double
    val centerMarker: MarkerState
    val edgeMarker: MarkerState
    val circleState: CircleState

    fun changeState(state: MapViewState<*>)
    fun cameraReset(listener: MapViewState.MoveCameraCallback? = null)
    fun onMarkerClick(clicked: MarkerState)
    fun onMapClick(clicked: GeoPoint)
    fun onCircleClick(event: CircleClickEvent)
    fun onMarkerDrag(dragged: MarkerState)
    fun showToast(text: String)
    fun removeToast(toastMessage: ToastMessage)
}

class CirclePageViewModelImpl : ViewModel(), CirclePageViewModel {
    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    override val initCameraPosition = MapCameraPosition(
        position = GeoPoint.fromLatLong(
            latitude = 21.382314,
            longitude = -157.933097,
        ),
        zoom = 12.0,
        bearing = 0.0,
        tilt = 0.0,
        paddings = null,
    )

    override val circleCenter = GeoPoint.fromLatLong(21.382314, -157.933097)

    override val centerMarker = MarkerState(
        id = "center_marker",
        position = circleCenter,
        icon = DefaultIcon(
            fillColor = Color.Red,
            strokeColor = Color.White,
            label = "C"
        ),
        draggable = false
    )

    private val _edgeMarker: MutableState<MarkerState> = mutableStateOf(
        MarkerState(
            id = "edge_marker",
            position = calculatePositionAtDistance(
                center = circleCenter,
                distanceMeters = 1000.0,
                bearingDegrees = 90.0 // East
            ),
            icon = DefaultIcon(
                fillColor = Color.Green,
                strokeColor = Color.White,
                label = "E"
            ),
            draggable = true
        )
    )
    override val edgeMarker: MarkerState
        get() = _edgeMarker.value

    override val radiusMeters by derivedStateOf {
        calculateDistance(circleCenter, _edgeMarker.value.position)
    }

    private val _circleState: MutableState<CircleState> = mutableStateOf(
        CircleState(
            id = "circle",
            center = circleCenter,
            radius = 1000.0, // Initial radius
            strokeColor = Color.Blue,
            strokeWidth = 2.dp,
            fillColor = Color.Blue.copy(alpha = 0.2f)
        )
    )
    override val circleState: CircleState
        get() = _circleState.value

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    override fun changeState(newState: MapViewState<*>) {
        this._mapViewState.value = newState
    }

    override fun cameraReset(listener: MapViewState.MoveCameraCallback?) {
        this.mapViewState.value?.moveCameraTo(
            cameraPosition = initCameraPosition,
            durationMs = 3000,
            listener = listener,
        )
    }

    override fun onMarkerClick(clicked: MarkerState) {
        showToast("${clicked.icon?.let { (it as? DefaultIcon)?.label } ?: "Marker"} clicked")
    }

    override fun onMapClick(clicked: GeoPoint) {
        showToast("Map clicked at: ${clicked.toUrlValue()}")
    }

    override fun onCircleClick(event: CircleClickEvent) {
        event.state.fillColor = Color.Blue.copy(alpha = 0.5f)
        showToast("Circle clicked - Radius: ${radiusMeters.toInt()}m")
    }

    override fun onMarkerDrag(dragged: MarkerState) {

        // Update circle radius
        _circleState.value.radiusMeters = calculateDistance(circleCenter, _edgeMarker.value.position)

//        showToast("Radius updated: ${radiusMeters.toInt()}m")
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

    // Calculate distance between two GeoPoints using Haversine formula
    private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
        val earthRadiusKm = 6371.0

        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLngRad = Math.toRadians(point2.longitude - point1.longitude)

        val a = sin(deltaLatRad / 2) * sin(deltaLatRad / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLngRad / 2) * sin(deltaLngRad / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c * 1000 // Convert to meters
    }

    // Calculate a position at a specific distance and bearing from a center point
    private fun calculatePositionAtDistance(
        center: GeoPoint,
        distanceMeters: Double,
        bearingDegrees: Double
    ): GeoPoint {
        val earthRadiusKm = 6371.0
        val distanceKm = distanceMeters / 1000.0
        val bearingRad = Math.toRadians(bearingDegrees)

        val lat1Rad = Math.toRadians(center.latitude)
        val lng1Rad = Math.toRadians(center.longitude)

        val lat2Rad = Math.asin(
            sin(lat1Rad) * cos(distanceKm / earthRadiusKm) +
            cos(lat1Rad) * sin(distanceKm / earthRadiusKm) * cos(bearingRad)
        )

        val lng2Rad = lng1Rad + atan2(
            sin(bearingRad) * sin(distanceKm / earthRadiusKm) * cos(lat1Rad),
            cos(distanceKm / earthRadiusKm) - sin(lat1Rad) * sin(lat2Rad)
        )

        return GeoPoint.fromLatLong(
            latitude = Math.toDegrees(lat2Rad),
            longitude = Math.toDegrees(lng2Rad)
        )
    }
}
