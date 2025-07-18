package com.mapconductor.core.marker

import com.mapconductor.core.marker.MarkerRenderer.UpdateParams
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineEntityImpl
import com.mapconductor.core.polyline.PolylineRenderer
import com.mapconductor.core.polyline.PolylineState
import kotlinx.coroutines.sync.Semaphore

interface MarkerOverlayManager<ActualMarker> {
    val markerManager: MarkerManager<ActualMarker>

    suspend fun addMarkers(markerList: List<MarkerState>)

    suspend fun updateMarker(marker: MarkerState)

    suspend fun clearOverlays()

    fun getMarkerState(id: String): MarkerState?
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

        val defaultIcon = DefaultIcon()
        val defaultIconBitmapIcon = defaultIcon.toBitmapIcon()
        val modifiedEntities = mutableListOf<MarkerEntity<ActualMarker>>()
        val previous = markerManager.allEntities().map { it.state.id }.toMutableSet()
        val added = mutableListOf<MarkerState>()
        val updated = mutableListOf<UpdateParams<ActualMarker>>()
        val removed = mutableListOf<MarkerEntity<ActualMarker>>()
        markerList.forEach { state ->
            if (previous.contains(state.id)) {
                val prevEntity = markerManager.getEntity(state.id)!!
                val markerIcon = state.icon ?: defaultIcon
                updated.add(
                    object : UpdateParams<ActualMarker> {
                        override val entity: MarkerEntity<ActualMarker> =
                            MarkerEntityImpl(
                                state = state,
                                marker = prevEntity.marker,
                            )
                        override val bitmapIcon: BitmapIcon
                            get() { return markerIcon.toBitmapIcon() }
                        override val prevEntity: MarkerEntity<ActualMarker> = prevEntity
                    },
                )
                previous.remove(state.id)
                return@forEach
            }
            added.add(state)
            previous.remove(state.id)
        }
        previous.forEach { remainId ->
            markerManager.removeEntity(remainId)?.let { removedEntity ->
                removed.add(removedEntity)
            }
        }


        // Remove markers
        if (removed.isNotEmpty()) {
            onRemove(removed)
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
            val actualMarkers: List<ActualMarker?> = onChange(updated)

            actualMarkers.forEachIndexed { index, actualMarker ->
                actualMarker?.let {
                    val params = updated[index]
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
