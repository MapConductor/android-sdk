package com.mapconductor.arcgis.polygon

import com.mapconductor.arcgis.ArcGISActualPolygon
import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonManagerInterface
import com.mapconductor.core.polygon.PolygonManager

class ArcGISPolygonOverlayController(
    polygonManager: PolygonManagerInterface<ArcGISActualPolygon> = PolygonManager(),
    override val renderer: ArcGISPolygonOverlayRenderer,
) : PolygonController<ArcGISActualPolygon>(polygonManager, renderer)
