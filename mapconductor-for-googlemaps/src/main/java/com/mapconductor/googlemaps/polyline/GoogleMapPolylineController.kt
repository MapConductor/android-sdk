package com.mapconductor.googlemaps.polyline

import com.mapconductor.core.polyline.PolylineController
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.core.polyline.PolylineManagerInterface
import com.mapconductor.googlemaps.GoogleMapActualPolyline

class GoogleMapPolylineController(
    polylineManager: PolylineManagerInterface<GoogleMapActualPolyline> = PolylineManager(),
    renderer: GoogleMapPolylineOverlayRenderer,
) : PolylineController<GoogleMapActualPolyline>(polylineManager, renderer)
