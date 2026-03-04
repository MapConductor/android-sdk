package com.mapconductor.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

sealed class Settings(
    val tapTolerance: Dp,
    val markerDropAnimateDuration: Long,
    val markerBounceAnimateDuration: Long,
    val iconSize: Dp,
    val iconStroke: Dp,
    val composeEventDebounce: Duration,
) {
    object Default : Settings(
        tapTolerance = 14.dp,
        markerDropAnimateDuration = 300,
        markerBounceAnimateDuration = 2000,
        iconSize = MarkerIconSize.Regular,
        iconStroke = 1.dp,
        composeEventDebounce = 5.milliseconds,
    )
}

object MarkerIconSize {
    val Small: Dp = 32.dp
    val Regular: Dp = 48.dp
    val Large: Dp = 60.dp
}
