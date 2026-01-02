package com.mapconductor.heatmap

import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.geocell.HexGeocellImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.marker.AbstractMarkerRenderingStrategy
import com.mapconductor.core.marker.MarkerEntityImpl
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.spherical.expandBounds
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class HeatmapStrategy(
    private val expandMargin: Double = DEFAULT_EXPAND_MARGIN,
    private val weightProvider: (MarkerState) -> Double = DEFAULT_WEIGHT_PROVIDER,
    private val debounceMillis: Long = DEFAULT_CAMERA_DEBOUNCE_MILLIS,
    semaphore: Semaphore = Semaphore(1),
    geocell: HexGeocell = HexGeocellImpl.defaultGeocell(),
) : AbstractMarkerRenderingStrategy<Unit>(semaphore) {
    override val markerManager: MarkerManager<Unit> = MarkerManager(geocell)
    private val sourceStates = mutableMapOf<String, MarkerState>()
    private var lastCameraPosition: MapCameraPositionImpl? = null
    private val debounceScope = CoroutineScope(Dispatchers.Default)
    private val cameraUpdateToken = AtomicLong(0)
    private var debounceJob: Job? = null

    override fun clear() {
        sourceStates.clear()
        markerManager.clear()
    }

    override suspend fun onAdd(
        data: List<MarkerState>,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<Unit>,
    ): Boolean {
        val removedIds = updateSourceStates(data)
        syncMarkerEntities(data, removedIds)
        renderHeatmap(viewport, renderer, cameraUpdateToken.get())
        return true
    }

    override suspend fun onUpdate(
        state: MarkerState,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<Unit>,
    ): Boolean {
        sourceStates[state.id] = state
        val existing = markerManager.getEntity(state.id)
        val nextEntity =
            MarkerEntityImpl(
                marker = existing?.marker,
                state = state,
                isRendered = true,
            )
        if (existing == null) {
            markerManager.registerEntity(nextEntity)
        } else {
            markerManager.updateEntity(nextEntity)
        }
        renderHeatmap(viewport, renderer, cameraUpdateToken.get())
        return true
    }

    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPositionImpl,
        renderer: MarkerOverlayRenderer<Unit>,
    ) {
        lastCameraPosition = cameraPosition
        val token = cameraUpdateToken.incrementAndGet()
        debounceJob?.cancel()
        debounceJob =
            debounceScope.launch {
                delay(debounceMillis)
                if (token != cameraUpdateToken.get()) return@launch
                val viewport = lastCameraPosition?.visibleRegion?.bounds ?: return@launch
                renderHeatmap(viewport, renderer, token)
            }
    }

    private fun updateSourceStates(data: List<MarkerState>): Set<String> {
        val nextIds = data.map { it.id }.toSet()
        val removedIds = sourceStates.keys - nextIds
        removedIds.forEach { sourceStates.remove(it) }
        data.forEach { state -> sourceStates[state.id] = state }
        return removedIds
    }

    private fun syncMarkerEntities(
        data: List<MarkerState>,
        removedIds: Set<String>,
    ) {
        removedIds.forEach { id -> markerManager.removeEntity(id) }
        data.forEach { state ->
            val existing = markerManager.getEntity(state.id)
            val nextEntity =
                MarkerEntityImpl(
                    marker = existing?.marker,
                    state = state,
                    isRendered = true,
                )
            if (existing == null) {
                markerManager.registerEntity(nextEntity)
            } else {
                markerManager.updateEntity(nextEntity)
            }
        }
    }

    private suspend fun renderHeatmap(
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<Unit>,
        token: Long,
    ) {
        semaphore.withPermit {
            if (token != cameraUpdateToken.get()) return@withPermit
            currentCoroutineContext().ensureActive()
            val expandedBounds = expandBounds(viewport, expandMargin)
            val points = mutableListOf<HeatmapPoint>()

            sourceStates.values.forEach { state ->
                currentCoroutineContext().ensureActive()
                if (!expandedBounds.contains(state.position)) return@forEach
                val weight = weightProvider(state)
                if (weight.isNaN() || weight <= 0.0) return@forEach
                points.add(HeatmapPoint(position = state.position, weight = weight))
            }

            if (token != cameraUpdateToken.get()) return@withPermit
            (renderer as? HeatmapOverlayRenderer)?.updateHeatmap(points)
        }
    }

    companion object {
        const val DEFAULT_EXPAND_MARGIN: Double = 0.2
        private const val DEFAULT_CAMERA_DEBOUNCE_MILLIS: Long = 100L
        val DEFAULT_WEIGHT_PROVIDER: (MarkerState) -> Double = { state ->
            val weight = (state.extra as? Number)?.toDouble() ?: 1.0
            if (weight.isNaN()) 1.0 else weight
        }
    }
}
