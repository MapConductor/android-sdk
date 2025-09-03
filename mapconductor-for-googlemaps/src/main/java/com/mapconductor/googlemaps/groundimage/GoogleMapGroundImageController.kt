package com.mapconductor.googlemaps.groundimage

import com.mapconductor.core.groundimage.GroundImageController
import com.mapconductor.core.groundimage.GroundImageManager
import com.mapconductor.core.groundimage.GroundImageManagerImpl
import com.mapconductor.googlemaps.GoogleMapActualGroundImage

class GoogleMapGroundImageController(
    groundImageManager: GroundImageManager<GoogleMapActualGroundImage> = GroundImageManagerImpl(),
    renderer: GoogleMapGroundImageOverlayRenderer,
) : GroundImageController<GoogleMapActualGroundImage>(groundImageManager, renderer)
