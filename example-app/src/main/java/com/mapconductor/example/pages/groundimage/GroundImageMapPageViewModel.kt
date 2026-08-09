package com.mapconductor.example.pages.groundimage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.groundimage.GroundImageEvent
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.toast.ToastMessage
import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface GroundImageMapPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    val messages: StateFlow<List<ToastMessage>>

    val markers: List<MarkerState>
    val imageResources: GroundImageResources
    val image: Drawable
    var opacity: Float
    val groundImageState: GroundImageState

    fun onMapViewChanged(state: MapViewStateInterface<*>)

    fun onGroundImageClick(clicked: GroundImageEvent)

    fun onMarkerDrag(dragged: MarkerState)

    fun showToast(text: String)

    fun removeToast(toastMessage: ToastMessage)
}

class GroundImageMapPageViewModel(
    override val imageResources: GroundImageResources,
) : ViewModel(),
    GroundImageMapPageViewModelInterface {
    override val initCameraPosition =
        MapCameraPosition(
            // Drone imagery over the University of Eswatini campus. Same content as
            // react-sdk/examples/basic and the iOS sample.
            position =
                GeoPoint.fromLatLong(
                    latitude = -26.479235,
                    longitude = 31.306239,
                ),
            zoom = 15.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        mapViewState.value?.cameraPosition?.let {
            state.moveCameraTo(it)
        }
        this._mapViewState.value = state
    }

    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    private var southWestPosition by mutableStateOf(
        GeoPoint(
            latitude = -26.484901389754125,
            longitude = 31.2995982170105,
        ),
    )

    private var northEastPosition by mutableStateOf(
        GeoPoint(
            latitude = -26.473569450536356,
            longitude = 31.31288051605225,
        ),
    )

    private fun calculateMarkerLabels(): Pair<String, String> {
        val swLat = southWestPosition.latitude
        val swLng = southWestPosition.longitude
        val neLat = northEastPosition.latitude
        val neLng = northEastPosition.longitude

        val southWestLabel =
            when {
                swLat <= neLat && swLng <= neLng -> "SW" // Normal
                swLat <= neLat && swLng > neLng -> "SE" // East-West flipped
                swLat > neLat && swLng <= neLng -> "NW" // North-South flipped
                else -> "NE" // Both flipped
            }

        val northEastLabel =
            when {
                neLat >= swLat && neLng >= swLng -> "NE" // Normal
                neLat >= swLat && neLng < swLng -> "NW" // East-West flipped
                neLat < swLat && neLng >= swLng -> "SE" // North-South flipped
                else -> "SW" // Both flipped
            }

        return Pair(southWestLabel, northEastLabel)
    }

    override val markers: List<MarkerState>
        get() {
            val (swLabel, neLabel) = calculateMarkerLabels()
            return listOf(
                MarkerState(
                    id = "south_west",
                    position = southWestPosition,
                    icon =
                        DefaultMarkerIcon(
                            fillColor = Color.Blue,
                            strokeColor = Color.White,
                            label = swLabel,
                            labelTextColor = Color.White,
                        ),
                    draggable = true,
                    onDrag = this::onMarkerDrag,
                ),
                MarkerState(
                    id = "north_east",
                    position = northEastPosition,
                    icon =
                        DefaultMarkerIcon(
                            fillColor = Color.Red,
                            strokeColor = Color.White,
                            label = neLabel,
                            labelTextColor = Color.White,
                        ),
                    draggable = true,
                    onDrag = this::onMarkerDrag,
                ),
            )
        }

    override var opacity by mutableStateOf(1.0f)

    override var image by mutableStateOf(imageResources.image)

    private var bounds by mutableStateOf(
        GeoRectBounds(
            southWest = southWestPosition,
            northEast = northEastPosition,
        ),
    )

    override val groundImageState
        get() =
            GroundImageState(
                id = "groundImage",
                bounds = bounds,
                image = image,
                opacity = opacity,
                onClick = this::onGroundImageClick,
            )

    override fun onGroundImageClick(clicked: GroundImageEvent) {
        showToast("Ground image clicked.")
    }

    override fun onMarkerDrag(dragged: MarkerState) {
        // Update the internal position based on which marker was dragged
        when (dragged.id) {
            "south_west" -> southWestPosition = GeoPoint.from(dragged.position)
            "north_east" -> northEastPosition = GeoPoint.from(dragged.position)
        }

        // Update bounds using the new positions
        bounds =
            GeoRectBounds().also {
                it.extend(markers[0].position)
                it.extend(markers[1].position)
            }
    }

    override fun showToast(text: String) {
        this._messages.value = this._messages.value + ToastMessage(text = text)
    }

    override fun removeToast(toastMessage: ToastMessage) {
        this._messages.value = this._messages.value.filter { it != toastMessage }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
