package com.mapconductor.here

import androidx.compose.ui.geometry.Offset
import com.here.sdk.core.Point2D
import com.here.sdk.mapview.MapRenderMode
import com.here.sdk.mapview.MapScene
import com.here.sdk.mapview.MapView
import com.here.sdk.mapview.MapViewOptions
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapViewHolder
import android.content.Context

internal class HereViewHolderImpl(
    override val mapView: MapView,
    override val map: MapScene,
) : MapViewHolder<MapView, MapScene> {

    override fun toScreenOffset(position: GeoPoint): Offset? {
        val result =
            mapView.geoToViewCoordinates(
                GeoPointImpl.from(position).toGeoCoordinates(),
            ) ?: return null

        return Offset(
            x = result.x.toFloat(),
            y = result.y.toFloat(),
        )
    }

    override suspend fun fromScreenOffset(offset: Offset): GeoPointImpl? =
        mapView
            .viewToGeoCoordinates(
                Point2D(offset.x.toDouble(), offset.y.toDouble()),
            )?.toGeoPoint()

    override fun fromScreenOffsetSync(offset: Offset): GeoPointImpl? =
        mapView
            .viewToGeoCoordinates(
                Point2D(offset.x.toDouble(), offset.y.toDouble()),
            )?.toGeoPoint()

}
