package com.mapconductor.marker.strategy.spatial

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.marker.MarkerEntityImpl
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.spherical.expandBounds
import java.util.concurrent.ConcurrentHashMap
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Process
import android.util.Log
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore

/**
 * Background service that handles marker spatial calculations in a separate process.
 * This service offloads heavy spatial computations from the main process.
 *
 * This is an optional service that can be used by implementing apps when they want
 * to enable IPC-based spatial processing.
 */
open class SpatialMarkerService : Service() {
    companion object {
        private const val TAG = "SpatialMarkerService"
        private const val MAX_MARKERS_PER_SESSION = 10000 // Throttling limit
    }

    /**
     * Session data for managing multiple spatial contexts
     */
    private data class SpatialSession(
        val config: SpatialConfigDTO,
        val markerManager: MarkerManager<String>, // Using String as marker type since we only track IDs
        val markerData: MutableMap<String, MarkerDataDTO> = ConcurrentHashMap(),
        val renderedMarkers: MutableSet<String> = ConcurrentHashMap.newKeySet(),
        val semaphore: Semaphore = Semaphore(1),
    )

    private val sessions = ConcurrentHashMap<String, SpatialSession>()

    private val binder =
        object : Binder() { // Temporarily disabled: ISpatialMarkerService.Stub() {

            fun initializeSession(
                sessionId: String,
                config: SpatialConfigDTO,
            ): Boolean =
                try {
                    Log.d(TAG, "Initializing session: $sessionId")

                    // Create marker manager for spatial operations
                    val markerManager = MarkerManager.defaultManager<String>()

                    val session = SpatialSession(config, markerManager)
                    sessions[sessionId] = session

                    Log.d(TAG, "Session $sessionId initialized successfully")
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize session $sessionId", e)
                    false
                }

            fun updateMarkers(
                sessionId: String,
                markers: List<MarkerDataDTO>,
            ): Boolean {
                return try {
                    val session = sessions[sessionId] ?: return false

                    // Throttle markers if too many
                    val limitedMarkers =
                        if (markers.size > MAX_MARKERS_PER_SESSION) {
                            Log.w(TAG, "Throttling markers from ${markers.size} to $MAX_MARKERS_PER_SESSION")
                            markers.take(MAX_MARKERS_PER_SESSION)
                        } else {
                            markers
                        }

                    // Update marker data in session
                    limitedMarkers.forEach { marker ->
                        session.markerData[marker.id] = marker
                    }

                    // Convert to MarkerState for strategy
                    val markerStates =
                        limitedMarkers.map { dto ->
                            MarkerState(
                                id = dto.id,
                                position = GeoPoint.fromLatLong(dto.latitude, dto.longitude),
                                clickable = dto.clickable,
                            )
                        }

                    // Add to marker manager
                    runBlocking {
                        markerStates.forEach { state ->
                            val entity =
                                MarkerEntityImpl<String>(
                                    state = state,
                                    marker = state.id, // Use ID as the "marker" for tracking
                                    isRendered = false,
                                )
                            session.markerManager.registerEntity(entity)
                        }
                    }

                    Log.d(TAG, "Updated ${limitedMarkers.size} markers in session $sessionId")
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update markers in session $sessionId", e)
                    false
                }
            }

            fun removeMarkers(
                sessionId: String,
                markerIds: List<String>,
            ): Boolean {
                return try {
                    val session = sessions[sessionId] ?: return false

                    markerIds.forEach { id ->
                        session.markerData.remove(id)
                        session.renderedMarkers.remove(id)
                        session.markerManager.removeEntity(id)
                    }

                    Log.d(TAG, "Removed ${markerIds.size} markers from session $sessionId")
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to remove markers from session $sessionId", e)
                    false
                }
            }

            fun calculateSpatialChanges(
                sessionId: String,
                camera: CameraPositionDTO,
            ): SpatialResultDTO {
                return try {
                    val session = sessions[sessionId] ?: return SpatialResultDTO(emptyList(), emptyList(), emptyList())

                    val bounds =
                        GeoRectBounds(
                            southWest = GeoPoint.fromLatLong(camera.boundsMinLat, camera.boundsMinLng),
                            northEast = GeoPoint.fromLatLong(camera.boundsMaxLat, camera.boundsMaxLng),
                        )

                    val expandedBounds = expandBounds(bounds, session.config.expandMargin)

                    // Find markers in viewport using marker manager's spatial index
                    val markersInBounds = session.markerManager.findMarkersInBounds(expandedBounds)
                    val markerIdsInBounds = markersInBounds.map { it.state.id }.toSet()

                    val markersToAdd = mutableListOf<String>()
                    val markersToRemove = mutableListOf<String>()
                    val markersToUpdate = mutableListOf<String>()

                    // Find markers that should be added (in viewport but not rendered)
                    markerIdsInBounds.forEach { id ->
                        if (!session.renderedMarkers.contains(id)) {
                            markersToAdd.add(id)
                            session.renderedMarkers.add(id)
                        }
                    }

                    // Find markers that should be removed (rendered but not in viewport, only if not add-only mode)
                    if (!session.config.addOnlyMode) {
                        val markersToRemoveSet =
                            session.renderedMarkers.filter { id ->
                                !markerIdsInBounds.contains(id)
                            }
                        markersToRemove.addAll(markersToRemoveSet)
                        markersToRemoveSet.forEach { id ->
                            session.renderedMarkers.remove(id)
                        }
                    }

                    Log
                        .d(TAG, "Spatial calculation for $sessionId: +${markersToAdd.size} -${markersToRemove.size} ~${markersToUpdate.size}")

                    SpatialResultDTO(markersToAdd, markersToRemove, markersToUpdate)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to calculate spatial changes for session $sessionId", e)
                    SpatialResultDTO(emptyList(), emptyList(), emptyList())
                }
            }

            fun findNearestMarker(
                sessionId: String,
                latitude: Double,
                longitude: Double,
            ): String? {
                return try {
                    val session = sessions[sessionId] ?: return null
                    val position = GeoPoint.fromLatLong(latitude, longitude)

                    val nearestEntity = session.markerManager.findNearest(position)
                    nearestEntity?.state?.id
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to find nearest marker in session $sessionId", e)
                    null
                }
            }

            fun destroySession(sessionId: String): Boolean =
                try {
                    val session = sessions.remove(sessionId)
                    session?.markerManager?.destroy()

                    Log.d(TAG, "Session $sessionId destroyed")
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to destroy session $sessionId", e)
                    false
                }

            fun getPerformanceStats(sessionId: String): String {
                return try {
                    val session = sessions[sessionId] ?: return "{\"error\": \"session_not_found\"}"

                    val stats =
                        mapOf(
                            "sessionId" to sessionId,
                            "markerCount" to session.markerData.size,
                            "renderedCount" to session.renderedMarkers.size,
                            "addOnlyMode" to session.config.addOnlyMode,
                            "expandMargin" to session.config.expandMargin,
                        )

                    // Simple JSON serialization (or use a proper JSON library)
                    stats.entries.joinToString(
                        prefix = "{",
                        postfix = "}",
                        separator = ", ",
                    ) { "\"${it.key}\": ${if (it.value is String) "\"${it.value}\"" else it.value}" }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get performance stats for session $sessionId", e)
                    "{\"error\": \"${e.message}\"}"
                }
            }
        }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "SpatialMarkerService bound with intent: $intent")
        Log.d(TAG, "Returning binder: $binder")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SpatialMarkerService created in process: ${Process.myPid()}")
    }

    override fun onDestroy() {
        super.onDestroy()

        // Clean up all sessions
        sessions.values.forEach { session ->
            try {
                session.markerManager.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying session during service cleanup", e)
            }
        }
        sessions.clear()

        Log.d(TAG, "SpatialMarkerService destroyed")
    }
}
