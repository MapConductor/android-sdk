package com.mapconductor.tileserver

interface TileProvider {
    fun renderTile(request: TileRequest): ByteArray?
}
