package com.mapconductor.arcgis.polygon

import com.mapconductor.arcgis.ArcGISActualPolygon
import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonManagerInterface

class ArcGISPolygonOverlayController(
    polygonManager: PolygonManagerInterface<ArcGISActualPolygon> = PolygonManager(),
    override val renderer: ArcGISPolygonOverlayRenderer,
) : PolygonController<ArcGISActualPolygon>(polygonManager, renderer)
