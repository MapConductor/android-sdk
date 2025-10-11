package com.mapconductor.marker.nativestrategy.spatial

import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.marker.AbstractMarkerRenderingStrategy
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerEntityImpl
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.spherical.expandBounds
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * Native-backed remote spatial marker rendering strategy.
 * Binds to a remote Service running in a separate process that hosts
 * the C++ spatial engine. Falls back to in-process native engine if
 * binding is unavailable.
 */
class NativeRemoteSpatialMarkerStrategy<ActualMarker>(
    private val context: Context,
    private val expandMargin: Double = 0.3,
    private val addOnlyMode: Boolean = false,
    semaphore: Semaphore = Semaphore(1),
) : AbstractMarkerRenderingStrategy<ActualMarker>(semaphore) {
    companion object {
        private const val TAG = "RemoteSpatialNative"
        private const val BATCH_DELAY_MS = 50L
        private const val MAX_BATCH_SIZE = 500
    }

    private val sessionId = UUID.randomUUID().toString()
    private var nativeStrategy: NativeRemoteSpatialEngine? = null // local fallback
    private var remoteService: INativeSpatialMarkerService? = null
    private val isServiceConnected = AtomicBoolean(false)
    private val serviceConnectionLock = Object()

    override val markerManager: MarkerManager<ActualMarker> = MarkerManager.defaultManager()

    private val pendingUpdates = ConcurrentLinkedQueue<NativeMarkerDataDTO>()
    private val batchScope = CoroutineScope(Dispatchers.IO)
    private var batchJob: Job? = null
    private val renderingMutex = kotlinx.coroutines.sync.Mutex()

    private val serviceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?,
            ) {
                try {
                    remoteService = INativeSpatialMarkerService.Stub.asInterface(service)
                    val ok =
                        remoteService
                            ?.initializeSession(
                                sessionId,
                                NativeSpatialConfigDTO(expandMargin, addOnlyMode),
                            ) == true
                    isServiceConnected.set(ok)
                } catch (e: Exception) {
                    Log.e(TAG, "Remote service cast/init failed", e)
                    remoteService = null
                    isServiceConnected.set(false)
                }
                synchronized(serviceConnectionLock) { serviceConnectionLock.notifyAll() }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                remoteService = null
                isServiceConnected.set(false)
            }
        }

    init {
        // Try remote first, fallback to local native engine
        connectToService()
        startBatchProcessor()
    }

    private fun connectToService() {
        try {
            val intent = Intent(context, NativeSpatialMarkerService::class.java)
            val ok =
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (!ok) initializeNativeStrategy()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind NativeSpatialMarkerService, falling back", e)
            initializeNativeStrategy()
        }
    }

    private fun initializeNativeStrategy() {
        try {
            nativeStrategy = NativeRemoteSpatialEngine.create(expandMargin, addOnlyMode)
            if (nativeStrategy != null) {
                val initialized = nativeStrategy!!.initializeSession(NativeSpatialConfigDTO(expandMargin, addOnlyMode))
                if (initialized) {
                    isServiceConnected.set(true)
                    Log.d(TAG, "Native strategy initialized: $sessionId")
                } else {
                    Log.e(TAG, "Failed to initialize native strategy session")
                    nativeStrategy = null
                }
            } else {
                Log.e(TAG, "Failed to create native strategy")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during native strategy initialization", e)
            nativeStrategy = null
        }
    }

    private fun startBatchProcessor() {
        batchJob =
            batchScope.launch {
                while (true) {
                    delay(BATCH_DELAY_MS)
                    renderingMutex.withLock {
                        processPendingUpdates()
                    }
                }
            }
    }

    private fun processPendingUpdates() {
        if (!isServiceConnected.get()) return
        val batch = mutableListOf<NativeMarkerDataDTO>()
        repeat(MAX_BATCH_SIZE) {
            val update = pendingUpdates.poll() ?: return@repeat
            batch.add(update)
        }
        if (batch.isNotEmpty()) {
            try {
                if (remoteService != null) {
                    batch.forEach { remoteService?.updateMarker(sessionId, it) }
                } else if (nativeStrategy != null) {
                    batch.forEach { nativeStrategy?.updateMarker(it) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process batch update", e)
                batch.forEach { pendingUpdates.offer(it) }
            }
        }
    }

    private fun addToBatch(markerDTO: NativeMarkerDataDTO) {
        pendingUpdates.offer(markerDTO)
        if (pendingUpdates.size >= MAX_BATCH_SIZE) {
            batchScope.launch {
                renderingMutex.withLock {
                    processPendingUpdates()
                }
            }
        }
    }

    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPositionImpl,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        val visibleRegion = cameraPosition.visibleRegion ?: return
        semaphore.withPermit {
            renderingMutex.withLock {
                try {
                    val result: NativeSpatialResultDTO? =
                        when {
                            remoteService != null && isServiceConnected.get() -> {
                                try {
                                    val dto =
                                        NativeCameraPositionDTO(
                                            latitude = cameraPosition.position.latitude,
                                            longitude = cameraPosition.position.longitude,
                                            zoom = cameraPosition.zoom,
                                            bearing = cameraPosition.bearing,
                                            tilt = cameraPosition.tilt,
                                            boundsMinLat = visibleRegion.bounds.southWest!!.latitude,
                                            boundsMaxLat = visibleRegion.bounds.northEast!!.latitude,
                                            boundsMinLng = visibleRegion.bounds.southWest!!.longitude,
                                            boundsMaxLng = visibleRegion.bounds.northEast!!.longitude,
                                        )
                                    remoteService?.processCameraChange(sessionId, dto)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Remote processCameraChange failed", e)
                                    null
                                }
                            }
                            nativeStrategy != null && isServiceConnected.get() -> {
                                val nativeCameraPosition =
                                    CameraPosition(
                                        latitude = cameraPosition.position.latitude,
                                        longitude = cameraPosition.position.longitude,
                                        zoom = cameraPosition.zoom,
                                        bearing = cameraPosition.bearing,
                                        tilt = cameraPosition.tilt,
                                        visibleBounds =
                                            NativeGeoRectBounds(
                                                south = visibleRegion.bounds.southWest!!.latitude,
                                                north = visibleRegion.bounds.northEast!!.latitude,
                                                west = visibleRegion.bounds.southWest!!.longitude,
                                                east = visibleRegion.bounds.northEast!!.longitude,
                                            ),
                                    )
                                nativeStrategy!!.processCameraChange(nativeCameraPosition)
                            }
                            else -> {
                                val expandedBounds = expandBounds(visibleRegion.bounds, expandMargin)
                                val markersInBounds = markerManager.findMarkersInBounds(expandedBounds)
                                val markerIdsInBounds = markersInBounds.map { it.state.id }.toSet()
                                val markersToAdd = mutableListOf<String>()
                                val markersToRemove = mutableListOf<String>()
                                val currentlyRendered =
                                    markerManager
                                        .allEntities()
                                        .filter { it.isRendered }
                                        .map { it.state.id }
                                        .toSet()
                                markerIdsInBounds.forEach { id ->
                                    if (!currentlyRendered.contains(id)) markersToAdd.add(id)
                                }
                                if (!addOnlyMode) {
                                    currentlyRendered.forEach { id ->
                                        if (!markerIdsInBounds.contains(id)) markersToRemove.add(id)
                                    }
                                }
                                NativeSpatialResultDTO(markersToAdd.toTypedArray(), markersToRemove.toTypedArray())
                            }
                        }
                    result?.let { processRenderingChanges(it, renderer) }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to process camera change", e)
                }
            }
        }
    }

    private suspend fun processRenderingChanges(
        result: NativeSpatialResultDTO,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        val markersToRemove = mutableListOf<MarkerEntity<ActualMarker>>()
        val markersToAdd = mutableListOf<MarkerOverlayRenderer.AddParams>()
        val entitiesToUpdate = mutableListOf<Pair<MarkerEntity<ActualMarker>, ActualMarker?>>()

        result.markersToRemove.forEach { markerId ->
            markerManager.getEntity(markerId)?.let { entity ->
                if (entity.isRendered) {
                    markersToRemove.add(entity)
                    entitiesToUpdate.add(entity to null)
                }
            }
        }
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

        if (markersToRemove.isNotEmpty()) {
            renderer.onRemove(markersToRemove)
        }

        if (markersToAdd.isNotEmpty()) {
            val actualMarkers = renderer.onAdd(markersToAdd)
            actualMarkers.forEachIndexed { index, actualMarker ->
                if (actualMarker != null) {
                    val entity = markerManager.getEntity(markersToAdd[index].state.id)
                    entity?.let { e ->
                        entitiesToUpdate.add(e to actualMarker)
                    }
                }
            }
        }

        entitiesToUpdate.forEach { (entity, actualMarker) ->
            if (actualMarker != null) {
                entity.marker = actualMarker
                entity.isRendered = true
            } else {
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
                val markersToRender = mutableListOf<MarkerOverlayRenderer.AddParams>()
                val markersToRegister = mutableListOf<MarkerEntityImpl<ActualMarker>>()
                if (isServiceConnected.get()) {
                    val nativeMarkers =
                        data.map { state ->
                            NativeMarkerDataDTO(
                                id = state.id,
                                latitude = state.position.latitude,
                                longitude = state.position.longitude,
                                clickable = state.clickable,
                            )
                        }
                    try {
                        if (remoteService != null) {
                            remoteService?.addMarkers(sessionId, nativeMarkers)
                        } else if (nativeStrategy != null) {
                            nativeStrategy!!.addMarkers(nativeMarkers)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to add markers to backend", e)
                    }
                }
                val chunks = data.chunked(100)
                chunks.forEach { chunk ->
                    chunk.forEach { state ->
                        val isInViewport = viewport.contains(state.position)
                        if (isInViewport) {
                            markersToRender.add(
                                object : MarkerOverlayRenderer.AddParams {
                                    override val state = state
                                    override val bitmapIcon = state.icon?.toBitmapIcon() ?: defaultIcon
                                },
                            )
                        } else {
                            val entity =
                                MarkerEntityImpl<ActualMarker>(state = state, marker = null, isRendered = false)
                            markersToRegister.add(entity)
                        }
                    }
                    yield()
                }
                markersToRegister.forEach { entity -> markerManager.registerEntity(entity) }

                semaphore.withPermit {
                    renderingMutex.withLock {
                        withContext(Dispatchers.Main) {
                            if (markersToRender.isNotEmpty()) {
                                val renderChunks = markersToRender.chunked(50)
                                renderChunks.forEach { renderChunk ->
                                    val actualMarkers = renderer.onAdd(renderChunk)
                                    actualMarkers.forEachIndexed { index, actualMarker ->
                                        actualMarker?.let {
                                            val entity =
                                                MarkerEntityImpl<ActualMarker>(
                                                    state = renderChunk[index].state,
                                                    marker = actualMarker,
                                                    isRendered = true,
                                                    visible = true,
                                                )
                                            markerManager.registerEntity(entity)
                                        }
                                    }
                                    if (renderChunks.size > 1) delay(1)
                                }
                            }
                            renderer.onPostProcess()
                        }
                    }
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add markers", e)
                false
            }
        }

    override suspend fun onUpdate(
        state: MarkerState,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ): Boolean {
        return try {
            semaphore.withPermit {
                renderingMutex.withLock {
                    val entity = markerManager.getEntity(state.id) ?: return@withLock false
                    val isInViewport = viewport.contains(state.position)
                    val wasRendered = entity.isRendered

                    if (isInViewport && !wasRendered) {
                        val addParams =
                            object : MarkerOverlayRenderer.AddParams {
                                override val state = state
                                override val bitmapIcon = state.icon?.toBitmapIcon() ?: defaultIcon
                            }
                        val actualMarkers = renderer.onAdd(listOf(addParams))
                        actualMarkers.firstOrNull()?.let { actualMarker ->
                            entity.marker = actualMarker
                            entity.isRendered = true
                        }
                        renderer.onPostProcess()
                    } else if (!isInViewport && wasRendered && !addOnlyMode) {
                        renderer.onRemove(listOf(entity))
                        entity.marker = null
                        entity.isRendered = false
                        renderer.onPostProcess()
                    } else if (isInViewport && wasRendered) {
                        val changeParams =
                            object : MarkerOverlayRenderer.ChangeParams<ActualMarker> {
                                override val current =
                                    MarkerEntityImpl(state = state, marker = entity.marker, isRendered = true)
                                override val prev = entity
                                override val bitmapIcon = state.icon?.toBitmapIcon() ?: defaultIcon
                            }
                        val actualMarkers = renderer.onChange(listOf(changeParams))
                        actualMarkers.firstOrNull()?.let { actualMarker ->
                            entity.marker = actualMarker
                        }
                        renderer.onPostProcess()
                    }

                    val latitude = state.position.latitude
                    val longitude = state.position.longitude

                    val dto =
                        NativeMarkerDataDTO(
                            id = state.id,
                            latitude = latitude,
                            longitude = longitude,
                            clickable = state.clickable,
                        )
                    addToBatch(dto)
                    return@withLock true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update marker ${state.id}", e)
            false
        }
    }

    suspend fun findNearestMarker(
        latitude: Double,
        longitude: Double,
    ): MarkerEntity<ActualMarker>? =
        try {
            renderingMutex.withLock {
                if (isServiceConnected.get()) {
                    if (remoteService != null) {
                        val id = remoteService?.findNearestMarker(sessionId, latitude, longitude)
                        id?.let { markerManager.getEntity(it) }
                    } else if (nativeStrategy != null) {
                        val nearestId = nativeStrategy!!.findNearestMarker(latitude, longitude)
                        nearestId?.let { markerManager.getEntity(it) }
                    } else {
                        null
                    }
                } else {
                    markerManager.findNearest(GeoPointImpl.fromLatLong(latitude, longitude))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to find nearest marker", e)
            null
        }

    fun getPerformanceStats(): String? =
        try {
            if (isServiceConnected.get()) {
                if (remoteService != null) {
                    remoteService?.getPerformanceStats(sessionId)
                } else if (nativeStrategy != null) {
                    nativeStrategy!!.getPerformanceStats()?.toString() ?: "Native performance stats not available"
                } else {
                    "Backend not connected"
                }
            } else {
                "Native strategy not connected - using local fallback"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get performance stats", e)
            null
        }

    fun getMarkerCount(): Long =
        try {
            nativeStrategy?.getMarkerCount() ?: markerManager.allEntities().size.toLong()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get marker count", e)
            0L
        }

    fun getRenderedMarkerCount(): Long =
        try {
            nativeStrategy?.getRenderedMarkerCount() ?: markerManager.allEntities().count { it.isRendered }.toLong()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get rendered marker count", e)
            0L
        }

    fun destroy() {
        try {
            isServiceConnected.set(false)
            batchJob?.cancel()

            batchScope.launch {
                renderingMutex.withLock {
                    processPendingUpdates()
                }
            }

            try {
                remoteService?.destroySession(sessionId)
            } catch (_: Exception) {
            }
            try {
                context.unbindService(serviceConnection)
            } catch (_: Exception) {
            }
            nativeStrategy?.destroy()
            nativeStrategy = null
            Log.d(TAG, "NativeRemoteSpatialMarkerStrategy destroyed: $sessionId")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}
