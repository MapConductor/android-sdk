package com.mapconductor.arcgis.raster

import com.arcgismaps.mapping.layers.Layer
import com.mapconductor.core.raster.RasterLayerController
import com.mapconductor.core.raster.RasterLayerManager
import com.mapconductor.core.raster.RasterLayerManagerImpl

class ArcGISRasterLayerController(
    rasterLayerManager: RasterLayerManager<Layer> = RasterLayerManagerImpl(),
    renderer: ArcGISRasterLayerOverlayRenderer,
) : RasterLayerController<Layer>(rasterLayerManager, renderer)
