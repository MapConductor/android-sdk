package com.mapconductor.example.pages.marker.postoffice

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.AndroidDrawableIcon
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.pages.map.basic.StarbucksHI_list
import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PostOfficeViewModel {
    val initCameraPosition: MapCameraPosition
    val selectedMarker: StateFlow<MarkerState?>
    val markerList: List<MarkerState>
    val mapViewState: StateFlow<MapViewState<*>?>
    fun onMapViewChanged(mapViewState: MapViewState<*>)
    fun onMarkerClick(clicked: MarkerState)
    fun onMapClick(clicked: GeoPoint)
}

class PostOfficeViewModelImpl(icon: ImageIcon, postOffices: List<PostOffice>) :
    ViewModel(),
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

    override val markerList = postOffices.map {
        MarkerState(
            position = it.position,
            id = it.hashCode().toString(),
            icon = icon,
            extra = it,
        )
    }

    private var _mapViewState: MutableStateFlow<MapViewState<*>?> = MutableStateFlow(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    private val _selectedMarker: MutableStateFlow<MarkerState?> = MutableStateFlow(null)
    override val selectedMarker: StateFlow<MarkerState?> = _selectedMarker.asStateFlow()

    override fun onMarkerClick(clicked: MarkerState) {
        this._selectedMarker.value = clicked
    }

    override fun onMapClick(clicked: GeoPoint) {
        this._selectedMarker.value = null
    }

    override fun onMapViewChanged(mapViewState: MapViewState<*>) {
        this._selectedMarker.value = null
        _mapViewState.value = mapViewState
    }
}
