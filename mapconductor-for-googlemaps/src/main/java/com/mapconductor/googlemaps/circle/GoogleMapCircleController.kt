package com.mapconductor.googlemaps.circle

import com.mapconductor.core.circle.CircleController
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.core.circle.CircleManagerImpl
import com.mapconductor.googlemaps.GoogleMapActualCircle

class GoogleMapCircleController(
    circleManager: CircleManager<GoogleMapActualCircle> = CircleManagerImpl(),
    renderer: GoogleMapCircleOverlayRenderer,
) : CircleController<GoogleMapActualCircle>(circleManager, renderer)
