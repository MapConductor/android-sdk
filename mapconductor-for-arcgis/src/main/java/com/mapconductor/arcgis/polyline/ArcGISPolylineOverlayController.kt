package com.mapconductor.arcgis.polyline

import com.mapconductor.arcgis.ArcGISActualPolyline
import com.mapconductor.core.polyline.PolylineController
import com.mapconductor.core.polyline.PolylineManagerInterface
import com.mapconductor.core.polyline.PolylineManager

class ArcGISPolylineOverlayController(
    polylineManager: PolylineManagerInterface<ArcGISActualPolyline> = PolylineManager(),
    override val renderer: ArcGISPolylineOverlayRenderer,
) : PolylineController<ArcGISActualPolyline>(polylineManager, renderer)
