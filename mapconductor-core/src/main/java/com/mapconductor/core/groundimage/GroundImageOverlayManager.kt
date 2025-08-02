package com.mapconductor.core.groundimage

import kotlinx.coroutines.sync.Semaphore

interface GroundImageOverlayManager<ActualGroundImage> {
    suspend fun addGroundImages(overlays: List<GroundImageState>)

    suspend fun updateGroundImage(overlay: GroundImageState)

    suspend fun clearGroundImages()

    fun getGroundImageState(id: String): GroundImageState?

    fun getAllEntities(): List<GroundImageEntity<ActualGroundImage>>
}

class GroundImageOverlayManagerImpl<ActualGroundImage>(
    val onAdd: suspend (List<GroundImageState>) -> List<ActualGroundImage?>,
    val onChange: suspend (List<GroundImageRenderer.UpdateParams<ActualGroundImage>>) -> List<ActualGroundImage?>,

    val onRemove: suspend (List<GroundImageEntity<ActualGroundImage>>) -> Unit,
    val onPostProcess: (suspend () -> Unit)? = null,
) : GroundImageOverlayManager<ActualGroundImage> {

val groundImageEntities = mutableMapOf<String, GroundImageEntity<ActualGroundImage>>()
    val semaphore = Semaphore(1)

    override suspend fun addGroundImages(images: List<GroundImageState>) {
        semaphore.acquire()
        val previous = groundImageEntities.keys.toMutableSet()
        val added = mutableListOf<GroundImageState>()
        val updated = mutableListOf<GroundImageRenderer.UpdateParams<ActualGroundImage>>()
        val removed = mutableListOf<GroundImageEntity<ActualGroundImage>>()

        images.forEach {
            if (previous.contains(it.id)) {
                val prevEntity = groundImageEntities[it.id]!!
                updated.add(object : GroundImageRenderer.UpdateParams<ActualGroundImage> {
                    override val entity = GroundImageEntityImpl(
                        groundImage = prevEntity.groundImage,
                        state = it
                    )
                    override val prevEntity = prevEntity
                })
                previous.remove(it.id)
            } else {
                added.add(it)
                previous.remove(it.id)
            }
        }

        previous.forEach { remainId ->
            groundImageEntities.remove(remainId)?.let { removedEntity ->
                removed.add(removedEntity)
            }
        }

        if (added.isNotEmpty()) {
            val actualOverlays = onAdd(added)
            actualOverlays.forEachIndexed { index, actualOverlay ->
                actualOverlay?.let {
                    val state = added[index]
                    val entity = GroundImageEntityImpl<ActualGroundImage>(
                        groundImage = it,
                        state = state
                    )
                    groundImageEntities[state.id] = entity
                }
            }
        }

        if (updated.isNotEmpty()) {
            val actualOverlays = onChange(updated.toList())
            actualOverlays.forEachIndexed { index, actualOverlay ->
                actualOverlay?.let {
                    val state = updated[index].entity.state
                    val entity = GroundImageEntityImpl<ActualGroundImage>(
                        groundImage = it,
                        state = state
                    )
                    groundImageEntities[state.id] = entity
                }
            }
        }

        if (removed.isNotEmpty()) {
            onRemove(removed)
        }

        onPostProcess?.invoke()
        semaphore.release()
    }

    override suspend fun updateGroundImage(overlay: GroundImageState) {
        semaphore.acquire()
        // Implement fine-grained update logic if needed
        semaphore.release()
    }

    override suspend fun clearGroundImages() {
        semaphore.acquire()
        val entities = groundImageEntities.values.toList()
        onRemove(entities)
        groundImageEntities.clear()
        semaphore.release()
    }

    override fun getGroundImageState(id: String): GroundImageState? = groundImageEntities[id]?.state

    override fun getAllEntities(): List<GroundImageEntity<ActualGroundImage>> = groundImageEntities.values.toList()
}
