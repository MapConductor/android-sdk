package com.mapconductor.here.polyline

import com.mapconductor.core.polyline.PolylineController
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.core.polyline.PolylineManagerImpl
import com.mapconductor.here.HereActualPolyline

class HerePolylineController(
    polylineManager: PolylineManager<HereActualPolyline> = PolylineManagerImpl(),
    renderer: HerePolylineOverlayRenderer,
) : PolylineController<HereActualPolyline>(polylineManager, renderer)
