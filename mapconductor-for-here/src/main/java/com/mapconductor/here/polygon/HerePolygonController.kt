package com.mapconductor.here.polygon

import HerePolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonManagerImpl
import com.mapconductor.here.HereActualPolygon

class HerePolygonController(
    polygonManager: PolygonManager<HereActualPolygon> = PolygonManagerImpl(),
    renderer: HerePolygonOverlayRenderer,
) : PolygonController<HereActualPolygon>(polygonManager, renderer)
