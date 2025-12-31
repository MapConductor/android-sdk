package com.mapconductor.example.pages.polygon.basic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PolygonMapPageViewModel {
    val initCameraPosition: MapCameraPositionImpl
    val mapViewState: StateFlow<MapViewState<*>?>

    val polygonVertexMarkers: List<MarkerState>
    var fillOpacity: Float
    var strokeWidth: Float
    val polygonState: PolygonState

    fun onMapViewChanged(state: MapViewState<*>)

    fun onMarkerDrag(dragged: MarkerState)
}

class PolygonMapPageViewModelImpl :
    ViewModel(),
    PolygonMapPageViewModel {
    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    // Polygon vertices
    private val polygonVertices =
        mutableStateListOf(
            GeoPointImpl(41.79883, 140.75675),
            GeoPointImpl(41.799240000000005, 140.75875000000002),
            GeoPointImpl(41.797650000000004, 140.75905),
            GeoPointImpl(41.79637, 140.76018000000002),
            GeoPointImpl(41.79567, 140.75845),
            GeoPointImpl(41.794470000000004, 140.75714000000002),
            GeoPointImpl(41.795010000000005, 140.75611),
            GeoPointImpl(41.79477000000001, 140.75484),
            GeoPointImpl(41.79576, 140.75475),
            GeoPointImpl(41.796150000000004, 140.75364000000002),
            GeoPointImpl(41.79744, 140.75454000000002),
            GeoPointImpl(41.79909000000001, 140.75465),
        )

    override val initCameraPosition =
        MapCameraPositionImpl(
            position = GeoPointImpl(41.796855, 140.756910),
            zoom = 16.0,
        )

    override var fillOpacity by mutableStateOf(0.3f)
    override var strokeWidth by mutableStateOf(3.0f)

    override val polygonVertexMarkers: List<MarkerState> =
        polygonVertices.mapIndexed { index, point ->
            MarkerState(
                position = point,
                icon =
                    DefaultIcon(
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
                strokeWidth = strokeWidth.dp,
                fillColor = Color.Blue.copy(alpha = fillOpacity),
                geodesic = false,
            )

    override fun onMapViewChanged(state: MapViewState<*>) {
        _mapViewState.value = state
    }

    override fun onMarkerDrag(dragged: MarkerState) {
        (dragged.extra as? Int)?.let { index ->
            if (index >= 0 && index < polygonVertices.size) {
                polygonVertices[index] = GeoPointImpl.from(dragged.position)
            }
        }
    }
}
