package com.mapconductor.googlemaps.groundimage

import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.mapconductor.core.groundimage.AbstractGroundImageOverlayRenderer
import com.mapconductor.core.groundimage.GroundImageEntityInterface
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.googlemaps.GoogleMapActualGroundImage
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLngBounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleMapGroundImageOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractGroundImageOverlayRenderer<GoogleMapActualGroundImage>() {
    override suspend fun createGroundImage(state: GroundImageState): GoogleMapActualGroundImage? =
        withContext(coroutine.coroutineContext) {
            val bounds = state.bounds.toLatLngBounds() ?: return@withContext null
            val image = BitmapDescriptorFactory.fromBitmap(state.image.toBitmap())
            val opacity = state.opacity
            val options =
                GroundOverlayOptions()
                    .image(image)
                    .positionFromBounds(bounds)
                    .transparency(1.0f - opacity)
            holder.map.addGroundOverlay(options)?.also {
                it.tag = state.id
            }
        }

    override suspend fun removeGroundImage(entity: GroundImageEntityInterface<GoogleMapActualGroundImage>) {
        coroutine.launch {
            entity.groundImage.remove()
        }
    }

    override suspend fun updateGroundImageProperties(
        groundImage: GoogleMapActualGroundImage,
        current: GroundImageEntityInterface<GoogleMapActualGroundImage>,
        prev: GroundImageEntityInterface<GoogleMapActualGroundImage>,
    ): GoogleMapActualGroundImage? =
        withContext(coroutine.coroutineContext) {
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint
            if (finger.bounds != prevFinger.bounds) {
                current.state.bounds.toLatLngBounds()?.let {
                    groundImage.setPositionFromBounds(it)
                }
            }
            groundImage.transparency = 1.0f - current.state.opacity
            if (finger.image != prevFinger.image) {
                val bitmap =
                    current.state.image
                        .toBitmap()
                val bitmapDesc = BitmapDescriptorFactory.fromBitmap(bitmap)
                groundImage.setImage(bitmapDesc)
            }
            groundImage
        }
}
