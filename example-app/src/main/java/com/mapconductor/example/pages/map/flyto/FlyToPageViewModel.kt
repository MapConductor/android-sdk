package com.mapconductor.example.pages.map.flyto

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.toast.ToastMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface FlyToPageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val messages: StateFlow<List<ToastMessage>>
    val markers: List<MarkerState>
    val polylines: List<PolylineState>
    var geodesic: Boolean

    fun onMapViewChanged(state: MapViewState<*>)

    fun flyToHonolulu()

    fun flyToTokyo()

    fun flyToLondon()

    fun flyToNewYork()

    fun flyToSydney()
}

class FlyToPageViewModelImpl(
    icons: FlyToMapIcons,
) : ViewModel(),
    FlyToPageViewModel {
    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    // Define destination locations
    private val honoluluLocation = GeoPoint.fromLatLong(21.3099, -157.8581)
    private val tokyoLocation = GeoPoint.fromLatLong(35.6762, 139.6503)
    private val londonLocation = GeoPoint.fromLatLong(51.5074, -0.1278)
    private val newYorkLocation = GeoPoint.fromLatLong(40.7128, -74.0060)
    private val sydneyLocation = GeoPoint.fromLatLong(-33.9506, 151.1815)

    override var geodesic by mutableStateOf(false)

    override val initCameraPosition =
        MapCameraPosition(
            position = tokyoLocation,
            zoom = 0.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    override val markers =
        listOf(
            MarkerState(
                id = "honolulu_marker",
                position = honoluluLocation,
                icon =
                    ImageIcon(
                        drawable = icons.honolulu,
                    ),
            ),
            MarkerState(
                id = "tokyo_marker",
                position = tokyoLocation,
                icon =
                    ImageIcon(
                        drawable = icons.tokyo,
                    ),
            ),
            MarkerState(
                id = "london_marker",
                position = londonLocation,
                icon =
                    ImageIcon(
                        drawable = icons.london,
                    ),
            ),
            MarkerState(
                id = "newyork_marker",
                position = newYorkLocation,
                icon =
                    ImageIcon(
                        drawable = icons.newYork,
                    ),
            ),
            MarkerState(
                id = "sydney_marker",
                position = sydneyLocation,
                icon =
                    ImageIcon(
                        drawable = icons.sydney,
                    ),
            ),
        )

    override val polylines
        get() =
            listOf(
                // Honolulu to NewYork
                PolylineState(
                    id = "honolulu_to_newyork",
                    points = listOf(honoluluLocation, newYorkLocation),
                    strokeColor = Color.Green.copy(alpha = 0.7f),
                    strokeWidth = 3.dp,
                    geodesic = geodesic,
                ),
                // Honolulu to Sydney
                PolylineState(
                    id = "honolulu_to_sydney",
                    points = listOf(honoluluLocation, sydneyLocation),
                    strokeColor =
                        Color( // lime
                            red = 0.7f,
                            green = 0f,
                            blue = 0f,
                            alpha = 0.7f,
                        ),
                    strokeWidth = 3.dp,
                    geodesic = geodesic,
                ),
                // Tokyo to London
                PolylineState(
                    id = "tokyo_to_london",
                    points = listOf(tokyoLocation, londonLocation),
                    strokeColor =
                        Color( // Fuchsia
                            red = 1.0f,
                            green = 0f,
                            blue = 1.0f,
                            alpha = 0.7f,
                        ),
                    strokeWidth = 3.dp,
                    geodesic = geodesic,
                ),
                // Tokyo to NewYork
                PolylineState(
                    id = "tokyo_to_newyork",
                    points = listOf(tokyoLocation, newYorkLocation),
                    strokeColor = Color.Blue.copy(alpha = 0.7f),
                    strokeWidth = 3.dp,
                    geodesic = geodesic,
                ),
                // Tokyo to Honolulu
                PolylineState(
                    id = "tokyo_to_honolulu",
                    points = listOf(tokyoLocation, honoluluLocation),
                    strokeColor =
                        Color( // Neon orange
                            red = 1.0f,
                            green = 0.35f,
                            blue = 0.12f,
                            alpha = 0.7f,
                        ),
                    strokeWidth = 3.dp,
                    geodesic = geodesic,
                ),
                // London to New York
                PolylineState(
                    id = "london_to_newyork",
                    points = listOf(londonLocation, newYorkLocation),
                    strokeColor =
                        Color( // maroon
                            red = 0.75f,
                            green = 0f,
                            blue = 0f,
                            alpha = 0.7f,
                        ),
                    strokeWidth = 3.dp,
                    geodesic = geodesic,
                ),
                // London to Sydney
                PolylineState(
                    id = "london_to_sydney",
                    points = listOf(londonLocation, sydneyLocation),
                    strokeColor =
                        Color.Magenta.copy(alpha = 0.7f),
                    strokeWidth = 3.dp,
                    geodesic = geodesic,
                ),
            )

    override fun onMapViewChanged(state: MapViewState<*>) {
        _mapViewState.value = state
    }

    override fun flyToHonolulu() {
        flyToLocation(honoluluLocation, 10.0)
    }

    override fun flyToTokyo() {
        flyToLocation(tokyoLocation, 10.0)
    }

    override fun flyToLondon() {
        flyToLocation(londonLocation, 10.0)
    }

    override fun flyToNewYork() {
        flyToLocation(newYorkLocation, 10.0)
    }

    override fun flyToSydney() {
        flyToLocation(sydneyLocation, 10.0)
    }

    private fun flyToLocation(
        location: GeoPoint,
        zoom: Double,
    ) {
        viewModelScope.launch {
            _mapViewState.value?.let { mapState ->
                val newCameraPosition =
                    MapCameraPosition(
                        position = location,
                        zoom = zoom,
                        bearing = 0.0,
                        tilt = 0.0,
                        paddings = null,
                    )

                mapState.moveCameraTo(
                    cameraPosition = newCameraPosition,
                    durationMs = 1500,
                )
            }
        }
    }
}
