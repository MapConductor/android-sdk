package com.mapconductor.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mapconductor.core.MapViewStateImpl
import com.mapconductor.example.ui.IconItem
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapViewState
import com.mapconductor.mapbox.MapboxViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface AppViewModelImpl {
    val items: StateFlow<List<IconItem>>
    val selectedItem: StateFlow<IconItem?>
    val mapViewState: StateFlow<MapViewStateImpl?>
}
class AppViewModel(private val application: Application): AndroidViewModel(application), AppViewModelImpl {

    private val _items = MutableStateFlow<List<IconItem>>(emptyList())
    override val items: StateFlow<List<IconItem>> = _items

    private val _selectedItem = MutableStateFlow<IconItem?>(null)
    override val selectedItem: StateFlow<IconItem?> = _selectedItem

    private val _mapViewState = MutableStateFlow<MapViewStateImpl?>(null)
    override val mapViewState: StateFlow<MapViewStateImpl?> = _mapViewState.asStateFlow()

    private var googleMapViewState: GoogleMapViewState? = null
    private var mapboxViewState: MapboxViewState? = null
    private var hereMapViewState: HereMapViewState? = null

    init {
        val sdkOptions = listOf(
            IconItem("google", "Google Maps", R.drawable.google_maps),
            IconItem("mapbox", "Mapbox", R.drawable.mapbox),
            IconItem("here", "Here", R.drawable.here)
        )
        _items.value = sdkOptions
        this.selectItem(sdkOptions.firstOrNull())
    }
    private fun getOrCreate(key: String): MapViewStateImpl = when (key) {
        "google" -> this.googleMapViewState ?: GoogleMapViewState(application, "map")
        "mapbox" -> this.mapboxViewState ?: MapboxViewState(application, "map")
        "here" -> this.hereMapViewState ?: HereMapViewState(application, "map")
        else -> throw IllegalStateException("Unknown key: $key")
    }

    fun selectItem(item: IconItem?) {
        if (item == null) return
        _selectedItem.value = item
        viewModelScope.launch {
            if (_selectedItem.value == null) return@launch
            this@AppViewModel._mapViewState.value =
                this@AppViewModel.getOrCreate(_selectedItem.value!!.key)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}