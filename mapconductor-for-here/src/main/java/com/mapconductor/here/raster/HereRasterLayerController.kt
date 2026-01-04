package com.mapconductor.here.raster

import com.mapconductor.core.raster.RasterLayerController
import com.mapconductor.core.raster.RasterLayerManagerInterface
import com.mapconductor.core.raster.RasterLayerManager

class HereRasterLayerController(
    rasterLayerManager: RasterLayerManagerInterface<HereRasterLayerHandle> = RasterLayerManager(),
    renderer: HereRasterLayerOverlayRenderer,
) : RasterLayerController<HereRasterLayerHandle>(rasterLayerManager, renderer)
