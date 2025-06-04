package com.mapconductor.here

import com.here.sdk.mapview.MapScheme

data class HereMapViewInitOptions(
    val scheme: MapScheme = MapScheme.NORMAL_DAY,
)
