package com.mapconductor.maplibre

import com.mapconductor.core.controller.MapViewController

interface MapLibreMapViewController :
    MapViewController {
    fun setMapDesignType(value: MapLibreMapDesignType)

    fun setMapDesignTypeChangeListener(listener: MapLibreDesignTypeChangeHandler)
}
