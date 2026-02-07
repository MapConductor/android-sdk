package com.mapconductor.core.circle

interface CircleOverlayRendererInterface<ActualCircle> {
    interface AddParamsInterface {
        val state: CircleState
    }

    interface ChangeParamsInterface<ActualCircle> {
        val current: CircleEntityInterface<ActualCircle>
        val prev: CircleEntityInterface<ActualCircle>
    }

    suspend fun onAdd(data: List<AddParamsInterface>): List<ActualCircle?>

    suspend fun onChange(data: List<ChangeParamsInterface<ActualCircle>>): List<ActualCircle?>

    suspend fun onRemove(data: List<CircleEntityInterface<ActualCircle>>)

    suspend fun onPostProcess()
}
