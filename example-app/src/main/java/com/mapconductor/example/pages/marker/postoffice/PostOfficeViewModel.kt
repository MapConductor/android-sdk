package com.mapconductor.example.pages.marker.postoffice

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.MarkerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore

interface PostOfficeViewModel {
    val initCameraPosition: MapCameraPosition
    val selectedMarker: State<MarkerState?>
    val markerList: State<List<MarkerState>>
    val mapViewState: State<MapViewState<*>?>

    val renderingStrategy: MarkerRenderingStrategy<Any>?

    fun onMapViewChanged(mapViewState: MapViewState<*>)

    fun onMarkerClick(clicked: MarkerState)

    fun onMapClick(clicked: GeoPoint)

    fun onCameraChanged(cameraPosition: MapCameraPosition)

    fun onMapLoaded(mapViewState: MapViewState<*>)
}

data class PostOfficeIcons(
    val tiny: ImageIcon,
    val small: ImageIcon,
    val regular: ImageIcon,
)

class PostOfficeViewModelImpl(
//    private val icons: List<ImageIcon>,
//    private val dataLoader: PostOfficeDataLoader,
    private val postOffices: List<MarkerState>,
    private val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : ViewModel(),
    PostOfficeViewModel {
    private val semaphore = Semaphore(1)
    private var prevIconIndex = -1

    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 35.68049,
                    longitude = 139.76669,
                ),
            zoom = 16.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )
//    private val initIcon = getScaledIcon(initCameraPosition.zoom)

    private val _markerList: MutableState<List<MarkerState>> = mutableStateOf(emptyList())
    override val markerList: State<List<MarkerState>> = _markerList

//    private fun getScaledIcon(zoomLevel: Double): ImageIcon? {
//        val iconIndex = when {
//            zoomLevel < 9.0 -> 0
//            zoomLevel <= 14.0 -> 1
//            else -> 2
//        }
//        if (iconIndex == prevIconIndex) return null
//        return icons[iconIndex]
//    }
//
//    private fun updateMarkerList(zoomLevel: Double) {
//        val scaledIcon = getScaledIcon(zoomLevel) ?: return
//        markerList.forEach { it.icon = scaledIcon }
//    }

    private var _mapViewState = mutableStateOf<MapViewState<*>?>(null)
    override val mapViewState: State<MapViewState<*>?> = _mapViewState

    private var _selectedMarker: MutableState<MarkerState?> = mutableStateOf(null)
    override val selectedMarker: State<MarkerState?> = _selectedMarker

    override val renderingStrategy: MarkerRenderingStrategy<Any>? = null
//        NativeSpatialMarkerRenderingStrategy<Any>(
//            semaphore = semaphore,
//        )

    override fun onMarkerClick(clicked: MarkerState) {
        this._selectedMarker.value = clicked
    }

    override fun onMapClick(clicked: GeoPoint) {
        this._selectedMarker.value = null
    }

    override fun onCameraChanged(cameraPosition: MapCameraPosition) {
//        updateMarkerList(cameraPosition.zoom)
    }

    override fun onMapLoaded(mapViewState: MapViewState<*>) {
        _markerList.value = postOffices
    }

    override fun onMapViewChanged(mapViewState: MapViewState<*>) {
        this._selectedMarker.value = null
        _mapViewState.value = mapViewState
    }
}
