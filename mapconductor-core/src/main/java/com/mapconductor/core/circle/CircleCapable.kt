package com.mapconductor.core.circle

interface CircleCapable {
    suspend fun compositionCircles(data: List<CircleState>)

    suspend fun updateCircle(state: CircleState)

    fun setOnCircleClickListener(listener: OnCircleEventHandler?)
}
