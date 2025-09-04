package com.mapconductor.core.groundimage

interface GroundImageEntity<ActualGroundImage> {
    val groundImage: ActualGroundImage
    val state: GroundImageState
    val fingerPrint: GroundImageFingerPrint
}

data class GroundImageEntityImpl<ActualGroundImage>(
    override val groundImage: ActualGroundImage,
    override val state: GroundImageState,
) : GroundImageEntity<ActualGroundImage> {
    override val fingerPrint: GroundImageFingerPrint = state.fingerPrint()
}
