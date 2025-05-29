package com.mapconductor.core.controller

import com.mapconductor.core.MapViewHolder
import com.mapconductor.core.MarkerManager
import com.mapconductor.core.Offset
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerEntry
import com.mapconductor.core.projection.WebMercator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface MapViewController {
    val holder: MapViewHolder<*, *>
    val coroutine: CoroutineScope
    suspend fun addMarkers(markerList : List<MarkerEntry>)
    suspend fun clearOverlays()
    fun toScreenOffset(position: IGeoPoint): Offset?
}

class BaseMapViewController<
        ActualMarker: Any,   // Actual marker instance type
    >(
    val geocell: HexGeocell = HexGeocell(WebMercator),
    val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val onMarkerRemove: (id: String, marker: ActualMarker) -> Unit,
    val onMarkerAdd: (entry: MarkerEntry, bitmapIcon: BitmapIcon) -> ActualMarker?,
    val onMarkerChanged: (marker: ActualMarker, entry: MarkerEntry, bitmapIcon: BitmapIcon) -> Unit,
) {
    val markerManager: MarkerManager<ActualMarker> = MarkerManager(geocell)

    suspend fun addMarkers(markerList: List<MarkerEntry>) {

        val current = markerList.filter { !markerManager.containsKey(it.state.id) }
        if (current.size == 0) return

        val entriesSet = markerManager.getValueSet()
        val added = current - entriesSet
        val removed =  entriesSet - current
        val updated = markerList
            .filter { entry ->
                if (!markerManager.containsKey(entry.state.id)) return@filter false
                return@filter !markerManager.equalsValue(entry)
            }

        val defaultIcon = markerManager.createDefaultMarkerShape()

        // Remove markers
        if (removed.isNotEmpty()) {
            val willRemoveMarkers: List<Pair<String, ActualMarker?>> = removed
                .map { removedEntry ->
                    val id = removedEntry.state.id
                    val marker = markerManager.getMarker(id)
                    markerManager.removeEntry(id)
                    return@map Pair(id, marker)
                }

            coroutine.launch {
                willRemoveMarkers.forEach { pair ->
                    pair.second?.also { onMarkerRemove(pair.first, it) }
                }
            }
        }

        // Add new markers
        if (added.isNotEmpty()) {
            val willAdd: List<Pair<MarkerEntry, BitmapIcon>> = added.map { entry ->
                val markerIcon = entry.state.icon?.let {
                    markerManager.getBitmapIcon(it)
                } ?: defaultIcon
                Pair(entry, markerIcon)
            }
            withContext(coroutine.coroutineContext) {
                willAdd.forEach {
                    val entry = it.first
                    val markerIcon = it.second
                    val marker = onMarkerAdd(entry, markerIcon)
                    marker?.also {
                        markerManager.registerEntry(entry, marker)
                    }
                }
            }
        }

        // Update changed markers
        if (updated.isNotEmpty()) {
            val willUpdate: List<Pair<MarkerEntry, BitmapIcon>> = updated.map { entry ->
                val markerIcon = entry.state.icon?.let {
                    markerManager.getBitmapIcon(it)
                } ?: defaultIcon
                markerManager.updateEntry(entry)
                Pair(entry, markerIcon)
            }

            withContext(coroutine.coroutineContext) {
                willUpdate.forEach {
                    val entry = it.first
                    val icon = it.second
                    markerManager.getMarker(entry.id)?.also { marker ->
                        onMarkerChanged(marker, entry, icon)
                    }
                }
            }
        }
    }


    suspend fun clearOverlays() {
        withContext(coroutine.coroutineContext) {
            markerManager.forEach { entry, marker -> onMarkerRemove(entry.id, marker) }
        }
        markerManager.clear()
    }

    fun getMarkerEntry(id: String): MarkerEntry? = markerManager.getEntry(id)

}