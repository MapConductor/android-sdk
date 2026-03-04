package com.mapconductor.core.polyline

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.spherical.calculateMetersPerPixel
import com.mapconductor.core.spherical.isPointOnLinearLine
import com.mapconductor.core.spherical.pointOnGeodesicSegmentOrNull
import com.mapconductor.settings.Settings
import android.util.Log

data class PolylineHitResult<ActualPolyline>(
    val entity: PolylineEntityInterface<ActualPolyline>,
    val closestPoint: GeoPointInterface,
)

private data class DistanceResult(
    val distance: Double,
    val closestPoint: GeoPointInterface,
)

interface PolylineManagerInterface<ActualPolyline> {
    fun registerEntity(entity: PolylineEntityInterface<ActualPolyline>)

    fun removeEntity(id: String): PolylineEntityInterface<ActualPolyline>?

    fun getEntity(id: String): PolylineEntityInterface<ActualPolyline>?

    fun hasEntity(id: String): Boolean

    fun allEntities(): List<PolylineEntityInterface<ActualPolyline>>

    fun clear()

    fun find(
        position: GeoPointInterface,
        cameraPosition: MapCameraPosition? = null,
    ): PolylineHitResult<ActualPolyline>?
}

class PolylineManager<ActualPolyline> : PolylineManagerInterface<ActualPolyline> {
    companion object {
        private const val DEBUG_FIND = true
        private const val TAG = "PolylineManagerInterface"

        private fun d(msg: String) {
            if (DEBUG_FIND) Log.d(TAG, msg)
        }
    }

    private val entities = mutableMapOf<String, PolylineEntityInterface<ActualPolyline>>()

    override fun registerEntity(entity: PolylineEntityInterface<ActualPolyline>) {
        entities[entity.state.id] = entity
    }

    override fun removeEntity(id: String): PolylineEntityInterface<ActualPolyline>? = entities.remove(id)

    override fun getEntity(id: String): PolylineEntityInterface<ActualPolyline>? = entities[id]

    override fun hasEntity(id: String): Boolean = entities.containsKey(id)

    override fun allEntities(): List<PolylineEntityInterface<ActualPolyline>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun find(
        position: GeoPointInterface,
        cameraPosition: MapCameraPosition?,
    ): PolylineHitResult<ActualPolyline>? {
        val visibleRegion = cameraPosition?.visibleRegion?.bounds
        val candidates = mutableListOf<Triple<PolylineEntityInterface<ActualPolyline>, GeoPointInterface, Double>>()
        val fingerSize = ResourceProvider.dpToPx(Settings.Default.tapTolerance)
        val zoom = cameraPosition?.zoom ?: 0.0
        val threshold = calculateMetersPerPixel(position.latitude, zoom) * fingerSize

        entities.values.forEach { entity ->
            // 補間せず、元の線分を直接使う
            for (i in 0 until entity.state.points.size - 1) {
                val box = GeoRectBounds()
                box.extend(entity.state.points[i])
                box.extend(entity.state.points[i + 1])

                if (visibleRegion == null || visibleRegion.intersects(box)) {
                    when (entity.state.geodesic) {
                        true ->
                            pointOnGeodesicSegmentOrNull(
                                entity.state.points[i],
                                entity.state.points[i + 1],
                                position,
                                threshold,
                            )
                        false ->
                            isPointOnLinearLine(
                                entity.state.points[i],
                                entity.state.points[i + 1],
                                position,
                                threshold,
                            )
                    }?.let {
                        candidates.add(Triple(entity, it.first, it.second))
                    }
                }
            }
        }

        val closest = candidates.minByOrNull { it.third }
        return closest?.let { (entity, closestPoint, distance) ->
            PolylineHitResult(entity = entity, closestPoint = closestPoint)
        }
    }
}
