package com.mapconductor.core.raster

import com.mapconductor.core.features.GeoPoint

interface RasterLayerManager<ActualLayer> {
    fun registerEntity(entity: RasterLayerEntity<ActualLayer>)

    fun removeEntity(id: String): RasterLayerEntity<ActualLayer>?

    fun getEntity(id: String): RasterLayerEntity<ActualLayer>?

    fun hasEntity(id: String): Boolean

    fun allEntities(): List<RasterLayerEntity<ActualLayer>>

    fun clear()

    fun find(position: GeoPoint): RasterLayerEntity<ActualLayer>?
}

class RasterLayerManagerImpl<ActualLayer> : RasterLayerManager<ActualLayer> {
    private val entities = mutableMapOf<String, RasterLayerEntity<ActualLayer>>()

    override fun registerEntity(entity: RasterLayerEntity<ActualLayer>) {
        entities[entity.state.id] = entity
    }

    override fun removeEntity(id: String): RasterLayerEntity<ActualLayer>? = entities.remove(id)

    override fun getEntity(id: String): RasterLayerEntity<ActualLayer>? = entities[id]

    override fun hasEntity(id: String): Boolean = entities.containsKey(id)

    override fun allEntities(): List<RasterLayerEntity<ActualLayer>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun find(position: GeoPoint): RasterLayerEntity<ActualLayer>? = null
}
