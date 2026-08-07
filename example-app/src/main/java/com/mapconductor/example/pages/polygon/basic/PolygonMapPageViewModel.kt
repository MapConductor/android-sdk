package com.mapconductor.example.pages.polygon.basic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolygonMapPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>

    val polygonVertexMarkers: List<MarkerState>
    var fillOpacity: Float
    var strokeWidth: Dp
    val polygonState: PolygonState

    fun onMapViewChanged(state: MapViewStateInterface<*>)

    fun onMarkerDrag(dragged: MarkerState)
}

class PolygonMapPageViewModel :
    ViewModel(),
    PolygonMapPageViewModelInterface {
    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    // Polygon vertices
    private val polygonVertices =
        mutableStateListOf(
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
            GeoPoint(41.79909000000001, 140.75465),
        )

    override val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(41.796855, 140.756910),
            zoom = 16.0,
        )

    override var fillOpacity by mutableStateOf(0.3f)
    override var strokeWidth by mutableStateOf(3.0.dp)

    override val polygonVertexMarkers: List<MarkerState> =
        polygonVertices.mapIndexed { index, point ->
            MarkerState(
                position = point,
                icon =
                    DefaultMarkerIcon(
                        scale = 0.7f,
                        fillColor = Color.Yellow,
                        strokeColor = Color.Black,
                    ),
                id = "vertex_$index",
                draggable = true,
                extra = index,
                onDrag = this::onMarkerDrag,
            )
        }

    override val polygonState: PolygonState
        get() =
            PolygonState(
                points = polygonVertices,
                id = "example_polygon",
                strokeColor = Color.Red,
                strokeWidth = strokeWidth,
                fillColor = Color.Blue.copy(alpha = fillOpacity),
                geodesic = false,
            )

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        _mapViewState.value = state
    }

    override fun onMarkerDrag(dragged: MarkerState) {
        (dragged.extra as? Int)?.let { index ->
            if (index >= 0 && index < polygonVertices.size) {
                polygonVertices[index] = GeoPoint.from(dragged.position)
            }
        }
    }
}
