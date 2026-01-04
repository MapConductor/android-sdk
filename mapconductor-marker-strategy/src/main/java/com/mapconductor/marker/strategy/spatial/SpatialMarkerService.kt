package com.mapconductor.marker.strategy.spatial

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.geocell.HexGeocellInterface
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.spherical.expandBounds
import java.util.concurrent.ConcurrentHashMap
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore

/**
 * Background service that handles marker spatial calculations in a separate process.
 * Uses hex-cell buckets to answer viewport queries efficiently.
 */
internal class SpatialMarkerService : Service() {
    companion object {
        private const val TAG = "SpatialMarkerService"
        private const val MAX_MARKERS_PER_SESSION = 10000 // Throttling limit
    }

    data class SpatialSession(
        val config: SpatialConfigDTO,
        val markerManager: MarkerManager<String>, // For nearest() API only
        val markerData: MutableMap<String, MarkerDataDTO> = ConcurrentHashMap(),
        val renderedMarkers: MutableSet<String> = ConcurrentHashMap.newKeySet(),
        val semaphore: Semaphore = Semaphore(1),
        val geocell: HexGeocellInterface = HexGeocell.defaultGeocell(),
        val cellBucketsByZoom: MutableMap<Int, MutableMap<String, MutableSet<String>>> = ConcurrentHashMap(),
        val markerCellByZoom: MutableMap<Int, MutableMap<String, String>> = ConcurrentHashMap(),
    )

    private val sessions = ConcurrentHashMap<String, SpatialSession>()

    private val binder =
        object : ISpatialMarkerService.Stub() {
            override fun initializeSession(
                sessionId: String,
                config: SpatialConfigDTO,
            ): Boolean =
                try {
                    val markerManager = MarkerManager.defaultManager<String>()
                    val session = SpatialSession(config, markerManager)
                    sessions[sessionId] = session
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
                            // throttling; omit debug log in release
                            input.take(MAX_MARKERS_PER_SESSION)
                        } else {
                            input
                        }

                    limited.forEach { marker -> session.markerData[marker.id] = marker }

                    // Keep the nearest() support in sync
                    val markerStates =
                        limited.map { dto ->
                            MarkerState(
                                id = dto.id,
                                position = GeoPoint.fromLatLong(dto.latitude, dto.longitude),
                                clickable = dto.clickable,
                            )
                        }
                    runBlocking {
                        markerStates.forEach { state ->
                            val entity =
                                MarkerEntity<String>(
                                    state = state,
                                    marker = state.id,
                                    isRendered = false,
                                )
                            session.markerManager.registerEntity(entity)
                        }
                    }

                    // Index new markers into already-built zoom buckets
                    if (session.markerCellByZoom.isNotEmpty()) {
                        session.markerCellByZoom.keys.forEach { z ->
                            val buckets = session.cellBucketsByZoom.getOrPut(z) { ConcurrentHashMap() }
                            val markerToCell = session.markerCellByZoom.getOrPut(z) { ConcurrentHashMap() }
                            limited.forEach { dto ->
                                val cellId =
                                    this@SpatialMarkerService
                                        .latLngToCellId(session.geocell, dto.latitude, dto.longitude, z)
                                val prev = markerToCell.put(dto.id, cellId)
                                if (prev != null && prev != cellId) {
                                    buckets[prev]?.remove(dto.id)
                                }
                                val set = buckets.getOrPut(cellId) { ConcurrentHashMap.newKeySet() }
                                set.add(dto.id)
                            }
                        }
                    }

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
                        // Remove from all zoom buckets
                        session.markerCellByZoom.forEach { (z, map) ->
                            map.remove(id)?.let { cid -> session.cellBucketsByZoom[z]?.get(cid)?.remove(id) }
                        }
                    }
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
                    val session = sessions[sessionId] ?: return SpatialResultDTO(emptyList(), emptyList(), emptyList())

                    val bounds =
                        GeoRectBounds(
                            southWest = GeoPoint.fromLatLong(camera.boundsMinLat, camera.boundsMinLng),
                            northEast = GeoPoint.fromLatLong(camera.boundsMaxLat, camera.boundsMaxLng),
                        )
                    val expandedBounds = expandBounds(bounds, session.config.expandMargin)

                    val indexZoom = this@SpatialMarkerService.chooseIndexZoom(camera.zoom)
                    this@SpatialMarkerService.ensureIndexedForZoom(session, indexZoom)

                    // Build coverage cells for expanded bounds
                    val center =
                        expandedBounds.center ?: GeoPoint.fromLatLong(camera.centerLatitude, camera.centerLongitude)
                    val centerCoord = session.geocell.latLngToHexCoord(center, indexZoom.toDouble())
                    val sw = expandedBounds.southWest ?: center
                    val ne = expandedBounds.northEast ?: center
                    val se = GeoPoint.fromLongLat(ne.longitude, sw.latitude)
                    val nw = GeoPoint.fromLongLat(sw.longitude, ne.latitude)
                    val swc = session.geocell.latLngToHexCoord(sw, indexZoom.toDouble())
                    val nec = session.geocell.latLngToHexCoord(ne, indexZoom.toDouble())
                    val sec = session.geocell.latLngToHexCoord(se, indexZoom.toDouble())
                    val nwc = session.geocell.latLngToHexCoord(nw, indexZoom.toDouble())
                    val radius =
                        maxOf(
                            session.geocell.hexDistance(centerCoord, swc),
                            session.geocell.hexDistance(centerCoord, nec),
                            session.geocell.hexDistance(centerCoord, sec),
                            session.geocell.hexDistance(centerCoord, nwc),
                        )

                    val buckets = session.cellBucketsByZoom[indexZoom] ?: emptyMap()
                    val idsInBounds = mutableSetOf<String>()
                    session.geocell.hexRange(centerCoord, radius).forEach { coord ->
                        val cid = session.geocell.hexToCellId(coord, indexZoom.toDouble())
                        buckets[cid]?.let { idsInBounds.addAll(it) }
                    }

                    val markersToAdd = mutableListOf<String>()
                    val markersToRemove = mutableListOf<String>()
                    val markersToUpdate = mutableListOf<String>()

                    idsInBounds.forEach { id ->
                        if (!session.renderedMarkers.contains(id)) {
                            markersToAdd.add(id)
                            session.renderedMarkers.add(id)
                        }
                    }
                    if (!session.config.addOnlyMode) {
                        val toRemove = session.renderedMarkers.filter { id -> !idsInBounds.contains(id) }
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
                    val position = GeoPoint.fromLatLong(latitude, longitude)
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
                    if (session != null) {
                        session.markerManager.destroy()
                        session.cellBucketsByZoom.clear()
                        session.markerCellByZoom.clear()
                        session.markerData.clear()
                        session.renderedMarkers.clear()
                    }
                    // session destroyed
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
                            "indexedZooms" to session.cellBucketsByZoom.size,
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

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
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
    }

    private fun chooseIndexZoom(zoom: Double): Int =
        when {
            zoom < 7 -> 7
            zoom < 9 -> 8
            zoom < 11 -> 10
            zoom < 13 -> 12
            else -> 14
        }

    private fun latLngToCellId(
        geocell: HexGeocellInterface,
        lat: Double,
        lng: Double,
        z: Int,
    ): String {
        val coord = geocell.latLngToHexCoord(GeoPoint.fromLatLong(lat, lng), z.toDouble())
        return geocell.hexToCellId(coord, z.toDouble())
    }

    private fun ensureIndexedForZoom(
        session: SpatialSession,
        z: Int,
    ) {
        val buckets = session.cellBucketsByZoom.getOrPut(z) { ConcurrentHashMap() }
        val markerToCell = session.markerCellByZoom.getOrPut(z) { ConcurrentHashMap() }
        if (markerToCell.size == session.markerData.size) return
        session.markerData.values.forEach { dto ->
            if (!markerToCell.containsKey(dto.id)) {
                val cellId = this@SpatialMarkerService.latLngToCellId(session.geocell, dto.latitude, dto.longitude, z)
                markerToCell[dto.id] = cellId
                val set = buckets.getOrPut(cellId) { ConcurrentHashMap.newKeySet() }
                set.add(dto.id)
            }
        }
    }
}
