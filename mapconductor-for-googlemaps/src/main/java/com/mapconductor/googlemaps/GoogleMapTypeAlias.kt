package com.mapconductor.googlemaps

import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.TileOverlay

typealias GoogleMapActualMarker = Marker
typealias GoogleMapActualCircle = Polygon // Using Polygon to support geodesic circles
typealias GoogleMapActualPolyline = Polyline
typealias GoogleMapActualPolygon = Polygon
typealias GoogleMapActualGroundImage = GroundOverlay
typealias GoogleMapActualRasterLayer = TileOverlay
