package com.mapconductor.example.pages.marker.postoffice

import androidx.lifecycle.ViewModel
import android.os.SystemClock
import android.util.Log
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.postoffice.PostOffice
import com.mapconductor.postoffice.PostOfficeDataLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface PostOfficeViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val selectedMarker: StateFlow<MarkerState?>
    val markerList: StateFlow<List<MarkerState>>
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    val isMapLoaded: StateFlow<Boolean>
    val isDataLoading: StateFlow<Boolean>

    fun onMapViewChanged(mapViewState: MapViewStateInterface<*>)

    fun onMarkerClick(clicked: MarkerState)

    fun onMapClick(clicked: GeoPoint)

    fun onMapLoaded(mapViewState: MapViewStateInterface<*>)

    fun onInfoClick(postOffice: PostOffice)

    fun loadPostOfficeData()
}

class PostOfficeViewModel(
    private val postOfficeIcon: ImageIcon,
    private val dataLoader: PostOfficeDataLoader,
    private val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : ViewModel(),
    PostOfficeViewModelInterface {
    private val tag: String = "PostOfficeVM"
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

    override fun loadPostOfficeData() {
        if (_markerList.value.isNotEmpty()) return

        coroutine.launch {
            val start = SystemClock.elapsedRealtime()
            _isDataLoading.value = true
            Log.i(tag, "loadPostOfficeData:start")
            val postOffices = dataLoader.loadAllPostOffices()
            Log.i(tag, "loadAllPostOffices took ${SystemClock.elapsedRealtime() - start}ms | count=${postOffices.size}")

            val mapStart = SystemClock.elapsedRealtime()
            val markerStates = ArrayList<MarkerState>(postOffices.size)
            postOffices.forEachIndexed { index, postOffice ->
                markerStates.add(
                    MarkerState(
                        position = postOffice.position,
                        id = index.toString(),
                        icon = postOfficeIcon,
                        extra = postOffice,
                        onClick = this@PostOfficeViewModel::onMarkerClick,
                        autoScalable = false,
                    ),
                )
            }
            Log.i(tag, "build MarkerState took ${SystemClock.elapsedRealtime() - mapStart}ms | count=${markerStates.size}")
            _markerList.value = markerStates
            Log.i(tag, "_markerList updated")
            // Keep this non-blocking so it doesn't starve other background work.
            delay(6000)
            _isDataLoading.value = false
            Log.i(tag, "loadPostOfficeData:done total ${SystemClock.elapsedRealtime() - start}ms")
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

        this._selectedMarker.value = null
        _mapViewState.value = mapViewState
        _isMapLoaded.value = false
    }

    override fun onCleared() {
        super.onCleared()
    }
}
