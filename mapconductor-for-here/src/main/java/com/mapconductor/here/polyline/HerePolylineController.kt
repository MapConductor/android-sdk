package com.mapconductor.here.polyline

import com.mapconductor.core.polyline.PolylineController
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.core.polyline.PolylineManagerInterface
import com.mapconductor.here.HereActualPolyline

class HerePolylineController(
    polylineManager: PolylineManagerInterface<HereActualPolyline> = PolylineManager(),
    renderer: HerePolylineOverlayRenderer,
) : PolylineController<HereActualPolyline>(polylineManager, renderer)
