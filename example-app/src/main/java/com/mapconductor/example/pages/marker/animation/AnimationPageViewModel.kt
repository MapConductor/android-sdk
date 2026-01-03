package com.mapconductor.example.pages.marker.animation

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.toast.ToastMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private data class Spot(
    val id: String,
    val name: String,
    val animation: MarkerAnimation,
    val point: GeoPointImpl,
)

private val exampleSpots =
    listOf(
        Spot(
            id = "s1",
            name = "Bounce",
            animation = MarkerAnimation.Bounce,
            point = GeoPointImpl.fromLatLong(21.3069, -157.8583),
        ),
        Spot(
            id = "s2",
            name = "Drop",
            animation = MarkerAnimation.Drop,
            point = GeoPointImpl.fromLatLong(21.4513, -158.0152),
        ),
    )

interface AnimationPageViewModel {
    val initCameraPosition: MapCameraPositionImpl
    val mapViewState: StateFlow<MapViewState<*>?>
    val messages: StateFlow<List<ToastMessage>>
    val allMarkers: StateFlow<List<MarkerState>>

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
        MapCameraPositionImpl(
            position =
                GeoPointImpl.fromLatLong(
                    latitude = 21.382314,
                    longitude = -157.933097,
                ),
            zoom = 9.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    private val _allMarkers: MutableStateFlow<List<MarkerState>> = MutableStateFlow(emptyList())
    override val allMarkers: StateFlow<List<MarkerState>> = _allMarkers.asStateFlow()

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    override fun getSpotName(markerId: String): String =
        exampleSpots.firstOrNull { it.id == markerId }?.name
            ?: throw NoSuchElementException("Spot not found: $markerId")

    override fun onMapViewChanged(state: MapViewState<*>) {
        this._mapViewState.value = state
        this._allMarkers.value =
            exampleSpots.map { spot ->
                MarkerState(
                    id = spot.id,
                    position = spot.point,
                    icon = DefaultMarkerIcon(label = spot.name),
                    animation = null,
                    extra = spot.animation,
                    onClick = this::onMarkerClick,
                )
            }
    }

    override fun onMarkerClick(clicked: MarkerState) {
        // When you want to activate the marker, set the animation for the marker.
        clicked.animate(clicked.extra as? MarkerAnimation)
    }
}
