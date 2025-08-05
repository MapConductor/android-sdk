package com.mapconductor.core.circle

import com.mapconductor.core.calculateZIndex
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.spherical.haversineDistance
import java.util.concurrent.ConcurrentHashMap

class CircleManager<ActualCircle> {
    private val entities: ConcurrentHashMap<String, CircleEntity<ActualCircle>> = ConcurrentHashMap()

    fun getEntity(id: String): CircleEntity<ActualCircle>? = entities.get(id)

    fun removeEntity(id: String): CircleEntity<ActualCircle>? {
        val removed = entities.remove(id)
        return removed
    }

    fun registerEntity(entity: CircleEntity<ActualCircle>) {
        entities[entity.state.id] = entity
    }

    fun updateEntity(entity: CircleEntity<ActualCircle>) {
        entities[entity.state.id] = entity
    }

    fun allEntities(): List<CircleEntity<ActualCircle>> = entities.values.toList()

    fun clear() {
        entities.clear()
    }

    fun find(position: IGeoPoint): CircleEntity<ActualCircle>? {
        val filtered =
            allEntities().filter { entity ->
                val centerPos = entity.state.center
                val distance = haversineDistance(centerPos, position)
                return@filter (distance <= entity.state.radiusMeters) && entity.state.clickable
            }

        if (filtered.isEmpty()) {
            return null
        }

        var maxZIndex = Int.MIN_VALUE
        var maxEntity = filtered[0]

        filtered.forEach {
            val zIndex = it.state.zIndex ?: calculateZIndex(it.state.center)
            if (maxZIndex < zIndex) {
                maxZIndex = zIndex
                maxEntity = it
            }
        }
        return maxEntity
    }

//    private fun isPolygonContains(path: MutableList<LatLng?>, point: LatLng?): Boolean {
//        var wn = 0
//        val visibleRegion: VisibleRegion = projection.getVisibleRegion()
//        val bounds: LatLngBounds = visibleRegion.latLngBounds
//        val sw: Point = projection.toScreenLocation(bounds.southwest)
//
//        val touchPoint: Point = projection.toScreenLocation(point)
//        touchPoint.y = sw.y - touchPoint.y
//        var vt: Double
//
//        for (i in 0..<path.size - 1) {
//            val a: Point = projection.toScreenLocation(path.get(i))
//            a.y = sw.y - a.y
//            val b: Point = projection.toScreenLocation(path.get(i + 1))
//            b.y = sw.y - b.y
//
//            if ((a.y <= touchPoint.y) && (b.y > touchPoint.y)) {
//                vt = (touchPoint.y.toDouble() - a.y.toDouble()) / (b.y.toDouble() - a.y.toDouble())
//                if (touchPoint.x < (a.x.toDouble() + (vt * (b.x.toDouble() - a.x.toDouble())))) {
//                    wn++
//                }
//            } else if ((a.y > touchPoint.y) && (b.y <= touchPoint.y)) {
//                vt = (touchPoint.y.toDouble() - a.y.toDouble()) / (b.y.toDouble() - a.y.toDouble())
//                if (touchPoint.x < (a.x.toDouble() + (vt * (b.x.toDouble() - a.x.toDouble())))) {
//                    wn--
//                }
//            }
//        }
//
//        return (wn != 0)
//    }
}
