package com.mapconductor.marker.strategy

import com.mapconductor.marker.strategy.spatial.RemoteSpatialMarkerRenderingStrategy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import android.content.Context
import android.content.Intent
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
    private val activeStrategies = ConcurrentHashMap<String, RemoteSpatialMarkerRenderingStrategy<*>>()
    private var isServiceStarted = false
    private val serviceLock = Object()

    /**
     * Register a new remote strategy. Starts the service if this is the first strategy.
     */
    fun <T> registerStrategy(
        context: Context,
        strategy: RemoteSpatialMarkerRenderingStrategy<T>,
    ): String {
        val strategyId = strategy.hashCode().toString()

        synchronized(serviceLock) {
            activeStrategies[strategyId] = strategy
            val count = activeStrategyCount.incrementAndGet()

            Log.d(TAG, "Registered strategy $strategyId. Active strategies: $count")

            // For now, we skip starting the actual service since we're using local fallback
            // if (!isServiceStarted) {
            //     startService(context)
            // }
        }

        return strategyId
    }

    /**
     * Unregister a strategy. Stops the service if this was the last strategy.
     */
    fun unregisterStrategy(
        context: Context,
        strategyId: String,
    ) {
        synchronized(serviceLock) {
            if (activeStrategies.remove(strategyId) != null) {
                val count = activeStrategyCount.decrementAndGet()

                Log.d(TAG, "Unregistered strategy $strategyId. Active strategies: $count")

                // For now, we skip stopping the service since we're using local fallback
                // if (count == 0 && isServiceStarted) {
                //     stopService(context)
                // }
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

    /**
     * Start the background service (when IPC functionality is enabled)
     */
    private fun startService(
        context: Context,
        serviceClass: Class<*>,
    ) {
        try {
            val intent = Intent(context, serviceClass)
            context.startService(intent)
            isServiceStarted = true
            Log.d(TAG, "SpatialMarkerService started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start SpatialMarkerService", e)
        }
    }

    /**
     * Stop the background service (when IPC functionality is enabled)
     */
    private fun stopService(
        context: Context,
        serviceClass: Class<*>,
    ) {
        try {
            val intent = Intent(context, serviceClass)
            context.stopService(intent)
            isServiceStarted = false
            Log.d(TAG, "SpatialMarkerService stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop SpatialMarkerService", e)
        }
    }

    /**
     * Get current service statistics for debugging
     */
    fun getServiceStats(): Map<String, Any> {
        synchronized(serviceLock) {
            return mapOf(
                "isServiceStarted" to isServiceStarted,
                "activeStrategyCount" to activeStrategyCount.get(),
                "activeStrategyIds" to activeStrategies.keys.toList(),
                "mode" to "local_fallback",
            )
        }
    }
}
