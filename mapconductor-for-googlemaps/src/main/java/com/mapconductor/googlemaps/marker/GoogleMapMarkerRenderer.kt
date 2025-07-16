package com.mapconductor.googlemaps.marker

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.marker.AbstractMarkerRenderer
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayManager
import com.mapconductor.core.marker.MarkerOverlayManagerImpl
import com.mapconductor.core.marker.MarkerRenderer.UpdateParams
import com.mapconductor.core.marker.MarkerRendererFactory
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultGoogleMapMarkerRenderer : MarkerRendererFactory<Marker> {
    override fun create(
        hexGeocell: HexGeocell,
        onIconAdd: suspend (List<Pair<MarkerState, BitmapIcon>>) -> List<Marker?>,
        onIconRemove: suspend (List<MarkerEntity<Marker>>) -> Unit,
        onIconChange: suspend (List<UpdateParams<Marker>>) -> List<Marker>,
        onAnimate: suspend (MarkerEntity<Marker>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): MarkerOverlayManager<Marker> =
        MarkerOverlayManagerImpl(
            markerManager = MarkerManager(hexGeocell),
            onRemove = onIconRemove,
            onAdd = onIconAdd,
            onChange = onIconChange,
            onPostProcess = onPostProcess,
            onAnimate = onAnimate,
        )
}

class GoogleMapMarkerRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractMarkerRenderer<Marker>() {
    override fun setMarkerPosition(
        markerEntity: MarkerEntity<Marker>,
        position: GeoPoint,
    ) {
        markerEntity.marker.position = position.toLatLng()
    }

    override suspend fun addIcons(newMarkers: List<Pair<MarkerState, BitmapIcon>>): List<Marker?> {
        return withContext(coroutine.coroutineContext) {
            newMarkers.map { params ->
                val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(params.second.bitmap)
                val options =
                    MarkerOptions()
                        .position(GeoPoint.from(params.first.position).toLatLng())
                        .anchor(
                            params.second.anchor.x,
                            params.second.anchor.y,
                        )
                        .icon(bitmapDescriptor)
                        .draggable(params.first.draggable)
                val marker =
                    holder.map.addMarker(options)?.also {
                        it.tag = params.first.id
                    }
                return@map marker
            }
        }
    }

    override suspend fun removeIcons(removeEntities: List<MarkerEntity<Marker>>) {
        coroutine.launch {
            removeEntities.forEach { params -> params.marker.remove() }
        }
    }

    override suspend fun changeIcons(changes: List<UpdateParams<Marker>>): List<Marker> =
        withContext(coroutine.coroutineContext) {
            changes.map { params ->
                val prevFinger = params.prevEntity.fingerPrint
                val currentFinger = params.entity.fingerPrint
                if (prevFinger.icon != currentFinger.icon) {
                    val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(params.bitmapIcon.bitmap)
                    params.entity.marker.setIcon(bitmapDescriptor)
                }
                if (params.entity.state.position != params.prevEntity.state.position) {
                    params.entity.marker.position =
                        params.entity.state.position
                            .toLatLng()
                }

                // Google Mapsはマーカーを再作成しなくてよいので、同じマーカーのインスタンスを返す
                params.entity.marker
            }
        }
}
