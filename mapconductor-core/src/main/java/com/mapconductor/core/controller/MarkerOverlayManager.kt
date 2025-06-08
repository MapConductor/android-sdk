package com.mapconductor.core.controller

import com.mapconductor.core.MarkerManager
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerState

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

class MarkerOverlayManager<
    // Actual marker instance type
    ActualMarker : Any,
>(
    val markerManager: MarkerManager<ActualMarker>,
    var onRemove: (List<MarkerRemoveParams<ActualMarker>>) -> Unit,
    var onAdd: (List<MarkerAddParams>) -> List<ActualMarker?>,
    var onChange: (List<MarkerUpdateParams<ActualMarker>>) -> Unit,
) {
    @Synchronized
    fun addMarkers(markerList: List<MarkerState>) {
        val current = markerList.toSet()
        val previous = markerManager.getValueSet()
        val added = current - previous
        val removed = previous - current
        val updated =
            current
                .filter { state ->
                    val prevState = markerManager.getState(state.id) ?: return@filter false
                    return@filter !prevState.equals(state)
                }

        val defaultIcon = markerManager.drawIcon()

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
                    onRemove(it)
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
                    val actualMarkers: List<ActualMarker?> = onAdd(it)
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
    }

    fun clearOverlays() {
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
    }

    fun getMarkerState(id: String): MarkerState? = markerManager.getState(id)
}
