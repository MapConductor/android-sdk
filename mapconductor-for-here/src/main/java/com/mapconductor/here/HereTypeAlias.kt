package com.mapconductor.here

import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapPolygon
import com.here.sdk.mapview.MapPolyline

typealias HereActualMarker = MapMarker
typealias HereActualCircle = MapPolygon
typealias HereActualPolyline = MapPolyline
typealias HereActualPolygon = List<MapPolygon>
typealias HereActualGroundImage = com.mapconductor.here.groundimage.HereGroundImageHandle
