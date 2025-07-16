package com.mapconductor.core.polyline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.StateFlowDelegate
import com.mapconductor.core.features.IGeoPoint
import android.graphics.Color
import android.os.Parcelable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class PolylineState(
    points: List<IGeoPoint>,
    id: String? = null,
    strokeColor: Int = Color.BLACK,
    strokeWidth: Dp = 2.dp,
    geodesic: Boolean = false,
    extra: Parcelable? = null,
) {
    val id =
        (
            id ?: polylineId(
                listOf(
                    listHashCode(points),
                    strokeColor,
                    strokeWidth.hashCode(),
                    geodesic.hashCode(),
                    extra?.hashCode() ?: 0,
                ),
            )
        ).toString()
    var strokeColor by mutableIntStateOf(strokeColor)
    var strokeWidth by mutableStateOf(strokeWidth)
    var geodesic by mutableStateOf(geodesic)
    var points by StateFlowDelegate<List<IGeoPoint>>(points)

    var extra by mutableStateOf(extra)

    private fun polylineId(hashCodes: List<Int>): Int =
        hashCodes.reduce { result, hashCode ->
            31 * result + hashCode
        }

    override fun equals(other: Any?): Boolean {
        val otherState = (other as? PolylineState) ?: return false
        return hashCode() == otherState.hashCode()
    }

    override fun hashCode(): Int {
        var result = extra?.hashCode() ?: 0
        result = 31 * result + strokeColor.hashCode()
        result = 31 * result + strokeWidth.hashCode()
        result = 31 * result + geodesic.hashCode()
        result = 31 * result + points.hashCode()
        return result
    }

    private fun <T> listHashCode(list: List<T>): Int {
        var result = 0
        list.forEach {
            result = 31 * result + it.hashCode()
        }
        return result
    }

    fun fingerPrint(): PolylineFingerPrint =
        PolylineFingerPrint(
            id = this.id.hashCode(),
            color = strokeColor,
            width = strokeWidth.hashCode(),
            geodesic = geodesic.toString().hashCode(),
            points = listHashCode(points),
            extra = extra?.hashCode() ?: 0,
        )

    fun asFlow(): Flow<PolylineFingerPrint> = snapshotFlow { fingerPrint() }.distinctUntilChanged()
}

data class PolylineFingerPrint(
    val id: Int,
    val color: Int,
    val width: Int,
    val geodesic: Int,
    val points: Int,
    val extra: Int,
)

typealias OnPolylineEventHandler = (PolylineState) -> Unit
