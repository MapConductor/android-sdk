package com.mapconductor.core.groundimage

interface GroundImageOverlayRendererInterface<ActualGroundImage> {
    interface AddParamsInterface {
        val state: GroundImageState
    }

    interface ChangeParamsInterface<ActualGroundImage> {
        val current: GroundImageEntityInterface<ActualGroundImage>
        val prev: GroundImageEntityInterface<ActualGroundImage>
    }

    suspend fun onAdd(data: List<AddParamsInterface>): List<ActualGroundImage?>

    suspend fun onChange(data: List<ChangeParamsInterface<ActualGroundImage>>): List<ActualGroundImage?>

    suspend fun onRemove(data: List<GroundImageEntityInterface<ActualGroundImage>>)

    suspend fun onPostProcess()
}
