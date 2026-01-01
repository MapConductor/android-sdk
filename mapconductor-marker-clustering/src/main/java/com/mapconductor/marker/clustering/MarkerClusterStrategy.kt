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
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.core.spherical.expandBounds
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

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
    private var debounceJob: Job? = null
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
        renderClusters(cameraPosition, viewport, renderer, cameraUpdateToken.get())
        return true
    }

    override suspend fun onUpdate(
        state: MarkerState,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ): Boolean {
        sourceStates[state.id] = state
        val cameraPosition = lastCameraPosition ?: return true
        renderClusters(cameraPosition, viewport, renderer, cameraUpdateToken.get())
        return true
    }

    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPositionImpl,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        lastCameraPosition = cameraPosition
        lastRenderer = renderer
        val token = cameraUpdateToken.incrementAndGet()
        debounceJob?.cancel()
        debounceJob =
            debounceScope.launch {
                delay(CAMERA_DEBOUNCE_MILLIS)
                if (token != cameraUpdateToken.get()) return@launch
                val currentCamera = lastCameraPosition ?: return@launch
                val viewport = currentCamera.visibleRegion?.bounds ?: return@launch
                val currentRenderer = lastRenderer ?: return@launch
                renderClusters(currentCamera, viewport, currentRenderer, token)
            }
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
        token: Long,
    ) {
        semaphore.withPermit {
            if (token != cameraUpdateToken.get()) return@withPermit
            currentCoroutineContext().ensureActive()
            val expandedBounds = expandBounds(viewport, expandMargin)
            val zoom = cameraPosition.zoom
            val turn = updateClusteringTurn(zoom)
            val clustered = mutableMapOf<ClusterCell, MutableList<MarkerState>>()
            val debugInfos = mutableListOf<MarkerClusterDebugInfo>()

            sourceStates.values.forEach { state ->
                currentCoroutineContext().ensureActive()
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
            val candidates =
                clustered.entries
                    .sortedWith(
                        compareBy<MutableMap.MutableEntry<ClusterCell, MutableList<MarkerState>>> { it.key.x }
                            .thenBy { it.key.y },
                    ).mapNotNull { entry ->
                        val members = entry.value
                        val center = members.firstOrNull()?.position ?: return@mapNotNull null
                        ClusterCandidate(
                            center = GeoPointImpl.from(center),
                            members = members.toMutableList(),
                        )
                    }
            val mergedClusters = mergeClusters(candidates, zoom)

            mergedClusters.forEach { merged ->
                currentCoroutineContext().ensureActive()
                if (merged.members.size >= minClusterSize) {
                    val center = merged.center
                    val (cx, cy) = projectToPixel(center, zoom, tileSize)
                    val cell =
                        ClusterCell(
                            x = floor(cx / clusterRadiusPx).toInt(),
                            y = floor(cy / clusterRadiusPx).toInt(),
                        )
                    val clusterId = buildClusterId(cell, zoom, turn)
                    val radiusMeters = calculateClusterRadiusMeters(center, merged.members)
                    val cluster =
                        MarkerCluster(
                            count = merged.members.size,
                            markerIds = merged.members.map { it.id },
                        )
                    debugInfos.add(
                        MarkerClusterDebugInfo(
                            id = clusterId,
                            center = center,
                            radiusMeters = radiusMeters,
                            count = merged.members.size,
                        ),
                    )
                    val clusterState =
                        MarkerState(
                            id = clusterId,
                            position = center,
                            extra = cluster,
                            icon =
                                clusterIconProviderWithTurn?.invoke(merged.members.size, turn)
                                    ?: clusterIconProvider(merged.members.size),
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
                    desiredMarkerStates.addAll(merged.members)
                }
            }

            if (token != cameraUpdateToken.get()) return@withPermit
            _debugInfoFlow.value = debugInfos
            updateRenderedMarkers(desiredMarkerStates, renderer)
        }
    }

    private suspend fun updateRenderedMarkers(
        desiredStates: List<MarkerState>,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
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

        if (removedEntities.isNotEmpty() || addStates.isNotEmpty() || changeParams.isNotEmpty()) {
            renderer.onPostProcess()
        }
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

    private fun mergeClusters(
        candidates: List<ClusterCandidate>,
        zoom: Double,
    ): List<MergedCluster> {
        if (candidates.isEmpty()) return emptyList()
        val parent = IntArray(candidates.size) { it }

        fun find(index: Int): Int {
            var i = index
            while (parent[i] != i) {
                parent[i] = parent[parent[i]]
                i = parent[i]
            }
            return i
        }

        fun union(
            a: Int,
            b: Int,
        ) {
            val rootA = find(a)
            val rootB = find(b)
            if (rootA != rootB) {
                parent[rootB] = rootA
            }
        }

        for (i in 0 until candidates.size) {
            val centerA = candidates[i].center
            val metersPerPixelA = metersPerPixel(centerA, zoom, tileSize)
            for (j in i + 1 until candidates.size) {
                val centerB = candidates[j].center
                val metersPerPixelB = metersPerPixel(centerB, zoom, tileSize)
                val thresholdMeters = clusterRadiusPx * max(metersPerPixelA, metersPerPixelB)
                val distanceMeters = Spherical.computeDistanceBetween(centerA, centerB)
                if (distanceMeters <= thresholdMeters) {
                    union(i, j)
                }
            }
        }

        val mergedMap = linkedMapOf<Int, MutableList<ClusterCandidate>>()
        candidates.forEachIndexed { index, candidate ->
            val root = find(index)
            mergedMap.getOrPut(root) { mutableListOf() }.add(candidate)
        }

        return mergedMap.values.map { group ->
            val members = mutableListOf<MarkerState>()
            group.forEach { candidate ->
                members.addAll(candidate.members)
            }
            val center = selectDenseCenter(members, zoom)
            MergedCluster(center = center, members = members)
        }
    }

    private data class ClusterCandidate(
        val center: GeoPointImpl,
        val members: MutableList<MarkerState>,
    )

    private data class MergedCluster(
        val center: GeoPointImpl,
        val members: List<MarkerState>,
    )

    private fun selectDenseCenter(
        members: List<MarkerState>,
        zoom: Double,
    ): GeoPointImpl {
        if (members.isEmpty()) {
            return GeoPointImpl.fromLatLong(0.0, 0.0)
        }
        if (members.size == 1) {
            return GeoPointImpl.from(members[0].position)
        }

        val points =
            members.map { member ->
                val (x, y) = projectToPixel(member.position, zoom, tileSize)
                PixelPoint(member = member, x = x, y = y)
            }
        val cellSize = clusterRadiusPx
        val cellMap = linkedMapOf<CellKey, MutableList<PixelPoint>>()
        points.forEach { point ->
            val key =
                CellKey(
                    x = floor(point.x / cellSize).toInt(),
                    y = floor(point.y / cellSize).toInt(),
                )
            cellMap.getOrPut(key) { mutableListOf() }.add(point)
        }

        val sortedCells = cellMap.entries.sortedByDescending { it.value.size }
        val candidates =
            sortedCells
                .take(MAX_DENSE_CELLS)
                .flatMap { it.value }
                .take(MAX_DENSE_CANDIDATES)

        val radiusSq = cellSize * cellSize
        var bestPoint = candidates.firstOrNull() ?: points.first()
        var bestNeighborCount = -1
        var bestTotalDistance = Double.MAX_VALUE
        candidates.forEach { candidate ->
            var neighborCount = 0
            var totalDistance = 0.0
            for (dx in -1..1) {
                for (dy in -1..1) {
                    val key =
                        CellKey(
                            x = floor(candidate.x / cellSize).toInt() + dx,
                            y = floor(candidate.y / cellSize).toInt() + dy,
                        )
                    val neighbors = cellMap[key] ?: continue
                    neighbors.forEach { other ->
                        val dxp = candidate.x - other.x
                        val dyp = candidate.y - other.y
                        val distSq = dxp * dxp + dyp * dyp
                        if (distSq <= radiusSq) {
                            neighborCount += 1
                            totalDistance += sqrt(distSq)
                        }
                    }
                }
            }
            if (neighborCount > bestNeighborCount ||
                (neighborCount == bestNeighborCount && totalDistance < bestTotalDistance)
            ) {
                bestNeighborCount = neighborCount
                bestTotalDistance = totalDistance
                bestPoint = candidate
            }
        }

        return GeoPointImpl.from(bestPoint.member.position)
    }

    private fun calculateClusterRadiusMeters(
        center: GeoPointImpl,
        members: List<MarkerState>,
    ): Double {
        var maxDistance = 0.0
        members.forEach { state ->
            val distance = Spherical.computeDistanceBetween(center, state.position)
            if (distance > maxDistance) {
                maxDistance = distance
            }
        }
        return maxDistance
    }

    private data class ClusterCell(
        val x: Int,
        val y: Int,
    )

    private data class PixelPoint(
        val member: MarkerState,
        val x: Double,
        val y: Double,
    )

    private data class CellKey(
        val x: Int,
        val y: Int,
    )

    companion object {
        const val DEFAULT_CLUSTER_RADIUS_PX: Double = 60.0
        const val DEFAULT_MIN_CLUSTER_SIZE: Int = 2
        const val DEFAULT_EXPAND_MARGIN: Double = 0.2
        const val DEFAULT_TILE_SIZE: Double = 256.0
        private const val CAMERA_DEBOUNCE_MILLIS: Long = 100L
        private const val MAX_DENSE_CELLS: Int = 4
        private const val MAX_DENSE_CANDIDATES: Int = 50
        val DEFAULT_ICON_PROVIDER: (Int) -> MarkerIcon =
            { count -> ColorDefaultIcon(label = count.toString()) }
        private const val DEG_TO_RAD: Double = Math.PI / 180.0
        private const val MAX_SIN_LAT: Double = 0.9999
    }
}
