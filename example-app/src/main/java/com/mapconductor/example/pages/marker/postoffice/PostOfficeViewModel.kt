package com.mapconductor.example.pages.marker.postoffice

import androidx.lifecycle.ViewModel
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.map.ArcGISMapViewState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereActualMarker
import com.mapconductor.here.HereViewState
import com.mapconductor.mapbox.MapboxActualMarker
import com.mapconductor.mapbox.MapboxViewState
import com.mapconductor.maplibre.MapLibreActualMarker
import com.mapconductor.maplibre.MapLibreViewState
import com.mapconductor.marker.strategy.SimpleMarkerStrategy
import com.mapconductor.marker.strategy.spatial.RemoteSpatialMarkerStrategy
import java.lang.Thread.sleep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface PostOfficeViewModel {
    val initCameraPosition: MapCameraPositionImpl
    val selectedMarker: StateFlow<MarkerState?>
    val markerList: StateFlow<List<MarkerState>>
    val mapViewState: StateFlow<MapViewState<*>?>
    val isMapLoaded: StateFlow<Boolean>
    val isDataLoading: StateFlow<Boolean>

    val renderingStrategy: StateFlow<MarkerRenderingStrategy<Any>?>

    fun onMapViewChanged(mapViewState: MapViewState<*>)

    fun onMarkerClick(clicked: MarkerState)

    fun onMapClick(clicked: GeoPointImpl)

    fun onMapLoaded(mapViewState: MapViewState<*>)

    fun onInfoClick(postOffice: PostOffice)

    fun loadPostOfficeData()
}

data class Strategies(
    val google: MarkerRenderingStrategy<GoogleMapActualMarker>,
    val mapbox: MarkerRenderingStrategy<MapboxActualMarker>,
    val here: MarkerRenderingStrategy<HereActualMarker>,
    val arcgis: MarkerRenderingStrategy<ArcGISActualMarker>,
    val maplibre: MarkerRenderingStrategy<MapLibreActualMarker>,
)

class PostOfficeViewModelImpl(
    private val strategies: Strategies,
    private val postOfficeIcon: ImageIcon,
    private val dataLoader: PostOfficeDataLoader,
    private val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : ViewModel(),
    PostOfficeViewModel {
    override val initCameraPosition =
        MapCameraPositionImpl(
            position =
                GeoPointImpl.fromLatLong(
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

    private var _mapViewState: MutableStateFlow<MapViewState<*>?> = MutableStateFlow(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    private var _selectedMarker: MutableStateFlow<MarkerState?> = MutableStateFlow(null)
    override val selectedMarker: StateFlow<MarkerState?> = _selectedMarker.asStateFlow()

    private var cameraPosition: MapCameraPositionImpl = initCameraPosition

    private val _renderingStrategy: MutableStateFlow<MarkerRenderingStrategy<Any>?> =
        MutableStateFlow(null)
    override val renderingStrategy: StateFlow<MarkerRenderingStrategy<Any>?> = _renderingStrategy.asStateFlow()

    override fun loadPostOfficeData() {
        if (_markerList.value.isNotEmpty()) return

        coroutine.launch {
            _isDataLoading.value = true
            sleep(3000)
            val postOffices = dataLoader.loadAllPostOffices()

            val markerStates =
                postOffices.map { it ->
                    MarkerState(
                        position = it.position,
                        id = it.hashCode().toString(),
                        icon = postOfficeIcon,
                        extra = it,
                    )
                }
            _markerList.value = markerStates
            sleep(3000)
            _isDataLoading.value = false
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

    override fun onMapClick(clicked: GeoPointImpl) {
        this._selectedMarker.value = null
    }

    override fun onMapLoaded(mapViewState: MapViewState<*>) {
        coroutine.launch {
            _isMapLoaded.value = true
            _mapViewState.value?.moveCameraTo(
                cameraPosition = cameraPosition,
            )
        }
    }

    override fun onInfoClick(postOffice: PostOffice) {
        _mapViewState.value?.moveCameraTo(
            cameraPosition =
                MapCameraPositionImpl(
                    position = postOffice.position,
                    zoom = 18.0,
                    tilt = 30.0,
                ),
            durationMills = 2000,
        )
    }

    override fun onMapViewChanged(mapViewState: MapViewState<*>) {
        cameraPosition = mapViewState.cameraPosition

        renderingStrategy.value?.clear()
        this._selectedMarker.value = null
        _mapViewState.value = mapViewState
        _isMapLoaded.value = false
        _renderingStrategy.value =
            when (mapViewState) {
                is GoogleMapViewState -> strategies.google
                is MapboxViewState -> strategies.mapbox
                is HereViewState -> strategies.here
                is ArcGISMapViewState -> strategies.arcgis
                is MapLibreViewState -> strategies.maplibre
                else -> SimpleMarkerStrategy<Any>()
            } as MarkerRenderingStrategy<Any>?
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up remote strategy if it's being used
        (renderingStrategy as? RemoteSpatialMarkerStrategy<*>)?.destroy()
    }
}
