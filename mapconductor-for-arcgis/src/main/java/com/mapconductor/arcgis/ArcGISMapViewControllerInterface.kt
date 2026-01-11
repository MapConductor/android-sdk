package com.mapconductor.arcgis.map

import com.mapconductor.core.circle.CircleCapableInterface
import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.groundimage.GroundImageCapableInterface
import com.mapconductor.core.marker.MarkerCapableInterface
import com.mapconductor.core.polygon.PolygonCapableInterface
import com.mapconductor.core.polyline.PolylineCapableInterface
import com.mapconductor.core.raster.RasterLayerCapableInterface

typealias ArcGISDesignTypeChangeHandler = (ArcGISDesignTypeInterface) -> Unit

interface ArcGISMapViewControllerInterface :
    MapViewControllerInterface,
    MarkerCapableInterface,
    PolylineCapableInterface,
    PolygonCapableInterface,
    CircleCapableInterface,
    GroundImageCapableInterface,
    RasterLayerCapableInterface {
    fun setMapDesignType(value: ArcGISDesignTypeInterface)

    fun setMapDesignTypeChangeListener(listener: ArcGISDesignTypeChangeHandler)
}
