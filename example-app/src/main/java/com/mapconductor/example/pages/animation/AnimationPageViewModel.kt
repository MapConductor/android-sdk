package com.mapconductor.example.pages.animation

import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.toast.ToastMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AnimationPageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val messages: StateFlow<List<ToastMessage>>

    val circleCenter: GeoPoint
    val bounceMarker: MarkerState

    fun onMapViewChanged(state: MapViewState<*>)

    fun onMarkerClick(clicked: MarkerState)
}

class AnimationPageViewModelImpl :
    ViewModel(),
    AnimationPageViewModel {
    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()
    private val colors: List<Color> =
        listOf(
            Color.Blue.copy(0.2f),
            Color.Red.copy(alpha = 0.2f),
            Color.Green.copy(alpha = 0.2f),
            Color.Cyan.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.Magenta.copy(alpha = 0.2f),
        )

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

    override val circleCenter = GeoPoint.fromLatLong(21.382314, -157.933097)

    private val _bounceMarker =
        MarkerState(
            id = "bounce_marker",
            position = circleCenter,
            icon =
                DefaultIcon(
                    fillColor = colors[0],
                    strokeColor = Color.White,
                    label = "B",
                ),
            // If the marker is set animation on creating an instance,
            // the marker will be animated when the map will be opened.
            animation = MarkerAnimation.Bounce,
        )

    override val bounceMarker: MarkerState
        get() = _bounceMarker

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewState<*>) {
        this._mapViewState.value = state
    }

    override fun onMarkerClick(clicked: MarkerState) {
        // When you want to activate the marker, set the animation for the marker.
        clicked.animation = MarkerAnimation.Bounce
    }
}
