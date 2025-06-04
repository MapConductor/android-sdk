package com.mapconductor.core.controller

import com.mapconductor.core.MarkerManager
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface MarkerAddParams {
    val entry: MarkerEntry
    val icon: BitmapIcon
}
interface MarkerUpdateParams<ActualMarker>  {
    val entry: MarkerEntry
    val icon: BitmapIcon
    val marker: ActualMarker
}

interface MarkerRemoveParams<ActualMarker> {
    val entry: MarkerEntry
    val marker: ActualMarker
}

class MarkerOverlayManager<
        ActualMarker: Any,   // Actual marker instance type
    >(
    val markerManager: MarkerManager<ActualMarker>,
    val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val onRemove: (List<MarkerRemoveParams<ActualMarker>>) -> Unit,
    val onAdd: (List<MarkerAddParams>) -> List<ActualMarker?>,
    val onChange: (List<MarkerUpdateParams<ActualMarker>>) -> Unit,
) {

    suspend fun addMarkers(markerList: List<MarkerEntry>) {
        val current = markerList.filter { !markerManager.containsKey(it.state.id) }
        if (current.size == 0) return

        val entriesSet = markerManager.getValueSet()
        val added = current - entriesSet
        val removed = entriesSet - current
        val updated =
            markerList
                .filter { entry ->
                    if (!markerManager.containsKey(entry.state.id)) return@filter false
                    return@filter !markerManager.equalsValue(entry)
                }

        val defaultIcon = markerManager.createDefaultMarkerShape()

        // Remove markers
        if (removed.isNotEmpty()) {
            val willRemoveMarkers: List<MarkerRemoveParams<ActualMarker>> = removed
                .map { removedEntry ->
                    val id = removedEntry.state.id
                    val marker = markerManager.getMarker(id)!!
                    markerManager.removeEntry(id)
                    object : MarkerRemoveParams<ActualMarker> {
                        override val entry: MarkerEntry = removedEntry
                        override val marker: ActualMarker = marker
                    }
                }

            withContext(coroutine.coroutineContext) {
                return@withContext onRemove.invoke(willRemoveMarkers)
            }
        }

        // Add new markers
        if (added.isNotEmpty()) {
            val willAdd: List<MarkerAddParams> = added.map { entry ->
                val markerIcon = entry.state.icon?.let {
                    markerManager.getBitmapIcon(it)
                } ?: defaultIcon
                object : MarkerAddParams {
                    override val entry: MarkerEntry = entry
                    override val icon: BitmapIcon = markerIcon
                }
            }
            val actualMarkers: List<ActualMarker?> = withContext(coroutine.coroutineContext) {
                return@withContext onAdd.invoke(willAdd)
            }
            actualMarkers.forEachIndexed { index, actualMarker ->
                actualMarker?.let {
                    val entry = added[index]
                    markerManager.registerEntry(entry, actualMarker)
                }
            }
        }

        // Update changed markers
        if (updated.isNotEmpty()) {
            val willUpdate: List<MarkerUpdateParams<ActualMarker>> =
                updated.map { entry ->
                    val markerIcon =
                        entry.state.icon?.let {
                            markerManager.getBitmapIcon(it)
                        } ?: defaultIcon
                    markerManager.updateEntry(entry)
                    val marker = markerManager.getMarker(entry.id)!!
                    object : MarkerUpdateParams<ActualMarker> {
                        override val entry: MarkerEntry = entry
                        override val icon: BitmapIcon = markerIcon
                        override val marker: ActualMarker = marker
                    }
                }

            withContext(coroutine.coroutineContext) {
                onChange(willUpdate)
            }
        }
    }


    suspend fun clearOverlays() {
        val markerIDs: List<String> = markerManager.allKeys()
        val removes: List<MarkerRemoveParams<ActualMarker>> = markerIDs.map { markerId ->
            return@map object : MarkerRemoveParams<ActualMarker> {
                override val entry : MarkerEntry = markerManager.getEntry(markerId)!!
                override val marker: ActualMarker = markerManager.getMarker(markerId)!!
            }
        }

        withContext(coroutine.coroutineContext) {
            onRemove(removes)
        }
        markerManager.clear()
    }

    fun getMarkerEntry(id: String): MarkerEntry? = markerManager.getEntry(id)
}
