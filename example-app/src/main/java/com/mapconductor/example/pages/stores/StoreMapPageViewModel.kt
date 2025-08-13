package com.mapconductor.example.pages.stores

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerState
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface StoreMapPageViewModel {
    val initCameraPosition: MapCameraPosition
    val selectedMarker: StateFlow<MarkerState?>
    val markerList: List<MarkerState>
    val mapViewState: StateFlow<MapViewState<*>?>

    fun onMapViewChanged(mapViewState: MapViewState<*>)

    fun onMarkerClick(clicked: MarkerState)

    fun onMapClick(clicked: GeoPoint)

    fun onDirectionButtonClick(markerState: MarkerState): Intent
}

class StoreMapPageViewModelImpl :
    ViewModel(),
    StoreMapPageViewModel {
    // カメラの初期位置
    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 21.382314,
                    longitude = -157.933097,
                ),
            zoom = 10.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    override val markerList = StarbucksHI_list

    private var _mapViewState: MutableStateFlow<MapViewState<*>?> = MutableStateFlow(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    private val _selectedMarker: MutableStateFlow<MarkerState?> = MutableStateFlow(null)
    override val selectedMarker: StateFlow<MarkerState?> = _selectedMarker.asStateFlow()

    override fun onDirectionButtonClick(markerState: MarkerState): Intent {
        val query =
            (markerState.extra as? Bundle)?.let {
                Uri.encode(it.getString("address", ""))
            } ?: GeoPoint.from(markerState.position).toUrlValue()
        val gmmIntentUri = "google.navigation:q=$query".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        return mapIntent
    }

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
