package com.mapconductor.tileserver

interface TileProviderInterface {
    fun renderTile(request: TileRequest): ByteArray?
}
