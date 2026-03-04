package com.mapconductor.mapbox.polyline

import com.mapconductor.core.polyline.PolylineController
import com.mapconductor.core.polyline.PolylineManagerInterface
import com.mapconductor.mapbox.MapboxActualPolyline

class MapboxPolylineController(
    override val renderer: MapboxPolylineOverlayRenderer,
    polylineManager: PolylineManagerInterface<MapboxActualPolyline> = renderer.polylineManager,
) : PolylineController<MapboxActualPolyline>(polylineManager, renderer)
