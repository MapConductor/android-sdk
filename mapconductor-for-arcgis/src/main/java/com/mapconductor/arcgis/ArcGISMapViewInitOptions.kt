package com.mapconductor.arcgis.map

import com.arcgismaps.mapping.BasemapStyle

data class ArcGISMapViewInitOptions(
    val basemapStyle: BasemapStyle,
    val elevationSources: List<String>,
)
