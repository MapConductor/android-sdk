package com.mapconductor.arcgis

import com.mapconductor.core.circle.CircleCapable
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.marker.MarkerCapable
import com.mapconductor.core.polygon.PolygonCapable
import com.mapconductor.core.polyline.PolylineCapable

typealias ArcGISDesignTypeChangeHandler = (ArcGISDesignType) -> Unit

interface ArcGISMapViewController :
    MapViewController,
    MarkerCapable,
    PolylineCapable,
    PolygonCapable,
    CircleCapable {
    fun setMapDesignType(value: ArcGISDesignType)

    fun setMapDesignTypeChangeListener(listener: ArcGISDesignTypeChangeHandler)
}
