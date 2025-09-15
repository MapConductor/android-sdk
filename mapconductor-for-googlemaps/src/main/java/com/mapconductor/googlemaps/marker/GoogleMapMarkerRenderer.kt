package com.mapconductor.googlemaps.marker

import androidx.compose.ui.util.fastMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLng
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleMapMarkerRenderer(
    holder: GoogleMapViewHolder,
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractMarkerOverlayRenderer<GoogleMapViewHolder, GoogleMapActualMarker>(
        holder = holder,
        coroutine = coroutine,
    ) {
    override fun setMarkerPosition(
        markerEntity: MarkerEntity<GoogleMapActualMarker>,
        position: GeoPoint,
    ) {
        coroutine.launch {
            markerEntity.marker?.position = position.toLatLng()
        }
    }

    override suspend fun onAdd(data: List<MarkerOverlayRenderer.AddParams>): List<GoogleMapActualMarker?> {
        val markerOptions =
            data.map { params ->
                val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(params.bitmapIcon.bitmap)
                val options = MarkerOptions()
                    .position(GeoPoint.from(params.state.position).toLatLng())
                    .anchor(
                        params.bitmapIcon.anchor.x,
                        params.bitmapIcon.anchor.y,
                    )
                    .icon(bitmapDescriptor)
                    .draggable(params.state.draggable)
                Pair(params.state.id, options)
            }

        val results = withContext(coroutine.coroutineContext) {
            markerOptions.fastMap { options ->
                val marker =
                    holder.map.addMarker(options.second)?.also {
                        it.tag = options.first
                    }
                return@fastMap marker
            }
        }
        return results
    }

    override suspend fun onRemove(data: List<MarkerEntity<GoogleMapActualMarker>>) {
        coroutine.launch {
            data.forEach { params -> params.marker?.remove() }
        }
    }

    override suspend fun onPostProcess() {
        // Do nothing here
    }

    override suspend fun onChange(
        changes: List<MarkerOverlayRenderer.ChangeParams<GoogleMapActualMarker>>,
    ): List<Marker> =
        withContext(coroutine.coroutineContext) {
            changes.mapNotNull { params ->
                val prevFinger = params.prev.fingerPrint
                val currentFinger = params.current.fingerPrint
                val marker = params.current.marker ?: return@mapNotNull null
                if (prevFinger.icon != currentFinger.icon) {
                    val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(params.bitmapIcon.bitmap)
                    marker.setIcon(bitmapDescriptor)
                }
                if (params.current.state.position != params.prev.state.position) {
                    marker.position =
                        GeoPoint.from(params.current.state.position).toLatLng()
                }
                marker.isVisible = params.current.visible

                // Google Mapsはマーカーを再作成しなくてよいので、同じマーカーのインスタンスを返す
                marker
            }
        }
}
