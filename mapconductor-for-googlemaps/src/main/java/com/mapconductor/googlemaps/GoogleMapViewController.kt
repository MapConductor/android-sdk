package com.mapconductor.googlemaps

import com.mapconductor.core.circle.CircleCapable
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.groundimage.GroundImageCapable
import com.mapconductor.core.marker.MarkerCapable
import com.mapconductor.core.polygon.PolygonCapable
import com.mapconductor.core.polyline.PolylineCapable
import com.mapconductor.core.raster.RasterLayerCapable

typealias GoogleMapDesignTypeChangeHandler = (GoogleMapDesignType) -> Unit

interface GoogleMapViewController :
    MapViewController,
    GroundImageCapable,
    PolygonCapable,
    MarkerCapable,
    PolylineCapable,
    CircleCapable,
    RasterLayerCapable {
    fun setMapDesignType(value: GoogleMapDesignType)

    fun setMapDesignTypeChangeListener(listener: GoogleMapDesignTypeChangeHandler)
}
