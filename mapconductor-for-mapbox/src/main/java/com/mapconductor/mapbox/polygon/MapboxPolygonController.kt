package com.mapconductor.mapbox.polygon

import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.mapbox.MapboxActualPolygon
import com.mapconductor.mapbox.MapboxMapViewHolder
import kotlinx.coroutines.CoroutineScope

class MapboxPolygonController(
    override val renderer: MapboxPolygonRenderer,
    polygonManager: PolygonManager<>
)
fun createMapboxPolygonController(
    holder: MapboxMapViewHolder,
    coroutine: CoroutineScope,
    layer: MapboxPolygonLayer,
): PolygonController<MapboxActualPolygon> =
    PolygonController(
        renderer =
            MapboxPolygonRenderer(
                holder = holder,
                coroutine = coroutine,
                layer = layer,
            ),
    )
