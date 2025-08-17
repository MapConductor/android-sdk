package com.mapconductor.example.pages.groundimage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.groundimage.GroundImageEvent
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.toast.ToastMessage
import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface GroundImageMapPageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val messages: StateFlow<List<ToastMessage>>

    val southWest: MarkerState
    val northEast: MarkerState
    val imageResources: GroundImageResources
    val image: Drawable
    var opacity: Float
    val groundImageState: GroundImageState

    fun onMapViewChanged(state: MapViewState<*>)

    fun onGroundImageClick(clicked: GroundImageEvent)

    fun onMarkerDrag(dragged: MarkerState)

    fun showToast(text: String)

    fun removeToast(toastMessage: ToastMessage)
}

class GroundImageMapPageViewModelImpl(
    override val imageResources: GroundImageResources,
) : ViewModel(),
    GroundImageMapPageViewModel {
    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 40.7430785,
                    longitude = -74.175995,
                ),
            zoom = 12.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewState<*>) {
        this._mapViewState.value = state
    }

    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    override val southWest: MarkerState =
        MarkerState(
            id = "south_west",
            position =
                GeoPoint(
                    latitude = 40.712216,
                    longitude = -74.22655,
                ),
            icon =
                DefaultIcon(
                    fillColor = Color.Blue,
                    strokeColor = Color.White,
                    label = "SW",
                ),
            draggable = true,
        )

    override val northEast: MarkerState =
        MarkerState(
            id = "north_east",
            position =
                GeoPoint(
                    latitude = 40.773941,
                    longitude = -74.12544,
                ),
            icon =
                DefaultIcon(
                    fillColor = Color.Red,
                    strokeColor = Color.White,
                    label = "NE",
                ),
            draggable = true,
        )

    override var opacity by mutableStateOf(0.5f)

    override var image by mutableStateOf(imageResources.image)

    private var bounds by mutableStateOf(
        GeoRectBounds(
            southWest = GeoPoint.from(southWest.position),
            northEast = GeoPoint.from(northEast.position),
        ),
    )

    override val groundImageState
        get() =
            GroundImageState(
                id = "groundImage",
                bounds = bounds,
                image = image,
                opacity = opacity,
            )

    override fun onGroundImageClick(clicked: GroundImageEvent) {
        if (clicked.state.image == imageResources.image) {
            image = imageResources.clickedImage
        } else {
            image = imageResources.image
        }

        showToast("Ground Image clicked.")
    }

    override fun onMarkerDrag(dragged: MarkerState) {
        bounds = GeoRectBounds().also {
            it.extend(southWest.position)
            it.extend(northEast.position)
        }
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
