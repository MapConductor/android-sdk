package com.mapconductor.core.marker

import android.util.Log
import kotlinx.coroutines.sync.Semaphore

interface MarkerOverlayManager<ActualMarker> {
    val markerManager: MarkerManager<ActualMarker>

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

class MarkerOverlayManagerImpl<ActualMarker>(
    override val markerManager: MarkerManager<ActualMarker>,
    val onRemove: suspend (List<MarkerEntity<ActualMarker>>) -> Unit,
    val onAdd: suspend (List<Pair<MarkerState, BitmapIcon>>) -> List<ActualMarker?>,
    val onChange: suspend (List<UpdateParams<ActualMarker>>) -> List<ActualMarker?>,
    val onPostProcess: (suspend () -> Unit)? = null,
    val onAnimate: suspend (entity: MarkerEntity<ActualMarker>) -> Unit,
) : MarkerOverlayManager<ActualMarker> {
    val semaphore = Semaphore(1)

    override suspend fun addMarkers(markerList: List<MarkerState>) {
        semaphore.acquire()

        val modifiedEntities = mutableListOf<MarkerEntity<ActualMarker>>()
        val current = markerList.toSet()
        val currentSize = current.size
        val previousEntities = markerManager.allEntities()
        val previous = previousEntities.map { it.state }.toSet()
        val added = current - previous
        val removed = previous - current
        val updated =
            current.filter { state ->
                val prevEntity = markerManager.getEntity(state.id) ?: return@filter false
                // Log.d("Debug", "==>${prevEntity.state.equals(state)}")
                return@filter !prevEntity.state.equals(state)
            }
        // Log.d("Debug", "--->currentSize=$currentSize, added=${added.size}, updated=${updated.size}, removed=${removed.size}")

        val defaultIcon = DefaultIcon()
        val defaultIconBitmapIcon = defaultIcon.toBitmapIcon()

        // Remove markers
        if (removed.isNotEmpty()) {
            removed
                .map { removedState ->
                    val id = removedState.id
                    markerManager.removeEntity(id)!!
                }.also {
                    onRemove(it)
                }
        }

        // Add new markers
        if (added.isNotEmpty()) {
            val addedList = added.toList()

            addedList
                .map { state ->
                    val markerIcon = state.icon?.toBitmapIcon() ?: defaultIconBitmapIcon
                    Pair(state, markerIcon)
                }.also {
                    val actualMarkers: List<ActualMarker?> = onAdd(it)
                    actualMarkers.forEachIndexed { index, actualMarker ->
                        actualMarker?.let {
                            val entity =
                                MarkerEntityImpl<ActualMarker>(
                                    marker = actualMarker,
                                    state = addedList[index],
                                )
                            markerManager.registerEntity(entity)
                            modifiedEntities.add(entity)
                        }
                    }
                }
        }

        // Update changed markers
        if (updated.isNotEmpty()) {
            val updates =
                updated
                    .map { state ->
                        val markerIcon = state.icon ?: defaultIcon
                        val prevEntity = markerManager.getEntity(state.id) ?: return@map null

                        // プロパティが変わっていなければ、マーカーを再描画しない
                        return@map if (prevEntity.fingerPrint == state.fingerPrint()) {
                            null
                        } else {
                            val entity =
                                MarkerEntityImpl(
                                    state = state,
                                    marker = prevEntity.marker,
                                )
                            markerManager.registerEntity(entity)
                            modifiedEntities.add(entity)
                            object : UpdateParams<ActualMarker> {
                                override val entity: MarkerEntity<ActualMarker> = entity
                                override val bitmapIcon: BitmapIcon = markerIcon.toBitmapIcon()
                                override val prevEntity: MarkerEntity<ActualMarker> = prevEntity
                            }
                        }
                    }.filterNotNull()

            val actualMarkers: List<ActualMarker?> = onChange(updates)

            actualMarkers.forEachIndexed { index, actualMarker ->
                actualMarker?.let {
                    val params = updates[index]
                    val entity =
                        MarkerEntityImpl<ActualMarker>(
                            state = params.entity.state,
                            marker = actualMarker,
                        )
                    markerManager.registerEntity(entity)
                }
            }
        }
        modifiedEntities.forEach { entity ->
            entity.state.animation?.let {
                onAnimate(entity)
            }
        }
        onPostProcess?.invoke()

        semaphore.release()
    }

    override suspend fun updateMarker(state: MarkerState) {
        val prevEntity = markerManager.getEntity(state.id) ?: return
        val currentFinger = state.fingerPrint()
        val prevFinger = prevEntity.fingerPrint
        if (currentFinger == prevFinger) {
            return
        }

        semaphore.acquire()
        val marker = prevEntity.marker
        val defaultIcon = DefaultIcon()
        val markerIcon = state.icon ?: defaultIcon

        val entity =
            MarkerEntityImpl(
                marker = marker,
                state = state,
            )
        val markerParams =
            object : UpdateParams<ActualMarker> {
                override val entity: MarkerEntity<ActualMarker> = entity
                override val bitmapIcon: BitmapIcon = markerIcon.toBitmapIcon()
                override val prevEntity: MarkerEntity<ActualMarker> = prevEntity
            }
        val markers = onChange(listOf(markerParams))

        markers[0]?.let {
            val entity =
                MarkerEntityImpl<ActualMarker>(
                    marker = it,
                    state = state,
                )
            markerManager.registerEntity(entity)

            // Execute the animation property
            if (prevFinger.animation != currentFinger.animation) {
                state.animation?.let {
                    onAnimate(entity)
                }
            }
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
