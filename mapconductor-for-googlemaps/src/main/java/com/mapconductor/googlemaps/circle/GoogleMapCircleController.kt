package com.mapconductor.googlemaps.circle

import com.mapconductor.core.circle.CircleController
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.core.circle.CircleManagerInterface
import com.mapconductor.googlemaps.GoogleMapActualCircle

class GoogleMapCircleController(
    circleManager: CircleManagerInterface<GoogleMapActualCircle> = CircleManager(),
    renderer: GoogleMapCircleOverlayRenderer,
) : CircleController<GoogleMapActualCircle>(circleManager, renderer)
