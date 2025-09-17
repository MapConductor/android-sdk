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
import com.mapconductor.core.marker.spatial.RemoteSpatialMarkerRenderingStrategy
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereViewState
import com.mapconductor.mapbox.MapboxViewState
import com.mapconductor.marker.nativestrategy.NativeSpatialMarkerRenderingStrategy
import android.content.Context
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

    fun addMarkersProgressively(markers: List<MarkerState>)

    fun updateMarkerList(markers: List<MarkerState>)

    fun onInfoClick(postOffice: PostOffice)
}

data class PostOfficeIcons(
    val tiny: ImageIcon,
    val small: ImageIcon,
    val regular: ImageIcon,
)

class PostOfficeViewModelImpl(
    private val context: Context,
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
            zoom = 10.0,
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
//        updateMarkerList(cameraPosition.zoom)
    }

    override fun onMapLoaded(mapViewState: MapViewState<*>) {
        // Don't load all markers at once - use progressive loading instead
    }

    override fun addMarkersProgressively(markers: List<MarkerState>) {
        val currentMarkers = _markerList.value.toMutableList()
        currentMarkers.addAll(markers)
        _markerList.value = currentMarkers
    }

    override fun updateMarkerList(markers: List<MarkerState>) {
        _markerList.value = markers
    }

    override fun onInfoClick(postOffice: PostOffice) {
        _mapViewState.value?.moveCameraTo(
            cameraPosition = MapCameraPosition(
                position = postOffice.position,
                zoom = 18.0,
                tilt = 30.0,
            ),
            durationMs = 2000,
        )
    }

    override fun onMapViewChanged(mapViewState: MapViewState<*>) {
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
                        addOnlyMode = false,
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
