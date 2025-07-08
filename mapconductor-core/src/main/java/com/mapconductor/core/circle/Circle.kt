package com.mapconductor.core.circle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.IGeoPoint
import android.os.Parcelable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class CircleState(
    center: IGeoPoint,
    radius: Double,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color(
        red = 255,
        green = 255,
        blue = 255,
        alpha = 127,
    ),
    id: String? = null,
    extra: Parcelable? = null,
) {
    var center by mutableStateOf(center)
    var radius by mutableStateOf(radius)
    var strokeColor by mutableStateOf(strokeColor)
    var strokeWidth by mutableStateOf(strokeWidth)
    var fillColor by mutableStateOf(fillColor)
    var extra by mutableStateOf(extra)

    val id =
        (
                id ?: circleId(
                    listOf(
                        center.hashCode(),
                        radius.hashCode(),
                        extra?.hashCode() ?: 0,
                        strokeColor.hashCode(),
                        strokeWidth.hashCode(),
                        fillColor.hashCode(),
                    ),
                )
                ).toString()

    private fun circleId(hashCodes: List<Int>): Int =
        hashCodes.reduce { result, hashCode ->
            31 * result + hashCode
        }

    fun fingerPrint(): CircleFingerPrint {
        return CircleFingerPrint(
            id = this.id.hashCode(),
            center = center.hashCode(),
            radius = radius.hashCode(),
            strokeColor = strokeColor.hashCode(),
            strokeWidth = strokeWidth.hashCode(),
            fillColor = fillColor.hashCode(),
            extra = extra?.hashCode() ?: 0,
        )
    }

    fun asFlow(): Flow<CircleFingerPrint> = snapshotFlow { fingerPrint() }.distinctUntilChanged()
}

data class CircleFingerPrint(
    val id: Int,
    val center: Int,
    val radius: Int,
    val strokeColor: Int,
    val strokeWidth: Int,
    val fillColor: Int,
    val extra: Int,
)

typealias OnCircleEventHandler = (CircleState) -> Unit
