package com.mapconductor.here.polygon

import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonManagerInterface
import com.mapconductor.here.HereActualPolygon

class HerePolygonController(
    polygonManager: PolygonManagerInterface<HereActualPolygon> = PolygonManager(),
    renderer: HerePolygonOverlayRenderer,
) : PolygonController<HereActualPolygon>(polygonManager, renderer)
