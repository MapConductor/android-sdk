package com.mapconductor.example.pages.map.uisettings

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UISettingsMapPageViewModel : ViewModel() {
    val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(35.681236, 139.767125),
            zoom = 14.0,
            tilt = 30.0,
            bearing = 20.0,
        )

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    fun onMapViewChanged(state: MapViewStateInterface<*>) {
        _mapViewState.value = state
    }
}
