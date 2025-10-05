package com.mapconductor.example.pages.map.visibleregion

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.VisibleRegion
import com.mapconductor.core.spherical.WGS84Geodesic.computeDistanceBetween

interface VisibleRegionViewModel {
    val mapViewState: State<MapViewState<*>?>
    val currentCameraPosition: State<MapCameraPosition?>
    val currentVisibleRegion: State<VisibleRegion?>
    val visibleRegionInfo: State<VisibleRegionInfo?>

    fun onMapViewStateChanged(mapViewState: MapViewState<*>)

    fun onMapLoaded(mapViewState: MapViewState<*>)

    fun onCameraChanged(cameraPosition: MapCameraPosition)
}

data class VisibleRegionInfo(
    val bounds: String,
    val corners: List<String>,
    val centerPoint: String,
    val widthKm: Double,
    val heightKm: Double,
)

class VisibleRegionViewModelImpl :
    ViewModel(),
    VisibleRegionViewModel {
    private val _mapViewState = mutableStateOf<MapViewState<*>?>(null)
    override val mapViewState: State<MapViewState<*>?> = _mapViewState

    private val _currentCameraPosition = mutableStateOf<MapCameraPosition?>(null)
    override val currentCameraPosition: State<MapCameraPosition?> = _currentCameraPosition

    private val _currentVisibleRegion = mutableStateOf<VisibleRegion?>(null)
    override val currentVisibleRegion: State<VisibleRegion?> = _currentVisibleRegion

    private val _visibleRegionInfo = mutableStateOf<VisibleRegionInfo?>(null)
    override val visibleRegionInfo: State<VisibleRegionInfo?> = _visibleRegionInfo

    override fun onMapViewStateChanged(mapViewState: MapViewState<*>) {
        _mapViewState.value = mapViewState
    }

    override fun onMapLoaded(mapViewState: MapViewState<*>) {
        // Map is loaded
    }

    override fun onCameraChanged(cameraPosition: MapCameraPosition) {
        _currentCameraPosition.value = cameraPosition
        _currentVisibleRegion.value = cameraPosition.visibleRegion

        cameraPosition.visibleRegion?.let { visibleRegion ->
            _visibleRegionInfo.value = createVisibleRegionInfo(visibleRegion)
        }
    }

    private fun createVisibleRegionInfo(visibleRegion: VisibleRegion): VisibleRegionInfo {
        val bounds = visibleRegion.bounds

        if (bounds.isEmpty || bounds.southWest == null || bounds.northEast == null) {
            return VisibleRegionInfo(
                bounds = "Empty bounds",
                corners = emptyList(),
                centerPoint = "N/A",
                widthKm = 0.0,
                heightKm = 0.0,
            )
        }

        val boundsString =
            "SW: (${String.format("%.6f", bounds.southWest!!.latitude)}, ${String.format(
                "%.6f",
                bounds.southWest!!
                    .longitude,
            )}) " +
                "NE: (${String.format("%.6f", bounds.northEast!!.latitude)}, ${String.format(
                    "%.6f",
                    bounds.northEast!!
                        .longitude,
                )})"

        val corners =
            listOf(
                visibleRegion.nearLeft?.let {
                    "NearLeft: (${String.format("%.6f", it.latitude)}, ${String.format("%.6f", it.longitude)})"
                }
                    ?: "NearLeft: null",
                visibleRegion.nearRight?.let {
                    "NearRight: (${String.format("%.6f", it.latitude)}, ${String.format("%.6f", it.longitude)})"
                }
                    ?: "NearRight: null",
                visibleRegion.farLeft?.let {
                    "FarLeft: (${String.format("%.6f", it.latitude)}, ${String.format("%.6f", it.longitude)})"
                }
                    ?: "FarLeft: null",
                visibleRegion.farRight?.let {
                    "FarRight: (${String.format("%.6f", it.latitude)}, ${String.format("%.6f", it.longitude)})"
                }
                    ?: "FarRight: null",
            )

        val centerLat = (bounds.northEast!!.latitude + bounds.southWest!!.latitude) / 2
        val centerLng = (bounds.northEast!!.longitude + bounds.southWest!!.longitude) / 2
        val centerString = "Center: (${String.format("%.6f", centerLat)}, ${String.format("%.6f", centerLng)})"

        val widthKm =
            computeDistanceBetween(
                bounds.southWest!!,
                GeoPointImpl(bounds.southWest!!.latitude, bounds.southWest!!.longitude),
            )
        val heightKm =
            computeDistanceBetween(
                bounds.southWest!!,
                GeoPointImpl(bounds.northEast!!.latitude, bounds.southWest!!.longitude),
            )

        return VisibleRegionInfo(
            bounds = boundsString,
            corners = corners,
            centerPoint = centerString,
            widthKm = widthKm,
            heightKm = heightKm,
        )
    }
}
