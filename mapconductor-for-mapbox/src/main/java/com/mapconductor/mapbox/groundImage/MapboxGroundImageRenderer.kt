package com.mapconductor.mapbox.groundImage

import com.mapbox.geojson.Feature
import com.mapconductor.core.groundimage.AbstractGroundImageRenderer
import com.mapconductor.core.groundimage.GroundImageEntity
import com.mapconductor.core.groundimage.GroundImageRenderer
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.mapbox.MapboxMapViewHolder
import kotlinx.coroutines.CoroutineScope

class MapboxGroundImageRenderer(
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractGroundImageRenderer<Feature>() {
    override suspend fun addGroundImages(newGroundImages: List<GroundImageState>): List<Feature?> = emptyList()

    override suspend fun changeGroundImages(changes: List<GroundImageRenderer.UpdateParams<Feature>>): List<Feature?> =
        emptyList()

    override suspend fun removeGroundImages(removeEntities: List<GroundImageEntity<Feature>>) {
    }
}
