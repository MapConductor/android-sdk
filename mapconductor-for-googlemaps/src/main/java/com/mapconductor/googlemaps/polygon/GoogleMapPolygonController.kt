package com.mapconductor.googlemaps.polygon

import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonManagerImpl
import com.mapconductor.googlemaps.GoogleMapActualPolygon

class GoogleMapPolygonController(
    polygonManager: PolygonManager<GoogleMapActualPolygon> = PolygonManagerImpl(),
    renderer: GoogleMapPolygonOverlayRenderer,
) : PolygonController<GoogleMapActualPolygon>(polygonManager, renderer)
