package com.mapconductor.here.circle

import com.mapconductor.core.circle.CircleController
import com.mapconductor.core.circle.CircleManagerImpl
import com.mapconductor.here.HereActualCircle

class HereCircleController(
    circleManager: CircleManagerImpl<HereActualCircle> = CircleManagerImpl(),
    renderer: HereCircleOverlayRenderer,
) : CircleController<HereActualCircle>(circleManager, renderer)
