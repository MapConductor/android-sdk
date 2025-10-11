package com.mapconductor.marker.strategy

import com.mapconductor.marker.strategy.spatial.RemoteSpatialMarkerStrategy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import android.content.Context
import android.util.Log

/**
 * Manager for handling the lifecycle of the background marker service.
 * Ensures the service is started when needed and stopped when no longer required.
 *
 * Note: This manager now primarily tracks strategy lifecycle since we're using
 * local fallback processing instead of IPC services.
 */
object SpatialMarkerServiceManager {
    private const val TAG = "SpatialMarkerServiceManager"

    private val activeStrategyCount = AtomicInteger(0)
    private val activeStrategies = ConcurrentHashMap<String, RemoteSpatialMarkerStrategy<*>>()
    private var isServiceStarted = false // deprecated: bind-only mode
    private val serviceLock = Object()

    /**
     * Register a new remote strategy. Bind-only mode: no startService.
     */
    fun <T> registerStrategy(
        context: Context,
        strategy: RemoteSpatialMarkerStrategy<T>,
    ): String {
        val strategyId = strategy.hashCode().toString()

        synchronized(serviceLock) {
            activeStrategies[strategyId] = strategy
//            val count = activeStrategyCount.incrementAndGet()

            // Bind-only: do not start the service here. Client binds on demand.
        }

        return strategyId
    }

    /**
     * Unregister a strategy. Bind-only: no explicit stopService.
     */
    fun unregisterStrategy(
        context: Context,
        strategyId: String,
    ) {
        synchronized(serviceLock) {
            if (activeStrategies.remove(strategyId) != null) {
                val count = activeStrategyCount.decrementAndGet()

                Log.d(TAG, "Unregistered strategy $strategyId. Active strategies: $count")

                // Bind-only: when all clients unbind, the system destroys the service automatically.
            }
        }
    }

    /**
     * Force stop the service and clean up all strategies.
     * Should be called when the application is being destroyed.
     */
    fun forceStopService(context: Context) {
        synchronized(serviceLock) {
            Log.d(TAG, "Force stopping marker service")

            // Clean up all active strategies
            activeStrategies.values.forEach { strategy ->
                try {
                    strategy.destroy()
                } catch (e: Exception) {
                    Log.e(TAG, "Error destroying strategy during force stop", e)
                }
            }

            activeStrategies.clear()
            activeStrategyCount.set(0)

            // For now, we skip stopping the service since we're using local fallback
            // if (isServiceStarted) {
            //     stopService(context)
            // }
        }
    }

    // No start/stop in bind-only mode

    /**
     * Get current service statistics for debugging
     */
    fun getServiceStats(): Map<String, Any> {
        synchronized(serviceLock) {
            return mapOf(
                "isServiceStarted" to false,
                "activeStrategyCount" to activeStrategyCount.get(),
                "activeStrategyIds" to activeStrategies.keys.toList(),
                "mode" to "bind_only",
            )
        }
    }
}
