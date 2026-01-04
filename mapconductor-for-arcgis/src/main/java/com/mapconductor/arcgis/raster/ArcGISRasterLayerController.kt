package com.mapconductor.arcgis.raster

import com.arcgismaps.mapping.layers.Layer
import com.mapconductor.core.raster.RasterLayerController
import com.mapconductor.core.raster.RasterLayerManagerInterface
import com.mapconductor.core.raster.RasterLayerManager

class ArcGISRasterLayerController(
    rasterLayerManager: RasterLayerManagerInterface<Layer> = RasterLayerManager(),
    renderer: ArcGISRasterLayerOverlayRenderer,
) : RasterLayerController<Layer>(rasterLayerManager, renderer)
