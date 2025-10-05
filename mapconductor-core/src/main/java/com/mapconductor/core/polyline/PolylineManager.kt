package com.mapconductor.core.polyline

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.calculateMetersPerPixel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.isPointOnLinearLine
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.pointOnGeodesicSegmentOrNull
import com.mapconductor.settings.Settings
import android.util.Log

data class PolylineHitResult<ActualPolyline>(
    val entity: PolylineEntity<ActualPolyline>,
    val closestPoint: GeoPoint,
)

private data class DistanceResult(
    val distance: Double,
    val closestPoint: GeoPoint,
)

interface PolylineManager<ActualPolyline> {
    fun registerEntity(entity: PolylineEntity<ActualPolyline>)

    fun removeEntity(id: String): PolylineEntity<ActualPolyline>?

    fun getEntity(id: String): PolylineEntity<ActualPolyline>?

    fun hasEntity(id: String): Boolean

    fun allEntities(): List<PolylineEntity<ActualPolyline>>

    fun clear()

    fun find(
        position: GeoPoint,
        cameraPosition: MapCameraPositionImpl? = null,
    ): PolylineHitResult<ActualPolyline>?
}

class PolylineManagerImpl<ActualPolyline> : PolylineManager<ActualPolyline> {
    companion object {
        private const val DEBUG_FIND = true
        private const val TAG = "PolylineManager"

        private fun d(msg: String) {
            if (DEBUG_FIND) Log.d(TAG, msg)
        }
    }

    private val entities = mutableMapOf<String, PolylineEntity<ActualPolyline>>()

    override fun registerEntity(entity: PolylineEntity<ActualPolyline>) {
        entities[entity.state.id] = entity
    }

    override fun removeEntity(id: String): PolylineEntity<ActualPolyline>? = entities.remove(id)

    override fun getEntity(id: String): PolylineEntity<ActualPolyline>? = entities[id]

    override fun hasEntity(id: String): Boolean = entities.containsKey(id)

    override fun allEntities(): List<PolylineEntity<ActualPolyline>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun find(
        position: GeoPoint,
        cameraPosition: MapCameraPositionImpl?,
    ): PolylineHitResult<ActualPolyline>? {
        val visibleRegion = cameraPosition?.visibleRegion?.bounds
        val candidates = mutableListOf<Triple<PolylineEntity<ActualPolyline>, GeoPoint, Double>>()
        val fingerSize = ResourceProvider.dpToPx(Settings.Default.tapTolerance)
        val zoom = cameraPosition?.zoom ?: 0.0
        val threshold = calculateMetersPerPixel(position.latitude, zoom) * fingerSize

        val entities =
            if (visibleRegion != null) {
                entities.values.filter { visibleRegion.intersects(it.bounds) }
            } else {
                entities.values
            }

        entities.forEach { entity ->
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
