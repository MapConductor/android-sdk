package com.mapconductor.here.circle

import com.mapconductor.core.circle.CircleController
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.here.HereActualCircle

class HereCircleController(
    circleManager: CircleManager<HereActualCircle> = CircleManager(),
    renderer: HereCircleOverlayRenderer,
) : CircleController<HereActualCircle>(circleManager, renderer)
