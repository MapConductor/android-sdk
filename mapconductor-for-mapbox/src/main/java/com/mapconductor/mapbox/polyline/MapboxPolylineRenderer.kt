package com.mapconductor.mapbox.polyline

import com.mapbox.geojson.Feature
import com.mapconductor.core.polyline.AbstractPolylineRenderer
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineOverlayManagerImpl
import com.mapconductor.core.polyline.PolylineRenderer
import com.mapconductor.core.polyline.PolylineRenderer.UpdateParams
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.mapbox.MapboxMapViewHolder
import kotlinx.coroutines.CoroutineScope

class DefaultMapboxPolylineRenderer : PolylineRendererFactory<Feature> {
    override fun create(
        onAdd: suspend (List<PolylineState>) -> List<Feature?>,
        onChange: suspend (List<UpdateParams<Feature>>) -> List<Feature?>,
        onRemove: suspend (List<PolylineEntity<Feature>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?
    ): PolylineOverlayManager<Feature> =
        PolylineOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class MapboxPolylineRenderer(
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope
) : AbstractPolylineRenderer<Feature>() {
    override suspend fun addLines(newLines: List<PolylineState>): List<Feature?> {
        TODO("Not yet implemented")
    }

    override suspend fun removeLines(removeEntities: List<PolylineEntity<Feature>>) {
        TODO("Not yet implemented")
    }

    override suspend fun changeLine(changes: List<PolylineRenderer.UpdateParams<Feature>>): List<Feature> {
        TODO("Not yet implemented")
    }
}
