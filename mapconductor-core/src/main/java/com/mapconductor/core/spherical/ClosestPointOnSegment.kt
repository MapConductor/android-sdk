package com.mapconductor.core.spherical

import androidx.compose.ui.geometry.Offset

fun closestPointOnSegment(
    startPoint: Offset,
    endPoint: Offset,
    testPoint: Offset,
): Offset {
    val segmentVector = Offset(endPoint.x - startPoint.x, endPoint.y - startPoint.y)
    val pointVector = Offset(testPoint.x - startPoint.x, testPoint.y - startPoint.y)
    val segmentLengthSquared = segmentVector.x * segmentVector.x + segmentVector.y * segmentVector.y
    if (segmentLengthSquared == 0.0f) return startPoint // AとBが同じ点

    // 内積で射影係数 projectionRatio を求める (0 ≤ projectionRatio ≤ 1)
    val projectionRatio =
        ((pointVector.x * segmentVector.x + pointVector.y * segmentVector.y) / segmentLengthSquared)
            .coerceIn(0.0f, 1.0f)

    return Offset(startPoint.x + projectionRatio * segmentVector.x, startPoint.y + projectionRatio * segmentVector.y)
}
