package com.mapconductor.arcgis.polygon

import com.mapconductor.arcgis.ArcGISActualPolygon
import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonManagerImpl

class ArcGISPolygonController(
    polygonManager: PolygonManager<ArcGISActualPolygon> = PolygonManagerImpl(),
    override val renderer: ArcGISPolygonRenderer,
) : PolygonController<ArcGISActualPolygon>(polygonManager, renderer)
