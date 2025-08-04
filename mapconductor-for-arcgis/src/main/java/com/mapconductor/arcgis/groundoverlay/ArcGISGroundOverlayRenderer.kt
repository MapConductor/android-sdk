package com.mapconductor.arcgis.groundoverlay

import com.arcgismaps.mapping.view.Graphic
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.core.groundimage.AbstractGroundImageRenderer
import com.mapconductor.core.groundimage.GroundImageEntity
import com.mapconductor.core.groundimage.GroundImageRenderer
import com.mapconductor.core.groundimage.GroundImageState
import kotlinx.coroutines.CoroutineScope

class ArcGISGroundImageRenderer(
        override val holder: ArcGISMapViewHolder,
        override val coroutine: CoroutineScope
    ) : AbstractGroundImageRenderer<Graphic>() {
    override suspend fun addGroundImages(newGroundImages: List<GroundImageState>): List<Graphic?> {
        return emptyList()
    }

    override suspend fun changeGroundImages(changes: List<GroundImageRenderer.UpdateParams<Graphic>>): List<Graphic?> {
        return emptyList()
    }

    override suspend fun removeGroundImages(removeEntities: List<GroundImageEntity<Graphic>>) {
    }
}
