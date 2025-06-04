package com.mapconductor.core.controller

import com.mapconductor.core.MarkerManager
import com.mapconductor.core.Offset
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapViewHolder
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

    suspend fun addMarkers(markerList: List<MarkerEntry>)

    suspend fun clearOverlays()

    fun toScreenOffset(position: IGeoPoint): Offset?
}

interface MarkerAddParams {
    val entry: MarkerEntry
    val icon: BitmapIcon
}

interface MarkerUpdateParams<ActualMarker> {
    val entry: MarkerEntry
    val icon: BitmapIcon
    val marker: ActualMarker
}

class BaseMapViewController<
    // Actual marker instance type
    ActualMarker : Any,
>(
    val geocell: HexGeocell = HexGeocell(WebMercator),
    val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val onMarkerRemove: (id: String, marker: ActualMarker) -> Unit,
    val onMarkerAdd: (List<MarkerAddParams>) -> List<ActualMarker?>,
    val onMarkerChanged: (List<MarkerUpdateParams<ActualMarker>>) -> Unit,
) {
    val markerManager: MarkerManager<ActualMarker> = MarkerManager(geocell)

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
            val willRemoveMarkers: List<Pair<String, ActualMarker?>> =
                removed
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
            val willAdd: List<MarkerAddParams> =
                added.map { entry ->
                    val markerIcon =
                        entry.state.icon?.let {
                            markerManager.getBitmapIcon(it)
                        } ?: defaultIcon
                    object : MarkerAddParams {
                        override val entry: MarkerEntry = entry
                        override val icon: BitmapIcon = markerIcon
                    }
                }
            val actualMarkers: List<ActualMarker?> =
                withContext(coroutine.coroutineContext) {
                    return@withContext onMarkerAdd.invoke(willAdd)
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
                onMarkerChanged(willUpdate)
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
