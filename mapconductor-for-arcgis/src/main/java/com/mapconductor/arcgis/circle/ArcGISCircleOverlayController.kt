package com.mapconductor.arcgis.circle

import com.mapconductor.arcgis.ArcGISActualCircle
import com.mapconductor.core.circle.CircleController
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.core.circle.CircleManagerInterface

class ArcGISCircleOverlayController(
    circleManager: CircleManagerInterface<ArcGISActualCircle> = CircleManager(),
    override val renderer: ArcGISCircleOverlayRenderer,
) : CircleController<ArcGISActualCircle>(circleManager, renderer)
