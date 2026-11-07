package com.mapconductor.example.pages.polygon.boston

import android.content.res.AssetManager
import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
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
private const val TAP_LOG_TAG = "BostonTap"
private val TAPPED = Color(0xD92563EB)

/** The three nested areas around Downtown Crossing; a building belongs to the smallest that holds it. */
val AREAS = listOf("0.8 × 1.1 km", "1.6 × 2.2 km", "3.2 × 4.4 km")

class Building(
    val id: String,
    val level: Int,
    val height: Double?,
    val name: String?,
    val rings: List<DoubleArray>,
)

/** What the last tap hit: a building, or nothing. */
data class Tap(
    val seq: Int,
    val building: Building?,
)

data class BuildingStats(
    val polygons: Int,
    val vertices: Int,
    val buildMillis: Long,
)

/**
 * Building footprints of central Boston from the Overture Maps buildings theme,
 * drawn on top of the real map. Three nested areas — 0.8 x 1.1 km, 1.6 x 2.2 km
 * and 3.2 x 4.4 km — make the page double as a load test.
 *
 * Tapping a building highlights it and names it; tapping elsewhere says nothing
 * was hit. Every tap is also logged under `BostonTap`, which is how the hit
 * testing is checked from outside.
 *
 * `camera` ("lat,lng,zoom") and `area` (0, 1 or 2) come from the intent extras,
 * so a measurement opens the same view on every provider.
 */
class BostonBuildingsViewModel(
    private val assets: AssetManager,
    initCamera: String?,
    initArea: Int?,
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

    private val _area = MutableStateFlow(initArea?.takeIf { it in AREAS.indices } ?: 0)
    val area: StateFlow<Int> = _area.asStateFlow()

    private val _polygonStates = MutableStateFlow<List<PolygonState>>(emptyList())
    val polygonStates: StateFlow<List<PolygonState>> = _polygonStates.asStateFlow()

    private val _stats = MutableStateFlow<BuildingStats?>(null)
    val stats: StateFlow<BuildingStats?> = _stats.asStateFlow()

    private val _attribution = MutableStateFlow("")
    val attribution: StateFlow<String> = _attribution.asStateFlow()

    private val _tap = MutableStateFlow<Tap?>(null)
    val tap: StateFlow<Tap?> = _tap.asStateFlow()

    private var buildings: List<Building> = emptyList()
    private var states: List<PolygonState> = emptyList()
    private var buildMillis = 0L
    private var highlighted: Pair<PolygonState, Color>? = null

    init {
        viewModelScope.launch {
            val started = SystemClock.elapsedRealtime()
            val loaded = withContext(Dispatchers.IO) { load() }
            buildings = loaded
            states = withContext(Dispatchers.Default) { loaded.map(::toState) }
            buildMillis = SystemClock.elapsedRealtime() - started
            show()
        }
    }

    fun onMapViewChanged(state: MapViewStateInterface<*>) {
        _mapViewState.value = state
    }

    fun setArea(level: Int) {
        if (_area.value == level) return
        _area.value = level
        show()
    }

    fun onMapClick(point: GeoPointInterface) {
        onTap(null, null, point)
    }

    private fun show() {
        val level = _area.value
        val shown = states.filterIndexed { i, _ -> buildings[i].level <= level }
        val vertices = shown.sumOf { state -> state.points.size + state.holes.sumOf { it.size } }
        _stats.value = BuildingStats(shown.size, vertices, buildMillis)
        _polygonStates.value = shown
    }

    // Moves the highlight to `state` (or clears it) and records the tap.
    private fun onTap(
        state: PolygonState?,
        building: Building?,
        point: GeoPointInterface? = null,
    ) {
        highlighted?.let { (previous, fill) -> previous.fillColor = fill }
        highlighted = state?.let { it to it.fillColor }
        state?.fillColor = TAPPED
        val seq = (_tap.value?.seq ?: 0) + 1
        _tap.value = Tap(seq, building)
        Log.i(TAP_LOG_TAG, "seq=$seq id=${building?.id ?: ""} lat=${point?.latitude ?: ""} lng=${point?.longitude ?: ""}")
    }

    private fun toState(building: Building): PolygonState {
        val rings = building.rings.map(::toPoints)
        return PolygonState(
            points = rings.first(),
            holes = rings.drop(1),
            id = building.id,
            strokeColor = Color(0xCC44403C),
            strokeWidth = 1.dp,
            fillColor = fillForHeight(building.height),
            extra = building.id,
            onClick = { event -> onTap(event.state, building) },
        )
    }

    private fun load(): List<Building> {
        val json = JSONObject(assets.open(ASSET).bufferedReader().use { it.readText() })
        _attribution.value = "${json.getString("attribution")} (${json.getString("license")})"
        val buildings = json.getJSONArray("buildings")
        return List(buildings.length()) { i ->
            val building = buildings.getJSONObject(i)
            val rings = building.getJSONArray("rings")
            Building(
                id = building.getString("id"),
                level = building.getInt("l"),
                height = if (building.isNull("h")) null else building.getDouble("h"),
                name = building.optString("n").takeIf { it.isNotEmpty() },
                rings =
                    List(rings.length()) { r ->
                        val ring = rings.getJSONArray(r)
                        DoubleArray(ring.length()) { ring.getDouble(it) }
                    },
            )
        }
    }
}

private fun toPoints(ring: DoubleArray): List<GeoPoint> = List(ring.size / 2) { GeoPoint(ring[it * 2 + 1], ring[it * 2]) }

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
