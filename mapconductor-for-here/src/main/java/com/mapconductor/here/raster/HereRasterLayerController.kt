package com.mapconductor.here.raster

import com.mapconductor.core.raster.RasterLayerController
import com.mapconductor.core.raster.RasterLayerManager
import com.mapconductor.core.raster.RasterLayerManagerInterface

class HereRasterLayerController(
    rasterLayerManager: RasterLayerManagerInterface<HereRasterLayerHandle> = RasterLayerManager(),
    renderer: HereRasterLayerOverlayRenderer,
) : RasterLayerController<HereRasterLayerHandle>(rasterLayerManager, renderer)
