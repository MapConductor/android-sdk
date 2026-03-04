package com.mapconductor.core.groundimage

interface GroundImageEntityInterface<ActualGroundImage> {
    val groundImage: ActualGroundImage
    val state: GroundImageState
    val fingerPrint: GroundImageFingerPrint
}

data class GroundImageEntity<ActualGroundImage>(
    override val groundImage: ActualGroundImage,
    override val state: GroundImageState,
) : GroundImageEntityInterface<ActualGroundImage> {
    override val fingerPrint: GroundImageFingerPrint = state.fingerPrint()
}
