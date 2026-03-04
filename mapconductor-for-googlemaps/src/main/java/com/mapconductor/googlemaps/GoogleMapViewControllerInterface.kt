package com.mapconductor.googlemaps

import com.mapconductor.core.circle.CircleCapableInterface
import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.groundimage.GroundImageCapableInterface
import com.mapconductor.core.marker.MarkerCapableInterface
import com.mapconductor.core.polygon.PolygonCapableInterface
import com.mapconductor.core.polyline.PolylineCapableInterface
import com.mapconductor.core.raster.RasterLayerCapableInterface

typealias GoogleMapDesignTypeChangeHandler = (GoogleMapDesignType) -> Unit

interface GoogleMapViewControllerInterface :
    MapViewControllerInterface,
    GroundImageCapableInterface,
    PolygonCapableInterface,
    MarkerCapableInterface,
    PolylineCapableInterface,
    CircleCapableInterface,
    RasterLayerCapableInterface {
    fun setMapDesignType(value: GoogleMapDesignType)

    fun setMapDesignTypeChangeListener(listener: GoogleMapDesignTypeChangeHandler)
}
