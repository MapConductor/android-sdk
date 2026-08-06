package com.mapconductor.example.pages.circle

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapconductor.core.circle.CircleEvent
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.spherical.Planar
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.core.spherical.WGS84Geodesic.computeDistanceBetween
import com.mapconductor.example.toast.ToastMessage
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

interface CirclePageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    val messages: StateFlow<List<ToastMessage>>

    val labelPosition: StateFlow<IntOffset?>
    val polylineState: StateFlow<PolylineState?>

    val circleCenter: GeoPoint
    val radiusMeters: Double
    val centerMarker: MarkerState
    val edgeMarker: MarkerState
    val circleState: CircleState
    var fillOpacity: Float
    var strokeWidth: Float

    fun onMapViewChanged(state: MapViewStateInterface<*>)

    fun onMapCameraMove(cameraPosition: MapCameraPosition)

    fun onCircleClick(event: CircleEvent)

    fun onMarkerMove(dragged: MarkerState)

    fun showToast(text: String)

    fun removeToast(toastMessage: ToastMessage)
}

@OptIn(FlowPreview::class)
class CirclePageViewModel(
    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : ViewModel(),
    CirclePageViewModelInterface {
    private val markerDragFlow = MutableSharedFlow<GeoPointInterface>()

    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    private val _labelPosition: MutableStateFlow<IntOffset?> = MutableStateFlow(null)
    override val labelPosition: StateFlow<IntOffset?> = _labelPosition.asStateFlow()

    private val _polylineState: MutableStateFlow<PolylineState?> = MutableStateFlow(null)
    override val polylineState: StateFlow<PolylineState?> = _polylineState.asStateFlow()

    private val colors: List<Color> =
        listOf(
            Color.Blue,
            Color.Red,
            Color.Green,
            Color.Cyan,
            Color.LightGray,
            Color.Magenta,
        )
    private var tapIdx by mutableStateOf(0)
    override var fillOpacity by mutableStateOf(0.3f)
    override var strokeWidth by mutableStateOf(3.0f)

    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 21.382314,
                    longitude = -157.933097,
                ),
            zoom = 3.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    override val circleCenter = GeoPoint.fromLatLong(21.382314, -157.933097)

    override val centerMarker =
        MarkerState(
            id = "center_marker",
            position = circleCenter,
            icon =
                DefaultMarkerIcon(
                    fillColor = Color.Red,
                    strokeColor = Color.White,
                    label = "C",
                ),
            draggable = false,
        )

    override val edgeMarker: MarkerState =
        MarkerState(
            id = "edge_marker",
            position =
                Spherical.computeOffset(
                    origin = circleCenter,
                    distance = 5000000.0,
                    heading = 90.0, // East
                ),
            icon =
                DefaultMarkerIcon(
                    fillColor = Color.Green,
                    strokeColor = Color.White,
                    label = "E",
                ),
            draggable = true,
            onDragStart = this::onMarkerMove,
            onDrag = this::onMarkerMove,
            onDragEnd = this::onMarkerMove,
        )

    override val radiusMeters by derivedStateOf {
        computeDistanceBetween(circleCenter, edgeMarker.position)
    }

    init {
        updatePolylineState()
        viewModelScope.launch {
            markerDragFlow
                .sample(50)
                .collect {
                    edgeMarker.position = it
                    updateLabelPosition()
                    updatePolylineState()
                }
        }
    }

    private fun updatePolylineState() {
        viewModelScope.launch {
            _polylineState.emit(
                PolylineState(
                    points = listOf(centerMarker.position, edgeMarker.position),
                    id = "circle-radius-line",
                    geodesic = true,
                    strokeColor = Color.White,
                    strokeWidth = 3.dp,
                ),
            )
        }
    }

    private fun updateLabelPosition() {
        mainScope.launch {
            mapViewState.value?.getMapViewHolder()?.let { holder ->
                val midPoint =
                    Planar.interpolate(
                        from = centerMarker.position,
                        to = edgeMarker.position,
                        fraction = 0.5,
                    )
                holder.toScreenOffset(midPoint)?.let {
                    _labelPosition.value =
                        IntOffset(
                            x = it.x.roundToInt(),
                            y = it.y.roundToInt(),
                        )
                }
            }
        }
    }

    override val circleState: CircleState
        get() =
            CircleState(
                id = "circle",
                center = circleCenter,
                geodesic = true,
                radiusMeters = radiusMeters,
                strokeColor = Color.Blue.copy(alpha = 0.5f),
                strokeWidth = strokeWidth.dp,
                fillColor = colors[tapIdx].copy(alpha = fillOpacity),
                onClick = this::onCircleClick,
            )

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        mapViewState.value?.cameraPosition?.let {
            state.moveCameraTo(it)
        }
        this._mapViewState.value = state
    }

    override fun onMapCameraMove(cameraPosition: MapCameraPosition) {
        this.updateLabelPosition()
    }

    override fun onCircleClick(event: CircleEvent) {
        tapIdx = (tapIdx + 1) % colors.size
        showToast("Circle clicked - Radius: ${radiusMeters.toInt()}m")
    }

    override fun onMarkerMove(dragged: MarkerState) {
        viewModelScope.launch {
            markerDragFlow.emit(dragged.position)
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
