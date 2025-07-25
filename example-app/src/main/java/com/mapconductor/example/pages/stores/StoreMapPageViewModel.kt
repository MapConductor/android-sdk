package com.mapconductor.example.pages.stores

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.mapconductor.core.circle.CircleClickEvent
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.info.InfoBubbleState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.toast.ToastMessage
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface StoreMapPageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val selectedMarker: MarkerState?
    val infoBubbleState: InfoBubbleState
    val markerList: List<MarkerState>
    val messages: StateFlow<List<ToastMessage>>

    fun changeState(state: MapViewState<*>)

    fun cameraReset(listener: MapViewState.MoveCameraCallback? = null)

    fun onMarkerClick(clicked: MarkerState)

    fun onMapClick(clicked: GeoPoint)

    fun onCircleClick(event: CircleClickEvent)

    fun showToast(text: String)

    fun removeToast(toastMessage: ToastMessage)

    fun createIntentForDirection(markerState: MarkerState): Intent
}

class StoreMapPageViewModelImpl :
    ViewModel(),
    StoreMapPageViewModel {
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

    override val markerList = StarbucksHI_list

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
        this.infoBubbleState.close()
    }

    override fun createIntentForDirection(markerState: MarkerState): Intent {
        val query =
            (markerState.extra as? Bundle)?.let {
                Uri.encode(it.getString("address", ""))
            } ?: markerState.position.toUrlValue()
        val gmmIntentUri = "google.navigation:q=$query".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        return mapIntent
    }

    override fun cameraReset(listener: MapViewState.MoveCameraCallback?) {
        this.mapViewState.value?.moveCameraTo(
            cameraPosition = initCameraPosition,
            durationMs = 3000,
            listener = listener,
        )
    }

    override fun onMarkerClick(clicked: MarkerState) {
        this._selectedMarker.value = clicked
        this._infoBubbleState.value.open(clicked)
    }

    override fun showToast(text: String) {
        this._messages.value = this._messages.value +
            ToastMessage(
                text = text,
            )
    }

    override fun removeToast(toastMessage: ToastMessage) {
        this._messages.value =
            this._messages.value.filter {
                it != toastMessage
            }
    }

    override fun onMapClick(clicked: GeoPoint) {
        this._selectedMarker.value = null
        this.infoBubbleState.close()
    }

    override fun onCircleClick(event: CircleClickEvent) {
        event.state.fillColor = Color.Blue.copy(alpha = 0.5f)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
