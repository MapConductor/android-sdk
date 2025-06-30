package com.mapconductor.core.circle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.room.util.copy
import com.mapconductor.core.features.IGeoPoint
import android.graphics.Color

class CircleState(
    center: IGeoPoint,
    radius: Int,
    strokeColor: Int = Color.RED,
    strokeWidth: Float = 2.0f,
    fillColor: Int = Color.argb(127, 255, 255, 255),
) {
    var center by mutableStateOf(center)
    var radius by mutableStateOf(radius)
    var strokeColor by mutableStateOf(strokeColor)
    var strokeWidth by mutableStateOf(strokeWidth)
    var fillColor by mutableStateOf(fillColor)
}
