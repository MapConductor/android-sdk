package com.mapconductor.marker.clustering

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.geocell.HexGeocellImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.marker.AbstractMarkerRenderingStrategy
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerEntityImpl
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.Earth
import com.mapconductor.core.spherical.expandBounds
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import android.util.Log
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MarkerClusterStrategy<ActualMarker>(
    private val clusterRadiusPx: Double = DEFAULT_CLUSTER_RADIUS_PX,
    private val minClusterSize: Int = DEFAULT_MIN_CLUSTER_SIZE,
    private val expandMargin: Double = DEFAULT_EXPAND_MARGIN,
    private val clusterIconProvider: (Int) -> MarkerIcon = DEFAULT_ICON_PROVIDER,
    private val clusterIconProviderWithTurn: ((Int, Int) -> MarkerIcon)? = null,
    private val includeTurnInClusterId: Boolean = false,
    private val onClusterClick: ((MarkerCluster) -> Unit)? = null,
    private val tileSize: Double = DEFAULT_TILE_SIZE,
    semaphore: Semaphore = Semaphore(1),
    geocell: HexGeocell = HexGeocellImpl.defaultGeocell(),
) : AbstractMarkerRenderingStrategy<ActualMarker>(semaphore) {
    override val markerManager: MarkerManager<ActualMarker> = MarkerManager(geocell)
    private val sourceStates = mutableMapOf<String, MarkerState>()
    private var lastCameraPosition: MapCameraPositionImpl? = null
    private var clusteringTurn = 0
    private var lastZoomKey: Int? = null
    private val debounceScope = CoroutineScope(Dispatchers.Default)
    private val cameraUpdateToken = AtomicLong(0)
    private var lastRenderer: MarkerOverlayRenderer<ActualMarker>? = null
    private val _debugInfoFlow = MutableStateFlow<List<MarkerClusterDebugInfo>>(emptyList())
    val debugInfoFlow: StateFlow<List<MarkerClusterDebugInfo>> = _debugInfoFlow

    override fun clear() {
        sourceStates.clear()
        markerManager.clear()
        _debugInfoFlow.value = emptyList()
    }

    override suspend fun onAdd(
        data: List<MarkerState>,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ): Boolean {
        updateSourceStates(data)
        val cameraPosition = lastCameraPosition ?: return true
        renderClusters(cameraPosition, viewport, renderer)
        return true
    }

    override suspend fun onUpdate(
        state: MarkerState,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ): Boolean {
        sourceStates[state.id] = state
        val cameraPosition = lastCameraPosition ?: return true
        renderClusters(cameraPosition, viewport, renderer)
        return true
    }

    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPositionImpl,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        Log.d("DEBUG", "----->onCameraChanged() start")
        lastCameraPosition = cameraPosition
        lastRenderer = renderer
        val token = cameraUpdateToken.incrementAndGet()
        debounceScope.launch {
            delay(CAMERA_DEBOUNCE_MILLIS)
            if (token != cameraUpdateToken.get()) return@launch
            val currentCamera = lastCameraPosition ?: return@launch
            val viewport = currentCamera.visibleRegion?.bounds ?: return@launch
            val currentRenderer = lastRenderer ?: return@launch
            renderClusters(currentCamera, viewport, currentRenderer)
        }
        Log.d("DEBUG", "----->onCameraChanged() end")
    }

    private fun updateSourceStates(data: List<MarkerState>) {
        val nextIds = data.map { it.id }.toSet()
        val removedIds = sourceStates.keys - nextIds
        removedIds.forEach { sourceStates.remove(it) }
        data.forEach { state -> sourceStates[state.id] = state }
    }

    private suspend fun renderClusters(
        cameraPosition: MapCameraPositionImpl,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        semaphore.withPermit {
            Log.d("DEBUG", "----->renderClusters() start")
            val expandedBounds = expandBounds(viewport, expandMargin)
            val zoom = cameraPosition.zoom
            val turn = updateClusteringTurn(zoom)
            Log.d("DEBUG", "turn=${turn}")
            val clustered = mutableMapOf<ClusterCell, MutableList<MarkerState>>()
            val debugInfos = mutableListOf<MarkerClusterDebugInfo>()

            sourceStates.values.forEach { state ->
                if (!expandedBounds.contains(state.position)) return@forEach
                val (x, y) = projectToPixel(state.position, zoom, tileSize)
                val cell =
                    ClusterCell(
                        x = floor(x / clusterRadiusPx).toInt(),
                        y = floor(y / clusterRadiusPx).toInt(),
                    )
                clustered.getOrPut(cell) { mutableListOf() }.add(state)
            }

            val desiredMarkerStates = mutableListOf<MarkerState>()

            clustered.forEach { (cell, members) ->
                if (members.size >= minClusterSize) {
                    val center = averagePosition(members)
                    val clusterId = buildClusterId(cell, zoom, turn)
                    val radiusMeters = metersPerPixel(center, zoom, tileSize) * clusterRadiusPx
                    val cluster =
                        MarkerCluster(
                            count = members.size,
                            markerIds = members.map { it.id },
                        )
                    debugInfos.add(
                        MarkerClusterDebugInfo(
                            id = clusterId,
                            center = center,
                            radiusMeters = radiusMeters,
                            count = members.size,
                        ),
                    )
                    val clusterState =
                        MarkerState(
                            id = clusterId,
                            position = center,
                            extra = cluster,
                            icon =
                                clusterIconProviderWithTurn?.invoke(members.size, turn)
                                    ?: clusterIconProvider(members.size),
                            clickable = onClusterClick != null,
                            draggable = false,
                            onClick =
                                if (onClusterClick != null) {
                                    { onClusterClick.invoke(cluster) }
                                } else {
                                    null
                                },
                        )
                    desiredMarkerStates.add(clusterState)
                } else {
                    desiredMarkerStates.addAll(members)
                }
            }
            Log.d("DEBUG", "desiredMarkerStates.size=${desiredMarkerStates.size}")

            _debugInfoFlow.value = debugInfos
            updateRenderedMarkers(desiredMarkerStates, renderer)
            Log.d("DEBUG", "----->renderClusters() end")
        }
    }

    private suspend fun updateRenderedMarkers(
        desiredStates: List<MarkerState>,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        Log.d("DEBUG", "----->updateRenderedMarkers() start")
        val desiredById = desiredStates.associateBy { it.id }
        val existing = markerManager.allEntities()
        val existingById = existing.associateBy { it.state.id }

        val removeIds = existingById.keys - desiredById.keys
        val addStates = desiredById.filterKeys { it !in existingById }.values
        val updateStates = desiredById.filterKeys { it in existingById }.values

        val removedEntities =
            removeIds.mapNotNull { id ->
                markerManager.getEntity(id)
            }
        if (removedEntities.isNotEmpty()) {
            renderer.onRemove(removedEntities)
            removeIds.forEach { id -> markerManager.removeEntity(id) }
        }
        Log.d("DEBUG", "removedEntities.size: ${removedEntities.size}")

        Log.d("DEBUG", "addStates.size: ${addStates.size}")
        if (addStates.isNotEmpty()) {
            val addParams =
                addStates.map { state ->
                    object : MarkerOverlayRenderer.AddParams {
                        override val state: MarkerState = state
                        override val bitmapIcon =
                            state.icon?.toBitmapIcon() ?: defaultIcon
                    }
                }
            val actualMarkers = renderer.onAdd(addParams)
            actualMarkers.forEachIndexed { index, actualMarker ->
                actualMarker?.let {
                    val entity: MarkerEntity<ActualMarker> =
                        MarkerEntityImpl(
                            marker = it as ActualMarker,
                            state = addParams[index].state,
                            isRendered = true,
                        )
                    markerManager.registerEntity(entity)
                }
            }
        }

        val changeParams = mutableListOf<MarkerOverlayRenderer.ChangeParams<ActualMarker>>()
        val changeEntities = mutableListOf<MarkerEntity<ActualMarker>>()

        Log.d("DEBUG", "updateStates.size: ${updateStates.size}")
        updateStates.forEach { state ->
            val prev = existingById[state.id] ?: return@forEach
            val nextEntity: MarkerEntity<ActualMarker> =
                MarkerEntityImpl(
                    marker = prev.marker,
                    state = state,
                    isRendered = true,
                )
            markerManager.registerEntity(nextEntity)

            if (prev.fingerPrint == state.fingerPrint()) {
                return@forEach
            }

            val change =
                object : MarkerOverlayRenderer.ChangeParams<ActualMarker> {
                    override val current: MarkerEntity<ActualMarker> = nextEntity
                    override val prev: MarkerEntity<ActualMarker> = prev
                    override val bitmapIcon =
                        state.icon?.toBitmapIcon() ?: defaultIcon
                }
            changeParams.add(change)
            changeEntities.add(nextEntity)
        }

        Log.d("DEBUG", "changeParams.size: ${changeParams.size}")
        if (changeParams.isNotEmpty()) {
            val actualMarkers = renderer.onChange(changeParams)
            actualMarkers.forEachIndexed { index, actualMarker ->
                actualMarker?.let {
                    val entity: MarkerEntity<ActualMarker> =
                        MarkerEntityImpl(
                            marker = it as ActualMarker,
                            state = changeEntities[index].state,
                            isRendered = true,
                        )
                    markerManager.registerEntity(entity)
                }
            }
        }

        Log.d("DEBUG", "removedEntities.size: ${removedEntities.size}")
        if (removedEntities.isNotEmpty() || addStates.isNotEmpty() || changeParams.isNotEmpty()) {
            renderer.onPostProcess()
        }
        Log.d("DEBUG", "----->updateRenderedMarkers() end")
    }

    private fun averagePosition(states: List<MarkerState>): GeoPointImpl {
        var sumLat = 0.0
        var sumLon = 0.0
        states.forEach { state ->
            sumLat += state.position.latitude
            sumLon += state.position.longitude
        }
        val count = states.size.coerceAtLeast(1)
        return GeoPointImpl.fromLatLong(
            latitude = sumLat / count,
            longitude = sumLon / count,
        )
    }

    private fun buildClusterId(
        cell: ClusterCell,
        zoom: Double,
        turn: Int,
    ): String =
        if (includeTurnInClusterId) {
            "cluster_${zoom.roundToInt()}_${cell.x}_${cell.y}_$turn"
        } else {
            "cluster_${zoom.roundToInt()}_${cell.x}_${cell.y}"
        }

    private fun projectToPixel(
        position: GeoPoint,
        zoom: Double,
        tileSize: Double,
    ): Pair<Double, Double> {
        val scale = tileSize * 2.0.pow(zoom)
        val sinLat = sin(position.latitude * DEG_TO_RAD).coerceIn(-MAX_SIN_LAT, MAX_SIN_LAT)
        val x = (position.longitude + 180.0) / 360.0 * scale
        val y = (0.5 - ln((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * Math.PI)) * scale
        return Pair(x, y)
    }

    private fun updateClusteringTurn(zoom: Double): Int {
        val zoomKey = (zoom * 100).roundToInt()
        if (lastZoomKey == null) {
            clusteringTurn = 1
            lastZoomKey = zoomKey
            return clusteringTurn
        }
        if (lastZoomKey != zoomKey) {
            clusteringTurn += 1
            lastZoomKey = zoomKey
        }
        return clusteringTurn
    }

    private fun metersPerPixel(
        position: GeoPoint,
        zoom: Double,
        tileSize: Double,
    ): Double {
        val scale = tileSize * 2.0.pow(zoom)
        val latitudeRadians = position.latitude * DEG_TO_RAD
        return (Earth.CIRCUMFERENCE_METERS * cos(latitudeRadians)) / scale
    }

    private data class ClusterCell(
        val x: Int,
        val y: Int,
    )


    companion object {
        const val DEFAULT_CLUSTER_RADIUS_PX: Double = 60.0
        const val DEFAULT_MIN_CLUSTER_SIZE: Int = 2
        const val DEFAULT_EXPAND_MARGIN: Double = 0.2
        const val DEFAULT_TILE_SIZE: Double = 256.0
        private const val CAMERA_DEBOUNCE_MILLIS: Long = 100L
        val DEFAULT_ICON_PROVIDER: (Int) -> MarkerIcon =
            { count -> ColorDefaultIcon(label = count.toString()) }
        private const val DEG_TO_RAD: Double = Math.PI / 180.0
        private const val MAX_SIN_LAT: Double = 0.9999
    }
}
