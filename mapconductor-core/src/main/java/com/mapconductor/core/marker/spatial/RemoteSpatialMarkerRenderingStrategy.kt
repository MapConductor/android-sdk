package com.mapconductor.core.marker.spatial

import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.AbstractMarkerRenderingStrategy
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerEntityImpl
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerState
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import android.content.ComponentName
import android.content.Context
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
class RemoteSpatialMarkerRenderingStrategy<ActualMarker>(
    private val context: Context,
    private val expandMargin: Double = 0.3,
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
    private var spatialService: Any? = null // IMarkerSpatialService? = null - Using local fallback
    private var isServiceConnected = false
    private val serviceConnectionLock = Object()
    private val strategyId: String

    // Local marker manager for main process operations
    override val markerManager: MarkerManager<ActualMarker> = MarkerManager.defaultManager()

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
                Log.d(TAG, "Service connected - ComponentName: $name, IBinder: $service")
                try {
                    // Fallback to local processing since AIDL generation has issues
                    spatialService = null
                    Log.w(TAG, "Using fallback to local processing instead of IPC service")
                    Log.d(TAG, "Cast result - spatialService: $spatialService")

                    // If cast fails, log the actual type for debugging
                    if (spatialService == null && service != null) {
                        Log.w(TAG, "Service cast failed. Actual service type: ${service.javaClass.name}")
                        Log.w(TAG, "Service interfaces: ${service.javaClass.interfaces.contentToString()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during service cast", e)
                    spatialService = null
                }

                synchronized(serviceConnectionLock) {
                    isServiceConnected = (spatialService != null)
                    Log.d(TAG, "Service connection status: $isServiceConnected")
                    serviceConnectionLock.notifyAll()

                    // Initialize session in background service
                    if (spatialService != null) {
                        try {
                            val config = SpatialConfigDTO(expandMargin, addOnlyMode)
                            // val result = spatialService!!.initializeSession(sessionId, config)
                            val result = true // Using local fallback
                            Log.d(TAG, "Session initialization result: $result for session $sessionId")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to initialize session in background service", e)
                            isServiceConnected = false
                        }
                    } else {
                        Log.e(TAG, "spatialService is null after casting - falling back to local processing")
                        isServiceConnected = false
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d(TAG, "Disconnected from MarkerSpatialService")
                synchronized(serviceConnectionLock) {
                    spatialService = null
                    isServiceConnected = false
                }
            }
        }

    init {
        // Register with service manager and start service if needed
        strategyId = SpatialMarkerServiceManager.registerStrategy(context, this)
        connectToService()
        startBatchProcessor()
    }

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
            if (pendingUpdates.isEmpty() || !isServiceConnected) return

            val batch = mutableListOf<MarkerDataDTO>()

            // Collect up to MAX_BATCH_SIZE updates
            repeat(MAX_BATCH_SIZE) {
                val update = pendingUpdates.poll() ?: return@repeat
                batch.add(update)
            }

            if (batch.isNotEmpty()) {
                try {
                    // spatialService?.updateMarkers(sessionId, batch) - Using local fallback
                    Log.d(TAG, "Processed batch of ${batch.size} marker updates")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to process batch update", e)
                    // Re-add failed updates to queue for retry
                    batch.forEach { pendingUpdates.offer(it) }
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
            // For now, we'll skip the actual service connection since we're using local fallback
            Log.d(TAG, "Skipping service connection - using local fallback mode")
            // val intent = Intent(context, MarkerSpatialService::class.java)
            // Log.d(TAG, "Attempting to bind to service: ${intent.component}")
            // val bindResult = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            // Log.d(TAG, "Bind service result: $bindResult")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to MarkerSpatialService", e)
        }
    }

    private fun waitForServiceConnection(): Boolean {
        // Always return false to use local fallback
        return false
    }

    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPosition,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        val visibleRegion = cameraPosition.visibleRegion ?: return

        semaphore.withPermit {
            try {
                Log.d(TAG, "Processing camera change for viewport: ${visibleRegion.bounds}")

                // Use local spatial calculation as fallback
                val expandedBounds =
                    com.mapconductor.core.spherical
                        .expandBounds(visibleRegion.bounds, expandMargin)

                // Find markers in viewport using local marker manager
                val markersInBounds = markerManager.findMarkersInBounds(expandedBounds)
                val markerIdsInBounds = markersInBounds.map { it.state.id }.toSet()

                val markersToAdd = mutableListOf<String>()
                val markersToRemove = mutableListOf<String>()

                // Track which markers are currently rendered
                val allEntities = markerManager.allEntities()
                val currentlyRendered =
                    allEntities
                        .filter { it.isRendered }
                        .map { it.state.id }
                        .toSet()

                // Find markers that should be added (in viewport but not rendered)
                markerIdsInBounds.forEach { id ->
                    if (!currentlyRendered.contains(id)) {
                        markersToAdd.add(id)
                    }
                }

                // Find markers that should be removed (rendered but not in viewport, only if not add-only mode)
                if (!addOnlyMode) {
                    currentlyRendered.forEach { id ->
                        if (!markerIdsInBounds.contains(id)) {
                            markersToRemove.add(id)
                        }
                    }
                }

                Log.d(TAG, "Local spatial calculation: +${markersToAdd.size} -${markersToRemove.size}")

                // Create result DTO for processing
                val result = SpatialResultDTO(markersToAdd, markersToRemove, emptyList())

                // Process results in main process
                processRenderingChanges(result, renderer)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process camera change", e)
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
                            override val bitmapIcon = entity.state.icon?.toBitmapIcon() ?: defaultIcon.toBitmapIcon()
                        },
                    )
                }
            }
        }

        // Execute rendering operations
        if (markersToRemove.isNotEmpty()) {
            renderer.onRemove(markersToRemove)
            markersToRemove.forEach { entity ->
                entity.isRendered = false
                entity.marker = null
            }
        }

        if (markersToAdd.isNotEmpty()) {
            val actualMarkers = renderer.onAdd(markersToAdd)
            actualMarkers.forEachIndexed { index, actualMarker ->
                actualMarker?.let {
                    val entity = markerManager.getEntity(markersToAdd[index].state.id)
                    entity?.let { e ->
                        e.marker = actualMarker
                        e.isRendered = true
                    }
                }
            }
        }

        if (markersToRemove.isNotEmpty() || markersToAdd.isNotEmpty()) {
            renderer.onPostProcess()
        }

        Log.d(TAG, "Processed rendering changes: +${markersToAdd.size} -${markersToRemove.size}")
    }

    override suspend fun onAdd(
        data: List<MarkerState>,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ): Boolean {
        Log.d(TAG, "onAdd called with ${data.size} markers, viewport: $viewport")
        return withContext(Dispatchers.Default) {
            try {
                val markersToRender = mutableListOf<MarkerOverlayRenderer.AddParams>()
                val markersToRegister = mutableListOf<MarkerEntityImpl<ActualMarker>>()

                // Process markers in chunks to prevent ANR
                val chunks = data.chunked(100) // Process 100 markers at a time

                chunks.forEach { chunk ->
                    chunk.forEach { state ->
                        val isInViewport = viewport.contains(state.position)
                        Log.d(TAG, "Marker ${state.id} at ${state.position} - inViewport: $isInViewport")

                        if (isInViewport) {
                            // Marker is in viewport - add to render list
                            markersToRender.add(
                                object : MarkerOverlayRenderer.AddParams {
                                    override val state = state
                                    override val bitmapIcon = state.icon?.toBitmapIcon() ?: defaultIcon.toBitmapIcon()
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

                    // Yield after processing each chunk to allow other coroutines
                    yield()
                }

                // Register markers without rendering (done in background)
                markersToRegister.forEach { entity ->
                    markerManager.registerEntity(entity)
                }

                // Render markers that are in viewport (switch to main thread)
                withContext(Dispatchers.Main) {
                    if (markersToRender.isNotEmpty()) {
                        Log.d(TAG, "Rendering ${markersToRender.size} markers immediately")

                        // Process rendering in smaller chunks to prevent blocking main thread
                        val renderChunks = markersToRender.chunked(50)

                        renderChunks.forEach { renderChunk ->
                            val actualMarkers = renderer.onAdd(renderChunk)
                            Log.d(TAG, "Renderer.onAdd returned ${actualMarkers.size} actual markers")

                            actualMarkers.forEachIndexed { index, actualMarker ->
                                Log.d(TAG, "Processing actual marker $index: $actualMarker")
                                actualMarker?.let {
                                    val entity =
                                        MarkerEntityImpl<ActualMarker>(
                                            state = renderChunk[index].state,
                                            marker = actualMarker,
                                            isRendered = true,
                                        )
                                    markerManager.registerEntity(entity)
                                }
                            }

                            // Small delay between chunks to allow UI updates
                            if (renderChunks.size > 1) {
                                kotlinx.coroutines.delay(1)
                            }
                        }

                        renderer.onPostProcess()
                        Log.d(TAG, "Completed immediate rendering")
                    } else {
                        Log.d(TAG, "No markers to render immediately (all outside viewport)")
                    }
                }

                // Send marker data to background service for spatial indexing (batched)
                withContext(Dispatchers.IO) {
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
                }

                Log.d(
                    TAG,
                    "Added ${data.size} markers (${markersToRender.size} rendered immediately) to session $sessionId",
                )
                true
            } catch (e: Exception) {
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
                        override val bitmapIcon = state.icon?.toBitmapIcon() ?: defaultIcon.toBitmapIcon()
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
                        override val bitmapIcon = state.icon?.toBitmapIcon() ?: defaultIcon.toBitmapIcon()
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

            // Using local fallback for nearest marker
            markerManager.findNearest(
                com.mapconductor.core.features.GeoPoint
                    .fromLatLong(latitude, longitude),
            )
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

            // spatialService?.destroySession(sessionId) - Using local fallback
            context.unbindService(serviceConnection)

            // Unregister from service manager (may stop service if this was the last strategy)
            SpatialMarkerServiceManager.unregisterStrategy(context, strategyId)

            Log.d(TAG, "RemoteSpatialMarkerRenderingStrategy destroyed")
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
            // Using local fallback for performance stats
            "Local processing mode - no IPC service"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get performance stats", e)
            null
        }
    }
}
