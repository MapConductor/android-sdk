package com.mapconductor.core.raster

import java.io.Serializable

enum class TileScheme {
    XYZ,
    TMS,
}

sealed class RasterSource : Serializable {
    data class UrlTemplate(
        val template: String,
        val tileSize: Int = DEFAULT_TILE_SIZE,
        val minZoom: Int? = null,
        val maxZoom: Int? = null,
        val attribution: String? = null,
        val scheme: TileScheme = TileScheme.XYZ,
    ) : RasterSource()

    data class TileJson(
        val url: String,
    ) : RasterSource()

    data class ArcGisService(
        val serviceUrl: String,
    ) : RasterSource()

    companion object {
        const val DEFAULT_TILE_SIZE: Int = 256
    }
}
