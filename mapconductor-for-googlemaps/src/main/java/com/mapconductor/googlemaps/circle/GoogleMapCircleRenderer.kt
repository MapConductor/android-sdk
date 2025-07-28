package com.mapconductor.googlemaps.circle

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.circle.AbstractCircleRenderer
import com.mapconductor.core.circle.CircleEntity
import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleOverlayManagerImpl
import com.mapconductor.core.circle.CircleRenderer.UpdateParams
import com.mapconductor.core.circle.CircleRendererFactory
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultGoogleMapCircleRenderer : CircleRendererFactory<Circle> {
    override fun create(
        onAdd: suspend (List<CircleState>) -> List<Circle?>,
        onChange: suspend (List<UpdateParams<Circle>>) -> List<Circle?>,
        onRemove: suspend (List<CircleEntity<Circle>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): CircleOverlayManager<Circle> =
        CircleOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class GoogleMapCircleRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractCircleRenderer<Circle>() {
    override suspend fun addCircles(newCircles: List<CircleState>): List<Circle?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext newCircles.map { state ->
                val center = GeoPoint.from(state.center).toLatLng()
                val options =
                    CircleOptions()
                        .center(center)
                        .radius(state.radiusMeters)
                        .strokeColor(state.strokeColor.toArgb())
                        .strokeWidth(ResourceProvider.dpToPx(state.strokeWidth).toFloat())
                        .fillColor(state.fillColor.toArgb())
                        .clickable(true)
                holder.map.addCircle(options).also {
                    it.tag = state.id
                }
            }
        }
    }

    override suspend fun removeCircles(removeEntities: List<CircleEntity<Circle>>) {
        coroutine.launch {
            removeEntities.forEach { entity -> entity.circle.remove() }
        }
    }

    override suspend fun changeCircle(changes: List<UpdateParams<Circle>>): List<Circle> {
        return withContext(coroutine.coroutineContext) {
            return@withContext changes.map { params ->
                val circle = params.entity.circle
                val finger = params.entity.state.fingerPrint()
                val prevFinger = params.prevEntity.state.fingerPrint()

                if (finger.center != prevFinger.center) {
                    circle.center = GeoPoint.from(params.entity.state.center).toLatLng()
                }
                if (finger.radius != prevFinger.radius) {
                    circle.radius = params.entity.state.radiusMeters
                }
                if (finger.strokeColor != prevFinger.strokeColor) {
                    circle.strokeColor = params.entity.state.strokeColor.toArgb()
                }
                if (finger.strokeWidth != prevFinger.strokeWidth) {
                    circle.strokeWidth = ResourceProvider.dpToPx(params.entity.state.strokeWidth).toFloat()
                }
                if (finger.fillColor != prevFinger.fillColor) {
                    circle.fillColor = params.entity.state.fillColor.toArgb()
                }
                return@map circle
            }
        }
    }
}
