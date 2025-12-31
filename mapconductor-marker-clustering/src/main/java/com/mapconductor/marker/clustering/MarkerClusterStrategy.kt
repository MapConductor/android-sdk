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
import com.mapconductor.core.spherical.expandBounds
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class MarkerClusterStrategy<ActualMarker>(
    private val clusterRadiusPx: Double = DEFAULT_CLUSTER_RADIUS_PX,
    private val minClusterSize: Int = DEFAULT_MIN_CLUSTER_SIZE,
    private val expandMargin: Double = DEFAULT_EXPAND_MARGIN,
    private val clusterIconProvider: (Int) -> MarkerIcon = DEFAULT_ICON_PROVIDER,
    private val onClusterClick: ((MarkerCluster) -> Unit)? = null,
    private val tileSize: Double = DEFAULT_TILE_SIZE,
    semaphore: Semaphore = Semaphore(1),
    geocell: HexGeocell = HexGeocellImpl.defaultGeocell(),
) : AbstractMarkerRenderingStrategy<ActualMarker>(semaphore) {
    override val markerManager: MarkerManager<ActualMarker> = MarkerManager(geocell)
    private val sourceStates = mutableMapOf<String, MarkerState>()
    private var lastCameraPosition: MapCameraPositionImpl? = null

    override fun clear() {
        sourceStates.clear()
        markerManager.clear()
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
        lastCameraPosition = cameraPosition
        val viewport = cameraPosition.visibleRegion?.bounds ?: return
        renderClusters(cameraPosition, viewport, renderer)
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
            val expandedBounds = expandBounds(viewport, expandMargin)
            val zoom = cameraPosition.zoom
            val clustered = mutableMapOf<ClusterCell, MutableList<MarkerState>>()

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

            val desiredStates = mutableListOf<MarkerState>()
            clustered.forEach { (cell, members) ->
                if (members.size >= minClusterSize) {
                    val center = averagePosition(members)
                    val cluster =
                        MarkerCluster(
                            count = members.size,
                            markerIds = members.map { it.id },
                        )
                    val clusterState =
                        MarkerState(
                            id = buildClusterId(cell, zoom),
                            position = center,
                            extra = cluster,
                            icon = clusterIconProvider(members.size),
                            clickable = onClusterClick != null,
                            draggable = false,
                            onClick =
                                if (onClusterClick != null) {
                                    { onClusterClick.invoke(cluster) }
                                } else {
                                    null
                                },
                        )
                    desiredStates.add(clusterState)
                } else {
                    desiredStates.addAll(members)
                }
            }

            updateRenderedMarkers(desiredStates, renderer)
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
                markerManager.removeEntity(id)
            }
        if (removedEntities.isNotEmpty()) {
            renderer.onRemove(removedEntities)
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
    ): String = "cluster_${zoom.roundToInt()}_${cell.x}_${cell.y}"

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

    private data class ClusterCell(
        val x: Int,
        val y: Int,
    )

    companion object {
        const val DEFAULT_CLUSTER_RADIUS_PX: Double = 60.0
        const val DEFAULT_MIN_CLUSTER_SIZE: Int = 2
        const val DEFAULT_EXPAND_MARGIN: Double = 0.2
        const val DEFAULT_TILE_SIZE: Double = 256.0
        val DEFAULT_ICON_PROVIDER: (Int) -> MarkerIcon =
            { count -> ColorDefaultIcon(label = count.toString()) }
        private const val DEG_TO_RAD: Double = Math.PI / 180.0
        private const val MAX_SIN_LAT: Double = 0.9999
    }
}
