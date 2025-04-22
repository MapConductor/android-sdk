package com.mapconductor.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mapconductor.core.GeoPointBase
import com.mapconductor.core.MapCameraPositionBase
import com.mapconductor.core.MapViewState
import com.mapconductor.example.ui.IconItem
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapViewState
import com.mapconductor.mapbox.MapboxViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModelFactory(
    private val application: Application,
    private val lifecycleOwner: LifecycleOwner
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AppViewModelImpl(application, lifecycleOwner) as T
    }
}

interface AppViewModel {
    val items: StateFlow<List<IconItem>>
    val selectedItem: StateFlow<IconItem?>
    val mapViewState: StateFlow<MapViewState?>
    fun flyTo()
}
class AppViewModelImpl(
    private val application: Application,
    private val lifecycleOwner: LifecycleOwner
): AndroidViewModel(application), AppViewModel {

    private val _items = MutableStateFlow<List<IconItem>>(emptyList())
    override val items: StateFlow<List<IconItem>> = _items

    private val _selectedItem = MutableStateFlow<IconItem?>(null)
    override val selectedItem: StateFlow<IconItem?> = _selectedItem

    private val _mapViewState = MutableStateFlow<MapViewState?>(null)
    override val mapViewState: StateFlow<MapViewState?> = _mapViewState.asStateFlow()

    private var googleMapViewState: GoogleMapViewState? = null
    private var mapboxViewState: MapboxViewState? = null
    private var hereMapViewState: HereMapViewState? = null
//    private var arcGisMapViewState: ArcGisMapViewState? = null

    init {
        val sdkOptions = listOf(
            IconItem("google", "Google Maps", R.drawable.google_maps),
            IconItem("mapbox", "Mapbox", R.drawable.mapbox),
            IconItem("here", "Here", R.drawable.here),
//            IconItem("arcgis", "ArcGIS", R.drawable.esri_logo)
        )
        _items.value = sdkOptions
        this.selectItem(sdkOptions.get(2))
    }
    private fun getOrCreate(key: String): MapViewState = when (key) {
        "google" -> this.googleMapViewState ?: GoogleMapViewState(application, "map")
        "mapbox" -> this.mapboxViewState ?: MapboxViewState(application, "map")
        "here" -> this.hereMapViewState ?: HereMapViewState(application, "map")
//        "arcgis" -> this.arcGisMapViewState ?: ArcGisMapViewState(application, "map", lifecycleOwner)
        else -> throw IllegalStateException("Unknown key: $key")
    }

    fun selectItem(item: IconItem?) {
        if (item == null) return
        _selectedItem.value = item
        viewModelScope.launch {
            if (_selectedItem.value == null) return@launch
            this@AppViewModelImpl._mapViewState.value =
                this@AppViewModelImpl.getOrCreate(_selectedItem.value!!.key)
        }
    }

    override fun flyTo() {
        this@AppViewModelImpl._mapViewState.value?.moveCameraTo(
            dstPosition = MapCameraPositionBase(
                target = GeoPointBase(
                    latitude = 40.689184289566214,
                    longitude = -74.04454331830473,
                ),
                tilt = 70.0,
                zoom = 18.0,
            ),
            durationMs = 3000,
        )
    }

    override fun onCleared() {
        super.onCleared()
    }
}