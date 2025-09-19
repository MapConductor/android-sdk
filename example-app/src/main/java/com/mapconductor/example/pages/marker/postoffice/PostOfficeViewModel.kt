package com.mapconductor.example.pages.marker.postoffice

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.marker.nativestrategy.NativeSpatialMarkerRenderingStrategy
import com.mapconductor.marker.strategy.spatial.RemoteSpatialMarkerRenderingStrategy
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    val renderingStrategy: StateFlow<MarkerRenderingStrategy<Any>?>

    fun onMapViewChanged(mapViewState: MapViewState<*>)

    fun onMarkerClick(clicked: MarkerState)

    fun onMapClick(clicked: GeoPointImpl)

    fun onMapLoaded(mapViewState: MapViewState<*>)

    fun onInfoClick(postOffice: PostOffice)

    fun loadPostOfficeData()
}

data class PostOfficeIcons(
    val tiny: ImageIcon,
    val small: ImageIcon,
    val regular: ImageIcon,
)

class PostOfficeViewModelImpl(
    private val context: Context,
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

    private var _mapViewState: MutableStateFlow<MapViewState<*>?> = MutableStateFlow(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    private var _selectedMarker: MutableStateFlow<MarkerState?> = MutableStateFlow(null)
    override val selectedMarker: StateFlow<MarkerState?> = _selectedMarker.asStateFlow()

    private val _renderingStrategy: MutableStateFlow<MarkerRenderingStrategy<Any>?> =
        MutableStateFlow(
            RemoteSpatialMarkerRenderingStrategy(
                context = context,
                expandMargin = 0.4,
                addOnlyMode = false,
            ),
        )
    override val renderingStrategy: StateFlow<MarkerRenderingStrategy<Any>?> = _renderingStrategy.asStateFlow()

    override fun loadPostOfficeData() {
        if (_markerList.value.isNotEmpty()) return
        coroutine.launch {
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
            // Wait until map tiles are rendered.
            delay(3000)
            _isMapLoaded.value = true
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
            durationMs = 2000,
        )
    }

    override fun onMapViewChanged(mapViewState: MapViewState<*>) {
        _isMapLoaded.value = false
        this._selectedMarker.value = null
        _mapViewState.value = mapViewState
        _renderingStrategy.value =
            NativeSpatialMarkerRenderingStrategy(
                expandMargin = 0.4,
            )
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up remote strategy if it's being used
        (_renderingStrategy.value as? RemoteSpatialMarkerRenderingStrategy<*>)?.destroy()
    }
}
