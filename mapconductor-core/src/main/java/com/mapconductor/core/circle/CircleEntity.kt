package com.mapconductor.core.circle

interface CircleEntity<ActualCircle> {
    var circle: ActualCircle
    val state: CircleState
    val fingerPrint: CircleFingerPrint
}

class CircleEntityImpl<ActualCircle>(
    override var circle: ActualCircle,
    override val state: CircleState,
) : CircleEntity<ActualCircle> {
    override val fingerPrint: CircleFingerPrint = state.fingerPrint()
}
