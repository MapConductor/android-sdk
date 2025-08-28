package com.mapconductor.here.marker

import com.here.sdk.core.Metadata
import com.here.sdk.mapview.MapMarker
import com.mapconductor.core.calculateZIndex
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.here.HereActualMarker
import com.mapconductor.here.HereViewHolder
import com.mapconductor.here.toAnchor2D
import com.mapconductor.here.toGeoCoordinates
import com.mapconductor.here.toMapImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HereMarkerRenderer(
    holder: HereViewHolder,
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractMarkerOverlayRenderer<
        HereViewHolder,
        HereActualMarker,
    >(
        holder = holder,
        coroutine = coroutine,
    ) {
    override fun setMarkerPosition(
        markerEntity: MarkerEntity<HereActualMarker>,
        position: GeoPoint,
    ) {
        markerEntity.marker.coordinates = position.toGeoCoordinates()
    }

    override suspend fun onAdd(data: List<MarkerOverlayRenderer.AddParams>): List<HereActualMarker?> {
        val markers =
            withContext(coroutine.coroutineContext) {
                data.map { params ->
                    val marker =
                        MapMarker(
                            GeoPoint.from(params.state.position).toGeoCoordinates(),
                            params.bitmapIcon.toMapImage(),
                            params.bitmapIcon.toAnchor2D(),
                        ).apply {
                            drawOrder = calculateZIndex(params.state.position).toInt()
                            metadata =
                                Metadata().apply {
                                    setString("id", params.state.id)
                                }
                        }
                    return@map marker
                }
            }

        coroutine.launch {
            holder.mapView.mapScene.addMapMarkers(markers)
        }
        return markers
    }

    override suspend fun onRemove(data: List<MarkerEntity<HereActualMarker>>) {
        coroutine.launch {
            val markers: List<HereActualMarker> = data.map { params -> params.marker }
            holder.map.removeMapMarkers(markers)
        }
    }

    override suspend fun onPostProcess() {
        // Do nothing here
    }

    override suspend fun onChange(
        changes: List<MarkerOverlayRenderer.ChangeParams<HereActualMarker>>,
    ): List<HereActualMarker?> =
        changes.map { params ->
            val prevFinger = params.prev.fingerPrint
            val currFinger = params.current.fingerPrint
            if (currFinger.icon != prevFinger.icon) {
                params.current.marker.image = params.bitmapIcon.toMapImage()
                params.current.marker.anchor = params.bitmapIcon.toAnchor2D()
            }
            if (params.current.state.position != params.prev.state.position) {
                params.current.marker.coordinates =
                    GeoPoint.from(params.current.state.position).toGeoCoordinates()
            }

            // Hereはマーカーを再作成しなくてよいので、同じマーカーのインスタンスを返す
            params.current.marker
        }
}
