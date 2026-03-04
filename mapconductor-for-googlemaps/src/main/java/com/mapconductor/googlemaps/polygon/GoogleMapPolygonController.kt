package com.mapconductor.googlemaps.polygon

import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonManagerInterface
import com.mapconductor.googlemaps.GoogleMapActualPolygon

class GoogleMapPolygonController(
    polygonManager: PolygonManagerInterface<GoogleMapActualPolygon> = PolygonManager(),
    renderer: GoogleMapPolygonOverlayRenderer,
) : PolygonController<GoogleMapActualPolygon>(polygonManager, renderer)
