package com.mapconductor.example.pages.rasterlayer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.raster.RasterLayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface RasterLayerPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    var opacity: Float

    val availableLayers: List<GsiLayer>
    val selectedLayer: GsiLayer

    val rasterLayerState: RasterLayerState

    fun selectLayer(layer: GsiLayer)

    fun onMapViewChanged(state: MapViewStateInterface<*>)
}

class RasterLayerPageViewModel(
    layers: List<GsiLayer>,
    initialLayer: GsiLayer,
) : ViewModel(),
    RasterLayerPageViewModelInterface {
    init {
        require(layers.isNotEmpty()) { "At least one GSI layer must be injected" }
        require(layers.distinctBy(GsiLayer::id).size == layers.size) {
            "Injected GSI layer IDs must be unique"
        }
        require(layers.any { it.id == initialLayer.id }) {
            "The initial GSI layer must be included in the injected layers"
        }
    }

    override val availableLayers = layers.toList()

    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 35.6812,
                    longitude = 139.7671,
                ),
            zoom = 7.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    override var opacity by mutableStateOf(0.75f)

    override var selectedLayer by
        mutableStateOf(
            availableLayers.first { it.id == initialLayer.id },
        )
        private set

    override val rasterLayerState: RasterLayerState
        get() =
            RasterLayerState(
                id = "rasterLayer",
                source = selectedLayer.source,
                opacity = opacity,
            )

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    override fun selectLayer(layer: GsiLayer) {
        selectedLayer =
            availableLayers.firstOrNull { it.id == layer.id }
                ?: throw IllegalArgumentException("Unknown GSI layer: ${layer.id}")
    }

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
