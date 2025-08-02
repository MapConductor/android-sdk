package com.mapconductor.core.groundimage

interface GroundImageEntity<ActualGroundImage> {
    val groundImage: ActualGroundImage
    val state: GroundImageState
    val stateHashCode: Int
}

data class GroundImageEntityImpl<ActualGroundImage>(
    override val groundImage: ActualGroundImage,
    override val state: GroundImageState,
) : GroundImageEntity<ActualGroundImage> {
    override val stateHashCode: Int = state.hashCode()
}
