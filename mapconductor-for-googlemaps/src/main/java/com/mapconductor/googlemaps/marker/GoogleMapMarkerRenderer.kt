package com.mapconductor.googlemaps.marker

import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLng
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
    private var tiledOverlay: GoogleMapTiledMarkerOverlay? = null

    internal fun getOrCreateTiledOverlay(
        finalTileDownscaleFilter: Boolean = true,
        debugTileOverlay: Boolean = false,
        renderScaleOverride: Int? = null,
        tileSize: Int = 256,
        declutterEnabled: Boolean = false,
        declutterMaxZoomInt: Int = 7,
        declutterMaxMarkersPerTile: Int = 800,
        declutterIconPx: Int = 28,
        declutterCellPx: Int = 8,
        fixedMarkerPixelSize: Boolean = true,
        fixedMarkerPixelSizeReferenceZoom: Int = 10,
    ): GoogleMapTiledMarkerOverlay {
        val existing = tiledOverlay
        if (existing != null) return existing
        return GoogleMapTiledMarkerOverlay(
            map = this.holder.map,
            tileSize = tileSize,
            finalTileDownscaleFilter = finalTileDownscaleFilter,
            debugTileOverlay = debugTileOverlay,
            renderScaleOverride = renderScaleOverride,
            declutterEnabled = declutterEnabled,
            declutterMaxZoomInt = declutterMaxZoomInt,
            declutterMaxMarkersPerTile = declutterMaxMarkersPerTile,
            declutterIconPx = declutterIconPx,
            declutterCellPx = declutterCellPx,
            fixedMarkerPixelSize = fixedMarkerPixelSize,
            fixedMarkerPixelSizeReferenceZoom = fixedMarkerPixelSizeReferenceZoom,
        ).also { tiledOverlay = it }
    }

    internal fun removeTiledOverlay() {
        tiledOverlay?.remove()
        tiledOverlay = null
    }

    override fun setMarkerPosition(
        markerEntity: MarkerEntityInterface<GoogleMapActualMarker>,
        position: GeoPoint,
    ) {
        coroutine.launch {
            markerEntity.marker?.position = position.toLatLng()
        }
    }

    override suspend fun onAdd(
        data: List<MarkerOverlayRendererInterface.AddParamsInterface>,
    ): List<GoogleMapActualMarker?> {
        return withContext(coroutine.coroutineContext) {
            data.map { params ->
                val bitmapDescriptor = BitmapDescriptorCache.fromBitmap(params.bitmapIcon.bitmap)
                val options =
                    MarkerOptions()
                        .position(GeoPoint.from(params.state.position).toLatLng())
                        .anchor(
                            params.bitmapIcon.anchor.x,
                            params.bitmapIcon.anchor.y,
                        ).icon(bitmapDescriptor)
                        .draggable(params.state.draggable)
                val marker =
                    holder.map.addMarker(options)?.also {
                        it.tag = params.state.id
                    }
                return@map marker
            }
        }
    }

    override suspend fun onRemove(data: List<MarkerEntityInterface<GoogleMapActualMarker>>) {
        withContext(coroutine.coroutineContext) {
            data.forEach { params ->
                params.marker?.let { marker ->
                    marker.isVisible = false
                    marker.remove()
                }
            }
        }
    }

    override suspend fun onPostProcess() {
        // Do nothing here
    }

    override suspend fun onChange(
        data: List<MarkerOverlayRendererInterface.ChangeParamsInterface<GoogleMapActualMarker>>,
    ): List<Marker?> =
        withContext(coroutine.coroutineContext) {
            data.map { params ->
                val prevFinger = params.prev.fingerPrint
                val currentFinger = params.current.fingerPrint
                val marker =
                    if (params.prev.marker != null) {
                        params.prev.marker!!
                    } else {
                        val bitmapDescriptor = BitmapDescriptorCache.fromBitmap(params.bitmapIcon.bitmap)
                        val options =
                            MarkerOptions()
                                .position(GeoPoint.from(params.current.state.position).toLatLng())
                                .anchor(
                                    params.bitmapIcon.anchor.x,
                                    params.bitmapIcon.anchor.y,
                                ).icon(bitmapDescriptor)
                                .draggable(params.current.state.draggable)

                        holder.map.addMarker(options)?.apply {
                            tag = params.current.state.id
                        }
                    }
                if (marker == null) return@map null

                if (prevFinger.icon != currentFinger.icon) {
                    val bitmapDescriptor = BitmapDescriptorCache.fromBitmap(params.bitmapIcon.bitmap)
                    marker.setIcon(bitmapDescriptor)
                }
                marker.position =
                    GeoPoint.from(params.current.state.position).toLatLng()
                marker.isVisible = params.current.visible

                // Google Mapsはマーカーを再作成しなくてよいので、同じマーカーのインスタンスを返す
                marker
            }
        }
}
