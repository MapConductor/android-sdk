package com.mapconductor.core.tileserver

object TileServerRegistry {
    private val lock = Any()

    @Volatile
    private var server: LocalTileServer? = null

    @Volatile
    private var forceNoStoreCache: Boolean = false

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
}
