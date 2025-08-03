package com.mapconductor.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class Settings(
    val tapTolerance: Dp,
    val markerDropAnimateDuration: Int,
    val markerBounceAnimateDuration: Int,
    val iconSize: Dp,
    val iconStroke: Dp,
    val composeEventDebounce: Long,
) {
    object Default : Settings(
        tapTolerance = 14.dp,
        markerDropAnimateDuration = 100,
        markerBounceAnimateDuration = 2000,
        iconSize = MarkerIconSize.Regular,
        iconStroke = 1.dp,
        composeEventDebounce = 5,
    )
}
object MarkerIconSize {
    val ExtraSmall: Dp = 12.dp
    val Small: Dp = 24.dp
    val Regular: Dp = 32.dp
    val Large: Dp = 48.dp
    val ExtraLarge: Dp = 60.dp
}
