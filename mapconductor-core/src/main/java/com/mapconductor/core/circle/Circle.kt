package com.mapconductor.core.circle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.mapconductor.core.features.IGeoPoint
import android.graphics.Color
import android.os.Parcelable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class CircleState(
    center: IGeoPoint,
    radius: Int,
    strokeColor: Int = Color.RED,
    strokeWidth: Int = 2,
    fillColor: Int = Color.argb(127, 255, 255, 255),
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
                        radius,
                        extra?.hashCode() ?: 0,
                        strokeColor,
                        strokeWidth,
                        fillColor,
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
            radius = radius,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            fillColor = fillColor,
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
