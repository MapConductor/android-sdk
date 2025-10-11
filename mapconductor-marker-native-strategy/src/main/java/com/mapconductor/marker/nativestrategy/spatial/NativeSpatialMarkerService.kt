package com.mapconductor.marker.nativestrategy.spatial

import java.util.concurrent.ConcurrentHashMap
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.util.Log

/**
 * Bound service that hosts the C++-backed NativeRemoteSpatialEngine in a separate process.
 * Exposes an AIDL interface for clients to initialize sessions, push marker data, and request
 * spatial calculations without sharing memory with the app process.
 */
internal class NativeSpatialMarkerService : Service() {
    companion object {
        private const val TAG = "NativeSpatialService"
        private const val MAX_MARKERS_PER_BATCH = 10000
    }

    private val sessions = ConcurrentHashMap<String, NativeRemoteSpatialEngine>()

    private val binder =
        object : INativeSpatialMarkerService.Stub() {
            override fun initializeSession(
                sessionId: String,
                config: NativeSpatialConfigDTO,
            ): Boolean =
                try {
                    Log.d(TAG, "initializeSession: $sessionId")
                    val strategy =
                        NativeRemoteSpatialEngine.create(config.expandMargin, config.addOnlyMode)
                            ?: return false
                    val ok = strategy.initializeSession(config)
                    if (ok) sessions[sessionId] = strategy
                    ok
                } catch (e: Exception) {
                    Log.e(TAG, "initializeSession failed", e)
                    false
                }

            override fun addMarkers(
                sessionId: String,
                markers: MutableList<NativeMarkerDataDTO>?,
            ): Boolean =
                try {
                    val strategy = sessions[sessionId] ?: return false
                    val input = markers ?: emptyList()
                    val limited = if (input.size > MAX_MARKERS_PER_BATCH) input.take(MAX_MARKERS_PER_BATCH) else input
                    strategy.addMarkers(limited)
                } catch (e: Exception) {
                    Log.e(TAG, "addMarkers failed", e)
                    false
                }

            override fun updateMarker(
                sessionId: String,
                marker: NativeMarkerDataDTO?,
            ): Boolean =
                try {
                    val strategy = sessions[sessionId] ?: return false
                    marker?.let { strategy.updateMarker(it) } ?: false
                } catch (e: Exception) {
                    Log.e(TAG, "updateMarker failed", e)
                    false
                }

            override fun removeMarkers(
                sessionId: String,
                markerIds: MutableList<String>?,
            ): Boolean =
                try {
                    val strategy = sessions[sessionId] ?: return false
                    val ids = markerIds ?: return false
                    var ok = true
                    ids.forEach { id -> ok = ok && strategy.removeMarker(id) }
                    ok
                } catch (e: Exception) {
                    Log.e(TAG, "removeMarkers failed", e)
                    false
                }

            override fun processCameraChange(
                sessionId: String,
                camera: NativeCameraPositionDTO,
            ): NativeSpatialResultDTO =
                try {
                    val strategy = sessions[sessionId] ?: return NativeSpatialResultDTO()
                    val result =
                        strategy.processCameraChange(
                            CameraPosition(
                                latitude = camera.latitude,
                                longitude = camera.longitude,
                                zoom = camera.zoom,
                                bearing = camera.bearing,
                                tilt = camera.tilt,
                                visibleBounds =
                                    NativeGeoRectBounds(
                                        south = camera.boundsMinLat,
                                        north = camera.boundsMaxLat,
                                        west = camera.boundsMinLng,
                                        east = camera.boundsMaxLng,
                                    ),
                            ),
                        )
                    result ?: NativeSpatialResultDTO()
                } catch (e: Exception) {
                    Log.e(TAG, "processCameraChange failed", e)
                    NativeSpatialResultDTO()
                }

            override fun findNearestMarker(
                sessionId: String,
                latitude: Double,
                longitude: Double,
            ): String? =
                try {
                    val strategy = sessions[sessionId] ?: return null
                    strategy.findNearestMarker(latitude, longitude)
                } catch (e: Exception) {
                    Log.e(TAG, "findNearestMarker failed", e)
                    null
                }

            override fun destroySession(sessionId: String): Boolean =
                try {
                    sessions.remove(sessionId)?.let { s ->
                        s.destroy()
                        true
                    } ?: false
                } catch (e: Exception) {
                    Log.e(TAG, "destroySession failed", e)
                    false
                }

            override fun getPerformanceStats(sessionId: String): String =
                try {
                    val s = sessions[sessionId] ?: return "{\"error\":\"session_not_found\"}"
                    s.getPerformanceStats()?.toString() ?: "{\"error\":\"no_stats\"}"
                } catch (e: Exception) {
                    Log.e(TAG, "getPerformanceStats failed", e)
                    "{\"error\":\"${e.message}\"}"
                }
        }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "NativeSpatialMarkerService bound in pid=${Process.myPid()}")
        return binder
    }
}
