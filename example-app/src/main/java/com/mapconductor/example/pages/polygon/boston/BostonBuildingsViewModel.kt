package com.mapconductor.example.pages.polygon.boston

import android.content.res.AssetManager
import android.os.SystemClock
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.polygon.PolygonState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private const val ASSET = "boston/boston-buildings.json"
private val TAPPED = Color(0xD92563EB)
val GRID_SIZES = listOf(1, 2, 4)

private class Building(
    val height: Double?,
    val rings: List<DoubleArray>,
)

private class BuildingsDoc(
    val attribution: String,
    val license: String,
    val west: Double,
    val south: Double,
    val east: Double,
    val north: Double,
    val buildings: List<Building>,
)

data class TappedBuilding(
    val height: Double?,
)

data class BuildingStats(
    val polygons: Int,
    val vertices: Int,
    val buildMillis: Long,
)

/**
 * Building footprints of central Boston (about 0.8 x 1.1 km) from the Overture
 * Maps buildings theme. The grid lays the same block out 2 x 2 or 4 x 4 times
 * side by side, so the page doubles as a load test for many polygons.
 *
 * `camera` ("lat,lng,zoom") and `grid` (1, 2 or 4) come from the intent extras,
 * so a measurement opens the same view on every provider.
 */
class BostonBuildingsViewModel(
    private val assets: AssetManager,
    initCamera: String?,
    initGrid: Int?,
) : ViewModel() {
    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    val initCameraPosition: MapCameraPosition =
        initCamera
            ?.split(',')
            ?.mapNotNull { it.trim().toDoubleOrNull() }
            ?.takeIf { it.size == 3 }
            ?.let { (lat, lng, zoom) -> MapCameraPosition(position = GeoPoint(lat, lng), zoom = zoom) }
            ?: MapCameraPosition(position = GeoPoint(42.35545, -71.0605), zoom = 16.0)

    private val _grid = MutableStateFlow(initGrid?.takeIf { it in GRID_SIZES } ?: 1)
    val grid: StateFlow<Int> = _grid.asStateFlow()

    private val _polygonStates = MutableStateFlow<List<PolygonState>>(emptyList())
    val polygonStates: StateFlow<List<PolygonState>> = _polygonStates.asStateFlow()

    private val _stats = MutableStateFlow<BuildingStats?>(null)
    val stats: StateFlow<BuildingStats?> = _stats.asStateFlow()

    private val _attribution = MutableStateFlow("")
    val attribution: StateFlow<String> = _attribution.asStateFlow()

    private val _tapped = MutableStateFlow<TappedBuilding?>(null)
    val tapped: StateFlow<TappedBuilding?> = _tapped.asStateFlow()

    private var doc: BuildingsDoc? = null

    init {
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) { load() }
            doc = loaded
            _attribution.value = "${loaded.attribution} (${loaded.license})"
            rebuild()
        }
    }

    fun onMapViewChanged(state: MapViewStateInterface<*>) {
        _mapViewState.value = state
    }

    fun setGrid(size: Int) {
        if (_grid.value == size) return
        _grid.value = size
        rebuild()
    }

    private fun rebuild() {
        val loaded = doc ?: return
        val size = _grid.value
        viewModelScope.launch {
            val started = SystemClock.elapsedRealtime()
            val states = withContext(Dispatchers.Default) { buildStates(loaded, size) }
            val vertices = states.sumOf { state -> state.points.size + state.holes.sumOf { it.size } }
            _stats.value = BuildingStats(states.size, vertices, SystemClock.elapsedRealtime() - started)
            _polygonStates.value = states
        }
    }

    private fun buildStates(
        doc: BuildingsDoc,
        size: Int,
    ): List<PolygonState> {
        val states = ArrayList<PolygonState>(doc.buildings.size * size * size)
        for (row in 0 until size) {
            for (column in 0 until size) {
                // Copies go east and south of the original block.
                val dLng = column * (doc.east - doc.west)
                val dLat = -row * (doc.north - doc.south)
                doc.buildings.forEachIndexed { index, building ->
                    val rings = building.rings.map { toPoints(it, dLng, dLat) }
                    states +=
                        PolygonState(
                            points = rings.first(),
                            holes = rings.drop(1),
                            id = "boston-$row-$column-$index",
                            strokeColor = Color(0xCC44403C),
                            strokeWidth = 1.dp,
                            fillColor = fillForHeight(building.height),
                            onClick = { event ->
                                event.state.fillColor = TAPPED
                                _tapped.value = TappedBuilding(building.height)
                            },
                        )
                }
            }
        }
        return states
    }

    private fun load(): BuildingsDoc {
        val json = JSONObject(assets.open(ASSET).bufferedReader().use { it.readText() })
        val bbox = json.getJSONArray("bbox")
        val buildings = json.getJSONArray("buildings")
        return BuildingsDoc(
            attribution = json.getString("attribution"),
            license = json.getString("license"),
            west = bbox.getDouble(0),
            south = bbox.getDouble(1),
            east = bbox.getDouble(2),
            north = bbox.getDouble(3),
            buildings =
                List(buildings.length()) { i ->
                    val building = buildings.getJSONObject(i)
                    val rings = building.getJSONArray("rings")
                    Building(
                        height = if (building.isNull("h")) null else building.getDouble("h"),
                        rings =
                            List(rings.length()) { r ->
                                val ring = rings.getJSONArray(r)
                                DoubleArray(ring.length()) { ring.getDouble(it) }
                            },
                    )
                },
        )
    }
}

private fun toPoints(
    ring: DoubleArray,
    dLng: Double,
    dLat: Double,
): List<GeoPoint> = List(ring.size / 2) { GeoPoint(ring[it * 2 + 1] + dLat, ring[it * 2] + dLng) }

// Darker for taller buildings; grey when the height is unknown.
private fun fillForHeight(height: Double?): Color =
    when {
        height == null -> Color(0x9994A3B8)
        height < 15 -> Color(0xB3FEF08A)
        height < 30 -> Color(0xB3FDBA74)
        height < 60 -> Color(0xB3F97316)
        height < 120 -> Color(0xB3DC2626)
        else -> Color(0xBF7F1D1D)
    }
