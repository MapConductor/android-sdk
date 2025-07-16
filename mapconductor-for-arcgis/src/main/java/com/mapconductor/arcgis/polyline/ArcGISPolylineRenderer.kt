package com.mapconductor.arcgis.polyline

import com.arcgismaps.mapping.view.Graphic
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.core.polyline.AbstractPolylineRenderer
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineOverlayManagerImpl
import com.mapconductor.core.polyline.PolylineRenderer
import com.mapconductor.core.polyline.PolylineRenderer.UpdateParams
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import kotlinx.coroutines.CoroutineScope

class DefaultArcGISPolylineRenderer : PolylineRendererFactory<Graphic> {
    override fun create(
        onAdd: suspend (List<PolylineState>) -> List<Graphic?>,
        onChange: suspend (List<UpdateParams<Graphic>>) -> List<Graphic?>,
        onRemove: suspend (List<PolylineEntity<Graphic>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?
    ): PolylineOverlayManager<Graphic> =
        PolylineOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}
class ArcGISPolylineRenderer(
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope
) : AbstractPolylineRenderer<Graphic>() {
    override suspend fun addLines(newLines: List<PolylineState>): List<Graphic?> {
        TODO("Not yet implemented")
    }

    override suspend fun removeLines(removeEntities: List<PolylineEntity<Graphic>>) {
        TODO("Not yet implemented")
    }

    override suspend fun changeLine(changes: List<PolylineRenderer.UpdateParams<Graphic>>): List<Graphic> {
        TODO("Not yet implemented")
    }
}
