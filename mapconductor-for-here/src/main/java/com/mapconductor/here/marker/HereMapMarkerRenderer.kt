package com.mapconductor.here.marker

import com.here.sdk.core.Metadata
import com.here.sdk.mapview.MapMarker
import com.mapconductor.core.calculateZIndex
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.marker.AbstractMarkerRenderer
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayManager
import com.mapconductor.core.marker.MarkerOverlayManagerImpl
import com.mapconductor.core.marker.MarkerRendererFactory
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.UpdateParams
import com.mapconductor.here.HereMapViewHolder
import com.mapconductor.here.toAnchor2D
import com.mapconductor.here.toGeoCoordinates
import com.mapconductor.here.toMapImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultHereMapMarkerRenderer : MarkerRendererFactory<MapMarker> {
    override fun create(
        hexGeocell: HexGeocell,
        onIconAdd: suspend (List<Pair<MarkerState, BitmapIcon>>) -> List<MapMarker?>,
        onIconRemove: suspend (List<MarkerEntity<MapMarker>>) -> Unit,
        onIconChange: suspend (List<UpdateParams<MapMarker>>) -> List<MapMarker>,
        onAnimate: suspend (MarkerEntity<MapMarker>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): MarkerOverlayManager<MapMarker> =
        MarkerOverlayManagerImpl(
            markerManager = MarkerManager(hexGeocell),
            onAdd = onIconAdd,
            onChange = onIconChange,
            onRemove = onIconRemove,
            onPostProcess = onPostProcess,
            onAnimate = onAnimate,
        )
}

class HereMapMarkerRenderer(
    override val holder: HereMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractMarkerRenderer<MapMarker>() {
    override fun setMarkerPosition(
        markerEntity: MarkerEntity<MapMarker>,
        position: GeoPoint,
    ) {
        markerEntity.marker.coordinates = position.toGeoCoordinates()
    }

    override suspend fun addIcons(newMarkers: List<Pair<MarkerState, BitmapIcon>>): List<MapMarker?> {
        val markers =
            withContext(coroutine.coroutineContext) {
                newMarkers.map { params ->
                    val marker =
                        MapMarker(
                            GeoPoint.from(params.first.position).toGeoCoordinates(),
                            params.second.toMapImage(),
                            params.second.toAnchor2D(),
                        ).apply {
                            drawOrder = calculateZIndex(params.first.position).toInt()
                            metadata =
                                Metadata().apply {
                                    setString("id", params.first.id)
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

    override suspend fun removeIcons(removeEntities: List<MarkerEntity<MapMarker>>) {
        coroutine.launch {
            val markers: List<MapMarker> = removeEntities.map { params -> params.marker }
            holder.mapView.mapScene.removeMapMarkers(markers)
        }
    }

    override suspend fun changeIcons(changes: List<UpdateParams<MapMarker>>): List<MapMarker> =
        changes.map { params ->
            val prevFinger = params.prevEntity.fingerPrint
            val currFinger = params.entity.fingerPrint
            if (currFinger.icon != prevFinger.icon) {
                params.entity.marker.image = params.bitmapIcon.toMapImage()
                params.entity.marker.anchor = params.bitmapIcon.toAnchor2D()
            }
            if (params.entity.state.position != params.prevEntity.state.position) {
                params.entity.marker.coordinates =
                    params.entity.state.position
                        .toGeoCoordinates()
            }

            // Hereはマーカーを再作成しなくてよいので、同じマーカーのインスタンスを返す
            params.entity.marker
        }
}
