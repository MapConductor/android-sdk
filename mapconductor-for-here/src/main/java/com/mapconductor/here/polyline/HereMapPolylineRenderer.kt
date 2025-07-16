package com.mapconductor.here.polyline

import com.here.sdk.mapview.MapPolyline
import com.mapconductor.core.polyline.AbstractPolylineRenderer
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineOverlayManagerImpl
import com.mapconductor.core.polyline.PolylineRenderer
import com.mapconductor.core.polyline.PolylineRenderer.UpdateParams
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.here.HereMapViewHolder
import kotlinx.coroutines.CoroutineScope

class DefaultHereMapPolylineRenderer : PolylineRendererFactory<MapPolyline> {
    override fun create(
        onAdd: suspend (List<PolylineState>) -> List<MapPolyline?>,
        onChange: suspend (List<UpdateParams<MapPolyline>>) -> List<MapPolyline?>,
        onRemove: suspend (List<PolylineEntity<MapPolyline>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?
    ): PolylineOverlayManager<MapPolyline> =
        PolylineOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}
class HereMapPolylineRenderer(
    override val holder: HereMapViewHolder,
    override val coroutine: CoroutineScope
) : AbstractPolylineRenderer<MapPolyline>() {
    override suspend fun addLines(newLines: List<PolylineState>): List<MapPolyline?> {
        TODO("Not yet implemented")
    }

    override suspend fun removeLines(removeEntities: List<PolylineEntity<MapPolyline>>) {
        TODO("Not yet implemented")
    }

    override suspend fun changeLine(changes: List<PolylineRenderer.UpdateParams<MapPolyline>>): List<MapPolyline> {
        TODO("Not yet implemented")
    }
}
