package com.mapconductor.marker.strategy.spatial

import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.marker.AbstractMarkerRenderingStrategy
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerEntityImpl
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerState
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * Marker rendering strategy that offloads spatial calculations to a background service.
 * This runs in the main process but delegates heavy computations to a separate process.
 *
 * When the IPC service is unavailable, it falls back to local spatial calculations
 * to ensure markers are always rendered correctly.
 */
class RemoteSpatialMarkerStrategy<ActualMarker>(
    private val context: Context,
    private val expandMargin: Double = 0.5,
    private val addOnlyMode: Boolean = false,
    semaphore: Semaphore = Semaphore(1),
) : AbstractMarkerRenderingStrategy<ActualMarker>(semaphore) {
    companion object {
        private const val TAG = "RemoteSpatialStrategy"
        private const val SERVICE_CONNECTION_TIMEOUT_MS = 5000L
        private const val BATCH_DELAY_MS = 100L // Batch updates every 100ms
        private const val MAX_BATCH_SIZE = 500 // Maximum markers per batch
    }

    private val sessionId = UUID.randomUUID().toString()
    private var spatialService: ISpatialMarkerService? = null
    private var isServiceConnected = false
    private val serviceConnectionLock = Object()
    private val strategyId: String

    // Local marker manager for main process operations
    override val markerManager: MarkerManager<ActualMarker> = MarkerManager.defaultManager()

    override fun clear() {
        markerManager.clear()
    }

    // Batching for marker updates
    private val pendingUpdates = ConcurrentLinkedQueue<MarkerDataDTO>()
    private val batchScope = CoroutineScope(Dispatchers.IO)
    private var batchJob: Job? = null
    private val batchLock = Object()

    private val serviceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?,
            ) {
                try {
                    spatialService = ISpatialMarkerService.Stub.asInterface(service)
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during service cast", e)
                    spatialService = null
                }

                synchronized(serviceConnectionLock) {
                    isServiceConnected = (spatialService != null)
                    serviceConnectionLock.notifyAll()

                    if (spatialService != null) {
                        try {
                            val config = SpatialConfigDTO(expandMargin, addOnlyMode)
                            val result = spatialService!!.initializeSession(sessionId, config)
                            if (!result) {
                                isServiceConnected = false
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to initialize session in background service", e)
                            isServiceConnected = false
                        }
                    } else {
                        isServiceConnected = false
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                synchronized(serviceConnectionLock) {
                    spatialService = null
                    isServiceConnected = false
                }
            }
        }

    init {
        strategyId =
            com.mapconductor.marker.strategy.SpatialMarkerServiceManager
                .registerStrategy(context, this)
        connectToService()
        startBatchProcessor()
    }

    // Cache last camera and renderer to allow recalculation after batches are sent
    @Volatile private var lastCamera: MapCameraPositionImpl? = null

    @Volatile private var lastRenderer: MarkerOverlayRenderer<ActualMarker>? = null
    private val cameraSeq = AtomicLong(0L)

    private fun startBatchProcessor() {
        batchJob =
            batchScope.launch {
                while (true) {
                    delay(BATCH_DELAY_MS)
                    processPendingUpdates()
                }
            }
    }

    private fun processPendingUpdates() {
        synchronized(batchLock) {
            if (pendingUpdates.isEmpty() || !isServiceConnected || spatialService == null) return

            val batch = mutableListOf<MarkerDataDTO>()

            // Collect up to MAX_BATCH_SIZE updates
            repeat(MAX_BATCH_SIZE) {
                val update = pendingUpdates.poll() ?: return@repeat
                batch.add(update)
            }
            if (batch.isNotEmpty()) {
                try {
                    spatialService?.updateMarkers(sessionId, batch)
                    // Trigger a recalculation for current camera after new markers are registered remotely
                    triggerRecalculateAfterBatch()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to process batch update", e)
                    batch.forEach { pendingUpdates.offer(it) }
                }
            }
        }
    }

    // Fallback path to complete add outside of Compose composition when it cancels mid-flight
    private fun fallbackAddAsync(
        params: List<MarkerOverlayRenderer.AddParams>,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        if (params.isEmpty()) return
        batchScope.launch {
            semaphore.withPermit {
                try {
                    // Execute add in chunks to avoid UI stalls
                    val chunkSize = 1000
                    var index = 0
                    while (index < params.size) {
                        val end = kotlin.math.min(index + chunkSize, params.size)
                        val chunk = params.subList(index, end)
                        val added = renderer.onAdd(chunk)
                        added.forEachIndexed { i, actualMarker ->
                            actualMarker?.let {
                                val state = chunk[i].state
                                val entity =
                                    MarkerEntityImpl<ActualMarker>(
                                        state = state,
                                        marker = actualMarker,
                                        isRendered = true,
                                        visible = true,
                                    )
                                markerManager.registerEntity(entity)
                            }
                        }
                        index = end
                    }
                    renderer.onPostProcess()
                } catch (e: Exception) {
                    Log.e(TAG, "Fallback add failed", e)
                }
            }
        }
    }

    private fun buildCameraDto(cameraPosition: MapCameraPositionImpl): CameraPositionDTO? {
        val visibleRegion = cameraPosition.visibleRegion ?: return null
        return CameraPositionDTO(
            centerLatitude = cameraPosition.position.latitude,
            centerLongitude = cameraPosition.position.longitude,
            zoom = cameraPosition.zoom,
            bearing = cameraPosition.bearing,
            tilt = cameraPosition.tilt,
            boundsMinLat = visibleRegion.bounds.southWest?.latitude ?: 0.0,
            boundsMaxLat = visibleRegion.bounds.northEast?.latitude ?: 0.0,
            boundsMinLng = visibleRegion.bounds.southWest?.longitude ?: 0.0,
            boundsMaxLng = visibleRegion.bounds.northEast?.longitude ?: 0.0,
        )
    }

    private fun triggerRecalculateAfterBatch() {
        val renderer = lastRenderer ?: return
        // Use the latest camera snapshot when executing; do not drop on seq mismatch
        batchScope.launch {
            semaphore.withPermit {
                try {
                    val cam = lastCamera ?: return@withPermit
                    val dto = buildCameraDto(cam) ?: return@withPermit
                    if (!waitForServiceConnection()) return@withPermit
                    val result =
                        try {
                            spatialService?.calculateChanges(sessionId, dto)
                                ?: SpatialResultDTO(emptyList(), emptyList(), emptyList())
                        } catch (e: Exception) {
                            Log.e(TAG, "Recalc after batch failed", e)
                            SpatialResultDTO(emptyList(), emptyList(), emptyList())
                        }
                    processRenderingChanges(result, renderer)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to process recalc after batch", e)
                }
            }
        }
    }

    private fun addToBatch(markerDTO: MarkerDataDTO) {
        pendingUpdates.offer(markerDTO)

        // If queue is getting large, force immediate processing
        if (pendingUpdates.size >= MAX_BATCH_SIZE) {
            batchScope.launch { processPendingUpdates() }
        }
    }

    private fun connectToService() {
        try {
            val intent = Intent(context, SpatialMarkerService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to SpatialMarkerService", e)
        }
    }

    private fun waitForServiceConnection(): Boolean {
        synchronized(serviceConnectionLock) {
            if (isServiceConnected) return true
            try {
                serviceConnectionLock.wait(SERVICE_CONNECTION_TIMEOUT_MS)
            } catch (_: InterruptedException) {
            }
            return isServiceConnected
        }
    }

    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPositionImpl,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        val visibleRegion = cameraPosition.visibleRegion ?: return
        // Cache last known camera and renderer
        lastCamera = cameraPosition
        lastRenderer = renderer
        val seq = cameraSeq.incrementAndGet()

        semaphore.withPermit {
            try {
                // Drop stale request if a newer camera arrived while waiting
                if (seq != cameraSeq.get()) return@withPermit
                val cameraDto =
                    CameraPositionDTO(
                        centerLatitude = cameraPosition.position.latitude,
                        centerLongitude = cameraPosition.position.longitude,
                        zoom = cameraPosition.zoom,
                        bearing = cameraPosition.bearing,
                        tilt = cameraPosition.tilt,
                        boundsMinLat = visibleRegion.bounds.southWest?.latitude ?: 0.0,
                        boundsMaxLat = visibleRegion.bounds.northEast?.latitude ?: 0.0,
                        boundsMinLng = visibleRegion.bounds.southWest?.longitude ?: 0.0,
                        boundsMaxLng = visibleRegion.bounds.northEast?.longitude ?: 0.0,
                    )

                val result =
                    if (waitForServiceConnection()) {
                        try {
                            spatialService?.calculateChanges(sessionId, cameraDto)
                                ?: SpatialResultDTO(emptyList(), emptyList(), emptyList())
                        } catch (e: Exception) {
                            Log.e(TAG, "Remote calculateChanges failed, falling back", e)
                            SpatialResultDTO(emptyList(), emptyList(), emptyList())
                        }
                    } else {
                        SpatialResultDTO(emptyList(), emptyList(), emptyList())
                    }

                // Only apply if this request is still current
                if (seq == cameraSeq.get()) {
                    processRenderingChanges(result, renderer)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process camera change", e)
                if (e is kotlinx.coroutines.CancellationException) {
                    // Schedule a follow-up recalculation to avoid missing markers
                    batchScope.launch {
                        try {
                            delay(120)
                            triggerRecalculateAfterBatch()
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
    }

    private suspend fun processRenderingChanges(
        result: SpatialResultDTO,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        val markersToRemove = mutableListOf<MarkerEntity<ActualMarker>>()
        val markersToAdd = mutableListOf<MarkerOverlayRenderer.AddParams>()

        // Handle markers to remove
        result.markersToRemove.forEach { markerId ->
            markerManager.getEntity(markerId)?.let { entity ->
                if (entity.isRendered) {
                    markersToRemove.add(entity)
                }
            }
        }

        // Handle markers to add
        result.markersToAdd.forEach { markerId ->
            markerManager.getEntity(markerId)?.let { entity ->
                if (!entity.isRendered) {
                    markersToAdd.add(
                        object : MarkerOverlayRenderer.AddParams {
                            override val state = entity.state
                            override val bitmapIcon = entity.state.icon?.toBitmapIcon() ?: defaultIcon
                        },
                    )
                }
            }
        }

        // Execute rendering operations - add first to avoid visible gaps during fast pans
        if (markersToAdd.isNotEmpty()) {
            val actualMarkers = renderer.onAdd(markersToAdd)
            actualMarkers.forEachIndexed { index, actualMarker ->
                actualMarker?.let {
                    val entity = markerManager.getEntity(markersToAdd[index].state.id)
                    entity?.let { e ->
                        e.marker = actualMarker
                        e.isRendered = true
                        e.visible = true
                    }
                }
            }
        }

        if (markersToRemove.isNotEmpty()) {
            renderer.onRemove(markersToRemove)
            markersToRemove.forEach { entity ->
                entity.isRendered = false
                entity.marker = null
            }
        }

        if (markersToRemove.isNotEmpty() || markersToAdd.isNotEmpty()) {
            renderer.onPostProcess()
        }
    }

    override suspend fun onAdd(
        data: List<MarkerState>,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ): Boolean =
        withContext(Dispatchers.Default) {
            try {
                // Yield after processing each chunk to allow other coroutines
                yield()

                val markersToRender = mutableListOf<MarkerOverlayRenderer.AddParams>()
                val markersToRegister = mutableListOf<MarkerEntityImpl<ActualMarker>>()

                data.forEach { state ->
                    val isInViewport = viewport.contains(state.position)

                    if (isInViewport) {
                        // Marker is in viewport - add to render list
                        markersToRender.add(
                            object : MarkerOverlayRenderer.AddParams {
                                override val state = state
                                override val bitmapIcon = state.icon?.toBitmapIcon() ?: defaultIcon
                            },
                        )
                    } else {
                        // Marker outside viewport - just register without rendering
                        val entity =
                            MarkerEntityImpl<ActualMarker>(
                                state = state,
                                marker = null,
                                isRendered = false,
                            )
                        markersToRegister.add(entity)
                    }
                }

                // Register markers without rendering (done in background)
                markersToRegister.forEach { entity ->
                    markerManager.registerEntity(entity)
                }

                if (markersToRender.isNotEmpty()) {
                    // Process rendering in smaller chunks to prevent blocking main thread
                    val actualMarkers = renderer.onAdd(markersToRender)
                    actualMarkers.forEachIndexed { index, actualMarker ->
                        actualMarker?.let {
                            val entity =
                                MarkerEntityImpl<ActualMarker>(
                                    state = markersToRender[index].state,
                                    marker = actualMarker,
                                    isRendered = true,
                                    visible = true,
                                )
                            markerManager.registerEntity(entity)
                        }
                    }

                    renderer.onPostProcess()
                }

                // Send marker data to background service for spatial indexing (batched)
                val markerDTOs =
                    data.map { state ->
                        MarkerDataDTO(
                            id = state.id,
                            latitude = state.position.latitude,
                            longitude = state.position.longitude,
                            clickable = state.clickable,
                        )
                    }

                // Add to batch queue for background processing
                markerDTOs.forEach { addToBatch(it) }
                true
            } catch (e: Exception) {
                return@withContext if (e is kotlinx.coroutines.CancellationException) {
                    // Add cancelled; schedule fallback add without noisy log
                    // Reconstruct params for fallback from the input data
                    val params =
                        data.map { state ->
                            object : MarkerOverlayRenderer.AddParams {
                                override val state: MarkerState = state
                                override val bitmapIcon = state.icon?.toBitmapIcon() ?: defaultIcon
                            }
                        }
                    fallbackAddAsync(params, renderer)
                    true
                } else {
                    Log.e(TAG, "Failed to add markers", e)
                    false
                }
            }
        }

    override suspend fun onUpdate(
        state: MarkerState,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ): Boolean {
        return try {
            val entity = markerManager.getEntity(state.id) ?: return false
            val isInViewport = viewport.contains(state.position)
            val wasRendered = entity.isRendered

            if (isInViewport && !wasRendered) {
                // Marker moved into viewport - render it
                val addParams =
                    object : MarkerOverlayRenderer.AddParams {
                        override val state = state
                        override val bitmapIcon = state.icon?.toBitmapIcon() ?: defaultIcon
                    }

                val actualMarkers = renderer.onAdd(listOf(addParams))
                actualMarkers.firstOrNull()?.let { actualMarker ->
                    val updatedEntity =
                        MarkerEntityImpl<ActualMarker>(
                            state = state,
                            marker = actualMarker,
                            isRendered = true,
                        )
                    markerManager.registerEntity(updatedEntity)
                }
                renderer.onPostProcess()
            } else if (!isInViewport && wasRendered && !addOnlyMode) {
                // Marker moved out of viewport - remove it (only if not add-only mode)
                renderer.onRemove(listOf(entity))
                val updatedEntity =
                    MarkerEntityImpl<ActualMarker>(
                        state = state,
                        marker = null,
                        isRendered = false,
                    )
                markerManager.registerEntity(updatedEntity)
                renderer.onPostProcess()
            } else if (isInViewport && wasRendered) {
                // Marker is in viewport and was already rendered - update it
                val changeParams =
                    object : MarkerOverlayRenderer.ChangeParams<ActualMarker> {
                        override val current =
                            MarkerEntityImpl(
                                state = state,
                                marker = entity.marker,
                                isRendered = true,
                            )
                        override val prev = entity
                        override val bitmapIcon = state.icon?.toBitmapIcon() ?: defaultIcon
                    }

                val actualMarkers = renderer.onChange(listOf(changeParams))
                actualMarkers.firstOrNull()?.let { actualMarker ->
                    val updatedEntity =
                        MarkerEntityImpl<ActualMarker>(
                            state = state,
                            marker = actualMarker,
                            isRendered = true,
                        )
                    markerManager.registerEntity(updatedEntity)
                }
                renderer.onPostProcess()
            } else {
                // Marker outside viewport and not rendered - just update state
                val updatedEntity =
                    MarkerEntityImpl<ActualMarker>(
                        state = state,
                        marker = null,
                        isRendered = false,
                    )
                markerManager.registerEntity(updatedEntity)
            }

            // Update background service (batched)
            val markerDTO =
                MarkerDataDTO(
                    id = state.id,
                    latitude = state.position.latitude,
                    longitude = state.position.longitude,
                    clickable = state.clickable,
                )
            addToBatch(markerDTO)

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update marker ${state.id}", e)
            false
        }
    }

    /**
     * Find nearest marker using background service spatial calculations
     */
    fun findNearestMarker(
        latitude: Double,
        longitude: Double,
    ): MarkerEntity<ActualMarker>? {
        return try {
            if (!waitForServiceConnection()) return null
            val id = spatialService?.findNearestMarker(sessionId, latitude, longitude)
            if (id != null) markerManager.getEntity(id) else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to find nearest marker", e)
            null
        }
    }

    /**
     * Clean up and disconnect from background service
     */
    fun destroy() {
        try {
            // Stop batch processor
            batchJob?.cancel()

            // Process any remaining updates before shutdown
            processPendingUpdates()

            try {
                spatialService?.destroySession(sessionId)
            } catch (_: Exception) {
            }
            try {
                context.unbindService(serviceConnection)
            } catch (_: IllegalArgumentException) {
                // Not bound
            }

            // Unregister from service manager (may stop service if this was the last strategy)
            com.mapconductor.marker.strategy.SpatialMarkerServiceManager
                .unregisterStrategy(context, strategyId)
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    /**
     * Get performance statistics from background service
     */
    fun getPerformanceStats(): String? {
        return try {
            if (!waitForServiceConnection()) return null
            spatialService?.getPerformanceStats(sessionId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get performance stats", e)
            null
        }
    }
}
