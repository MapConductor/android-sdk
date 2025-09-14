package com.mapconductor.googlemaps

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.mapconductor.core.geocell.HexGeocellImpl
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.StaticHolder
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.googlemaps.circle.GoogleMapCircleController
import com.mapconductor.googlemaps.circle.GoogleMapCircleOverlayRenderer
import com.mapconductor.googlemaps.groundimage.GoogleMapGroundImageController
import com.mapconductor.googlemaps.groundimage.GoogleMapGroundImageOverlayRenderer
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
        markerRenderingStrategy: MarkerRenderingStrategy<GoogleMapActualMarker>? = null,
    ): GoogleMapViewControllerImpl {
        val existing = this.get(id)
        if (existing != null) {
            existing.setMapDesignType(GoogleMapDesign.toMapDesignType(options.mapType))
            options.camera?.let { camera ->
                existing.moveCamera(
                    position = camera.toMapCameraPosition(),
                    listener = null,
                )
            }
            return existing
        }

        val holder =
            GoogleMapViewHolderImpl.create(
                context = context,
                options = options,
            )

        val controller =
            GoogleMapViewControllerImpl(
                markerController =
                    getMarkerController(
                        holder = holder,
                        markerRenderingStrategy = markerRenderingStrategy,
                    ),
                groundImageController = getGroundImageController(holder),
                polylineController = getPolylineController(holder),
                polygonController = getPolygonController(holder),
                circleController = getCircleController(holder),
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

    private fun getGroundImageController(holder: GoogleMapViewHolder): GoogleMapGroundImageController {
        val renderer =
            GoogleMapGroundImageOverlayRenderer(
                holder = holder,
            )

        val controller =
            GoogleMapGroundImageController(
                renderer = renderer,
            )
        return controller
    }

    private fun getCircleController(holder: GoogleMapViewHolder): GoogleMapCircleController {
        val renderer =
            GoogleMapCircleOverlayRenderer(
                holder = holder,
            )

        val controller =
            GoogleMapCircleController(
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

    private fun getMarkerController(
        holder: GoogleMapViewHolder,
        markerRenderingStrategy: MarkerRenderingStrategy<GoogleMapActualMarker>? = null,
    ) = GoogleMapMarkerController.create(
        holder = holder,
        renderingStrategy =  markerRenderingStrategy,
    )
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }


