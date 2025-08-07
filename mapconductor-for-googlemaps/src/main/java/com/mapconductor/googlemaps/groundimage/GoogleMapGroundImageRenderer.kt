package com.mapconductor.googlemaps.groundimage

import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.groundimage.AbstractGroundImageRenderer
import com.mapconductor.core.groundimage.GroundImageEntity
import com.mapconductor.core.groundimage.GroundImageOverlayManager
import com.mapconductor.core.groundimage.GroundImageOverlayManagerImpl
import com.mapconductor.core.groundimage.GroundImageRenderer
import com.mapconductor.core.groundimage.GroundImageRendererFactory
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLngBounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultGoogleMapGroundImageRenderer : GroundImageRendererFactory<GroundOverlay> {
    override fun create(
        onAdd: suspend (List<GroundImageState>) -> List<GroundOverlay?>,
        onChange: suspend (List<GroundImageRenderer.UpdateParams<GroundOverlay>>) -> List<GroundOverlay?>,
        onRemove: suspend (List<GroundImageEntity<GroundOverlay>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?
    ): GroundImageOverlayManager<GroundOverlay> =
        GroundImageOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class GoogleMapGroundImageRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractGroundImageRenderer<GroundOverlay>() {
    override suspend fun addGroundImages(newImages: List<GroundImageState>): List<GroundOverlay?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext newImages.map { state ->
                val bounds = state.bounds.toLatLngBounds() ?: return@withContext emptyList()
                val image = BitmapDescriptorFactory.fromBitmap(state.image.toBitmap())
                val alpha = state.alpha
                val options =
                    GroundOverlayOptions()
                        .image(image)
                        .positionFromBounds(bounds)
//                        .transparency(alpha)
                holder.map.addGroundOverlay(options)?.also {
                    it.tag = state.id
                }
            }
        }
    }

    override suspend fun removeGroundImages(removeEntities: List<GroundImageEntity<GroundOverlay>>) {
        coroutine.launch {
            removeEntities.forEach { params -> params.groundImage.remove() }
        }
    }

    override suspend fun changeGroundImages(changes: List<GroundImageRenderer.UpdateParams<GroundOverlay>>): List<GroundOverlay?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext changes.map { params->
                val groundOverlay = params.entity.groundImage
                val finger = params.entity.state.fingerPrint()
                val prevFinger = params.prevEntity.fingerPrint
                if (finger.bounds != prevFinger.bounds) {
                    params.entity.state.bounds.toLatLngBounds()?.let{
                        groundOverlay.setPositionFromBounds(it)
                    }
                }
                groundOverlay.transparency = params.prevEntity.state.alpha
                groundOverlay.setImage(BitmapDescriptorFactory.fromBitmap(params.entity.state.image.toBitmap()))
                return@map groundOverlay
            }
        }
    }

    private fun GeoRectBounds.toLatLngBounds(): LatLngBounds? {
        val sw = southWest ?: return null
        val ne = northEast ?: return null
        return LatLngBounds(
            LatLng(sw.latitude, sw.longitude),
            LatLng(ne.latitude, ne.longitude)
        )
    }
}
