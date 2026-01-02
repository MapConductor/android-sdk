package com.mapconductor.maplibre

import com.mapconductor.core.circle.CircleCapable
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.marker.MarkerCapable
import com.mapconductor.core.polygon.PolygonCapable
import com.mapconductor.core.polyline.PolylineCapable
import com.mapconductor.core.raster.RasterLayerCapable

interface MapLibreViewController :
    MapViewController,
    MarkerCapable,
    PolylineCapable,
    PolygonCapable,
    CircleCapable,
    RasterLayerCapable {
    fun setMapDesignType(value: MapLibreMapDesignType)

    fun setMapDesignTypeChangeListener(listener: MapLibreDesignTypeChangeHandler)
}
