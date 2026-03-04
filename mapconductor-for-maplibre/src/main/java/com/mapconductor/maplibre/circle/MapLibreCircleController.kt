package com.mapconductor.maplibre.circle

import com.mapconductor.core.circle.CircleController
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.core.circle.CircleManagerInterface
import com.mapconductor.maplibre.MapLibreActualCircle

class MapLibreCircleController(
    override val renderer: MapLibreCircleOverlayRenderer,
    circleManager: CircleManagerInterface<MapLibreActualCircle> = CircleManager(),
) : CircleController<MapLibreActualCircle>(circleManager, renderer)
