package com.mapconductor.core.tileserver

import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

object TileServerRegistry {
    private const val TAG = "TileServerRegistry"
    private val lock = Any()

    @Volatile
    private var server: LocalTileServer? = null

    @Volatile
    private var forceNoStoreCache: Boolean = false

    @Volatile
    private var warmedUp: Boolean = false

    fun get(forceNoStoreCache: Boolean = this.forceNoStoreCache): LocalTileServer =
        synchronized(lock) {
            this.forceNoStoreCache = forceNoStoreCache
            val existing = server
            if (existing != null) {
                existing.setForceNoStoreCache(forceNoStoreCache)
                return existing
            }
            val newServer = LocalTileServer.startServer(forceNoStoreCache = forceNoStoreCache)
            server = newServer
            newServer
        }

    fun setForceNoStoreCache(value: Boolean) {
        synchronized(lock) {
            forceNoStoreCache = value
            server?.setForceNoStoreCache(value)
        }
    }

    /**
     * Pre-initialize the tile server and make a warmup HTTP request.
     * Call this early in app initialization to reduce latency when raster layers are first used.
     */
    fun warmup() {
        if (warmedUp) return
        Thread {
            try {
                val tileServer = get()
                // Make a dummy HTTP request to establish connection
                val url = URL("${tileServer.baseUrl}/warmup")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"
                try {
                    connection.responseCode // triggers the request
                } catch (_: Exception) {
                    // Expected to get 404, that's fine
                }
                connection.disconnect()
                warmedUp = true
                Log.d(TAG, "Tile server warmed up at ${tileServer.baseUrl}")
            } catch (e: Exception) {
                Log.w(TAG, "Tile server warmup failed", e)
            }
        }.start()
    }
}
