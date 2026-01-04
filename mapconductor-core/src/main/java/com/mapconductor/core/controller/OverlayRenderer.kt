package com.mapconductor.core.controller

interface OverlayRendererInterface<ActualType, StateType, EntityType> {
    interface ChangeParamsInterface<EntityType> {
        val current: EntityType
        val prev: EntityType
    }

    suspend fun onAdd(data: List<StateType>): List<ActualType?>

    suspend fun onChange(data: List<ChangeParamsInterface<EntityType>>): List<ActualType?>

    suspend fun onRemove(data: List<EntityType>)

    suspend fun onPostProcess()
}
