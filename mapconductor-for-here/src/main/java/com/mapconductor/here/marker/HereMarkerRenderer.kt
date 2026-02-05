package com.mapconductor.here.marker

import com.here.sdk.core.Metadata
import com.here.sdk.mapview.MapMarker
import com.mapconductor.core.calculateZIndex
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.here.HereActualMarker
import com.mapconductor.here.HereViewHolder
import com.mapconductor.here.toAnchor2D
import com.mapconductor.here.toGeoCoordinates
import com.mapconductor.here.toMapImage
import java.io.Serializable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private fun resolveDrawOrder(state: com.mapconductor.core.marker.MarkerState): Int =
        (state.zIndex ?: calculateZIndex(state.position)).toInt()

    override fun setMarkerPosition(
        markerEntity: MarkerEntityInterface<HereActualMarker>,
        position: GeoPoint,
    ) {
        coroutine.launch {
            markerEntity.marker?.coordinates = position.toGeoCoordinates()
        }
    }

    override suspend fun onAdd(data: List<MarkerOverlayRendererInterface.AddParamsInterface>): List<HereActualMarker?> {
        val markers =
            data.map { params ->
                val marker =
                    MapMarker(
                        GeoPoint.from(params.state.position).toGeoCoordinates(),
                        params.bitmapIcon.toMapImage(),
                        params.bitmapIcon.toAnchor2D(),
                    ).apply {
                        drawOrder = resolveDrawOrder(params.state)
                        metadata =
                            Metadata().apply {
                                // Always include MapConductor marker id
                                setString("mc:id", params.state.id)
                                // Optional user-defined extras from MarkerState.extra
                                putExtras(params.state.extra)
                            }
                    }
                return@map marker
            }

        coroutine.launch {
            holder.mapView.mapScene.addMapMarkers(markers)
        }
        return markers
    }

    override suspend fun onRemove(data: List<MarkerEntityInterface<HereActualMarker>>) {
        coroutine.launch {
            val markers: List<HereActualMarker> = data.mapNotNull { params -> params.marker }
            if (markers.isNotEmpty()) {
                holder.mapView.mapScene.removeMapMarkers(markers)
            }
        }
    }

    override suspend fun onPostProcess() {
        // Do nothing here
    }

    override suspend fun onChange(
        data: List<MarkerOverlayRendererInterface.ChangeParamsInterface<HereActualMarker>>,
    ): List<HereActualMarker?> =
        data.mapNotNull { params ->
            val prevFinger = params.prev.fingerPrint
            val currFinger = params.current.fingerPrint
            if (!params.current.visible) return@mapNotNull null

            val marker = params.current.marker ?: return@mapNotNull null
            if (currFinger.icon != prevFinger.icon) {
                marker.image = params.bitmapIcon.toMapImage()
                marker.anchor = params.bitmapIcon.toAnchor2D()
            }
            marker.coordinates =
                GeoPoint.from(params.current.state.position).toGeoCoordinates()
            marker.drawOrder = resolveDrawOrder(params.current.state)

            // Hereはマーカーを再作成しなくてよいので、同じマーカーのインスタンスを返す
            marker
        }
}

// Convert MarkerState.extra into HERE Metadata entries.
// Supports Map<String, Any?> by best-effort type mapping; otherwise stores value as string under key "mc:extra".
private fun Metadata.putExtras(extra: Serializable?) {
    if (extra == null) return
    when (extra) {
        is Map<*, *> -> {
            extra.forEach { (k, v) ->
                val key = k?.toString() ?: return@forEach
                when (v) {
                    null -> setString(key, "null")
                    is String -> setString(key, v)
                    is Int -> setInteger(key, v)
                    is Long -> setInteger(key, v.toInt())
                    is Float -> setDouble(key, v.toDouble())
                    is Double -> setDouble(key, v)
                    is Boolean -> setString(key, v.toString())
                    else -> setString(key, v.toString())
                }
            }
        }
        else -> {
            setString("mc:extra", extra.toString())
        }
    }
}
