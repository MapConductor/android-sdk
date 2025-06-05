package com.mapconductor.example

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mapconductor.StarbucksHI_list
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.info.InfoBubbleState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.toast.ToastMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.os.Bundle

interface AppViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val selectedMarker: MarkerState?
    val infoBubbleState: InfoBubbleState
    val markerList: List<MarkerState>
    val messages: StateFlow<List<ToastMessage>>

    fun changeState(state: MapViewState<*>)

    fun flyTo(listener: MapViewState.MoveCameraCallback? = null)

    fun onCallButtonClick()

    fun onMarkerClick(clicked: MarkerState)

    fun onMapClick(clicked: GeoPoint)

    fun showToast(text: String)

    fun removeToast(toastMessage: ToastMessage)
}

class AppViewModelImpl :
    ViewModel(),
    AppViewModel {

    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    // カメラの初期位置
    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 21.382314,
                    longitude = -157.933097,
                ),
            zoom = 10.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    override val markerList = StarbucksHI_list.slice(IntRange(0, 10))

    private val _infoBubbleState: MutableState<InfoBubbleState> = mutableStateOf(InfoBubbleState())
    override val infoBubbleState: InfoBubbleState
        get() = _infoBubbleState.value

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    private val _selectedMarker: MutableState<MarkerState?> = mutableStateOf(null)
    override val selectedMarker: MarkerState?
        get() = _selectedMarker.value

    override fun changeState(newState: MapViewState<*>) {
        this._selectedMarker.value = null
        this._mapViewState.value = newState
        this.infoBubbleState?.close()
    }

    override fun flyTo(listener: MapViewState.MoveCameraCallback?) {
        this@AppViewModelImpl.mapViewState.value?.moveCameraTo(
            position =
                MapCameraPosition(
                    position =
                        GeoPoint(
                            latitude = 40.689184289566214,
                            longitude = -74.04454331830473,
                        ),
                    tilt = 70.0,
                    zoom = 18.0,
                ),
            durationMs = 3000,
            listener = listener,
        )
    }

    override fun onCallButtonClick() {
    }

    override fun onMarkerClick(clicked: MarkerState) {
        this._selectedMarker.value = clicked
        this._infoBubbleState.value.open(clicked)
//        showToast((clicked.extra as Bundle).getString("name", ""))
    }

    override fun showToast(text: String) {
        this._messages.value = this._messages.value + ToastMessage(
            text = text,
        )
    }

    override fun removeToast(toastMessage: ToastMessage) {
        this._messages.value = this._messages.value.filter {
            it != toastMessage
        }
    }

    override fun onMapClick(clicked: GeoPoint) {
        this._selectedMarker.value = null
        this.infoBubbleState?.close()
    }

    override fun onCleared() {
        super.onCleared()
    }
}
