package com.mapconductor.core.groundimage

import com.mapconductor.core.features.IGeoPoint

interface GroundImageManager<ActualGroundImage> {
    fun registerEntity(entity: GroundImageEntity<ActualGroundImage>)

    fun removeEntity(id: String): GroundImageEntity<ActualGroundImage>?

    fun getEntity(id: String): GroundImageEntity<ActualGroundImage>?

    fun hasEntity(id: String): Boolean

    fun allEntities(): List<GroundImageEntity<ActualGroundImage>>

    fun clear()

    fun find(position: IGeoPoint): GroundImageEntity<ActualGroundImage>?
}

class GroundImageManagerImpl<ActualGroundImage> : GroundImageManager<ActualGroundImage> {
    private val entities = mutableMapOf<String, GroundImageEntity<ActualGroundImage>>()

    override fun registerEntity(entity: GroundImageEntity<ActualGroundImage>) {
        entities[entity.state.id] = entity
    }

    override fun removeEntity(id: String): GroundImageEntity<ActualGroundImage>? = entities.remove(id)

    override fun getEntity(id: String): GroundImageEntity<ActualGroundImage>? = entities[id]

    override fun hasEntity(id: String): Boolean = entities.containsKey(id)

    override fun allEntities(): List<GroundImageEntity<ActualGroundImage>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun find(position: IGeoPoint): GroundImageEntity<ActualGroundImage>? = entities.values.firstOrNull()
}
