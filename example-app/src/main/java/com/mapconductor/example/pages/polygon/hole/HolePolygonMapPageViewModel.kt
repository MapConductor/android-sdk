package com.mapconductor.example.pages.polygon.hole

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface HolePolygonMapPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    val polygonState: PolygonState
    val holeVertexMarkers: List<MarkerState>

    fun onMapViewChanged(state: MapViewStateInterface<*>)

    fun onMarkerDrag(dragged: MarkerState)
}

class HolePolygonMapPageViewModel :
    ViewModel(),
    HolePolygonMapPageViewModelInterface {
    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    override val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(43.0602, 141.3195),
            zoom = 11.0,
        )

    private val holes =
        mutableStateListOf(
            listOf(
                GeoPoint(43.100869, 141.352909),
                GeoPoint(43.044443, 141.411895),
                GeoPoint(43.050601, 141.306563),
            ),
            listOf(
                GeoPoint(43.060351, 141.319905),
                GeoPoint(43.038285, 141.333247),
                GeoPoint(43.049062, 141.286901),
            ),
        )

    override val polygonState: PolygonState
        get() =
            PolygonState(
                id = "world-hole",
                points = worldPoints,
                holes = holes,
                fillColor = Color(0xCC787880),
                strokeColor = Color.Red,
                strokeWidth = 2.dp,
            )

    override val holeVertexMarkers: List<MarkerState>
        get() =
            holes.flatMapIndexed { holeIndex, hole ->
                hole.mapIndexed { vertexIndex, point ->
                    MarkerState(
                        id = "hole-$holeIndex-$vertexIndex",
                        position = point,
                        draggable = true,
                        clickable = false,
                        extra = HoleVertex(holeIndex, vertexIndex),
                        icon =
                            ColorDefaultIcon(
                                fillColor =
                                    holeMarkerColors.getOrElse(holeIndex) {
                                        Color(0xFF64748B)
                                    },
                                strokeColor = Color.White,
                                label = "${holeIndex + 1}-${vertexIndex + 1}",
                                labelTextColor = Color.White,
                            ),
                        onDrag = this::onMarkerDrag,
                        onDragEnd = this::onMarkerDrag,
                    )
                }
            }

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        _mapViewState.value = state
    }

    override fun onMarkerDrag(dragged: MarkerState) {
        val vertex = dragged.extra as? HoleVertex ?: return
        if (vertex.holeIndex !in holes.indices) return

        val hole = holes[vertex.holeIndex]
        if (vertex.vertexIndex !in hole.indices) return

        holes[vertex.holeIndex] =
            hole.mapIndexed { index, point ->
                if (index == vertex.vertexIndex) {
                    GeoPoint.from(dragged.position)
                } else {
                    point
                }
            }
    }

    private data class HoleVertex(
        val holeIndex: Int,
        val vertexIndex: Int,
    ) : java.io.Serializable

    private companion object {
        val worldPoints =
            listOf(
                GeoPoint(85.0, 90.0),
                GeoPoint(85.0, 0.1),
                GeoPoint(85.0, -90.0),
                GeoPoint(85.0, -179.9),
                GeoPoint(-85.0, -179.9),
                GeoPoint(-85.0, -90.0),
                GeoPoint(-85.0, 0.1),
                GeoPoint(-85.0, 90.0),
                GeoPoint(-85.0, 179.9),
                GeoPoint(85.0, 179.9),
            )

        val holeMarkerColors =
            listOf(
                Color(0xFF2563EB),
                Color(0xFFF97316),
            )
    }
}
