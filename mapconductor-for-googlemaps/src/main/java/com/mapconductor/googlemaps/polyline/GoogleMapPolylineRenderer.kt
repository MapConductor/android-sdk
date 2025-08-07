package com.mapconductor.googlemaps.polyline

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polyline.AbstractPolylineRenderer
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineOverlayManagerImpl
import com.mapconductor.core.polyline.PolylineRenderer.UpdateParams
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultGoogleMapPolylineRenderer : PolylineRendererFactory<Polyline> {
    override fun create(
        onAdd: suspend (List<PolylineState>) -> List<Polyline?>,
        onChange: suspend (List<UpdateParams<Polyline>>) -> List<Polyline?>,
        onRemove: suspend (List<PolylineEntity<Polyline>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): PolylineOverlayManager<Polyline> =
        PolylineOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class GoogleMapPolylineRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractPolylineRenderer<Polyline>() {
    override suspend fun addPolylines(newLines: List<PolylineState>): List<Polyline?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext newLines.map { state ->
                val points = state.points.map { GeoPoint.from(it).toLatLng() }
                val options =
                    PolylineOptions()
                        .addAll(points)
                        .color(state.strokeColor.toArgb())
                        .width(ResourceProvider.dpToPx(state.strokeWidth).toFloat())
                        .clickable(false)
                holder.map.addPolyline(options).also {
                    it.tag = state.id
                }
            }
        }
    }

    override suspend fun removePolylines(removeEntities: List<PolylineEntity<Polyline>>) {
        coroutine.launch {
            removeEntities.forEach { params -> params.polyline.remove() }
        }
    }

    override suspend fun changePolylines(changes: List<UpdateParams<Polyline>>): List<Polyline> {
        return withContext(coroutine.coroutineContext) {
            return@withContext changes.map { params ->
                val polyline = params.entity.polyline
                val finger = params.entity.fingerPrint
                val prevFinger = params.prevEntity.fingerPrint
//                if (finger.points != prevFinger.points) {
                val points =
                    params.entity.state.points
                        .map { GeoPoint.from(it).toLatLng() }
                polyline.points = points
//                }
//                printPoints("change", params.entity.state.points)
                polyline.width = ResourceProvider.dpToPx(params.entity.state.strokeWidth).toFloat()
                polyline.color =
                    params.entity.state.strokeColor
                        .toArgb()
                return@map polyline
            }
        }
    }
}
