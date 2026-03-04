package com.mapconductor.mapbox

import com.mapbox.geojson.Feature

typealias MapboxActualMarker = Feature
typealias MapboxActualCircle = Feature
typealias MapboxActualPolyline = List<Feature>
typealias MapboxActualPolygon = List<Feature>
typealias MapboxActualGroundImage = com.mapconductor.mapbox.groundimage.MapboxGroundImageHandle
