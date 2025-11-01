package com.mapconductor.maplibre.circle

import com.mapconductor.core.circle.CircleController
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.core.circle.CircleManagerImpl
import com.mapconductor.maplibre.MapLibreActualCircle

class MapLibreCircleController(
    override val renderer: MapLibreCircleOverlayRenderer,
    circleManager: CircleManager<MapLibreActualCircle> = CircleManagerImpl(),
) : CircleController<MapLibreActualCircle>(circleManager, renderer)
