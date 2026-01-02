package com.mapconductor.tileserver

object TileServerRegistry {
    private val lock = Any()

    @Volatile
    private var server: LocalTileServer? = null

    fun get(): LocalTileServer =
        synchronized(lock) {
            val existing = server
            if (existing != null) {
                return existing
            }
            val newServer = LocalTileServer.startServer()
            server = newServer
            newServer
        }
}
