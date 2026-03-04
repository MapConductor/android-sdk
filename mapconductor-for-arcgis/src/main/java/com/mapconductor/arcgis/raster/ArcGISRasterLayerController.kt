package com.mapconductor.arcgis.raster

import com.arcgismaps.mapping.layers.Layer
import com.mapconductor.core.raster.RasterLayerController
import com.mapconductor.core.raster.RasterLayerManager
import com.mapconductor.core.raster.RasterLayerManagerInterface

class ArcGISRasterLayerController(
    rasterLayerManager: RasterLayerManagerInterface<Layer> = RasterLayerManager(),
    renderer: ArcGISRasterLayerOverlayRenderer,
) : RasterLayerController<Layer>(rasterLayerManager, renderer)
