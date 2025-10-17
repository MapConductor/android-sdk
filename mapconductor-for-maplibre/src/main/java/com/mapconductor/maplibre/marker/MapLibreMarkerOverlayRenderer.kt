package com.mapconductor.maplibre.marker

import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.marker.AbstractMarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.maplibre.MapLibreActualMarker
import com.mapconductor.maplibre.MapLibreViewHolder
import com.mapconductor.maplibre.toLatLng
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import kotlin.collections.forEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapLibreMarkerRenderer(
    holder: MapLibreViewHolder,
    private val symbolManager: SymbolManager,
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractMarkerOverlayRenderer<MapLibreViewHolder, MapLibreActualMarker>(
    holder = holder,
    coroutine = coroutine,
) {
    override fun setMarkerPosition(
        markerEntity: MarkerEntity<MapLibreActualMarker>,
        position: GeoPointImpl,
    ) {
        coroutine.launch {
            markerEntity.marker?.latLng = position.toLatLng()
        }
    }

    override suspend fun onAdd(data: List<MarkerOverlayRenderer.AddParams>): List<MapLibreActualMarker?> {
        return withContext(coroutine.coroutineContext) {
            data.map { params ->
                val options = SymbolOptions()
                    .withLatLng(GeoPointImpl.from(params.state.position).toLatLng())
                symbolManager.create()
                return@map marker
            }
        }
    }

    override suspend fun onRemove(data: List<MarkerEntity<MapLibreActualMarker>>) {
        coroutine.launch {
//            data.forEach { params -> params.marker?.remove() }
        }
    }

    override suspend fun onPostProcess() {
        // Do nothing here
    }

    override suspend fun onChange(
        changes: List<MarkerOverlayRenderer.ChangeParams<MapLibreActualMarker>>,
    ): List<MapLibreActualMarker?> =
        withContext(coroutine.coroutineContext) {
            emptyList()
        }
}
