package com.mapconductor.arcgis.circle

import com.mapconductor.arcgis.ArcGISActualCircle
import com.mapconductor.core.circle.CircleController
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.core.circle.CircleManagerImpl

class ArcGISCircleOverlayController(
    circleManager: CircleManager<ArcGISActualCircle> = CircleManagerImpl(),
    override val renderer: ArcGISCircleOverlayRenderer,
) : CircleController<ArcGISActualCircle>(circleManager, renderer)
