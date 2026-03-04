package com.mapconductor.googlemaps.raster

import com.google.android.gms.maps.model.TileOverlay
import com.mapconductor.core.raster.RasterLayerController
import com.mapconductor.core.raster.RasterLayerManager
import com.mapconductor.core.raster.RasterLayerManagerInterface

class GoogleMapRasterLayerController(
    rasterLayerManager: RasterLayerManagerInterface<TileOverlay> = RasterLayerManager(),
    renderer: GoogleMapRasterLayerOverlayRenderer,
) : RasterLayerController<TileOverlay>(rasterLayerManager, renderer)
