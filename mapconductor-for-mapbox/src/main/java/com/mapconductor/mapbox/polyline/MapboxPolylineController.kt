package com.mapconductor.mapbox.polyline

import com.mapconductor.core.polyline.PolylineController
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.mapbox.MapboxActualPolyline

class MapboxPolylineController(
    override val renderer: MapboxPolylineOverlayRenderer,
    polylineManager: PolylineManager<MapboxActualPolyline> = renderer.polylineManager,
) : PolylineController<MapboxActualPolyline>(polylineManager, renderer)
