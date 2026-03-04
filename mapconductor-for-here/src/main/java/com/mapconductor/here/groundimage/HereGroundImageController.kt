package com.mapconductor.here.groundimage

import com.mapconductor.core.groundimage.GroundImageController
import com.mapconductor.core.groundimage.GroundImageManager
import com.mapconductor.core.groundimage.GroundImageManagerInterface
import com.mapconductor.here.HereActualGroundImage

class HereGroundImageController(
    groundImageManager: GroundImageManagerInterface<HereActualGroundImage> = GroundImageManager(),
    renderer: HereGroundImageOverlayRenderer,
) : GroundImageController<HereActualGroundImage>(groundImageManager, renderer)
