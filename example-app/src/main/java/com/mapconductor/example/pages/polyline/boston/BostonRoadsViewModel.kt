package com.mapconductor.example.pages.polyline.boston

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.polyline.PolylineTilingOptions
import com.mapconductor.example.pages.polygon.boston.LoadPhase
import android.content.Context
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val LOG_TAG = "BostonRoads"
private val TAPPED = Color(0xFF2563EB)

/** What the last tap hit: a road, or nothing (`road` null), and the point on the road nearest the tap. */
data class RoadTap(
    val seq: Int,
    val road: Road?,
    val point: GeoPoint?,
)

data class RoadStats(
    val roads: Int,
    val points: Int,
    val tiled: Boolean,
    val loadMillis: Long,
    val buildMillis: Long,
    /** From handing the list to Polylines() until two frames later; null until then. */
    val frameMillis: Long? = null,
    /** From handing the list to Polylines() until the map had all of it (every onAdded); null until then. */
    val addedMillis: Long? = null,
)

/**
 * Every road of Boston (Overture Maps, 54,645 polylines) drawn on the real map
 * with one Polylines() call: the polyline counterpart of the Boston buildings
 * page. Past the tiling threshold the SDK draws them as raster tiles; the
 * "Tiles" switch turns that off for this map ([PolylineTilingOptions.Disabled])
 * so the two can be compared, and reloads the roads so the time from handing
 * them over to the last onAdded is measured again.
 *
 * Roads are coloured, widened and stacked by class ([RoadClass]). Tapping a road
 * highlights it (an in-place change: only the tiles it is in are drawn again)
 * and shows its class, name and length in an InfoBubble at the point on the road
 * nearest the tap; tapping elsewhere closes it. Taps, loads and the time to the
 * last onAdded are logged under `BostonRoads`.
 *
 * "Overlap check" adds a tiled set of polygons, a circle and a marker over the
 * roads downtown, to see the stacking: polygon tiles < circles < polyline tiles
 * < markers.
 *
 * `camera` ("lat,lng,zoom") and `tiling` ("on" / "off") come from the intent
 * extras, so a measurement opens the same view on every provider.
 */
class BostonRoadsViewModel(
    private val context: Context,
    initCamera: String?,
    initTiling: Boolean?,
    initOverlap: Boolean,
) : ViewModel() {
    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    val initCameraPosition: MapCameraPosition =
        initCamera
            ?.split(',')
            ?.mapNotNull { it.trim().toDoubleOrNull() }
            ?.takeIf { it.size == 3 }
            ?.let { (lat, lng, zoom) -> MapCameraPosition(position = GeoPoint(lat, lng), zoom = zoom) }
            ?: MapCameraPosition(position = GeoPoint(42.3555, -71.0605), zoom = 14.0)

    private val _tiling = MutableStateFlow(initTiling ?: true)

    /** Whether this map may draw the roads as tiles (else every road is a native polyline). */
    val tiling: StateFlow<Boolean> = _tiling.asStateFlow()

    private val _overlap = MutableStateFlow(initOverlap)
    val overlap: StateFlow<Boolean> = _overlap.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private val _polylineStates = MutableStateFlow<List<PolylineState>>(emptyList())
    val polylineStates: StateFlow<List<PolylineState>> = _polylineStates.asStateFlow()

    private val _stats = MutableStateFlow<RoadStats?>(null)
    val stats: StateFlow<RoadStats?> = _stats.asStateFlow()

    private val _tap = MutableStateFlow<RoadTap?>(null)
    val tap: StateFlow<RoadTap?> = _tap.asStateFlow()

    /** Non-null while the roads are on their way; drives the loading overlay. */
    private val _progress = MutableStateFlow<LoadPhase?>(null)
    val progress: StateFlow<LoadPhase?> = _progress.asStateFlow()

    private var shownAt = 0L

    /** Which load the roads' onAdded belongs to; a callback from an earlier load is ignored. */
    private var generation = 0

    /** The ids of this load that have reached the map, from their states' onAdded. */
    private var addedIds = HashSet<String>()
    private var listSize = 0
    private var waitingForAdds = false
    private var highlighted: Pair<PolylineState, Color>? = null

    init {
        load()
    }

    /** The options for this map's [com.mapconductor.compose.polyline.PolylineTiling]. */
    fun tilingOptions(tiled: Boolean): PolylineTilingOptions =
        if (tiled) PolylineTilingOptions.Default else PolylineTilingOptions.Disabled

    fun setTiling(tiled: Boolean) {
        if (_tiling.value == tiled) return
        _tiling.value = tiled
        // The roads leave the map and come back, drawn the other way; timed again.
        load()
    }

    fun setOverlap(on: Boolean) {
        _overlap.value = on
    }

    fun onMapViewChanged(state: MapViewStateInterface<*>) {
        _mapViewState.value = state
    }

    fun onMapClick(point: GeoPointInterface) {
        onTap(null, null, point)
    }

    private fun load() {
        val gen = ++generation
        addedIds = HashSet()
        listSize = 0
        waitingForAdds = false
        _polylineStates.value = emptyList()
        _stats.value = null
        _loadError.value = null
        _tap.value = null
        highlighted = null
        _progress.value = LoadPhase.Read(0, null)
        val tiled = _tiling.value
        viewModelScope.launch {
            val started = SystemClock.elapsedRealtime()
            val roads =
                try {
                    withContext(Dispatchers.IO) {
                        loadBostonRoads(context) { phase -> _progress.value = phase }
                    }
                } catch (e: Exception) {
                    _progress.value = null
                    _loadError.value = e.message
                    return@launch
                }
            val loadMillis = SystemClock.elapsedRealtime() - started
            _progress.value = LoadPhase.Build
            val built = SystemClock.elapsedRealtime()
            val onAdded: (PolylineState) -> Unit = { state -> onRoadAdded(gen, state.id) }
            val states = withContext(Dispatchers.Default) { roads.map { toState(it, onAdded) } }
            if (gen != generation) return@launch
            val buildMillis = SystemClock.elapsedRealtime() - built
            val points = states.sumOf { it.points.size }
            _stats.value = RoadStats(states.size, points, tiled, loadMillis, buildMillis)
            Log.i(LOG_TAG, "shown roads=${states.size} points=$points tiled=$tiled load=$loadMillis build=$buildMillis")
            listSize = states.size
            waitingForAdds = true
            _progress.value = LoadPhase.Draw
            shownAt = SystemClock.elapsedRealtime()
            _polylineStates.value = states
        }
    }

    // Runs on the main thread, once per road each time the map takes it in.
    private fun onRoadAdded(
        gen: Int,
        id: String,
    ) {
        if (gen != generation || !addedIds.add(id)) return
        if (!waitingForAdds || addedIds.size < listSize) return
        waitingForAdds = false
        _progress.value = null
        val added = SystemClock.elapsedRealtime() - shownAt
        _stats.value = _stats.value?.copy(addedMillis = added)
        Log.i(LOG_TAG, "added roads=$listSize tiled=${_tiling.value} added=$added")
    }

    /** The page calls this two frames after the list reached Polylines(). */
    fun onDrawn(states: List<PolylineState>) {
        if (states.isEmpty() || states !== _polylineStates.value) return
        val frame = SystemClock.elapsedRealtime() - shownAt
        _stats.value = _stats.value?.copy(frameMillis = frame)
        Log.i(LOG_TAG, "drawn roads=${states.size} tiled=${_tiling.value} frame=$frame")
    }

    // Moves the highlight to `state` (or clears it) and records the tap.
    private fun onTap(
        state: PolylineState?,
        road: Road?,
        point: GeoPointInterface?,
    ) {
        highlighted?.let { (previous, color) -> previous.strokeColor = color }
        highlighted = state?.let { it to it.strokeColor }
        state?.strokeColor = TAPPED
        val seq = (_tap.value?.seq ?: 0) + 1
        _tap.value = RoadTap(seq, road, point?.let { GeoPoint(it.latitude, it.longitude) })
        Log.i(
            LOG_TAG,
            "tap seq=$seq id=${road?.id ?: ""} class=${road?.roadClass ?: ""} " +
                "lat=${point?.latitude ?: ""} lng=${point?.longitude ?: ""}",
        )
    }

    private fun toState(
        road: Road,
        onAdded: (PolylineState) -> Unit,
    ): PolylineState =
        PolylineState(
            points = road.points,
            id = road.id,
            strokeColor = road.roadClass.color,
            strokeWidth = road.roadClass.width.dp,
            zIndex = road.roadClass.zIndex,
            extra = road.id,
            onClick = { event -> onTap(event.state, road, event.clicked) },
            onAdded = onAdded,
        )
}
