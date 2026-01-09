package com.mapconductor.core.tileserver

interface TileProviderInterface {
    fun renderTile(request: TileRequest): ByteArray?
}
