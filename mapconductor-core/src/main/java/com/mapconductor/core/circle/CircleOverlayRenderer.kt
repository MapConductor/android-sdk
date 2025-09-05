package com.mapconductor.core.circle

interface CircleOverlayRenderer<ActualCircle> {
    interface AddParams {
        val state: CircleState
    }

    interface ChangeParams<ActualCircle> {
        val current: CircleEntity<ActualCircle>
        val prev: CircleEntity<ActualCircle>
    }

    suspend fun onAdd(data: List<AddParams>): List<ActualCircle?>

    suspend fun onChange(data: List<ChangeParams<ActualCircle>>): List<ActualCircle?>

    suspend fun onRemove(data: List<CircleEntity<ActualCircle>>)

    suspend fun onPostProcess()
}
