package com.mapconductor.core.groundimage

interface GroundImageCapable {
    suspend fun compositionGroundImages(data: List<GroundImageState>)

    suspend fun updateGroundImage(state: GroundImageState)

    fun setOnGroundImageClickListener(listener: OnGroundImageEventHandler?)

    fun hasGroundImage(state: GroundImageState): Boolean
}
