package com.mapconductor.here.groundimage

import com.here.sdk.mapview.MapImage
import com.mapconductor.core.groundimage.AbstractGroundImageRenderer
import com.mapconductor.core.groundimage.GroundImageEntity
import com.mapconductor.core.groundimage.GroundImageOverlayManager
import com.mapconductor.core.groundimage.GroundImageOverlayManagerImpl
import com.mapconductor.core.groundimage.GroundImageRenderer
import com.mapconductor.core.groundimage.GroundImageRendererFactory
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.here.HereMapViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newCoroutineContext
import kotlinx.coroutines.withContext

class DefaultHereMapGroundImageRenderer : GroundImageRendererFactory<MapImage> {
    override fun create(
        onAdd: suspend (List<GroundImageState>) -> List<MapImage?>,
        onChange: suspend (List<GroundImageRenderer.UpdateParams<MapImage>>) -> List<MapImage?>,
        onRemove: suspend (List<GroundImageEntity<MapImage>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?
    ): GroundImageOverlayManager<MapImage> =
        GroundImageOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class HereMapGroundImageRenderer(
    override val holder: HereMapViewHolder,
    override val coroutine: CoroutineScope
) : AbstractGroundImageRenderer<MapImage>() {
    override suspend fun addGroundImages(newGroundImages: List<GroundImageState>): List<MapImage?> {
        return withContext(coroutine.newCoroutineContext){
            return@withContext newGroundImages.map { state ->
                val bounds = state.bounds ?: return@withContext emptyList()
                val image =
                val alpha =
                val options =
            }
        }
    }

    override suspend fun changeGroundImages(changes: List<GroundImageRenderer.UpdateParams<MapImage>>): List<MapImage?> {
        return emptyList()
    }

    override suspend fun removeGroundImages(removeEntities: List<GroundImageEntity<MapImage>>) {
    }
}
