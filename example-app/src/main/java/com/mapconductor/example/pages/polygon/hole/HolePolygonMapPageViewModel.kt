package com.mapconductor.example.pages.polygon.hole

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface HolePolygonMapPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>

    fun onMapViewChanged(state: MapViewStateInterface<*>)
}

class HolePolygonMapPageViewModel :
    ViewModel(),
    HolePolygonMapPageViewModelInterface {
    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    override val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(43.06050568387817, 141.35374551567804),
            zoom = 11.0,
        )

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        _mapViewState.value = state
    }
}
