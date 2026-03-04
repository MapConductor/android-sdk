package com.mapconductor.example.pages.marker.animation

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
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

interface AnimationPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    val messages: StateFlow<List<ToastMessage>>
    val allMarkers: StateFlow<List<MarkerState>>

    fun getSpotName(markerId: String): String

    fun onMapViewChanged(state: MapViewStateInterface<*>)

    fun onMarkerClick(clicked: MarkerState)
}

class AnimationPageViewModel :
    ViewModel(),
    AnimationPageViewModelInterface {
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

    private val _allMarkers: MutableStateFlow<List<MarkerState>> = MutableStateFlow(emptyList())
    override val allMarkers: StateFlow<List<MarkerState>> = _allMarkers.asStateFlow()

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    override fun getSpotName(markerId: String): String =
        exampleSpots.firstOrNull { it.id == markerId }?.name
            ?: throw NoSuchElementException("Spot not found: $markerId")

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
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
