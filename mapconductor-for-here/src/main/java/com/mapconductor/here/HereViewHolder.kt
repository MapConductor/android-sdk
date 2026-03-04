package com.mapconductor.here

import androidx.compose.ui.geometry.Offset
import com.here.sdk.core.Point2D
import com.here.sdk.mapview.MapScene
import com.here.sdk.mapview.MapView
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapViewHolderInterface

class HereViewHolder(
    override val mapView: MapView,
    override val map: MapScene,
) : MapViewHolderInterface<MapView, MapScene> {
    override fun toScreenOffset(position: GeoPointInterface): Offset? {
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

    override fun fromScreenOffsetSync(offset: Offset): GeoPoint? =
        mapView
            .viewToGeoCoordinates(
                Point2D(offset.x.toDouble(), offset.y.toDouble()),
            )?.toGeoPoint()
}
