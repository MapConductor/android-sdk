package com.mapconductor.core.polygon

import com.mapconductor.core.features.IGeoPoint

interface PolygonManager<ActualPolygon> {
    fun registerEntity(entity: PolygonEntity<ActualPolygon>)
    fun removeEntity(id: String): PolygonEntity<ActualPolygon>?
    fun getEntity(id: String): PolygonEntity<ActualPolygon>?
    fun allEntities(): List<PolygonEntity<ActualPolygon>>
    fun clear()
    fun find(position: IGeoPoint): PolygonEntity<ActualPolygon>?
}

class PolygonManagerImpl<ActualPolygon> : PolygonManager<ActualPolygon> {
    private val entities = mutableMapOf<String, PolygonEntity<ActualPolygon>>()

    override fun registerEntity(entity: PolygonEntity<ActualPolygon>) {
        entities[entity.state.id] = entity
    }

    override fun removeEntity(id: String): PolygonEntity<ActualPolygon>? = entities.remove(id)

    override fun getEntity(id: String): PolygonEntity<ActualPolygon>? = entities[id]

    override fun allEntities(): List<PolygonEntity<ActualPolygon>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun find(position: IGeoPoint): PolygonEntity<ActualPolygon>? = entities.values.firstOrNull()

}
