package com.mapconductor.core.groundimage

interface GroundImageCapableInterface {
    suspend fun compositionGroundImages(data: List<GroundImageState>)

    suspend fun updateGroundImage(state: GroundImageState)

    @Deprecated("Use GroundImageState.onClick instead.")
    fun setOnGroundImageClickListener(listener: OnGroundImageEventHandler?)

    fun hasGroundImage(state: GroundImageState): Boolean
}
