package com.mapconductor.core.groundimage

import com.mapconductor.core.map.MapViewHolder
import kotlinx.coroutines.CoroutineScope

interface GroundImageRendererFactory<ActualGroundImage> {
    fun create(
        onAdd: suspend (List<GroundImageState>) -> List<ActualGroundImage?>,
        onChange: suspend (List<GroundImageRenderer.UpdateParams<ActualGroundImage>>) -> List<ActualGroundImage?>,
        onRemove: suspend (List<GroundImageEntity<ActualGroundImage>>) -> Unit,
        onPostProcess: (suspend () -> Unit)? = null,
    ): GroundImageOverlayManager<ActualGroundImage>
}

interface GroundImageRenderer<ActualGroundImage> {
    interface UpdateParams<ActualGroundImage> {
        val entity: GroundImageEntity<ActualGroundImage>
        val prevEntity: GroundImageEntity<ActualGroundImage>
    }

    fun init(groundImageOverlayManager: GroundImageOverlayManager<ActualGroundImage>)

    suspend fun addGroundImages(newGroundImages: List<GroundImageState>): List<ActualGroundImage?>

    suspend fun removeGroundImages(removeEntities: List<GroundImageEntity<ActualGroundImage>>)

    suspend fun changeGroundImages(changes: List<UpdateParams<ActualGroundImage>>): List<ActualGroundImage?>
}

abstract class AbstractGroundImageRenderer<ActualGroundImage> : GroundImageRenderer<ActualGroundImage> {
    protected lateinit var groundImageOverlayManager: GroundImageOverlayManager<ActualGroundImage>
    abstract val holder: MapViewHolder<*, *>
    abstract val coroutine: CoroutineScope

    override fun init(groundImageOverlayManager: GroundImageOverlayManager<ActualGroundImage>) {
        this.groundImageOverlayManager = groundImageOverlayManager
    }
}
