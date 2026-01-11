package com.mapconductor.arcgis.groundimage

import com.mapconductor.arcgis.ArcGISActualGroundImage
import com.mapconductor.core.groundimage.GroundImageController
import com.mapconductor.core.groundimage.GroundImageManager
import com.mapconductor.core.groundimage.GroundImageManagerInterface

class ArcGISGroundImageController(
    groundImageManager: GroundImageManagerInterface<ArcGISActualGroundImage> = GroundImageManager(),
    renderer: ArcGISGroundImageOverlayRenderer,
) : GroundImageController<ArcGISActualGroundImage>(groundImageManager, renderer)

