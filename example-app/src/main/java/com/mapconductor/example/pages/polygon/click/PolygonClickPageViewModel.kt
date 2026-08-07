package com.mapconductor.example.pages.polygon.click

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolygonClickPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    val markerState: StateFlow<MarkerState?>
    val showInfoBubble: StateFlow<Boolean>
    val message: StateFlow<String>

    fun onMapViewChanged(state: MapViewStateInterface<*>)

    fun onMapClicked(clicked: GeoPoint)

    fun onPolygonClicked(event: PolygonEvent)
}

class PolygonClickPageViewModel :
    ViewModel(),
    PolygonClickPageViewModelInterface {
    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    private val _markerState = MutableStateFlow<MarkerState?>(null)
    override val markerState: StateFlow<MarkerState?> = _markerState.asStateFlow()

    private val _showInfoBubble = MutableStateFlow(false)
    override val showInfoBubble: StateFlow<Boolean> = _showInfoBubble.asStateFlow()

    private val _message = MutableStateFlow<String>("")
    override val message: StateFlow<String> = _message.asStateFlow()

    private var markerSequence = 0L

    override val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(36.73030, -120.24512),
            zoom = 5.0,
        )

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        _mapViewState.value = state
    }

    override fun onMapClicked(clicked: GeoPoint) {
        showDroppedMarker(clicked, "Outside")
    }

    override fun onPolygonClicked(event: PolygonEvent) {
        val latLng = GeoPoint.from(event.clicked).toUrlValue()
        showDroppedMarker(event.clicked, "Inside\n$latLng")
    }

    private fun showDroppedMarker(
        position: GeoPointInterface,
        message: String,
    ) {
        _message.value = message
        _showInfoBubble.value = false
        val marker =
            MarkerState(
                // クリックごとに別Markerとして生成し、Dropを必ず先頭から再生する。
                id = "clicked-${++markerSequence}",
                position = position,
                animation = MarkerAnimation.Drop,
                onAnimateEnd = { completedMarker ->
                    // 連続タップ時、古いMarkerの完了通知で新しいBubbleを先に出さない。
                    if (_markerState.value === completedMarker) {
                        _showInfoBubble.value = true
                    }
                },
            )
        _markerState.value = marker
    }
}
