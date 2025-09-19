package com.mapconductor.googlemaps.marker

import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLng
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private val background = CoroutineScope(Dispatchers.Default)

    override suspend fun onAdd(data: List<MarkerOverlayRenderer.AddParams>): List<GoogleMapActualMarker?> {
        val results = mutableListOf<Marker>()
        val deferred = CompletableDeferred<Unit>()

        flow {
            data.forEach { params ->
                val bitmapDescriptor = BitmapDescriptorCache.fromBitmap(params.bitmapIcon.bitmap)
                val options =
                    MarkerOptions()
                        .position(GeoPoint.from(params.state.position).toLatLng())
                        .anchor(
                            params.bitmapIcon.anchor.x,
                            params.bitmapIcon.anchor.y,
                        ).icon(bitmapDescriptor)
                        .draggable(params.state.draggable)

                emit(Pair(params.state.id, options))
            }
        }.onEach { options ->
            coroutine.launch {
                val marker =
                    holder.map.addMarker(options.second)!!.apply {
                        tag = options.first
                    }
                results.add(marker)
                if (results.size == data.size) {
                    deferred.complete(Unit) // 完了シグナル
                }
            }
        }.launchIn(background)

        deferred.await() // 全部揃うまで待機
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
                    val bitmapDescriptor = BitmapDescriptorCache.fromBitmap(params.bitmapIcon.bitmap)
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
