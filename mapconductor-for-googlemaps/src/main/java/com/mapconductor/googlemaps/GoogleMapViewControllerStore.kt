package com.mapconductor.googlemaps

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.groundimage.GroundImageController
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.StaticHolder
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.googlemaps.groundimage.GoogleMapGroundImageRenderer
import com.mapconductor.googlemaps.marker.GoogleMapMarkerController
import com.mapconductor.googlemaps.marker.GoogleMapMarkerRenderer
import com.mapconductor.googlemaps.polygon.GoogleMapPolygonController
import com.mapconductor.googlemaps.polygon.GoogleMapPolygonOverlayRenderer
import com.mapconductor.googlemaps.polyline.GoogleMapPolylineController
import com.mapconductor.googlemaps.polyline.GoogleMapPolylineOverlayRenderer
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

typealias GoogleMapViewHolder = MapViewHolder<MapView, GoogleMap>

object GoogleMapViewControllerStore : StaticHolder<GoogleMapViewControllerImpl>() {
    suspend fun getOrCreate(
        context: Context,
        id: String,
        options: GoogleMapOptions,
    ): GoogleMapViewControllerImpl {
        val existing = this.get(id)
        if (existing != null) {
            return existing
        }

        val holder =
            GoogleMapViewHolderImpl.create(
                context = context,
                options = options,
            )

        val controller =
            GoogleMapViewControllerImpl(
                markerController = getMarkerController(holder),
                groundImageController = getGroundImageController(holder),
                polylineController = getPolylineController(holder),
                polygonController = getPolygonController(holder),
                holder = holder,
            )
        this.set(id, controller)

        return controller
    }

    private fun getPolygonController(holder: GoogleMapViewHolder): GoogleMapPolygonController {
        val renderer =
            GoogleMapPolygonOverlayRenderer(
                holder = holder,
            )

        val controller =
            GoogleMapPolygonController(
                renderer = renderer,
            )
        return controller
    }
    private fun getPolylineController(holder: GoogleMapViewHolder): GoogleMapPolylineController {
        val renderer =
            GoogleMapPolylineOverlayRenderer(
                holder = holder,
            )

        val controller =
            GoogleMapPolylineController(
                renderer = renderer,
            )
        return controller
    }

    private fun getMarkerController(holder: GoogleMapViewHolder): GoogleMapMarkerController {
        val hexGeocell =
            HexGeocell(
                projection = WebMercator,
                baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
            )
        val manager = MarkerManager<GoogleMapActualMarker>(hexGeocell)

        val renderer =
            GoogleMapMarkerRenderer(
                holder = holder,
            )

        val markerController =
            GoogleMapMarkerController(
                markerManager = manager,
                renderer = renderer,
            )

        return markerController
    }

    private fun getGroundImageController(
        holder: GoogleMapViewHolder,
    ): GroundImageController<GoogleMapActualGroundImage> {
        val groundImageRenderer =
            GoogleMapGroundImageRenderer(
                holder = holder,
            )

        val groundImageController =
            GroundImageController(
                renderer = groundImageRenderer,
            )

        return groundImageController
    }
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
