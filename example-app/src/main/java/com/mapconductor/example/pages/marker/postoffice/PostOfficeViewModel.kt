package com.mapconductor.example.pages.marker.postoffice

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mapconductor.arcgis.ArcGISMapViewState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereViewState
import com.mapconductor.mapbox.MapboxViewState
import com.mapconductor.marker.strategy.NativeSpatialMarkerRenderingStrategy
import com.mapconductor.marker.strategy.spatial.RemoteSpatialMarkerRenderingStrategy
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface PostOfficeViewModel {
    val initCameraPosition: MapCameraPosition
    val selectedMarker: State<MarkerState?>
    val markerList: State<List<MarkerState>>
    val mapViewState: State<MapViewState<*>?>
    val isMapLoaded: State<Boolean>

    val renderingStrategy: State<MarkerRenderingStrategy<Any>?>

    fun onMapViewChanged(mapViewState: MapViewState<*>)

    fun onMarkerClick(clicked: MarkerState)

    fun onMapClick(clicked: GeoPoint)

    fun onMapLoaded(mapViewState: MapViewState<*>)

    fun onInfoClick(postOffice: PostOffice)
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
) : ViewModel(),
    PostOfficeViewModel {
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
    private val _markerList: MutableState<List<MarkerState>> = mutableStateOf(emptyList())
    override val markerList: State<List<MarkerState>> = _markerList

    private val _isMapLoaded: MutableState<Boolean> = mutableStateOf(false)
    override val isMapLoaded: State<Boolean> = _isMapLoaded

    private var _mapViewState = mutableStateOf<MapViewState<*>?>(null)
    override val mapViewState: State<MapViewState<*>?> = _mapViewState

    private var _selectedMarker: MutableState<MarkerState?> = mutableStateOf(null)
    override val selectedMarker: State<MarkerState?> = _selectedMarker

    private val _renderingStrategy: MutableState<MarkerRenderingStrategy<Any>?> = mutableStateOf(null)
    override val renderingStrategy: State<MarkerRenderingStrategy<Any>?> = _renderingStrategy

    suspend fun loadPostOfficeData() {
        if (_markerList.value.isNotEmpty()) return

        val chunks = dataLoader.loadAllPostOffices().subList(0, 200)
        val markerStates = mutableListOf<MarkerState>()
        chunks.forEach { it ->
            markerStates.add(MarkerState(
                position = it.position,
                id = it.hashCode().toString(),
                icon = postOfficeIcon,
                extra = it,
            ))
//            val states =
//                chunk.map {
//                    MarkerState(
//                        position = it.position,
//                        id = it.hashCode().toString(),
//                        icon = postOfficeIcon,
//                        extra = it,
//                    )
//                }
//            markerStates.addAll(states)
//            delay(100)
        }
        _markerList.value = markerStates
    }

    override fun onMarkerClick(clicked: MarkerState) {
        this._selectedMarker.value = clicked
    }

    override fun onMapClick(clicked: GeoPoint) {
        this._selectedMarker.value = null
    }

    override fun onMapLoaded(mapViewState: MapViewState<*>) {
        CoroutineScope(Dispatchers.Default).launch {
            delay(1000)
            _isMapLoaded.value = true
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
            durationMs = 2000,
        )
    }

    override fun onMapViewChanged(mapViewState: MapViewState<*>) {
        _isMapLoaded.value = false
        this._selectedMarker.value = null
        _mapViewState.value = mapViewState
        _renderingStrategy.value =
            when (mapViewState) {
                is GoogleMapViewState ->
                    RemoteSpatialMarkerRenderingStrategy(
                        context = context,
                        expandMargin = 0.4,
                        addOnlyMode = false,
                    )
                is MapboxViewState ->
                    RemoteSpatialMarkerRenderingStrategy(
                        context = context,
                        expandMargin = 0.5,
                        addOnlyMode = true,
                    )
                is HereViewState ->
                    RemoteSpatialMarkerRenderingStrategy(
                        context = context,
                        expandMargin = 0.4,
                        addOnlyMode = true,
                    )
                is ArcGISMapViewState ->
                    RemoteSpatialMarkerRenderingStrategy(
                        context = context,
                        expandMargin = 0.4,
                        addOnlyMode = false,
                    )
                else ->
                    NativeSpatialMarkerRenderingStrategy(
                        expandMargin = 0.3,
                        addOnlyMode = true,
                    )
            }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up remote strategy if it's being used
        (_renderingStrategy.value as? RemoteSpatialMarkerRenderingStrategy<*>)?.destroy()
    }
}
