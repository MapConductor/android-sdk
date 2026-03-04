package com.mapconductor.core.raster

import java.io.Serializable

enum class TileScheme {
    XYZ,
    TMS,
}

sealed class RasterLayerSource : Serializable {
    data class UrlTemplate(
        val template: String,
        val tileSize: Int = DEFAULT_TILE_SIZE,
        val minZoom: Int? = null,
        val maxZoom: Int? = null,
        val attribution: String? = null,
        val scheme: TileScheme = TileScheme.XYZ,
    ) : RasterLayerSource()

    data class TileJson(
        val url: String,
    ) : RasterLayerSource()

    data class ArcGisService(
        val serviceUrl: String,
    ) : RasterLayerSource()

    companion object {
        const val DEFAULT_TILE_SIZE: Int = 512
    }
}
