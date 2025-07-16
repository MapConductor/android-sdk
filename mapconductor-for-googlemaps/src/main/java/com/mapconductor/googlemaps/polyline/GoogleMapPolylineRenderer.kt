package com.mapconductor.googlemaps.polyline

import com.google.android.gms.maps.model.Polyline
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.polyline.AbstractPolylineRenderer
import com.mapconductor.googlemaps.GoogleMapViewHolder
import kotlinx.coroutines.CoroutineScope

class GoogleMapPolylineRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope
) : AbstractPolylineRenderer<Polyline>() {

}
