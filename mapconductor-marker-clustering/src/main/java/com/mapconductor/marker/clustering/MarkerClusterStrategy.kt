package com.mapconductor.marker.clustering

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.geocell.HexGeocellInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.AbstractMarkerRenderingStrategy
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.core.marker.MarkerIconInterface
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.Earth
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.core.spherical.expandBounds
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
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
import kotlinx.coroutines.channels.Channel
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
    private val clusterIconProvider: (Int) -> MarkerIconInterface = DEFAULT_ICON_PROVIDER,
    private val clusterIconProviderWithTurn: ((Int, Int) -> MarkerIconInterface)? = null,
    private val onClusterClick: ((MarkerCluster) -> Unit)? = null,
    private val enableZoomAnimation: Boolean = false,
    private val enablePanAnimation: Boolean = false,
    private val zoomAnimationDurationMillis: Long = DEFAULT_ZOOM_ANIMATION_DURATION_MILLIS,
    private val debugIncludeRenderCount: Boolean = false,
    private val cameraIdleDebounceMillis: Long = DEFAULT_CAMERA_DEBOUNCE_MILLIS,
    private val tileSize: Double = DEFAULT_TILE_SIZE,
    semaphore: Semaphore = Semaphore(3),
    geocell: HexGeocellInterface = HexGeocell.defaultGeocell(),
) : AbstractMarkerRenderingStrategy<ActualMarker>(semaphore) {
    override val markerManager: MarkerManager<ActualMarker> = MarkerManager(geocell)
    private val sourceStates = mutableMapOf<String, MarkerState>()
    private var lastCameraPosition: MapCameraPosition? = null
    private var clusteringTurn = 0
    private var lastZoomKey: Int? = null
    private val debounceScope = CoroutineScope(Dispatchers.Default)
    private val cameraUpdateToken = AtomicLong(0)
    private var lastRenderer: MarkerOverlayRendererInterface<ActualMarker>? = null
    private var debounceJob: Job? = null

    @Volatile private var isRendering = false
    private val renderRequests = Channel<RenderRequest<ActualMarker>>(Channel.CONFLATED)
    private var renderWorker: Job? = null
    private var lastRenderCameraPosition: MapCameraPosition? = null
    private val _debugInfoFlow = MutableStateFlow<List<MarkerClusterDebugInfo>>(emptyList())
    val debugInfoFlow: StateFlow<List<MarkerClusterDebugInfo>> = _debugInfoFlow
    private var lastClusterMemberCenters: Map<String, GeoPoint> = emptyMap()
    private var lastClusterPositions: Map<String, GeoPoint> = emptyMap()
    private var lastClusterAssignments: Map<String, String> = emptyMap()
    private var lastClusterCoverageBounds: GeoRectBounds? = null
    private var renderCount = 0
    private val renderedMarkerEntities = mutableMapOf<String, MarkerEntityInterface<ActualMarker>>()

    override fun clear() {
        sourceStates.clear()
        markerManager.clear()
        _debugInfoFlow.value = emptyList()
        lastClusterMemberCenters = emptyMap()
        lastClusterPositions = emptyMap()
        lastClusterAssignments = emptyMap()
        lastClusterCoverageBounds = null
        lastZoomKey = null
        clusteringTurn = 0
        renderCount = 0
        renderedMarkerEntities.clear()
        lastRenderCameraPosition = null
    }

    override suspend fun onAdd(
        data: List<MarkerState>,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ): Boolean {
        updateSourceStates(data)
        val cameraPosition = lastCameraPosition ?: return true
        enqueueRender(cameraPosition, viewport, renderer, cameraUpdateToken.get())
        return true
    }

    override suspend fun onUpdate(
        state: MarkerState,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ): Boolean {
        sourceStates[state.id] = state
        val cameraPosition = lastCameraPosition ?: return true
        enqueueRender(cameraPosition, viewport, renderer, cameraUpdateToken.get())
        return true
    }

    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPosition,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ) {
        lastCameraPosition = cameraPosition
        lastRenderer = renderer
        val token = cameraUpdateToken.incrementAndGet()
        if (debounceJob?.isActive == true && !isRendering) {
            debounceJob?.cancel()
        }
        debounceJob =
            debounceScope.launch {
                if (cameraIdleDebounceMillis > 0) {
                    delay(cameraIdleDebounceMillis)
                }
                if (token != cameraUpdateToken.get()) return@launch
                val currentCamera = lastCameraPosition ?: return@launch
                val viewport = currentCamera.visibleRegion?.bounds ?: return@launch
                val currentRenderer = lastRenderer ?: return@launch
                enqueueRender(currentCamera, viewport, currentRenderer, token)
            }
    }

    private fun enqueueRender(
        cameraPosition: MapCameraPosition,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
        token: Long,
    ) {
        if (renderWorker == null) {
            startRenderWorker()
        }
        renderRequests.trySend(
            RenderRequest(
                cameraPosition = cameraPosition,
                viewport = viewport,
                renderer = renderer,
                token = token,
            ),
        )
    }

    private fun startRenderWorker() {
        if (renderWorker != null) return
        renderWorker =
            debounceScope.launch {
                for (request in renderRequests) {
                    isRendering = true
                    try {
                        renderClusters(
                            cameraPosition = request.cameraPosition,
                            viewport = request.viewport,
                            renderer = request.renderer,
                            token = request.token,
                        )
                    } finally {
                        isRendering = false
                    }
                }
            }
    }

    private fun updateSourceStates(data: List<MarkerState>) {
        val nextIds = data.map { it.id }.toSet()
        val removedIds = sourceStates.keys - nextIds
        removedIds.forEach { sourceStates.remove(it) }
        data.forEach { state -> sourceStates[state.id] = state }
    }

    private suspend fun renderClusters(
        cameraPosition: MapCameraPosition,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
        token: Long,
    ) {
        semaphore.withPermit {
            if (token != cameraUpdateToken.get()) return@withPermit
            currentCoroutineContext().ensureActive()
            renderCount++
            val expandedBounds = expandBounds(viewport, expandMargin)
            val zoom = cameraPosition.zoom
            val zoomChange = updateClusteringTurn(zoom)
            val turn = zoomChange.turn
            val zoomChanged = zoomChange.zoomChanged
            val cameraMoved =
                lastRenderCameraPosition?.let { previous ->
                    hasCameraMoved(previous, cameraPosition)
                } ?: false
            val animateTransitions =
                (enableZoomAnimation && zoomChanged) ||
                    (enablePanAnimation && cameraMoved)

            if (!zoomChanged &&
                lastClusterCoverageBounds != null &&
                containsBounds(lastClusterCoverageBounds!!, expandedBounds)
            ) {
                lastRenderCameraPosition = cameraPosition
                return@withPermit
            }

            cleanupStaleMarkers(
                currentZoom = zoom,
                renderer = renderer,
                skipClusterRemoval = animateTransitions,
            )

            val debugInfos = mutableListOf<MarkerClusterDebugInfo>()
            val clusterMemberCenters = mutableMapOf<String, GeoPoint>()
            val clusterPositions = mutableMapOf<String, GeoPoint>()

            if (zoomChanged) {
                lastClusterAssignments = emptyMap()
            }

            val cachedMarkers = mutableListOf<MarkerState>()
            val newMarkers = mutableListOf<MarkerState>()
            sourceStates.values.forEach { state ->
                currentCoroutineContext().ensureActive()
                if (!expandedBounds.contains(state.position)) return@forEach

                if (!zoomChanged &&
                    lastClusterCoverageBounds?.contains(state.position) == true &&
                    lastClusterAssignments.containsKey(state.id)
                ) {
                    cachedMarkers.add(state)
                } else {
                    newMarkers.add(state)
                }
            }

            val cachedClusterGroups = mutableMapOf<String, MutableList<MarkerState>>()
            val cachedMarkerGroups = mutableMapOf<String, MutableList<MarkerState>>()
            cachedMarkers.forEach { marker ->
                val clusterId = lastClusterAssignments[marker.id]
                if (clusterId != null && clusterId.startsWith("cluster_")) {
                    cachedClusterGroups.getOrPut(clusterId) { mutableListOf() }.add(marker)
                } else {
                    val key = clusterId ?: marker.id
                    cachedMarkerGroups.getOrPut(key) { mutableListOf() }.add(marker)
                }
            }

            val clustered = mutableMapOf<ClusterCell, MutableList<MarkerState>>()
            newMarkers.forEach { state ->
                currentCoroutineContext().ensureActive()
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
                            center = GeoPoint.from(center),
                            members = members.toMutableList(),
                        )
                    }
            val mergedClusters = mergeClusters(candidates, zoom)

            val finalMergedClusters = mutableListOf<MergedCluster>()
            val usedCachedClusters = mutableSetOf<String>()

            mergedClusters.forEach { merged ->
                currentCoroutineContext().ensureActive()
                var mergedWithCached = false
                val newCenter = merged.center

                cachedClusterGroups.forEach { (cachedClusterId, cachedMembers) ->
                    if (mergedWithCached || cachedClusterId in usedCachedClusters) return@forEach
                    val cachedPosition = lastClusterPositions[cachedClusterId] ?: return@forEach
                    val metersPerPixelVal = metersPerPixel(newCenter, zoom, tileSize)
                    val thresholdMeters = clusterRadiusPx * metersPerPixelVal
                    val distance = Spherical.computeDistanceBetween(newCenter, cachedPosition)
                    if (distance <= thresholdMeters) {
                        val combinedMembers = cachedMembers + merged.members
                        finalMergedClusters.add(
                            MergedCluster(
                                center = cachedPosition,
                                members = combinedMembers.toMutableList(),
                            ),
                        )
                        usedCachedClusters.add(cachedClusterId)
                        mergedWithCached = true
                    }
                }

                if (!mergedWithCached) {
                    finalMergedClusters.add(merged)
                }
            }

            cachedClusterGroups.forEach { (cachedClusterId, cachedMembers) ->
                if (cachedClusterId in usedCachedClusters) return@forEach
                val cachedPosition = lastClusterPositions[cachedClusterId] ?: return@forEach
                finalMergedClusters.add(
                    MergedCluster(
                        center = cachedPosition,
                        members = cachedMembers,
                    ),
                )
            }

            cachedMarkerGroups.values.forEach { cachedMembers ->
                val center = cachedMembers.firstOrNull()?.position ?: return@forEach
                finalMergedClusters.add(
                    MergedCluster(
                        center = GeoPoint.from(center),
                        members = cachedMembers,
                    ),
                )
            }

            val coverageBounds = GeoRectBounds()
            val nextClusterAssignments = mutableMapOf<String, String>()

            finalMergedClusters.forEach { merged ->
                currentCoroutineContext().ensureActive()
                if (merged.members.size >= minClusterSize) {
                    val initialCenter = merged.center
                    val center =
                        if (!zoomChanged) {
                            val (cx, cy) = projectToPixel(initialCenter, zoom, tileSize)
                            val cell =
                                ClusterCell(
                                    x = floor(cx / clusterRadiusPx).toInt(),
                                    y = floor(cy / clusterRadiusPx).toInt(),
                                )
                            val clusterId = buildClusterId(cell, zoom)
                            lastClusterPositions[clusterId] ?: initialCenter
                        } else {
                            initialCenter
                        }
                    val (cx, cy) = projectToPixel(center, zoom, tileSize)
                    val cell =
                        ClusterCell(
                            x = floor(cx / clusterRadiusPx).toInt(),
                            y = floor(cy / clusterRadiusPx).toInt(),
                        )
                    val clusterId = buildClusterId(cell, zoom)
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
                    merged.members.forEach { member ->
                        clusterMemberCenters[member.id] = center
                        nextClusterAssignments[member.id] = clusterId
                    }
                    clusterPositions[clusterId] = center
                    extendCoverageBounds(coverageBounds, center, radiusMeters)
                    val clusterIcon =
                        if (debugIncludeRenderCount) {
                            val baseLabel =
                                if (clusterIconProviderWithTurn != null) {
                                    "T$turn"
                                } else {
                                    merged.members.size.toString()
                                }
                            ColorDefaultIcon(label = "$baseLabel\nR$renderCount")
                        } else {
                            clusterIconProviderWithTurn?.invoke(merged.members.size, turn)
                                ?: clusterIconProvider(merged.members.size)
                        }
                    val clusterState =
                        MarkerState(
                            id = clusterId,
                            position = center,
                            extra = cluster,
                            icon = clusterIcon,
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
                    merged.members.forEach { member ->
                        coverageBounds.extend(member.position)
                        nextClusterAssignments[member.id] = member.id
                    }
                    desiredMarkerStates.addAll(merged.members)
                }
            }

            if (token != cameraUpdateToken.get()) return@withPermit
            _debugInfoFlow.value = debugInfos
            val previousClusterMemberCenters = lastClusterMemberCenters
            val previousClusterPositions = lastClusterPositions
            updateRenderedMarkers(
                desiredStates = desiredMarkerStates,
                renderer = renderer,
                token = token,
                animateTransitions = animateTransitions,
                previousClusterMemberCenters = previousClusterMemberCenters,
                nextClusterMemberCenters = clusterMemberCenters,
                previousClusterPositions = previousClusterPositions,
                nextClusterPositions = clusterPositions,
            )
            lastClusterMemberCenters = clusterMemberCenters
            lastClusterPositions = clusterPositions
            lastClusterAssignments = nextClusterAssignments
            lastRenderCameraPosition = cameraPosition
            lastClusterCoverageBounds = if (coverageBounds.isEmpty) null else coverageBounds
        }
    }

    private suspend fun updateRenderedMarkers(
        desiredStates: List<MarkerState>,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
        token: Long,
        animateTransitions: Boolean,
        previousClusterMemberCenters: Map<String, GeoPoint>,
        nextClusterMemberCenters: Map<String, GeoPoint>,
        previousClusterPositions: Map<String, GeoPoint>,
        nextClusterPositions: Map<String, GeoPoint>,
    ) {
        val desiredById = desiredStates.associateBy { it.id }
        val animateZoom = animateTransitions && zoomAnimationDurationMillis > 0L
        val existing = markerManager.allEntities()
        val existingById = existing.associateBy { it.state.id }

        if (!animateZoom) {
            val orphanedIds = existingById.keys - desiredById.keys
            val orphanedEntitiesBeforeAnimation =
                orphanedIds.mapNotNull { id ->
                    renderedMarkerEntities[id]
                }
            if (orphanedEntitiesBeforeAnimation.isNotEmpty()) {
                renderer.onRemove(orphanedEntitiesBeforeAnimation)
                orphanedEntitiesBeforeAnimation.forEach { entity ->
                    renderedMarkerEntities.remove(entity.state.id)
                    markerManager.removeEntity(entity.state.id)
                }
                renderer.onPostProcess()
            }
        }

        val existingAfterCleanup = markerManager.allEntities()
        val existingByIdAfterCleanup = existingAfterCleanup.associateBy { it.state.id }

        val removeIds = existingByIdAfterCleanup.keys - desiredById.keys
        val addStates = desiredById.filterKeys { it !in existingByIdAfterCleanup }.values
        val updateStates = desiredById.filterKeys { it in existingByIdAfterCleanup }.values

        val animatedRemoveEntries =
            if (animateZoom) {
                removeIds.mapNotNull { id ->
                    val entity = existingByIdAfterCleanup[id] ?: return@mapNotNull null
                    val isCluster = id.startsWith("cluster_")

                    val target =
                        if (isCluster) {
                            val cluster = entity.state.extra as? MarkerCluster
                            val memberIds = cluster?.markerIds ?: emptyList()
                            if (memberIds.isEmpty()) return@mapNotNull null
                            val memberTargets =
                                memberIds.mapNotNull { memberId ->
                                    nextClusterMemberCenters[memberId]
                                }
                            if (memberTargets.isEmpty()) return@mapNotNull null
                            averageGeoPoints(memberTargets)
                        } else {
                            nextClusterMemberCenters[id] ?: return@mapNotNull null
                        }
                    AnimatedRemove(entity = entity, target = target)
                }
            } else {
                emptyList()
            }
        val animatedRemoveIds = animatedRemoveEntries.map { it.entity.state.id }.toSet()

        val animatedAddEntries =
            if (animateZoom) {
                addStates.mapNotNull { state ->
                    val isCluster = state.id.startsWith("cluster_")

                    val start =
                        if (isCluster) {
                            val cluster = state.extra as? MarkerCluster
                            val memberIds = cluster?.markerIds ?: emptyList()
                            if (memberIds.isEmpty()) return@mapNotNull null
                            val memberStarts =
                                memberIds.mapNotNull { memberId ->
                                    previousClusterMemberCenters[memberId]
                                }
                            if (memberStarts.isEmpty()) return@mapNotNull null
                            averageGeoPoints(memberStarts)
                        } else {
                            previousClusterMemberCenters[state.id] ?: return@mapNotNull null
                        }
                    AnimatedAdd(state = state, start = start)
                }
            } else {
                emptyList()
            }
        val animatedAddIds = animatedAddEntries.map { it.state.id }.toSet()

        val immediateRemoveIds = removeIds - animatedRemoveIds
        val immediateAddStates = addStates.filterNot { it.id in animatedAddIds }

        var didImmediateChange = false
        if (immediateRemoveIds.isNotEmpty()) {
            val removedEntities =
                immediateRemoveIds.mapNotNull { id ->
                    renderedMarkerEntities[id]
                }
            if (removedEntities.isNotEmpty()) {
                renderer.onRemove(removedEntities)
                removedEntities.forEach { entity ->
                    renderedMarkerEntities.remove(entity.state.id)
                    markerManager.removeEntity(entity.state.id)
                }
                didImmediateChange = true
            }
        }
        if (immediateAddStates.isNotEmpty()) {
            addStatesToRenderer(immediateAddStates, renderer)
            didImmediateChange = true
        }

        val changeParams = mutableListOf<MarkerOverlayRendererInterface.ChangeParamsInterface<ActualMarker>>()
        val changeEntities = mutableListOf<MarkerEntityInterface<ActualMarker>>()

        updateStates.forEach { state ->
            val prev = existingByIdAfterCleanup[state.id] ?: return@forEach
            val nextEntity: MarkerEntityInterface<ActualMarker> =
                MarkerEntity(
                    marker = prev.marker,
                    state = state,
                    isRendered = true,
                )
            markerManager.registerEntity(nextEntity)

            if (prev.fingerPrint == state.fingerPrint()) {
                return@forEach
            }

            val change =
                object : MarkerOverlayRendererInterface.ChangeParamsInterface<ActualMarker> {
                    override val current: MarkerEntityInterface<ActualMarker> = nextEntity
                    override val prev: MarkerEntityInterface<ActualMarker> = prev
                    override val bitmapIcon =
                        state.icon?.toBitmapIcon() ?: defaultMarkerIcon
                }
            changeParams.add(change)
            changeEntities.add(nextEntity)
        }

        if (changeParams.isNotEmpty()) {
            val actualMarkers = renderer.onChange(changeParams)
            actualMarkers.forEachIndexed { index, actualMarker ->
                actualMarker?.let {
                    val entity: MarkerEntityInterface<ActualMarker> =
                        MarkerEntity(
                            marker = it as ActualMarker,
                            state = changeEntities[index].state,
                            isRendered = true,
                        )
                    markerManager.registerEntity(entity)
                    renderedMarkerEntities[entity.state.id] = entity
                }
            }
            didImmediateChange = true
        }

        if (didImmediateChange) {
            renderer.onPostProcess()
        }

        if (!animateZoom || (animatedRemoveEntries.isEmpty() && animatedAddEntries.isEmpty())) {
            return
        }
        if (token != cameraUpdateToken.get()) return

        val animatedStartEntities =
            if (animatedAddEntries.isNotEmpty()) {
                val animatedStartStates =
                    animatedAddEntries.map { entry ->
                        entry.state.copy(position = entry.start)
                    }
                val added = addStatesToRenderer(animatedStartStates, renderer)
                renderer.onPostProcess()
                added
            } else {
                emptyList()
            }

        val moves = mutableListOf<AnimatedMove<ActualMarker>>()
        animatedAddEntries.forEach { entry ->
            val entity = markerManager.getEntity(entry.state.id) ?: return@forEach
            moves.add(
                AnimatedMove(
                    id = entry.state.id,
                    start = entry.start,
                    end = entry.state.position,
                    baseState = entry.state,
                    entity = entity,
                ),
            )
        }
        animatedRemoveEntries.forEach { entry ->
            moves.add(
                AnimatedMove(
                    id = entry.entity.state.id,
                    start = entry.entity.state.position,
                    end = entry.target,
                    baseState = entry.entity.state,
                    entity = entry.entity,
                ),
            )
        }

        val completed = animateMarkerMoves(moves, renderer, zoomAnimationDurationMillis, token)

        if (animatedRemoveEntries.isNotEmpty()) {
            val entitiesToRemove =
                animatedRemoveEntries
                    .map { entry -> entry.entity }
                    .filter { entity -> renderedMarkerEntities.containsKey(entity.state.id) }
            if (entitiesToRemove.isNotEmpty()) {
                renderer.onRemove(entitiesToRemove)
                entitiesToRemove.forEach { entity ->
                    renderedMarkerEntities.remove(entity.state.id)
                    markerManager.removeEntity(entity.state.id)
                }
                renderer.onPostProcess()
            }
        }

        if (!completed) {
            if (animatedStartEntities.isNotEmpty()) {
                val entitiesToRemoveOnCancel =
                    animatedStartEntities.filter { entity ->
                        renderedMarkerEntities.containsKey(entity.state.id)
                    }
                if (entitiesToRemoveOnCancel.isNotEmpty()) {
                    renderer.onRemove(entitiesToRemoveOnCancel)
                    entitiesToRemoveOnCancel.forEach { entity ->
                        renderedMarkerEntities.remove(entity.state.id)
                        markerManager.removeEntity(entity.state.id)
                    }
                    renderer.onPostProcess()
                }
            }
        }
    }

    private suspend fun addStatesToRenderer(
        states: List<MarkerState>,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ): List<MarkerEntityInterface<ActualMarker>> {
        if (states.isEmpty()) return emptyList()
        val addedEntities = mutableListOf<MarkerEntityInterface<ActualMarker>>()
        val addParams =
            states.map { state ->
                object : MarkerOverlayRendererInterface.AddParamsInterface {
                    override val state: MarkerState = state
                    override val bitmapIcon =
                        state.icon?.toBitmapIcon() ?: defaultMarkerIcon
                }
            }
        val actualMarkers = renderer.onAdd(addParams)
        actualMarkers.forEachIndexed { index, actualMarker ->
            val marker = actualMarker ?: return@forEachIndexed
            val entity: MarkerEntityInterface<ActualMarker> =
                MarkerEntity(
                    marker = marker as ActualMarker,
                    state = addParams[index].state,
                    isRendered = true,
                )
            markerManager.registerEntity(entity)
            renderedMarkerEntities[entity.state.id] = entity
            addedEntities.add(entity)
        }
        return addedEntities
    }

    private suspend fun animateMarkerMoves(
        moves: MutableList<AnimatedMove<ActualMarker>>,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
        durationMillis: Long,
        token: Long,
    ): Boolean {
        if (moves.isEmpty()) return true
        val steps = max(1, (durationMillis / DEFAULT_ANIMATION_FRAME_MILLIS).toInt())
        val stepMillis =
            if (steps <= 1) {
                durationMillis
            } else {
                DEFAULT_ANIMATION_FRAME_MILLIS
            }
        for (step in 1..steps) {
            if (token != cameraUpdateToken.get()) return false
            currentCoroutineContext().ensureActive()
            val t = step.toDouble() / steps.toDouble()
            val changeParams = mutableListOf<MarkerOverlayRendererInterface.ChangeParamsInterface<ActualMarker>>()
            val changeEntities = mutableListOf<MarkerEntityInterface<ActualMarker>>()
            moves.forEach { move ->
                val position = interpolatePosition(move.start, move.end, t)
                val nextState = move.baseState.copy(position = position)
                val prevEntity = move.entity
                val nextEntity =
                    MarkerEntity(
                        marker = prevEntity.marker,
                        state = nextState,
                        isRendered = true,
                    )
                val change =
                    object : MarkerOverlayRendererInterface.ChangeParamsInterface<ActualMarker> {
                        override val current: MarkerEntityInterface<ActualMarker> = nextEntity
                        override val prev: MarkerEntityInterface<ActualMarker> = prevEntity
                        override val bitmapIcon =
                            nextState.icon?.toBitmapIcon() ?: defaultMarkerIcon
                    }
                changeParams.add(change)
                changeEntities.add(nextEntity)
            }
            if (changeParams.isNotEmpty()) {
                val actualMarkers = renderer.onChange(changeParams)
                actualMarkers.forEachIndexed { index, actualMarker ->
                    val fallbackMarker = moves[index].entity.marker
                    val updatedMarker = actualMarker ?: fallbackMarker
                    val updatedEntity =
                        MarkerEntity(
                            marker = updatedMarker,
                            state = changeEntities[index].state,
                            isRendered = true,
                        )
                    markerManager.updateEntity(updatedEntity)
                    renderedMarkerEntities[updatedEntity.state.id] = updatedEntity
                    moves[index].entity = updatedEntity
                }
                renderer.onPostProcess()
            }
            if (step < steps) {
                delay(stepMillis)
            }
        }
        return true
    }

    private fun interpolatePosition(
        start: GeoPointInterface,
        end: GeoPointInterface,
        t: Double,
    ): GeoPoint {
        val startAlt = start.altitude ?: 0.0
        val endAlt = end.altitude ?: 0.0
        return GeoPoint(
            latitude = start.latitude + (end.latitude - start.latitude) * t,
            longitude = start.longitude + (end.longitude - start.longitude) * t,
            altitude = startAlt + (endAlt - startAlt) * t,
        )
    }

    private fun averagePosition(states: List<MarkerState>): GeoPoint {
        var sumLat = 0.0
        var sumLon = 0.0
        states.forEach { state ->
            sumLat += state.position.latitude
            sumLon += state.position.longitude
        }
        val count = states.size.coerceAtLeast(1)
        return GeoPoint.fromLatLong(
            latitude = sumLat / count,
            longitude = sumLon / count,
        )
    }

    private fun averageGeoPoints(points: List<GeoPoint>): GeoPoint {
        if (points.isEmpty()) return GeoPoint.fromLatLong(0.0, 0.0)
        var sumLat = 0.0
        var sumLon = 0.0
        points.forEach { point ->
            sumLat += point.latitude
            sumLon += point.longitude
        }
        val count = points.size
        return GeoPoint.fromLatLong(
            latitude = sumLat / count,
            longitude = sumLon / count,
        )
    }

    private suspend fun cleanupStaleMarkers(
        currentZoom: Double,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
        skipClusterRemoval: Boolean,
    ) {
        val currentZoomKey = currentZoom.roundToInt()
        val staleEntities = mutableListOf<MarkerEntityInterface<ActualMarker>>()

        renderedMarkerEntities.values.forEach { entity ->
            val id = entity.state.id
            val isCluster = id.startsWith("cluster_")

            val isStale =
                if (isCluster) {
                    if (skipClusterRemoval) {
                        false
                    } else {
                        val parts = id.split("_")
                        if (parts.size >= 4) {
                            val markerZoomKey = parts[1].toIntOrNull() ?: -1
                            markerZoomKey != currentZoomKey
                        } else {
                            false
                        }
                    }
                } else {
                    !sourceStates.containsKey(id)
                }

            if (isStale) {
                staleEntities.add(entity)
            }
        }

        if (staleEntities.isNotEmpty()) {
            renderer.onRemove(staleEntities)
            staleEntities.forEach { entity ->
                renderedMarkerEntities.remove(entity.state.id)
                markerManager.removeEntity(entity.state.id)
            }
            renderer.onPostProcess()
        }
    }

    private suspend fun cleanupOrphanedMarkers(
        desiredStates: List<MarkerState>,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ) {
        val desiredIds = desiredStates.map { it.id }.toSet()
        val orphanedEntities =
            renderedMarkerEntities.values.filter { entity ->
                entity.state.id !in desiredIds
            }

        if (orphanedEntities.isNotEmpty()) {
            renderer.onRemove(orphanedEntities)
            orphanedEntities.forEach { entity ->
                renderedMarkerEntities.remove(entity.state.id)
                markerManager.removeEntity(entity.state.id)
            }
            renderer.onPostProcess()
        }
    }

    private fun buildClusterId(
        cell: ClusterCell,
        zoom: Double,
    ): String = "cluster_${zoom.roundToInt()}_${cell.x}_${cell.y}"

    private fun containsBounds(
        container: GeoRectBounds,
        target: GeoRectBounds,
    ): Boolean {
        if (container.isEmpty || target.isEmpty) return false
        val sw = target.southWest ?: return false
        val ne = target.northEast ?: return false
        return container.contains(sw) && container.contains(ne)
    }

    private fun extendCoverageBounds(
        bounds: GeoRectBounds,
        center: GeoPoint,
        radiusMeters: Double,
    ) {
        val latPad = radiusMeters / Earth.RADIUS_METERS * (180.0 / Math.PI)
        val latRad = center.latitude * DEG_TO_RAD
        val cosLat = cos(latRad).coerceAtLeast(1e-6)
        val lonPad = (radiusMeters / (Earth.RADIUS_METERS * cosLat)) * (180.0 / Math.PI)
        bounds.extend(GeoPoint(center.latitude - latPad, center.longitude - lonPad))
        bounds.extend(GeoPoint(center.latitude + latPad, center.longitude + lonPad))
    }

    private fun projectToPixel(
        position: GeoPointInterface,
        zoom: Double,
        tileSize: Double,
    ): Pair<Double, Double> {
        val scale = tileSize * 2.0.pow(zoom)
        val sinLat = sin(position.latitude * DEG_TO_RAD).coerceIn(-MAX_SIN_LAT, MAX_SIN_LAT)
        val x = (position.longitude + 180.0) / 360.0 * scale
        val y = (0.5 - ln((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * Math.PI)) * scale
        return Pair(x, y)
    }

    private fun updateClusteringTurn(zoom: Double): ZoomChange {
        val zoomKey = (zoom * 100).roundToInt()
        if (lastZoomKey == null) {
            clusteringTurn = 1
            lastZoomKey = zoomKey
            return ZoomChange(turn = clusteringTurn, zoomChanged = false)
        }
        val zoomChanged = lastZoomKey != zoomKey
        if (zoomChanged) {
            clusteringTurn += 1
            lastZoomKey = zoomKey
        }
        return ZoomChange(turn = clusteringTurn, zoomChanged = zoomChanged)
    }

    private fun hasCameraMoved(
        previous: MapCameraPosition,
        current: MapCameraPosition,
    ): Boolean {
        val distance = Spherical.computeDistanceBetween(previous.position, current.position)
        if (distance > PAN_ANIMATION_MIN_DISTANCE_METERS) return true
        if (abs(previous.bearing - current.bearing) > CAMERA_ANGLE_EPSILON) return true
        return abs(previous.tilt - current.tilt) > CAMERA_ANGLE_EPSILON
    }

    private data class ZoomChange(
        val turn: Int,
        val zoomChanged: Boolean,
    )

    private fun metersPerPixel(
        position: GeoPointInterface,
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
        val center: GeoPoint,
        val members: MutableList<MarkerState>,
    )

    private data class MergedCluster(
        val center: GeoPoint,
        val members: List<MarkerState>,
    )

    private data class AnimatedAdd(
        val state: MarkerState,
        val start: GeoPoint,
    )

    private data class AnimatedRemove<ActualMarker>(
        val entity: MarkerEntityInterface<ActualMarker>,
        val target: GeoPoint,
    )

    private data class AnimatedMove<ActualMarker>(
        val id: String,
        val start: GeoPointInterface,
        val end: GeoPointInterface,
        val baseState: MarkerState,
        var entity: MarkerEntityInterface<ActualMarker>,
    )

    private data class RenderRequest<ActualMarker>(
        val cameraPosition: MapCameraPosition,
        val viewport: GeoRectBounds,
        val renderer: MarkerOverlayRendererInterface<ActualMarker>,
        val token: Long,
    )

    private fun selectDenseCenter(
        members: List<MarkerState>,
        zoom: Double,
    ): GeoPoint {
        if (members.isEmpty()) {
            return GeoPoint.fromLatLong(0.0, 0.0)
        }
        if (members.size == 1) {
            return GeoPoint.from(members[0].position)
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

        return GeoPoint.from(bestPoint.member.position)
    }

    private fun calculateClusterRadiusMeters(
        center: GeoPoint,
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
        const val DEFAULT_ZOOM_ANIMATION_DURATION_MILLIS: Long = 200L
        private const val DEFAULT_ANIMATION_FRAME_MILLIS: Long = 16L
        const val DEFAULT_CAMERA_DEBOUNCE_MILLIS: Long = 100L
        private const val MAX_DENSE_CELLS: Int = 4
        private const val MAX_DENSE_CANDIDATES: Int = 50
        private const val PAN_ANIMATION_MIN_DISTANCE_METERS: Double = 1.0
        private const val CAMERA_ANGLE_EPSILON: Double = 1e-2
        val DEFAULT_ICON_PROVIDER: (Int) -> MarkerIconInterface =
            { count -> ColorDefaultIcon(label = count.toString()) }
        private const val DEG_TO_RAD: Double = Math.PI / 180.0
        private const val MAX_SIN_LAT: Double = 0.9999
    }
}
