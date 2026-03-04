package com.mapconductor.core.marker

interface MarkerOverlayRendererInterface<ActualMarker> {
    var animateStartListener: OnMarkerEventHandler?
    var animateEndListener: OnMarkerEventHandler?

    interface AddParamsInterface {
        val state: MarkerState
        val bitmapIcon: BitmapIcon
    }

    interface ChangeParamsInterface<ActualMarker> {
        val current: MarkerEntityInterface<ActualMarker>
        val bitmapIcon: BitmapIcon
        val prev: MarkerEntityInterface<ActualMarker>
    }

    suspend fun onAdd(data: List<AddParamsInterface>): List<ActualMarker?>

    suspend fun onChange(data: List<ChangeParamsInterface<ActualMarker>>): List<ActualMarker?>

    suspend fun onRemove(data: List<MarkerEntityInterface<ActualMarker>>)

    suspend fun onAnimate(entity: MarkerEntityInterface<ActualMarker>)

    suspend fun onPostProcess()
}
