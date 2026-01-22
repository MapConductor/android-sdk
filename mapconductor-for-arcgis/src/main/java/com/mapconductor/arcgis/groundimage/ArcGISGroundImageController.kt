package com.mapconductor.arcgis.groundimage

import com.mapconductor.core.groundimage.GroundImageController
import com.mapconductor.core.groundimage.GroundImageManager

class ArcGISGroundImageController(
    renderer: ArcGISGroundImageOverlayRenderer,
) : GroundImageController<ArcGISGroundImageHandle>(
        groundImageManager = GroundImageManager(),
        renderer = renderer,
    )
