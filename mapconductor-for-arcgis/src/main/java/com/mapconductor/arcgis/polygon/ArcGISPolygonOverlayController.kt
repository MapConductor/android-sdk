package com.mapconductor.arcgis.polygon

import com.mapconductor.arcgis.ArcGISActualPolygon
import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonManagerImpl

class ArcGISPolygonOverlayController(
    polygonManager: PolygonManager<ArcGISActualPolygon> = PolygonManagerImpl(),
    override val renderer: ArcGISPolygonOverlayRenderer,
) : PolygonController<ArcGISActualPolygon>(polygonManager, renderer)
