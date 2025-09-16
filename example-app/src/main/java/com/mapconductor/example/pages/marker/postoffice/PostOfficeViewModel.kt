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
import com.mapconductor.marker.nativestrategy.NativeSpatialMarkerRenderingStrategy
import com.mapconductor.marker.nativestrategy.SpatialMarkerRenderingStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore

interface PostOfficeViewModel {
    val initCameraPosition: MapCameraPosition
    val selectedMarker: State<MarkerState?>
    val markerList: State<List<MarkerState>>
    val mapViewState: State<MapViewState<*>?>

    val renderingStrategy: State<MarkerRenderingStrategy<Any>?>

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
    private val icons: List<ImageIcon>? = null,
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
    private val _markerList: MutableState<List<MarkerState>> = mutableStateOf(emptyList())
    override val markerList: State<List<MarkerState>> = _markerList

    private fun getScaledIcon(zoomLevel: Double): ImageIcon? {
        if (icons == null || icons.isEmpty()) return null
        val iconIndex =
            when {
                zoomLevel < 9.0 -> 0
                zoomLevel <= 14.0 -> 1
                else -> 2
            }
        if (iconIndex == prevIconIndex) return null
        prevIconIndex = iconIndex
        return icons.getOrNull(iconIndex)
    }

    private fun updateMarkerList(zoomLevel: Double) {
        val scaledIcon = getScaledIcon(zoomLevel) ?: return
        _markerList.value.forEach { it.icon = scaledIcon }
    }

    private var _mapViewState = mutableStateOf<MapViewState<*>?>(null)
    override val mapViewState: State<MapViewState<*>?> = _mapViewState

    private var _selectedMarker: MutableState<MarkerState?> = mutableStateOf(null)
    override val selectedMarker: State<MarkerState?> = _selectedMarker

    private val _renderingStrategy: MutableState<MarkerRenderingStrategy<Any>?> = mutableStateOf(null)
    override val renderingStrategy: State<MarkerRenderingStrategy<Any>?> = _renderingStrategy
//        SpatialMarkerRenderingStrategies.withAddRemoveMode(
//            semaphore = semaphore,
//            geocell = HexGeocellImpl(
//                projection = WebMercator,
//                baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
//            )
//        )

    override fun onMarkerClick(clicked: MarkerState) {
        this._selectedMarker.value = clicked
    }

    override fun onMapClick(clicked: GeoPoint) {
        this._selectedMarker.value = null
    }

    override fun onCameraChanged(cameraPosition: MapCameraPosition) {
        updateMarkerList(cameraPosition.zoom)
    }

    override fun onMapLoaded(mapViewState: MapViewState<*>) {
        _markerList.value = postOffices
    }

    override fun onMapViewChanged(mapViewState: MapViewState<*>) {
        this._selectedMarker.value = null
        _mapViewState.value = mapViewState
        _renderingStrategy.value =
            when (mapViewState) {
                is GoogleMapViewState ->
                    NativeSpatialMarkerRenderingStrategy(
                        expandMargin = 0.4,
                        addOnlyMode = false,
                    )
                is MapboxViewState ->
                    NativeSpatialMarkerRenderingStrategy(
                        expandMargin = 0.5,
                        addOnlyMode = true,
                    )
                is HereViewState ->
                    NativeSpatialMarkerRenderingStrategy(
                        expandMargin = 0.4,
                        addOnlyMode = true,
                    )
                is ArcGISMapViewState ->
                    NativeSpatialMarkerRenderingStrategy(
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
}
