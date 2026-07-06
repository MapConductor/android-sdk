package com.mapconductor.example.pages.marker.postofficecluster

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.MarkerRenderingStrategyInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.postoffice.PostOffice
import com.mapconductor.postoffice.PostOfficeDataLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface MarkerClusterMapPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val selectedMarker: StateFlow<MarkerState?>
    val markerList: StateFlow<List<MarkerState>>
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    val isMapLoaded: StateFlow<Boolean>
    val isDataLoading: StateFlow<Boolean>

    val renderingStrategy: StateFlow<MarkerRenderingStrategyInterface<Any>?>
    var debugHullPolygons: Boolean

    fun onMapViewChanged(mapViewState: MapViewStateInterface<*>)

    fun onMarkerClick(clicked: MarkerState)

    fun onMapClick(clicked: GeoPoint)

    fun onMapLoaded(mapViewState: MapViewStateInterface<*>)

    fun onInfoClick(postOffice: PostOffice)
}

class MarkerClusterMapPageViewModel(
    private val postOfficeIcon: ImageIcon,
    private val dataLoader: PostOfficeDataLoader,
) : ViewModel(),
    MarkerClusterMapPageViewModelInterface {
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
    private val _markerList: MutableStateFlow<List<MarkerState>> = MutableStateFlow(emptyList())
    override val markerList: StateFlow<List<MarkerState>> = _markerList.asStateFlow()

    private val _isMapLoaded: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isMapLoaded: StateFlow<Boolean> = _isMapLoaded.asStateFlow()

    private val _isDataLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isDataLoading: StateFlow<Boolean> = _isDataLoading.asStateFlow()

    private var _mapViewState: MutableStateFlow<MapViewStateInterface<*>?> = MutableStateFlow(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    private var _selectedMarker: MutableStateFlow<MarkerState?> = MutableStateFlow(null)
    override val selectedMarker: StateFlow<MarkerState?> = _selectedMarker.asStateFlow()

    private var cameraPosition: MapCameraPosition = initCameraPosition

    private val _renderingStrategy: MutableStateFlow<MarkerRenderingStrategyInterface<Any>?> =
        MutableStateFlow(null)
    override val renderingStrategy: StateFlow<MarkerRenderingStrategyInterface<Any>?> = _renderingStrategy.asStateFlow()

    override var debugHullPolygons by mutableStateOf(false)

    fun loadPostOfficeData() {
        if (_markerList.value.isNotEmpty()) return

        viewModelScope.launch(Dispatchers.Default) {
            _isDataLoading.value = true
            try {
                delay(1000)
                val postOffices = dataLoader.loadAllPostOffices()

                val markerStates =
                    postOffices.map { it ->
                        MarkerState(
                            position = it.position,
                            id = it.hashCode().toString(),
                            icon = postOfficeIcon,
                            extra = it,
                            onClick = this@MarkerClusterMapPageViewModel::onMarkerClick,
                        )
                    }
                _markerList.value = markerStates
            } catch (t: Throwable) {
                _markerList.value = emptyList()
            } finally {
                _isDataLoading.value = false
            }
        }
    }

    // Convenience: in case of error paths, ensure the dialog is hidden
    private fun markLoadingFinished() {
        if (_isDataLoading.value) {
            _isDataLoading.value = false
        }
    }

    override fun onMarkerClick(clicked: MarkerState) {
        this._selectedMarker.value = clicked
    }

    override fun onMapClick(clicked: GeoPoint) {
        this._selectedMarker.value = null
    }

    override fun onMapLoaded(mapViewState: MapViewStateInterface<*>) {
        viewModelScope.launch(Dispatchers.Default) {
            _isMapLoaded.value = true
            _mapViewState.value?.moveCameraTo(
                cameraPosition = cameraPosition,
            )
            loadPostOfficeData()
        }
    }

    override fun onInfoClick(postOffice: PostOffice) {
        _mapViewState.value?.moveCameraTo(
            cameraPosition =
                MapCameraPosition(
                    position = postOffice.position,
                    zoom = 18.0,
                    tilt = 30.0,
                ),
            durationMillis = 2000,
        )
    }

    override fun onMapViewChanged(mapViewState: MapViewStateInterface<*>) {
        cameraPosition = mapViewState.cameraPosition

        renderingStrategy.value?.clear()
        this._selectedMarker.value = null
        _mapViewState.value = mapViewState
        _isMapLoaded.value = false
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up remote strategy if it's being used
//        (renderingStrategy as? RemoteSpatialMarkerStrategy<*>)?.destroy()
    }
}
