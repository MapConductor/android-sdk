package com.mapconductor.arcgis.polyline

import com.mapconductor.arcgis.ArcGISActualPolyline
import com.mapconductor.core.polyline.PolylineController
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.core.polyline.PolylineManagerImpl

class ArcGISPolylineOverlayController(
    polylineManager: PolylineManager<ArcGISActualPolyline> = PolylineManagerImpl(),
    override val renderer: ArcGISPolylineOverlayRenderer,
) : PolylineController<ArcGISActualPolyline>(polylineManager, renderer)
