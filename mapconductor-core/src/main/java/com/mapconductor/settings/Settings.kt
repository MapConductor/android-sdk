package com.mapconductor.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class Settings(
    val tapTolerance: Dp,
    val markerDropAnimeDuration: Int,
) {
    object Default : Settings(
        tapTolerance = 14.dp,
        markerDropAnimeDuration = 100,
    )
}
