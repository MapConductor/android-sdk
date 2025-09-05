package com.mapconductor.googlemaps.polyline

import com.mapconductor.core.polyline.PolylineController
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.core.polyline.PolylineManagerImpl
import com.mapconductor.googlemaps.GoogleMapActualPolyline

class GoogleMapPolylineController(
    polylineManager: PolylineManager<GoogleMapActualPolyline> = PolylineManagerImpl(),
    renderer: GoogleMapPolylineOverlayRenderer,
) : PolylineController<GoogleMapActualPolyline>(polylineManager, renderer)
