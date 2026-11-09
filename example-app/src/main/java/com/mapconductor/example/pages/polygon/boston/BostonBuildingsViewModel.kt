package com.mapconductor.example.pages.polygon.boston

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.polygon.PolygonState
import android.content.Context
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    /** A parcel's land use, from the City of Boston's Property Assessment. */
    val use: LandUse? = null,
)

/** What the last tap hit: a building, or nothing (`building` null), and where it landed. */
data class Tap(
    val seq: Int,
    val building: Building?,
    val point: GeoPoint?,
)

data class BuildingStats(
    val polygons: Int,
    val vertices: Int,
    val loadMillis: Long,
    val buildMillis: Long,
    /** From handing the list to Polygons() until two frames later; null until then. */
    val frameMillis: Long? = null,
)

/**
 * Polygons of Boston drawn on top of the real map: Overture building footprints
 * of downtown in three nested areas (0.8 x 1.1 km, 1.6 x 2.2 km, 3.2 x 4.4 km),
 * or the whole city's buildings or parcels (about 100,000 polygons each, see
 * [Dataset]) as a load test.
 *
 * Tapping a polygon highlights it and shows what it carries in an InfoBubble at
 * the tap; tapping elsewhere closes it and says nothing was hit. Every tap is
 * also logged under `BostonTap`, which is how the hit testing is checked from
 * outside. Parcels are coloured by land use. A loading overlay shows how far
 * reading, decoding, building the states and drawing have got.
 *
 * `camera` ("lat,lng,zoom"), `data` (downtown, buildings or parcels) and `area`
 * (0, 1 or 2) come from the intent extras, so a measurement opens the same view
 * on every provider.
 */
class BostonBuildingsViewModel(
    private val context: Context,
    initCamera: String?,
    initArea: Int?,
    initDataset: Dataset?,
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

    private val _dataset = MutableStateFlow(initDataset ?: Dataset.DOWNTOWN)
    val dataset: StateFlow<Dataset> = _dataset.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private val _polygonStates = MutableStateFlow<List<PolygonState>>(emptyList())
    val polygonStates: StateFlow<List<PolygonState>> = _polygonStates.asStateFlow()

    private val _stats = MutableStateFlow<BuildingStats?>(null)
    val stats: StateFlow<BuildingStats?> = _stats.asStateFlow()

    private val _attribution = MutableStateFlow("")
    val attribution: StateFlow<String> = _attribution.asStateFlow()

    private val _tap = MutableStateFlow<Tap?>(null)
    val tap: StateFlow<Tap?> = _tap.asStateFlow()

    /** Non-null while a dataset is on its way; drives the loading overlay. */
    private val _progress = MutableStateFlow<LoadPhase?>(null)
    val progress: StateFlow<LoadPhase?> = _progress.asStateFlow()

    private var shownAt = 0L

    private var buildings: List<Building> = emptyList()
    private var states: List<PolygonState> = emptyList()
    private var loadMillis = 0L
    private var buildMillis = 0L
    private var highlighted: Pair<PolygonState, Color>? = null

    init {
        load()
    }

    fun setDataset(dataset: Dataset) {
        if (_dataset.value == dataset) return
        _dataset.value = dataset
        load()
    }

    private fun load() {
        val dataset = _dataset.value
        _polygonStates.value = emptyList()
        _stats.value = null
        _loadError.value = null
        _tap.value = null
        highlighted = null
        _progress.value = LoadPhase.Read(0, null)
        viewModelScope.launch {
            val started = SystemClock.elapsedRealtime()
            val loaded =
                try {
                    withContext(Dispatchers.IO) {
                        loadBostonData(context, dataset) { phase -> _progress.value = phase }
                    }
                } catch (e: Exception) {
                    _progress.value = null
                    _loadError.value = e.message
                    return@launch
                }
            loadMillis = SystemClock.elapsedRealtime() - started
            _attribution.value = loaded.attribution
            _progress.value = LoadPhase.Build
            val built = SystemClock.elapsedRealtime()
            buildings = loaded.buildings
            states = withContext(Dispatchers.Default) { loaded.buildings.map { toState(it, dataset) } }
            buildMillis = SystemClock.elapsedRealtime() - built
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
        _stats.value = BuildingStats(shown.size, vertices, loadMillis, buildMillis)
        Log.i(
            TAP_LOG_TAG,
            "shown dataset=${_dataset.value} polygons=${shown.size} vertices=$vertices load=$loadMillis build=$buildMillis",
        )
        if (_progress.value != null) _progress.value = LoadPhase.Draw
        shownAt = SystemClock.elapsedRealtime()
        _polygonStates.value = shown
    }

    /** The page calls this two frames after the list reached Polygons(). */
    fun onDrawn(states: List<PolygonState>) {
        if (states.isEmpty() || states !== _polygonStates.value) return
        val frame = SystemClock.elapsedRealtime() - shownAt
        _stats.value = _stats.value?.copy(frameMillis = frame)
        _progress.value = null
        Log.i(TAP_LOG_TAG, "drawn dataset=${_dataset.value} polygons=${states.size} frame=$frame")
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
        _tap.value = Tap(seq, building, point?.let { GeoPoint(it.latitude, it.longitude) })
        Log.i(
            TAP_LOG_TAG,
            "seq=$seq id=${building?.id ?: ""} lat=${point?.latitude ?: ""} lng=${point?.longitude ?: ""}",
        )
    }

    private fun toState(
        building: Building,
        dataset: Dataset,
    ): PolygonState {
        val rings = building.rings.map(::toPoints)
        return PolygonState(
            points = rings.first(),
            holes = rings.drop(1),
            id = building.id,
            strokeColor = if (dataset == Dataset.CITY_PARCELS) Color(0xB3475569) else Color(0xCC44403C),
            strokeWidth = 1.dp,
            fillColor =
                if (dataset ==
                    Dataset.CITY_PARCELS
                ) {
                    LandUse.of(building.use?.key).fill
                } else {
                    fillForHeight(building.height)
                },
            extra = building.id,
            onClick = { event -> onTap(event.state, building, event.clicked) },
        )
    }
}

private fun toPoints(ring: DoubleArray): List<GeoPoint> =
    List(ring.size / 2) {
        GeoPoint(ring[it * 2 + 1], ring[it * 2])
    }

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
