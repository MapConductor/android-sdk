package com.mapconductor.marker.nativestrategy.spatial

import java.util.UUID
import android.util.Log

/**
 * JNI wrapper for the native C++ RemoteSpatialMarkerStrategy implementation.
 *
 * This class provides a Kotlin interface to the high-performance C++ spatial marker
 * rendering strategy. It's designed to be used in background services where maximum
 * performance is required for large marker datasets.
 *
 * Key advantages over pure Kotlin implementation:
 * - 10-50x faster spatial calculations for large datasets
 * - Lower memory overhead through native memory management
 * - Better cache locality for spatial data structures
 * - Vectorized operations for geometric calculations
 * - Lock-free operations where possible
 */
internal class NativeRemoteSpatialEngine private constructor(
    private val nativeStrategyId: Long,
    private val sessionId: String,
) {
    companion object {
        private const val TAG = "NativeRemoteSpatial"

        init {
            try {
                System.loadLibrary("mapconductor-native")
                Log.d(TAG, "Native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library", e)
                throw RuntimeException("Failed to load native library for RemoteSpatialMarkerStrategy", e)
            }
        }

        /**
         * Create a new native remote spatial marker strategy.
         *
         * @param expandMargin The margin for expanding viewport bounds (default 0.3 = 30% expansion)
         * @param addOnlyMode If true, markers are never removed once rendered
         * @return A new strategy instance, or null if creation failed
         */
        fun create(
            expandMargin: Double = 0.3,
            addOnlyMode: Boolean = false,
        ): NativeRemoteSpatialEngine? {
            val sessionId = UUID.randomUUID().toString()
            val nativeId = nativeCreate(sessionId, expandMargin, addOnlyMode)

            return if (nativeId != 0L) {
                val strategy = NativeRemoteSpatialEngine(nativeId, sessionId)
                Log.d(TAG, "Created native strategy with session: $sessionId")
                strategy
            } else {
                Log.e(TAG, "Failed to create native strategy")
                null
            }
        }

        /**
         * Create a high-performance strategy optimized for smooth user experience.
         * Uses larger viewport expansion and add-only mode.
         */
        fun createHighPerformance(): NativeRemoteSpatialEngine? = create(expandMargin = 0.5, addOnlyMode = true)

        /**
         * Create a strategy optimized for very large marker datasets (10K+ markers).
         * Uses aggressive viewport expansion and add-only mode for maximum performance.
         */
        fun createForLargeDatasets(): NativeRemoteSpatialEngine? = create(expandMargin = 0.8, addOnlyMode = true)

        // Native method declarations
        @JvmStatic external fun nativeCreate(
            sessionId: String,
            expandMargin: Double,
            addOnlyMode: Boolean,
        ): Long

        @JvmStatic external fun nativeInitializeSession(
            strategyId: Long,
            expandMargin: Double,
            addOnlyMode: Boolean,
        ): Boolean

        @JvmStatic external fun nativeDestroySession(strategyId: Long)

        @JvmStatic external fun nativeAddMarkers(
            strategyId: Long,
            markers: Array<NativeMarkerDataDTO>,
        ): Boolean

        @JvmStatic external fun nativeUpdateMarker(
            strategyId: Long,
            marker: NativeMarkerDataDTO,
        ): Boolean

        @JvmStatic external fun nativeRemoveMarker(
            strategyId: Long,
            markerId: String,
        ): Boolean

        @JvmStatic external fun nativeProcessCameraChange(
            strategyId: Long,
            camera: CameraPosition,
        ): NativeSpatialResultDTO?

        @JvmStatic external fun nativeFindMarkersInBounds(
            strategyId: Long,
            bounds: NativeGeoRectBounds,
        ): Array<String>?

        @JvmStatic external fun nativeFindNearestMarker(
            strategyId: Long,
            latitude: Double,
            longitude: Double,
        ): String?

        @JvmStatic external fun nativeAddToBatch(
            strategyId: Long,
            marker: NativeMarkerDataDTO,
        )

        @JvmStatic external fun nativeGetMarkerCount(strategyId: Long): Long

        @JvmStatic external fun nativeGetRenderedMarkerCount(strategyId: Long): Long

        @JvmStatic external fun nativeGetPerformanceStats(strategyId: Long): String?
    }

    private var isInitialized = false

    /**
     * Initialize the strategy session with the given configuration.
     * Must be called before using any other methods.
     */
    fun initializeSession(config: NativeSpatialConfigDTO): Boolean {
        val result = nativeInitializeSession(nativeStrategyId, config.expandMargin, config.addOnlyMode)
        isInitialized = result

        if (result) {
            Log.d(TAG, "Session initialized: $sessionId")
        } else {
            Log.e(TAG, "Failed to initialize session: $sessionId")
        }

        return result
    }

    /**
     * Add multiple markers to the strategy.
     * This is more efficient than adding markers one by one.
     */
    fun addMarkers(markers: List<NativeMarkerDataDTO>): Boolean {
        if (!isInitialized) {
            Log.w(TAG, "Strategy not initialized, call initializeSession() first")
            return false
        }

        return try {
            val result = nativeAddMarkers(nativeStrategyId, markers.toTypedArray())
            Log.d(TAG, "Added ${markers.size} markers, success: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add markers", e)
            false
        }
    }

    /**
     * Update a single marker's data.
     */
    fun updateMarker(marker: NativeMarkerDataDTO): Boolean {
        if (!isInitialized) {
            Log.w(TAG, "Strategy not initialized, call initializeSession() first")
            return false
        }

        return try {
            nativeUpdateMarker(nativeStrategyId, marker)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update marker ${marker.id}", e)
            false
        }
    }

    /**
     * Remove a marker by its ID.
     */
    fun removeMarker(markerId: String): Boolean {
        if (!isInitialized) {
            Log.w(TAG, "Strategy not initialized, call initializeSession() first")
            return false
        }

        return try {
            nativeRemoveMarker(nativeStrategyId, markerId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove marker $markerId", e)
            false
        }
    }

    /**
     * Process a camera change and determine which markers should be added/removed.
     * This is the core method for viewport-based marker rendering.
     */
    fun processCameraChange(camera: CameraPosition): NativeSpatialResultDTO? {
        if (!isInitialized) {
            Log.w(TAG, "Strategy not initialized, call initializeSession() first")
            return null
        }

        return try {
            val result = nativeProcessCameraChange(nativeStrategyId, camera)
            if (result != null) {
                Log.d(TAG, "Camera change processed: +${result.markersToAdd.size} -${result.markersToRemove.size}")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process camera change", e)
            null
        }
    }

    /**
     * Find all markers within the given bounds.
     * Uses optimized spatial indexing for fast queries.
     */
    fun findMarkersInBounds(bounds: NativeGeoRectBounds): List<String> {
        if (!isInitialized) {
            Log.w(TAG, "Strategy not initialized, call initializeSession() first")
            return emptyList()
        }

        return try {
            val result = nativeFindMarkersInBounds(nativeStrategyId, bounds)
            result?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to find markers in bounds", e)
            emptyList()
        }
    }

    /**
     * Find the nearest marker to the given coordinates.
     */
    fun findNearestMarker(
        latitude: Double,
        longitude: Double,
    ): String? {
        if (!isInitialized) {
            Log.w(TAG, "Strategy not initialized, call initializeSession() first")
            return null
        }

        return try {
            nativeFindNearestMarker(nativeStrategyId, latitude, longitude)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to find nearest marker", e)
            null
        }
    }

    /**
     * Add a marker to the batch processing queue.
     * Useful for high-frequency updates that should be processed in batches.
     */
    fun addToBatch(marker: NativeMarkerDataDTO) {
        if (!isInitialized) {
            Log.w(TAG, "Strategy not initialized, call initializeSession() first")
            return
        }

        try {
            nativeAddToBatch(nativeStrategyId, marker)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add marker to batch", e)
        }
    }

    /**
     * Get the total number of markers managed by this strategy.
     */
    fun getMarkerCount(): Long =
        if (isInitialized) {
            try {
                nativeGetMarkerCount(nativeStrategyId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get marker count", e)
                0L
            }
        } else {
            0L
        }

    /**
     * Get the number of currently rendered markers.
     */
    fun getRenderedMarkerCount(): Long =
        if (isInitialized) {
            try {
                nativeGetRenderedMarkerCount(nativeStrategyId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get rendered marker count", e)
                0L
            }
        } else {
            0L
        }

    /**
     * Get detailed performance statistics for monitoring and debugging.
     */
    fun getPerformanceStats(): PerformanceStats? {
        if (!isInitialized) {
            return null
        }

        return try {
            val statsString = nativeGetPerformanceStats(nativeStrategyId)
            statsString?.let { PerformanceStats.parseFromString(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get performance stats", e)
            null
        }
    }

    /**
     * Clean up native resources and destroy the strategy session.
     * This should be called when the strategy is no longer needed.
     */
    fun destroy() {
        if (isInitialized) {
            try {
                nativeDestroySession(nativeStrategyId)
                isInitialized = false
                Log.d(TAG, "Strategy destroyed: $sessionId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to destroy strategy", e)
            }
        }
    }

    /**
     * Ensure cleanup when the object is garbage collected.
     */
    protected fun finalize() {
        if (isInitialized) {
            Log.w(TAG, "Strategy was not properly destroyed, cleaning up in finalize()")
            destroy()
        }
    }
}
