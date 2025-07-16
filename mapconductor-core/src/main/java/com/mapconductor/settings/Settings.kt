package com.mapconductor.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class Settings(
    val tapTolerance: Dp,
    val markerDropAnimateDuration: Int,
    val markerBounceAnimateDuration: Int,
    val iconSize: Dp,
    val iconStroke: Dp,
) {
    object Default : Settings(
        tapTolerance = 14.dp,
        markerDropAnimateDuration = 100,
        markerBounceAnimateDuration = 2000,
        iconSize = 34.dp,
        iconStroke = 0.5.dp,
    )
}
