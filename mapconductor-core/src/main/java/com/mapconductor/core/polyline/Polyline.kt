package com.mapconductor.core.polyline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.mapconductor.core.StateFlowDelegate
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.marker.MarkerState
import android.graphics.Color
import android.os.Parcelable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class PolylineState(
    points: List<IGeoPoint>,
    id: String? = null,
    color: Int = Color.BLACK,
    width: Int = 2,
    geodesic: Boolean = false,
    extra: Parcelable? = null,
) {
    val id =
        (
            id ?: polylineId(
                listOf(
                    listHashCode(points),
                    color,
                    width,
                    geodesic.hashCode(),
                    extra?.hashCode() ?: 0,
                ),
            )
        ).toString()
    var color by mutableStateOf(color)
    var width by mutableStateOf(width)
    var geodesic by mutableStateOf(geodesic)
    var points by StateFlowDelegate<List<IGeoPoint>>(points)

    var extra by mutableStateOf(extra)

    private fun polylineId(hashCodes: List<Int>): Int =
        hashCodes.reduce { result, hashCode ->
            31 * result + hashCode
        }

    override fun equals(other: Any?): Boolean {
        val otherState = (other as? MarkerState) ?: return false
        return hashCode() == otherState.hashCode()
    }

    override fun hashCode(): Int {
        var result = extra?.hashCode() ?: 0
        result = 31 * result + color.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + geodesic.hashCode()
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
            color = color,
            width = width,
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
