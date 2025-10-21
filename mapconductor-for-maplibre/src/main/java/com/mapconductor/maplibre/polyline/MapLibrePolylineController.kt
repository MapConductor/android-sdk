package com.mapconductor.maplibre.polyline

import com.mapconductor.core.polyline.PolylineController
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.maplibre.MapLibreActualPolyline

class MapLibrePolylineController(
    override val renderer: MapLibrePolylineOverlayRenderer,
    polylineManager: PolylineManager<MapLibreActualPolyline> = renderer.polylineManager,
) : PolylineController<MapLibreActualPolyline>(polylineManager, renderer)

