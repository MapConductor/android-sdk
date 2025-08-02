package com.mapconductor.arcgis

import androidx.compose.ui.graphics.Color

internal fun Color.toArcGISColor(): com.arcgismaps.Color =
    com.arcgismaps.Color.fromRgba(
        r = (this.red * 255).toInt(),
        g = (this.green * 255).toInt(),
        b = (this.blue * 255).toInt(),
        a = (this.alpha * 255).toInt(),
    )
