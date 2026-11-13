package com.mapconductor.example.pages.polygon.collection

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polygon.PolygonState
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val COLUMNS = 10
private const val SPACING = 0.004
private val ORIGIN = GeoPoint(35.6895, 139.6917)
private val PALETTE =
    listOf(
        Color(0x664285F4),
        Color(0x66EA4335),
        Color(0x66FBBC05),
        Color(0x6634A853),
    )
private val TAPPED = Color(0xCC9C27B0)

class PolygonsMapPageViewModel : ViewModel() {
    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(ORIGIN.latitude - 0.006, ORIGIN.longitude + 0.018),
            zoom = 14.0,
        )

    private var rows = 3
    private var paletteIndex = 0

    private val _polygonStates = MutableStateFlow(buildStates())
    val polygonStates: StateFlow<List<PolygonState>> = _polygonStates.asStateFlow()

    fun onMapViewChanged(state: MapViewStateInterface<*>) {
        _mapViewState.value = state
    }

    fun addRow() {
        rows++
        _polygonStates.value = _polygonStates.value + buildRow(rows - 1)
    }

    fun removeRow() {
        if (rows == 0) return
        rows--
        _polygonStates.value = _polygonStates.value.dropLast(COLUMNS)
    }

    // New state objects with the same ids: the collection must replace them, not add.
    fun recolor() {
        paletteIndex = (paletteIndex + 1) % PALETTE.size
        _polygonStates.value = buildStates()
    }

    private fun buildStates(): List<PolygonState> = (0 until rows).flatMap(::buildRow)

    private fun buildRow(row: Int): List<PolygonState> =
        (0 until COLUMNS).map { column ->
            val center =
                GeoPoint(
                    ORIGIN.latitude - row * SPACING,
                    ORIGIN.longitude + column * SPACING + if (row % 2 == 1) SPACING / 2 else 0.0,
                )
            PolygonState(
                points = hexagon(center, SPACING * 0.45),
                id = "hex-$row-$column",
                strokeColor = Color.DarkGray,
                strokeWidth = 1.dp,
                fillColor = PALETTE[(paletteIndex + row + column) % PALETTE.size],
                onClick = ::onPolygonClick,
            )
        }

    private fun onPolygonClick(event: PolygonEvent) {
        event.state.fillColor = TAPPED
    }

    private fun hexagon(
        center: GeoPoint,
        radius: Double,
    ): List<GeoPoint> =
        (0..6).map { i ->
            val angle = Math.toRadians(60.0 * i)
            GeoPoint(center.latitude + radius * sin(angle), center.longitude + radius * cos(angle))
        }
}
