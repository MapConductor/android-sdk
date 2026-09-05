package com.mapconductor.example.pages.marker.streettree

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.streettree.StreetTree
import com.mapconductor.streettree.StreetTreeDataLoader
import com.mapconductor.streettree.StreetTreeIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Tokyo's 144,183 street trees, one colour per species.
 *
 * The set is six times the post office sample's and packed into one
 * metropolitan area rather than spread over a country, so it is what the marker
 * tiling path looks like under real pressure.
 */
class StreetTreeViewModel(
    private val dataLoader: StreetTreeDataLoader,
    private val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : ViewModel() {
    val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint.fromLatLong(latitude = 35.6812, longitude = 139.7671),
            zoom = 11.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    private val _markerList = MutableStateFlow<List<MarkerState>>(emptyList())
    val markerList: StateFlow<List<MarkerState>> = _markerList.asStateFlow()

    private val _selectedMarker = MutableStateFlow<MarkerState?>(null)
    val selectedMarker: StateFlow<MarkerState?> = _selectedMarker.asStateFlow()

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    private val _isMapLoaded = MutableStateFlow(false)
    val isMapLoaded: StateFlow<Boolean> = _isMapLoaded.asStateFlow()

    private val _isDataLoading = MutableStateFlow(false)
    val isDataLoading: StateFlow<Boolean> = _isDataLoading.asStateFlow()

    val markerTiling: MarkerTilingOptions =
        MarkerTilingOptions.Default.copy(
            // Trees are planted a few metres apart along a road, so below street
            // level most of them sit on top of one another. Thinning them to one
            // per icon-width halves the tile bytes and shows the same map.
            declutterPx = 14,
            iconScaleCallback = { _, zoom ->
                when {
                    zoom > 15.0 -> 1.4
                    zoom > 13.0 -> 1.0
                    zoom > 11.0 -> 0.7
                    else -> 0.5
                }
            },
        )

    fun loadTrees() {
        if (_markerList.value.isNotEmpty()) return
        coroutine.launch {
            _isDataLoading.value = true
            val data = dataLoader.load()
            val icons = StreetTreeIcons.palette(data.species.size, sizePx = 14)
            _markerList.value =
                data.trees.mapIndexed { index, tree ->
                    MarkerState(
                        position = tree.position,
                        id = index.toString(),
                        icon = icons[tree.speciesIndex],
                        extra = tree,
                        onClick = ::onMarkerClick,
                    )
                }
            _isDataLoading.value = false
        }
    }

    fun onMapViewChanged(mapViewState: MapViewStateInterface<*>) {
        _mapViewState.value = mapViewState
    }

    fun onMapLoaded(mapViewState: MapViewStateInterface<*>) {
        _isMapLoaded.value = true
        loadTrees()
    }

    fun onMarkerClick(clicked: MarkerState) {
        _selectedMarker.value = clicked
    }

    fun onMapClick(clicked: GeoPoint) {
        _selectedMarker.value = null
    }

    val StreetTree.label: String get() = species
}
