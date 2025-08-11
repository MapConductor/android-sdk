package com.mapconductor.example.pages.groundimage

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.room.util.copy
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.groundimage.GroundImageClickEvent
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.example.toast.ToastMessage
import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface GroundImageMapPageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val messages: StateFlow<List<ToastMessage>>

    val bounds: GeoRectBounds
    val imageResources: GroundImageResources
    val image: Drawable
    val opacity: Float
    val groundImageState: GroundImageState

    fun onMapViewChanged(state: MapViewState<*>)

    fun cameraReset(listener: MapViewState.MoveCameraCallback? = null)

    fun onMapClick(clicked: GeoPoint)

    fun onGroundImageClick(clicked: GroundImageClickEvent)

    fun setOpacity(value: Float)

    fun showToast(text: String)

    fun removeToast(toastMessage: ToastMessage)
}

class GroundImageMapPageViewModelImpl(override val imageResources: GroundImageResources) :
    ViewModel(),
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

    override fun cameraReset(listener: MapViewState.MoveCameraCallback?) {
        this.mapViewState.value?.moveCameraTo(
            cameraPosition = initCameraPosition,
            durationMs = 3000,
            listener = listener,
        )
    }

    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    override val bounds = GeoRectBounds(    // ココが固定だと変化できないので、後日処理を書き換え。
        southWest = GeoPoint.fromLatLong(40.712216, -74.22655),
        northEast = GeoPoint.fromLatLong(40.773941, -74.12544)
    )

    override fun setOpacity(value: Float) {
        val normalizedValue = value.coerceIn(0f, 1f)
        _opacity = normalizedValue
        _groundImageState.value.opacity = normalizedValue
    }
    private var _opacity by mutableStateOf(1.0f)
    override val opacity
        get() = _opacity

    override val image = imageResources.imageToggle0

    private val _groundImageState: MutableState<GroundImageState> =
        mutableStateOf(
            GroundImageState(
                id = "groundImage",
                bounds = bounds,
                image = image,
            ),
        )

    override val groundImageState: GroundImageState
        get() = _groundImageState.value

    override fun onMapClick(clicked: GeoPoint) {
        showToast("Map clicked at: ${clicked.toUrlValue()}")
    }

    override fun onGroundImageClick(clicked: GroundImageClickEvent) {
        if (clicked.state.image == imageResources.imageToggle0) {
            _groundImageState.value.image = imageResources.imageToggle1
        }
        else {
            _groundImageState.value.image = imageResources.imageToggle0
        }

        showToast("Ground Image clicked.")
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
