package com.mapconductor.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class Settings(
    val tapTolerance: Dp,
    val markerDropAnimateDuration: Int,
    val markerBounceAnimateDuration: Int,
) {
    object Default : Settings(
        tapTolerance = 7.dp,
        markerDropAnimateDuration = 100,
        markerBounceAnimateDuration = 2000,
    )
}
