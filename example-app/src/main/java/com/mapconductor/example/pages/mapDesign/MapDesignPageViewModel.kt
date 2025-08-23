package com.mapconductor.example.pages.mapDesign

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISMapViewState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapDesignType
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.toast.ToastMessage
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.GoogleMapDesignType
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapDesign
import com.mapconductor.here.HereMapViewState
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.MapboxMapViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapDesignButton(
    val label: String,
    val designType: MapDesignType<*>
)


interface MapDesignPageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val messages: StateFlow<List<ToastMessage>>

    var design: Int

    val buttons: StateFlow<List<MapDesignButton>>
    fun onMapViewChanged(state: MapViewState<*>)

    fun onMapClick(clicked: GeoPoint)

    fun showToast(text: String)

    fun removeToast(toastMessage: ToastMessage)
}

class MapDesignPageViewModelImpl():
    ViewModel(),
    MapDesignPageViewModel {

    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 21.382314,
                    longitude = -157.933097,
                ),
            zoom = 12.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    private val _buttons: MutableStateFlow<List<MapDesignButton>> = MutableStateFlow(emptyList())
    override val buttons: StateFlow<List<MapDesignButton>> = _buttons.asStateFlow()

    override var design by mutableStateOf(1)

    override fun onMapViewChanged(state: MapViewState<*>) {
        this._mapViewState.value = state
        when(state) {
            is GoogleMapViewState -> {
                _buttons.value = listOf(
                    MapDesignButton(
                        label = "Normal",
                        designType = GoogleMapDesign.Normal,
                    ),
                    MapDesignButton(
                        label = "Satellite",
                        designType = GoogleMapDesign.Satellite,
                    ),
                )
            }
            is HereMapViewState -> {
                _buttons.value = listOf(
                    MapDesignButton(
                        label = "NormalDay",
                        designType = HereMapDesign.NormalDay,
                    ),
                    MapDesignButton(
                        label = "NormalNigh",
                        designType = HereMapDesign.NormalNigh,
                    ),
                )
            }
            is MapboxMapViewState -> {
                _buttons.value = listOf(
                    MapDesignButton(
                        label = "standard",
                        designType = MapboxMapDesign.Standard,
                    ),
                    MapDesignButton(
                        label = "standard-satellite",
                        designType = MapboxMapDesign.StandardSatellite,
                    ),
                )
            }
            is ArcGISMapViewState -> {
                _buttons.value = listOf(
                    MapDesignButton(
                        label = "Streets",
                        designType = ArcGISDesign.Streets,
                    ),
                    MapDesignButton(
                        label = "Imagery",
                        designType = ArcGISDesign.Imagery,
                    ),
                )
            }
        }
    }

    override fun onMapClick(clicked: GeoPoint) {
        showToast("Map clicked at: ${clicked.toUrlValue()}")
    }

    override fun showToast(text: String) {
        this._messages.value = this._messages.value + ToastMessage(text = text)
    }

    override fun removeToast(toastMessage: ToastMessage) {
        this._messages.value = this._messages.value.filter { it != toastMessage }
    }
}
