package com.mapconductor.core.marker

import com.mapconductor.core.icons.Default
import com.mapconductor.core.marker.MarkerIcon
import kotlinx.coroutines.sync.Semaphore

interface MarkerOverlayManager<ActualMarker> {
    suspend fun addMarkers(markerList: List<MarkerState>)

    suspend fun updateMarker(marker: MarkerState)

    suspend fun clearOverlays()

    fun getMarkerState(id: String): MarkerState?
}

interface UpdateParams<ActualMarker> {
    val entity: MarkerEntity<ActualMarker>
    val bitmapIcon: BitmapIcon
    val prevEntity: MarkerEntity<ActualMarker>
}
class MarkerOverlayManagerImpl<
    // Actual marker instance type
    ActualMarker : Any,
>(
    val markerManager: MarkerManager<ActualMarker>,
    val onRemove: suspend (List<MarkerEntity<ActualMarker>>) -> Unit,
    val onAdd: suspend (List<Pair<MarkerState, BitmapIcon>>) -> List<ActualMarker?>,
    val onChange: suspend (List<UpdateParams<ActualMarker>>) -> List<ActualMarker?>,
) : MarkerOverlayManager<ActualMarker> {
    val semaphore = Semaphore(1)

    override suspend fun addMarkers(markerList: List<MarkerState>) {
        semaphore.acquire()

        val current = markerList.toSet()
        val previousEntities = markerManager.allEntities()
        val previous = previousEntities.map { it.state }.toSet()
        val added = current - previous
        val removed = previous - current
        val updated = current.filter { state ->
            val prevEntity = markerManager.getEntity(state.id) ?: return@filter false
            return@filter !prevEntity.state.equals(state)
        }

        val defaultIcon = markerManager.createBitmapIcon(MarkerIcon.Companion.Default())

        // Remove markers
        if (removed.isNotEmpty()) {
            removed.map { removedState ->
                val id = removedState.id
                markerManager.removeEntity(id)
            }.also {
                onRemove(it as List<MarkerEntity<ActualMarker>>)
            }
        }

        // Add new markers
        if (added.isNotEmpty()) {
            val addedList = added.toList()

            addedList.map { state ->
                val markerIcon = state.icon?.let {
                    markerManager.getBitmapIcon(it)
                } ?: defaultIcon
                Pair(state, markerIcon)
            }.also {
                val actualMarkers: List<ActualMarker?> = onAdd(it)
                actualMarkers.forEachIndexed { index, actualMarker ->
                    actualMarker?.let {
                        val entity = MarkerEntityImpl(
                            marker = actualMarker,
                            state = addedList[index]
                        )
                        markerManager.registerEntity(entity)
                    }
                }
            }
        }

        // Update changed markers
        if (updated.isNotEmpty()) {
            val updates = updated
                .map { state ->
                    val markerIcon = state.icon?.let {
                        markerManager.getBitmapIcon(it)
                    } ?: defaultIcon
                    val prevEntity = markerManager.getEntity(state.id) ?: return@map null

                    // プロパティが変わっていなければ、マーカーを再描画しない
                    return@map if (prevEntity.stateHashCode == state.hashCode()) {
                        null
                    } else {
                        val entity = MarkerEntityImpl(
                            state = state,
                            marker = prevEntity.marker,
                        )
                        markerManager.registerEntity(entity)
                        object : UpdateParams<ActualMarker> {
                            override val entity: MarkerEntity<ActualMarker> = entity
                            override val bitmapIcon: BitmapIcon = markerIcon
                            override val prevEntity: MarkerEntity<ActualMarker> = prevEntity
                        }
                        Pair(entity, markerIcon)
                    }
                }
                .filter { it -> it != null } as List<UpdateParams<ActualMarker>>

            val actualMarkers: List<ActualMarker?> = onChange(updates)

            actualMarkers.forEachIndexed { index, actualMarker ->
                actualMarker?.let {
                    val params = updates[index]
                    val entity = MarkerEntityImpl(
                        state = params.entity.state,
                        marker = actualMarker,
                    )
                    markerManager.registerEntity(entity)
                }
            }
        }

        semaphore.release()
    }

    override suspend fun updateMarker(state: MarkerState) {
        val prevEntity = markerManager.getEntity(state.id) ?: return
        if (state.hashCode() == prevEntity.stateHashCode) {
            return
        }

        semaphore.acquire()
        val marker = prevEntity.marker
        val defaultIcon = markerManager.createBitmapIcon(MarkerIcon.Companion.Default())
        val markerIcon = state.icon?.let {
            markerManager.getBitmapIcon(it)
        } ?: defaultIcon

        val entity = MarkerEntityImpl(
            marker = marker,
            state = state,
        )
        val markerParams = object : UpdateParams<ActualMarker> {
            override val entity: MarkerEntity<ActualMarker> = entity
            override val bitmapIcon: BitmapIcon = markerIcon
            override val prevEntity: MarkerEntity<ActualMarker> = prevEntity
        }
        val markers = onChange(listOf(markerParams))

        markers[0]?.let {
            val entity = MarkerEntityImpl(it, state)
            markerManager.registerEntity(entity)
        }

        semaphore.release()
    }

    override suspend fun clearOverlays() {
        semaphore.acquire()
        val entities: List<MarkerEntity<ActualMarker>> = markerManager.allEntities()
        markerManager.clear()

        onRemove(entities)
        markerManager.clear()
        semaphore.release()
    }

    override fun getMarkerState(id: String): MarkerState? = markerManager.getEntity(id)?.state
}
