package com.mapconductor.example.pages.heatmaplayer

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.heatmap.HeatmapPointState
import com.mapconductor.postoffice.PostOfficeDataLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface HeatmapLayerViewModelInterface {
    val initCameraPosition: MapCameraPosition

    val points: StateFlow<List<HeatmapPointState>>
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    val isMapLoaded: StateFlow<Boolean>
    val isDataLoading: StateFlow<Boolean>

    fun onMapViewChanged(mapViewState: MapViewStateInterface<*>)

    fun onMapLoaded(mapViewState: MapViewStateInterface<*>)

    fun loadPostOfficeData()
}

class HeatmapLayerPageViewModel(
    private val dataLoader: PostOfficeDataLoader,
    private val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : ViewModel(),
    HeatmapLayerViewModelInterface {
    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 35.68049,
                    longitude = 139.76669,
                ),
            zoom = 10.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )
    private val _points: MutableStateFlow<List<HeatmapPointState>> = MutableStateFlow(emptyList())
    override val points: StateFlow<List<HeatmapPointState>> = _points.asStateFlow()

    private val _isMapLoaded: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isMapLoaded: StateFlow<Boolean> = _isMapLoaded.asStateFlow()

    private val _isDataLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isDataLoading: StateFlow<Boolean> = _isDataLoading.asStateFlow()

    private var _mapViewState: MutableStateFlow<MapViewStateInterface<*>?> = MutableStateFlow(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    private var cameraPosition: MapCameraPosition = initCameraPosition

    override fun loadPostOfficeData() {
        if (_points.value.isNotEmpty()) return

        coroutine.launch {
            _isDataLoading.value = true
            _points.value = dataLoader.loadAllPostOffices().map { HeatmapPointState(it.position) }
            _isDataLoading.value = false
        }
    }

    // Convenience: in case of error paths, ensure the dialog is hidden
    private fun markLoadingFinished() {
        if (_isDataLoading.value) {
            _isDataLoading.value = false
        }
    }

    override fun onMapLoaded(mapViewState: MapViewStateInterface<*>) {
        coroutine.launch {
            _isMapLoaded.value = true
            _mapViewState.value?.moveCameraTo(
                cameraPosition = cameraPosition,
            )
        }
    }

    override fun onMapViewChanged(mapViewState: MapViewStateInterface<*>) {
        cameraPosition = mapViewState.cameraPosition

        _mapViewState.value = mapViewState
        _isMapLoaded.value = false
    }

    override fun onCleared() {
        super.onCleared()
    }
}
