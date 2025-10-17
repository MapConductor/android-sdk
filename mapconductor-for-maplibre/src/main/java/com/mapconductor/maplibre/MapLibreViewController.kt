package com.mapconductor.maplibre

import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.marker.MarkerCapable

interface MapLibreViewController :
    MapViewController,
    MarkerCapable {
    fun setMapDesignType(value: MapLibreMapDesignType)

    fun setMapDesignTypeChangeListener(listener: MapLibreDesignTypeChangeHandler)
}
