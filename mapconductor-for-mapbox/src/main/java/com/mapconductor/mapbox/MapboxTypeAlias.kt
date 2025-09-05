package com.mapconductor.mapbox

import com.mapbox.geojson.Feature

typealias MapboxActualMarker = Feature
typealias MapboxActualCircle = Feature
typealias MapboxActualPolyline = List<Feature>
typealias MapboxActualPolygon = List<Feature>

data class MapboxOutlineAndFill(
    val outline: MapboxActualPolyline?,
    val fill: MapboxActualPolygon?,
)
