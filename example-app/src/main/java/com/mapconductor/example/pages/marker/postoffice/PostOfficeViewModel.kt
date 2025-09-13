package com.mapconductor.example.pages.marker.postoffice

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.ArcGISMapViewState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
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
import com.mapconductor.marker.nativestrategy.NativeSimpleMarkerRenderingStrategy
import com.mapconductor.marker.nativestrategy.NativeSpatialMarkerRenderingStrategies
import kotlinx.coroutines.sync.Semaphore

interface PostOfficeViewModel {
    val initCameraPosition: MapCameraPosition
    val selectedMarker: State<MarkerState?>
    val markerList: List<MarkerState>
    val mapViewState: State<MapViewState<*>?>

    val renderingStrategy: State<MarkerRenderingStrategy<*>?>

    fun onMapViewChanged(mapViewState: MapViewState<*>)

    fun onMarkerClick(clicked: MarkerState)

    fun onMapClick(clicked: GeoPoint)
}

class PostOfficeViewModelImpl(
    icon: ImageIcon,
    postOffices: List<PostOffice>,
) : ViewModel(),
    PostOfficeViewModel {
    private val semaphore = Semaphore(1)

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

    override val markerList =
        postOffices.map {
            MarkerState(
                position = it.position,
                id = it.hashCode().toString(),
                icon = icon,
                extra = it,
            )
        }

    private var _mapViewState = mutableStateOf<MapViewState<*>?>(null)
    override val mapViewState: State<MapViewState<*>?> = _mapViewState

    private var _selectedMarker: MutableState<MarkerState?> = mutableStateOf(null)
    override val selectedMarker: State<MarkerState?> = _selectedMarker

    private var _renderingStrategy: MutableState<MarkerRenderingStrategy<*>?> = mutableStateOf(null)
    override val renderingStrategy: State<MarkerRenderingStrategy<*>?> = _renderingStrategy

    override fun onMarkerClick(clicked: MarkerState) {
        this._selectedMarker.value = clicked
    }

    override fun onMapClick(clicked: GeoPoint) {
        this._selectedMarker.value = null
    }

    override fun onMapViewChanged(mapViewState: MapViewState<*>) {
        this._selectedMarker.value = null
        _mapViewState.value = mapViewState
        _renderingStrategy.value =
            when (mapViewState) {
                is GoogleMapViewState ->
                    NativeSpatialMarkerRenderingStrategies
                        .withAddRemoveMode<GoogleMapActualMarker>(semaphore)
                is MapboxViewState -> NativeSpatialMarkerRenderingStrategies.withAddOnlyMode<MapboxActualMarker>(semaphore)
                is HereViewState -> NativeSpatialMarkerRenderingStrategies.withAddOnlyMode<HereActualMarker>(semaphore)
                is ArcGISMapViewState ->
                    NativeSpatialMarkerRenderingStrategies
                        .withAddRemoveMode<ArcGISActualMarker>(semaphore)
                else -> NativeSimpleMarkerRenderingStrategy(semaphore)
            }
    }
}
