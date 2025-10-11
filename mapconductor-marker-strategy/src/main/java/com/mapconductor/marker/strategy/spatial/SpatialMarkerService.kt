package com.mapconductor.marker.strategy.spatial

import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.marker.MarkerEntityImpl
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.spherical.expandBounds
import java.util.concurrent.ConcurrentHashMap
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.util.Log
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore

/**
 * Background service that handles marker spatial calculations in a separate process.
 * Offloads heavy spatial computations from the main process via Binder (AIDL).
 */
internal class SpatialMarkerService : Service() {
    companion object {
        private const val TAG = "SpatialMarkerService"
        private const val MAX_MARKERS_PER_SESSION = 10000 // Throttling limit
    }

    private data class SpatialSession(
        val config: SpatialConfigDTO,
        val markerManager: MarkerManager<String>, // Track IDs only on remote side
        val markerData: MutableMap<String, MarkerDataDTO> = ConcurrentHashMap(),
        val renderedMarkers: MutableSet<String> = ConcurrentHashMap.newKeySet(),
        val semaphore: Semaphore = Semaphore(1),
    )

    private val sessions = ConcurrentHashMap<String, SpatialSession>()

    private val binder =
        object : ISpatialMarkerService.Stub() {
            override fun initializeSession(
                sessionId: String,
                config: SpatialConfigDTO,
            ): Boolean =
                try {
                    Log.d(TAG, "Initializing session: $sessionId")
                    val markerManager = MarkerManager.defaultManager<String>()
                    val session = SpatialSession(config, markerManager)
                    sessions[sessionId] = session
                    Log.d(TAG, "Session $sessionId initialized successfully")
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize session $sessionId", e)
                    false
                }

            override fun updateMarkers(
                sessionId: String,
                markers: MutableList<MarkerDataDTO>?,
            ): Boolean {
                return try {
                    val session = sessions[sessionId] ?: return false
                    val input = markers ?: emptyList()

                    val limited =
                        if (input.size > MAX_MARKERS_PER_SESSION) {
                            Log.w(TAG, "Throttling markers from ${input.size} to $MAX_MARKERS_PER_SESSION")
                            input.take(MAX_MARKERS_PER_SESSION)
                        } else {
                            input
                        }

                    limited.forEach { marker -> session.markerData[marker.id] = marker }

                    val markerStates =
                        limited.map { dto ->
                            MarkerState(
                                id = dto.id,
                                position = GeoPointImpl.fromLatLong(dto.latitude, dto.longitude),
                                clickable = dto.clickable,
                            )
                        }

                    runBlocking {
                        markerStates.forEach { state ->
                            val entity =
                                MarkerEntityImpl<String>(
                                    state = state,
                                    marker = state.id,
                                    isRendered = false,
                                )
                            session.markerManager.registerEntity(entity)
                        }
                    }

                    Log.d(TAG, "Updated ${limited.size} markers in session $sessionId")
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update markers in session $sessionId", e)
                    false
                }
            }

            override fun removeMarkers(
                sessionId: String,
                markerIds: MutableList<String>?,
            ): Boolean {
                return try {
                    val session = sessions[sessionId] ?: return false
                    val ids = markerIds ?: emptyList()
                    ids.forEach { id ->
                        session.markerData.remove(id)
                        session.renderedMarkers.remove(id)
                        session.markerManager.removeEntity(id)
                    }
                    Log.d(TAG, "Removed ${ids.size} markers from session $sessionId")
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to remove markers from session $sessionId", e)
                    false
                }
            }

            override fun calculateChanges(
                sessionId: String,
                camera: CameraPositionDTO,
            ): SpatialResultDTO {
                return try {
                    val session =
                        sessions[sessionId]
                            ?: return SpatialResultDTO(emptyList(), emptyList(), emptyList())

                    val bounds =
                        GeoRectBounds(
                            southWest = GeoPointImpl.fromLatLong(camera.boundsMinLat, camera.boundsMinLng),
                            northEast = GeoPointImpl.fromLatLong(camera.boundsMaxLat, camera.boundsMaxLng),
                        )

                    val expandedBounds = expandBounds(bounds, session.config.expandMargin)
                    val markersInBounds = session.markerManager.findMarkersInBounds(expandedBounds)
                    val markerIdsInBounds = markersInBounds.map { it.state.id }.toSet()

                    val markersToAdd = mutableListOf<String>()
                    val markersToRemove = mutableListOf<String>()
                    val markersToUpdate = mutableListOf<String>()

                    markerIdsInBounds.forEach { id ->
                        if (!session.renderedMarkers.contains(id)) {
                            markersToAdd.add(id)
                            session.renderedMarkers.add(id)
                        }
                    }

                    if (!session.config.addOnlyMode) {
                        val toRemove = session.renderedMarkers.filter { id -> !markerIdsInBounds.contains(id) }
                        markersToRemove.addAll(toRemove)
                        toRemove.forEach { id -> session.renderedMarkers.remove(id) }
                    }

                    SpatialResultDTO(markersToAdd, markersToRemove, markersToUpdate)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to calculate spatial changes for session $sessionId", e)
                    SpatialResultDTO(emptyList(), emptyList(), emptyList())
                }
            }

            override fun findNearestMarker(
                sessionId: String,
                latitude: Double,
                longitude: Double,
            ): String? {
                return try {
                    val session = sessions[sessionId] ?: return null
                    val position = GeoPointImpl.fromLatLong(latitude, longitude)
                    session.markerManager
                        .findNearest(position)
                        ?.state
                        ?.id
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to find nearest marker in session $sessionId", e)
                    null
                }
            }

            override fun destroySession(sessionId: String): Boolean =
                try {
                    val session = sessions.remove(sessionId)
                    session?.markerManager?.destroy()
                    Log.d(TAG, "Session $sessionId destroyed")
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to destroy session $sessionId", e)
                    false
                }

            override fun getPerformanceStats(sessionId: String): String {
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
                    stats.entries.joinToString(prefix = "{", postfix = "}", separator = ", ") {
                        "\"${it.key}\": ${if (it.value is String) "\"${it.value}\"" else it.value}"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get performance stats for session $sessionId", e)
                    "{\"error\": \"${e.message}\"}"
                }
            }
        }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "SpatialMarkerService bound with intent: $intent")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SpatialMarkerService created in process: ${Process.myPid()}")
    }

    override fun onDestroy() {
        super.onDestroy()
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
