package com.mapconductor.arcgis.groundimage

import com.arcgismaps.mapping.layers.Layer
import com.mapconductor.core.groundimage.GroundImageTileProvider

data class ArcGISGroundImageHandle(
    val routeId: String,
    val version: Long,
    val layer: Layer,
    val tileProvider: GroundImageTileProvider,
)

