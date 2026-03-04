package com.mapconductor.example.pages.map.basic

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.MarkerState
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface StoreMapPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val selectedMarker: StateFlow<MarkerState?>
    val markerList: List<MarkerState>
    val mapViewState: StateFlow<MapViewStateInterface<*>?>

    fun onMapViewChanged(mapViewState: MapViewStateInterface<*>)

    fun onMarkerClick(clicked: MarkerState)

    fun onMapClick(clicked: GeoPoint)

    fun onDirectionButtonClick(markerState: MarkerState): Intent
}

class StoreMapPageViewModel :
    ViewModel(),
    StoreMapPageViewModelInterface {
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

    override val markerList =
        StarbucksHI_list.map {
            it.copy(onClick = this::onMarkerClick)
        }

    private var _mapViewState: MutableStateFlow<MapViewStateInterface<*>?> = MutableStateFlow(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    private val _selectedMarker: MutableStateFlow<MarkerState?> = MutableStateFlow(null)
    override val selectedMarker: StateFlow<MarkerState?> = _selectedMarker.asStateFlow()

    override fun onDirectionButtonClick(markerState: MarkerState): Intent {
        val query =
            (markerState.extra as? StoreInfo)?.let {
                Uri.encode(it.address)
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

    override fun onMapViewChanged(mapViewState: MapViewStateInterface<*>) {
        this._selectedMarker.value = null
        _mapViewState.value = mapViewState
    }
}
