package com.mapconductor.maplibre

import com.mapconductor.core.circle.CircleCapableInterface
import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.marker.MarkerCapableInterface
import com.mapconductor.core.polygon.PolygonCapableInterface
import com.mapconductor.core.polyline.PolylineCapableInterface
import com.mapconductor.core.raster.RasterLayerCapableInterface

interface MapLibreViewControllerInterface :
    MapViewControllerInterface,
    MarkerCapableInterface,
    PolylineCapableInterface,
    PolygonCapableInterface,
    CircleCapableInterface,
    RasterLayerCapableInterface {
    fun setMapDesignType(value: MapLibreMapDesignTypeInterface)

    fun setMapDesignTypeChangeListener(listener: MapLibreDesignTypeChangeHandler)
}
