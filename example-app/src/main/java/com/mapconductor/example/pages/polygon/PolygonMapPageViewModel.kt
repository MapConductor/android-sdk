package com.mapconductor.example.pages.polygon

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.example.toast.ToastMessage
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolygonMapPageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val messages: StateFlow<List<ToastMessage>>

    val polygonVertexMarkers: List<MarkerState>
    var fillOpacity: Float
    var strokeWidth: Float
    val polygonState: PolygonState

    fun onMapViewChanged(state: MapViewState<*>)

    fun onPolygonClick(clicked: PolygonEvent)

    fun onMarkerDrag(dragged: MarkerState)

    fun showToast(text: String)

    fun removeToast(toastMessage: ToastMessage)
}

class PolygonMapPageViewModelImpl :
    ViewModel(),
    PolygonMapPageViewModel {


    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    private val _messages = MutableStateFlow<List<ToastMessage>>(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    // Polygon vertices (triangle around Tokyo landmarks)
    private val polygonVertices =
        mutableListOf(
            GeoPoint(41.79883, 140.75675),
            GeoPoint(41.799240000000005, 140.75875000000002),
            GeoPoint(41.797650000000004, 140.75905),
            GeoPoint(41.79637, 140.76018000000002),
            GeoPoint(41.79567, 140.75845),
            GeoPoint(41.794470000000004, 140.75714000000002),
            GeoPoint(41.795010000000005, 140.75611),
            GeoPoint(41.79477000000001, 140.75484),
            GeoPoint(41.79576, 140.75475),
            GeoPoint(41.796150000000004, 140.75364000000002),
            GeoPoint(41.79744, 140.75454000000002),
            GeoPoint(41.79909000000001, 140.75465)
        )

    override val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(41.796855,140.756910),
            zoom = 16.0,
        )

    override var fillOpacity by mutableStateOf(0.3f)
    override var strokeWidth by mutableStateOf(3.0f)

    override val polygonVertexMarkers: List<MarkerState> =
        polygonVertices.mapIndexed { index, point ->
            MarkerState(
                position = point,
                icon = DefaultIcon(),
                id = "vertex_$index",
                draggable = true,
            )
        }

    override val polygonState: PolygonState
        get() =
            PolygonState(
                points = polygonVertices,
                id = "example_polygon",
                strokeColor = Color.Blue,
                strokeWidth = strokeWidth.dp,
                fillColor = Color.Blue.copy(alpha = fillOpacity),
                geodesic = false,
            )

    override fun onMapViewChanged(state: MapViewState<*>) {
        _mapViewState.value = state
    }

    override fun onPolygonClick(clicked: PolygonEvent) {
        val clickedPoint = clicked.clicked
        if (clickedPoint != null) {
            showToast("Polygon clicked at: ${clickedPoint.latitude}, ${clickedPoint.longitude}")
        } else {
            showToast("Polygon clicked")
        }
    }

    override fun onMarkerDrag(dragged: MarkerState) {
        when (dragged.id) {
            "vertex_0" -> polygonVertices[0] = GeoPoint.from(dragged.position)
            "vertex_1" -> polygonVertices[1] = GeoPoint.from(dragged.position)
            "vertex_2" -> polygonVertices[2] = GeoPoint.from(dragged.position)
        }
    }

    override fun showToast(text: String) {
        val newMessage = ToastMessage(text = text)
        _messages.value = _messages.value + newMessage
    }

    override fun removeToast(toastMessage: ToastMessage) {
        _messages.value = _messages.value - toastMessage
    }
}
