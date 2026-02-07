package com.mapconductor.example.pages.rasterlayer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface RasterLayerPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    var opacity: Float

    val rasterLayerState: RasterLayerState

    fun onMapViewChanged(state: MapViewStateInterface<*>)
}

class RasterLayerPageViewModel :
    ViewModel(),
    RasterLayerPageViewModelInterface {
    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 21.382314,
                    longitude = -157.933097,
                ),
            zoom = 12.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    override var opacity by mutableStateOf(1.0f)

    override val rasterLayerState: RasterLayerState
        get() =
            RasterLayerState(
                id = "rasterLayer",
                source =
                    RasterLayerSource.UrlTemplate(
                        template = "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
                        tileSize = RasterLayerSource.DEFAULT_TILE_SIZE,
                    ),
                opacity = opacity,
                debug = true,
            )

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        mapViewState.value?.cameraPosition?.let {
            state.moveCameraTo(it)
        }
        this._mapViewState.value = state
    }

    override fun onCleared() {
        super.onCleared()
    }
}
