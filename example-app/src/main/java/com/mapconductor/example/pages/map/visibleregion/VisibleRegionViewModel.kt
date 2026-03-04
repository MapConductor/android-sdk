package com.mapconductor.example.pages.map.visibleregion

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.VisibleRegion
import com.mapconductor.core.spherical.WGS84Geodesic.computeDistanceBetween

interface VisibleRegionViewModelInterface {
    val mapViewState: State<MapViewStateInterface<*>?>
    val currentCameraPosition: State<MapCameraPositionInterface?>
    val currentVisibleRegion: State<VisibleRegion?>
    val visibleRegionInfo: State<VisibleRegionInfo?>

    fun onMapViewStateChanged(mapViewState: MapViewStateInterface<*>)

    fun onMapLoaded(mapViewState: MapViewStateInterface<*>)

    fun onCameraChanged(cameraPosition: MapCameraPositionInterface)
}

data class VisibleRegionInfo(
    val bounds: String,
    val corners: List<String>,
    val centerPoint: String,
    val widthKm: Double,
    val heightKm: Double,
)

class VisibleRegionViewModel :
    ViewModel(),
    VisibleRegionViewModelInterface {
    private val _mapViewState = mutableStateOf<MapViewStateInterface<*>?>(null)
    override val mapViewState: State<MapViewStateInterface<*>?> = _mapViewState

    private val _currentCameraPosition = mutableStateOf<MapCameraPositionInterface?>(null)
    override val currentCameraPosition: State<MapCameraPositionInterface?> = _currentCameraPosition

    private val _currentVisibleRegion = mutableStateOf<VisibleRegion?>(null)
    override val currentVisibleRegion: State<VisibleRegion?> = _currentVisibleRegion

    private val _visibleRegionInfo = mutableStateOf<VisibleRegionInfo?>(null)
    override val visibleRegionInfo: State<VisibleRegionInfo?> = _visibleRegionInfo

    override fun onMapViewStateChanged(mapViewState: MapViewStateInterface<*>) {
        _mapViewState.value = mapViewState
    }

    override fun onMapLoaded(mapViewState: MapViewStateInterface<*>) {
        // Map is loaded
    }

    override fun onCameraChanged(cameraPosition: MapCameraPositionInterface) {
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
                GeoPoint(bounds.southWest!!.latitude, bounds.northEast!!.longitude),
            )
        val heightKm =
            computeDistanceBetween(
                bounds.southWest!!,
                GeoPoint(bounds.northEast!!.latitude, bounds.southWest!!.longitude),
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
