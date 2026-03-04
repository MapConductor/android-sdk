package com.mapconductor.googlemaps.groundimage

import com.mapconductor.core.groundimage.GroundImageController
import com.mapconductor.core.groundimage.GroundImageManager
import com.mapconductor.core.groundimage.GroundImageManagerInterface
import com.mapconductor.googlemaps.GoogleMapActualGroundImage

class GoogleMapGroundImageController(
    groundImageManager: GroundImageManagerInterface<GoogleMapActualGroundImage> = GroundImageManager(),
    renderer: GoogleMapGroundImageOverlayRenderer,
) : GroundImageController<GoogleMapActualGroundImage>(groundImageManager, renderer)
