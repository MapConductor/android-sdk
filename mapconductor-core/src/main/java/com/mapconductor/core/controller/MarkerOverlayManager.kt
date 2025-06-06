package com.mapconductor.core.controller

import com.mapconductor.core.MarkerManager
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerEntry

interface MarkerAddParams {
    val entry: MarkerEntry
    val icon: BitmapIcon
}

interface MarkerUpdateParams<ActualMarker> {
    val entry: MarkerEntry
    val icon: BitmapIcon
    val marker: ActualMarker
}

interface MarkerRemoveParams<ActualMarker> {
    val entry: MarkerEntry
    val marker: ActualMarker
}

class MarkerOverlayManager<
    // Actual marker instance type
    ActualMarker : Any,
>(
    val markerManager: MarkerManager<ActualMarker>,
    val onRemove: (List<MarkerRemoveParams<ActualMarker>>) -> Unit,
    val onAdd: (List<MarkerAddParams>) -> List<ActualMarker?>,
    val onChange: (List<MarkerUpdateParams<ActualMarker>>) -> Unit,
) {
    @Synchronized
    fun addMarkers(markerList: List<MarkerEntry>) {
        val current = markerList.toSet()
        val previous = markerManager.getValueSet()
        val added = current - previous
        val removed = previous - current
        val updated =
            current
                .filter { entry ->
                    if (!markerManager.containsKey(entry.state.id)) return@filter false
                    return@filter !markerManager.equalsValue(entry)
                }

        val defaultIcon = markerManager.createDefaultMarkerShape()

        // Remove markers
        if (removed.isNotEmpty()) {
            removed
                .map { removedEntry ->
                    val id = removedEntry.state.id
                    val marker = markerManager.getMarker(id)!!
                    markerManager.removeEntry(id)
                    object : MarkerRemoveParams<ActualMarker> {
                        override val entry: MarkerEntry = removedEntry
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
                .map { entry ->
                    val markerIcon =
                        entry.state.icon?.let {
                            markerManager.getBitmapIcon(it)
                        } ?: defaultIcon
                    object : MarkerAddParams {
                        override val entry: MarkerEntry = entry
                        override val icon: BitmapIcon = markerIcon
                    }
                }.also {
                    val actualMarkers: List<ActualMarker?> = onAdd(it)
                    actualMarkers.forEachIndexed { index, actualMarker ->
                        actualMarker?.let {
                            val entry = addedList[index]
                            markerManager.registerEntry(entry, actualMarker)
                        }
                    }
                }
        }

        // Update changed markers
        if (updated.isNotEmpty()) {
            (
                updated
                    .map { entry ->
                        val markerIcon =
                            entry.state.icon?.let {
                                markerManager.getBitmapIcon(it)
                            } ?: defaultIcon
                        val prevEntry = markerManager.getEntry(entry.id)!!
                        markerManager.updateEntry(entry)

                        // プロパティが変わっていなければ、マーカーを再描画しない
                        return@map if (prevEntry.state == entry.state) {
                            null
                        } else {
                            val marker = markerManager.getMarker(entry.id)!!
                            object : MarkerUpdateParams<ActualMarker> {
                                override val entry: MarkerEntry = entry
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
                    override val entry: MarkerEntry = markerManager.getEntry(markerId)!!
                    override val marker: ActualMarker = markerManager.getMarker(markerId)!!
                }
            }

        onRemove(removes)
        markerManager.clear()
    }

    fun getMarkerEntry(id: String): MarkerEntry? = markerManager.getEntry(id)
}
