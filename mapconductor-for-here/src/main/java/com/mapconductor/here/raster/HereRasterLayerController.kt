package com.mapconductor.here.raster

import com.mapconductor.core.raster.RasterLayerController
import com.mapconductor.core.raster.RasterLayerManager
import com.mapconductor.core.raster.RasterLayerManagerImpl

class HereRasterLayerController(
    rasterLayerManager: RasterLayerManager<HereRasterLayerHandle> = RasterLayerManagerImpl(),
    renderer: HereRasterLayerOverlayRenderer,
) : RasterLayerController<HereRasterLayerHandle>(rasterLayerManager, renderer)
