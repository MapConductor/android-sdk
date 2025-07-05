package com.mapconductor.core.marker

import com.mapconductor.core.geocell.HexGeocell

interface MarkerRendererFactory<ActualMarker> {
    fun create(
        hexGeocell: HexGeocell,
        onIconAdd: suspend (List<Pair<MarkerState, BitmapIcon>>) -> List<ActualMarker?>,
        onIconRemove: suspend (List<MarkerEntity<ActualMarker>>) -> Unit,
        onIconChange: suspend (List<UpdateParams<ActualMarker>>) -> List<ActualMarker>,
        onAnimate: suspend (MarkerEntity<ActualMarker>) -> Unit,
        onPostProcess: (suspend () -> Unit)? = null,
    ): MarkerOverlayManager<ActualMarker>
}

interface MarkerRenderer<ActualMarker> {
    fun init(markerManager: MarkerManager<ActualMarker>)
    suspend fun addIcons(newMarkers: List<Pair<MarkerState, BitmapIcon>>): List<ActualMarker?>
    suspend fun removeIcons(removeEntities: List<MarkerEntity<ActualMarker>>)
    suspend fun changeIcons(changes: List<UpdateParams<ActualMarker>>): List<ActualMarker>
}

abstract class AbstractMarkerRenderer<ActualMarker>(): MarkerRenderer<ActualMarker> {}
