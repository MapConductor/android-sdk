package com.mapconductor.core.circle

interface CircleEntityInterface<ActualCircle> {
    var circle: ActualCircle
    val state: CircleState
    val fingerPrint: CircleFingerPrint
}

class CircleEntity<ActualCircle>(
    override var circle: ActualCircle,
    override val state: CircleState,
) : CircleEntityInterface<ActualCircle> {
    override val fingerPrint: CircleFingerPrint = state.fingerPrint()
}
