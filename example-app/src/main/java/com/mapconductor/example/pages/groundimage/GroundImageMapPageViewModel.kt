package com.mapconductor.example.pages.groundimage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.groundimage.GroundImageEvent
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineState
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
    val framePolyline: PolylineState
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

    /**
     * グラウンドイメージの外周をなぞる矩形。
     *
     * NE → NW → SW → SE → NE の順に 5 点で閉じる。SW / NE の 2 点だけでは
     * **画像がどこまで載っているのかが見えない**ので、ドラッグで範囲を変えたときの
     * 手応えを出すために引いている。react-sdk の `GroundImagePage.tsx` と同じ構成。
     *
     * `points` を差し替えるだけで再描画されるので、インスタンスは作り直さない
     * （作り直すとドラッグ中に id が変わってちらつく）。
     */
    override val framePolyline: PolylineState =
        PolylineState(
            id = "groundimage-frame",
            points = framePoints(southWestPosition, northEastPosition),
            strokeColor = Color.White,
            strokeWidth = 3.dp,
            clickable = false,
        )

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

        framePolyline.points = framePoints(southWestPosition, northEastPosition)
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

/**
 * 2 つの角から矩形の 4 辺をなぞる点列を作る。NE → NW → SW → SE → NE で閉じる。
 *
 * ドラッグで南北・東西が反転しても、常に 2 点を対角とする矩形になる。
 */
private fun framePoints(
    southWest: GeoPoint,
    northEast: GeoPoint,
): List<GeoPoint> =
    listOf(
        northEast,
        GeoPoint(latitude = northEast.latitude, longitude = southWest.longitude),
        southWest,
        GeoPoint(latitude = southWest.latitude, longitude = northEast.longitude),
        northEast,
    )
