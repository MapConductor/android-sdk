package com.mapconductor.maplibre

import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.marker.MarkerCapable
import com.mapconductor.core.polygon.PolygonCapable
import com.mapconductor.core.polyline.PolylineCapable

interface MapLibreViewController :
    MapViewController,
    MarkerCapable,
    PolylineCapable,
    PolygonCapable {
    fun setMapDesignType(value: MapLibreMapDesignType)

    fun setMapDesignTypeChangeListener(listener: MapLibreDesignTypeChangeHandler)
}
