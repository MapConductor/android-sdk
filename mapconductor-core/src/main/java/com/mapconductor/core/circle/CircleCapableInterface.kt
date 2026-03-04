package com.mapconductor.core.circle

interface CircleCapableInterface {
    suspend fun compositionCircles(data: List<CircleState>)

    suspend fun updateCircle(state: CircleState)

    @Deprecated("Use CircleState.onClick instead.")
    fun setOnCircleClickListener(listener: OnCircleEventHandler?)

    fun hasCircle(state: CircleState): Boolean
}
