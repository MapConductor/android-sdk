package com.mapconductor.googlemaps.groundimage

import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.mapconductor.core.controller.OverlayRenderer
import com.mapconductor.core.groundimage.GroundImageEntity
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.googlemaps.GoogleMapActualGroundImage
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLngBounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleMapGroundImageRenderer(
    val holder: GoogleMapViewHolder,
    val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : OverlayRenderer<GoogleMapActualGroundImage, GroundImageState, GroundImageEntity<GoogleMapActualGroundImage>> {
    override suspend fun onAdd(data: List<GroundImageState>): List<GoogleMapActualGroundImage?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext data.map { state ->
                val bounds = state.bounds.toLatLngBounds() ?: return@withContext emptyList()
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
        }
    }

    override suspend fun onRemove(data: List<GroundImageEntity<GoogleMapActualGroundImage>>) {
        coroutine.launch {
            data.forEach { params -> params.groundImage.remove() }
        }
    }

    override suspend fun onPostProcess() {
        // Do nothing here
    }

    override suspend fun onChange(
        data: List<OverlayRenderer.ChangeParams<GroundImageEntity<GoogleMapActualGroundImage>>>,
    ): List<GroundOverlay?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext data.map { params ->
                val groundOverlay = params.current.groundImage
                val finger = params.current.fingerPrint
                val prevFinger = params.prev.fingerPrint
                if (finger.bounds != prevFinger.bounds) {
                    params.current.state.bounds.toLatLngBounds()?.let {
                        groundOverlay.setPositionFromBounds(it)
                    }
                }
                groundOverlay.transparency = 1.0f - params.current.state.opacity
                if (finger.image != prevFinger.image) {
                    val bitmap =
                        params.current.state.image
                            .toBitmap()
                    val bitmapDesc = BitmapDescriptorFactory.fromBitmap(bitmap)
                    groundOverlay.setImage(bitmapDesc)
                }
                return@map groundOverlay
            }
        }
    }
}
