package com.mapconductor.example

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPositionBase
import com.mapconductor.core.map.MapViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AppViewModel {
    val initCameraPosition: MapCameraPositionBase
    val state: StateFlow<MapViewState<*>?>

    fun changeState(state: MapViewState<*>)

    fun flyTo(listener: MapViewState.MoveCameraCallback? = null)

    fun onCallButtonClick()
}

class AppViewModelImpl :
    ViewModel(),
    AppViewModel {
    // カメラの初期位置
    override val initCameraPosition =
        MapCameraPositionBase(
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

    private val _state = MutableStateFlow<MapViewState<*>?>(null)
    override val state: StateFlow<MapViewState<*>?> = _state.asStateFlow()

    override fun changeState(newState: MapViewState<*>) {
        this._state.value = newState
    }

    override fun flyTo(listener: MapViewState.MoveCameraCallback?) {
        this@AppViewModelImpl.state.value?.moveCameraTo(
            position =
                MapCameraPositionBase(
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

    override fun onCleared() {
        super.onCleared()
    }
}
