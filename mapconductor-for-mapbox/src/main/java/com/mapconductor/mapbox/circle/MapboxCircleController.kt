package com.mapconductor.mapbox.circle

import com.mapconductor.core.circle.CircleController
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.mapbox.MapboxActualCircle

class MapboxCircleController(
    override val renderer: MapboxCircleOverlayRenderer,
    circleManager: CircleManager<MapboxActualCircle> = renderer.circleManager,
) : CircleController<MapboxActualCircle>(circleManager, renderer)
