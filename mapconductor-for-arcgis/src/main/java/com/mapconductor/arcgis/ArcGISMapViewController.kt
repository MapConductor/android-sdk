package com.mapconductor.arcgis.map

import com.mapconductor.core.circle.CircleCapable
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.marker.MarkerCapable
import com.mapconductor.core.polygon.PolygonCapable
import com.mapconductor.core.polyline.PolylineCapable
import com.mapconductor.core.raster.RasterLayerCapable

typealias ArcGISDesignTypeChangeHandler = (ArcGISDesignType) -> Unit

interface ArcGISMapViewController :
    MapViewController,
    MarkerCapable,
    PolylineCapable,
    PolygonCapable,
    CircleCapable,
    RasterLayerCapable {
    fun setMapDesignType(value: ArcGISDesignType)

    fun setMapDesignTypeChangeListener(listener: ArcGISDesignTypeChangeHandler)
}
