package com.mapconductor.core.controller

interface OverlayRenderer<ActualType, StateType, EntityType> {
    interface Changes<EntityType> {
        val current: EntityType
        val prev: EntityType
    }

    suspend fun onAdd(data: List<StateType>): List<ActualType?>

    suspend fun onChange(data: List<Changes<EntityType>>): List<ActualType?>

    suspend fun onRemove(data: List<EntityType>)

    suspend fun onPostProcess()
}
