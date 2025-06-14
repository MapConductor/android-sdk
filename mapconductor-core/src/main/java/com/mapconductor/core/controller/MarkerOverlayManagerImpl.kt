package com.mapconductor.core.controller

import com.mapconductor.core.MarkerManager
import com.mapconductor.core.icons.Default
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.core.marker.MarkerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext

interface MarkerAddParams {
    val state: MarkerState
    val icon: BitmapIcon
}

interface MarkerUpdateParams<ActualMarker> {
    val state: MarkerState
    val icon: BitmapIcon
    val marker: ActualMarker
}

interface MarkerRemoveParams<ActualMarker> {
    val state: MarkerState
    val marker: ActualMarker
}

interface MarkerOverlayManager {
    suspend fun addMarkers(markerList: List<MarkerState>)

    suspend fun updateMarker(marker: MarkerState)

    suspend fun clearOverlays()

    fun getMarkerState(id: String): MarkerState?

    suspend fun setIcon(
        id: String,
        icon: MarkerIcon?,
    )
}

class MarkerOverlayManagerImpl<
    // Actual marker instance type
    ActualMarker : Any,
>(
    val markerManager: MarkerManager<ActualMarker>,
    val onRemove: (List<MarkerRemoveParams<ActualMarker>>) -> Unit,
    val onAdd: (List<MarkerAddParams>) -> List<ActualMarker?>,
    val onChange: (List<MarkerUpdateParams<ActualMarker>>) -> Unit,
    val onIconChange: (marker: ActualMarker, icon: BitmapIcon) -> Unit,
    val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : MarkerOverlayManager {
    val semaphore = Semaphore(1)

    override suspend fun addMarkers(markerList: List<MarkerState>) {
        semaphore.acquire()

        val current = markerList.toSet()
        val previous = markerManager.getValueSet()
        val added = current - previous
        val removed = previous - current
        val updated =
            current
                .filter { state ->
                    val prevState =
                        markerManager.getState(state.id) ?: return@filter false
                    return@filter !prevState.equals(state)
                }

        val defaultIcon = markerManager.createDefaultMarkerShape()

        // Remove markers
        if (removed.isNotEmpty()) {
            removed
                .map { removedState ->
                    val id = removedState.id
                    val marker = markerManager.getMarker(id)!!
                    markerManager.removeStateAndMarker(id)
                    object : MarkerRemoveParams<ActualMarker> {
                        override val state: MarkerState = removedState
                        override val marker: ActualMarker = marker
                    }
                }.also {
                    coroutine.launch {
                        onRemove(it)
                    }
                }
        }

        // Add new markers
        if (added.isNotEmpty()) {
            val addedList = added.toList()

            addedList
                .map { state ->
                    val markerIcon =
                        state.icon?.let {
                            markerManager.getBitmapIcon(it)
                        } ?: defaultIcon
                    object : MarkerAddParams {
                        override val state: MarkerState = state
                        override val icon: BitmapIcon = markerIcon
                    }
                }.also {
                    val actualMarkers: List<ActualMarker?> =
                        withContext(coroutine.coroutineContext) {
                            onAdd(it)
                        }
                    actualMarkers.forEachIndexed { index, actualMarker ->
                        actualMarker?.let {
                            val state = addedList[index]
                            markerManager.registerState(state, actualMarker)
                        }
                    }
                }
        }

        // Update changed markers
        if (updated.isNotEmpty()) {
            (
                updated
                    .map { state ->
                        val markerIcon =
                            state.icon?.let {
                                markerManager.getBitmapIcon(it)
                            } ?: defaultIcon
                        val prevState = markerManager.getState(state.id)!!
                        markerManager.updateState(state)

                        // プロパティが変わっていなければ、マーカーを再描画しない
                        return@map if (prevState == state) {
                            null
                        } else {
                            val marker = markerManager.getMarker(state.id)!!
                            object : MarkerUpdateParams<ActualMarker> {
                                override val state: MarkerState = state
                                override val icon: BitmapIcon = markerIcon
                                override val marker: ActualMarker = marker
                            }
                        }
                    }.filter { it -> it != null } as List<MarkerUpdateParams<ActualMarker>>
            ).also {
                onChange(it)
            }
        }

        semaphore.release()
    }

    override suspend fun updateMarker(state: MarkerState) {
        semaphore.acquire()
        val defaultIcon = markerManager.createDefaultMarkerShape()

        val markerId = state.id
        markerManager.updateState(state)
        val marker = markerManager.getMarker(markerId)
        if (marker == null) {
            semaphore.release()
            return
        }
        val markerIcon =
            state.icon?.let {
                markerManager.getBitmapIcon(it)
            } ?: defaultIcon
        val markerParams =
            object : MarkerUpdateParams<ActualMarker> {
                override val state: MarkerState = state
                override val icon: BitmapIcon = markerIcon
                override val marker: ActualMarker = marker
            }

        withContext(coroutine.coroutineContext) {
            onChange(listOf(markerParams))
        }
        semaphore.release()
    }

    override suspend fun clearOverlays() {
        semaphore.acquire()
        val markerIDs: List<String> = markerManager.allKeys()
        val removes: List<MarkerRemoveParams<ActualMarker>> =
            markerIDs.map { markerId ->
                return@map object : MarkerRemoveParams<ActualMarker> {
                    override val state: MarkerState = markerManager.getState(markerId)!!
                    override val marker: ActualMarker = markerManager.getMarker(markerId)!!
                }
            }

        onRemove(removes)
        markerManager.clear()
        semaphore.release()
    }

    override fun getMarkerState(id: String): MarkerState? = markerManager.getState(id)

    override suspend fun setIcon(
        id: String,
        icon: MarkerIcon?,
    ) {
        val marker = markerManager.getMarker(id)
        if (marker == null) {
            semaphore.release()
            return
        }
        semaphore.acquire()
        val newIcon = icon ?: MarkerIcon.Default()
        val bitmapIcon = markerManager.getBitmapIcon(newIcon)
        onIconChange(marker, bitmapIcon)
        semaphore.release()
    }
}
