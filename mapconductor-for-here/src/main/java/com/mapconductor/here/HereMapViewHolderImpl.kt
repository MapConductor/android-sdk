package com.mapconductor.here

import androidx.compose.ui.geometry.Offset
import com.here.sdk.core.Point2D
import com.here.sdk.mapview.HereMap
import com.here.sdk.mapview.MapRenderMode
import com.here.sdk.mapview.MapView
import com.here.sdk.mapview.MapViewOptions
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapViewHolder
import android.content.Context

internal class HereMapViewHolderImpl private constructor(
    override val mapView: MapView,
) : MapViewHolder<MapView, HereMap> {
    override lateinit var map: HereMap

    override fun toScreenOffset(position: IGeoPoint): Offset? {
        val result =
            mapView.geoToViewCoordinates(
                GeoPoint.from(position).toGeoCoordinates(),
            ) ?: return null

        return Offset(
            x = result.x.toFloat(),
            y = result.y.toFloat(),
        )
    }

    override suspend fun fromScreenOffset(offset: Offset): GeoPoint? =
        mapView
            .viewToGeoCoordinates(
                Point2D(offset.x.toDouble(), offset.y.toDouble()),
            )?.toGeoPoint()

    companion object {
        fun create(context: Context): MapViewHolder<MapView, HereMap> {
            // TEXTUREモードにしないとデバイスが回転したときに再描画を適切に行わない
            val viewOptions =
                MapViewOptions().also {
                    it.renderMode = MapRenderMode.TEXTURE
                }

            val mapView =
                MapView(context, viewOptions).apply {
                    onCreate(null)
                    onResume()
                }

            val holder = HereMapViewHolderImpl(mapView)
            holder.map = mapView.hereMap
            return holder
        }
    }
}
