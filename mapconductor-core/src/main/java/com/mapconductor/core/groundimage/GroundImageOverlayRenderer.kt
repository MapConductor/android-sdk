package com.mapconductor.core.groundimage

interface GroundImageOverlayRenderer<ActualGroundImage> {
    interface AddParams {
        val state: GroundImageState
    }

    interface ChangeParams<ActualGroundImage> {
        val current: GroundImageEntity<ActualGroundImage>
        val prev: GroundImageEntity<ActualGroundImage>
    }

    suspend fun onAdd(data: List<AddParams>): List<ActualGroundImage?>

    suspend fun onChange(data: List<ChangeParams<ActualGroundImage>>): List<ActualGroundImage?>

    suspend fun onRemove(data: List<GroundImageEntity<ActualGroundImage>>)

    suspend fun onPostProcess()
}
