package com.mapconductor.example.pages.animation

import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.toast.ToastMessage
import java.util.NoSuchElementException
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private data class Spot(
    val id: String,
    val name: String,
    val animation: MarkerAnimation,
    val point: GeoPoint,
)

private val exampleSpots =
    listOf(
        Spot(
            id = "s1",
            name = "Bounce",
            animation = MarkerAnimation.Bounce,
            point = GeoPoint.fromLatLong(21.3069, -157.8583),
        ),
        Spot(
            id = "s2",
            name = "Drop",
            animation = MarkerAnimation.Drop,
            point = GeoPoint.fromLatLong(21.4513, -158.0152),
        ),
    )

interface AnimationPageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val messages: StateFlow<List<ToastMessage>>
    val allMarkers: List<MarkerState>

    val circleCenter: GeoPoint

    fun getSpotName(markerId: String): String

    fun onMapViewChanged(state: MapViewState<*>)

    fun onMarkerClick(clicked: MarkerState)
}

class AnimationPageViewModelImpl :
    ViewModel(),
    AnimationPageViewModel {
    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 21.382314,
                    longitude = -157.933097,
                ),
            zoom = 9.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    override val circleCenter = GeoPoint.fromLatLong(21.382314, -157.933097)

    private val markers: Map<String, MarkerState> =
        exampleSpots.associate { spot ->
            spot.id to
                MarkerState(
                    id = spot.id,
                    position = spot.point,
                    icon = DefaultIcon(label = spot.name),
                    animation = null,
                )
        }
    override val allMarkers: List<MarkerState> get() = markers.values.toList()

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    override fun getSpotName(markerId: String): String =
        exampleSpots.firstOrNull { it.id == markerId }?.name
            ?: throw NoSuchElementException("Spot not found: $markerId")

    override fun onMapViewChanged(state: MapViewState<*>) {
        this._mapViewState.value = state
    }

    override fun onMarkerClick(clicked: MarkerState) {
        // When you want to activate the marker, set the animation for the marker.
        Log.i("AnimationPageViewModelImpl", "onMarkerClick: ${clicked.id}")

        val spot = exampleSpots.firstOrNull { it.id == clicked.id }
        clicked.setAnimation(spot?.animation)
    }
}
