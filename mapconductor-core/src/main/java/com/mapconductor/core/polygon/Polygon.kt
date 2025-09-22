package com.mapconductor.core.polygon

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.StateFlowDelegate
import com.mapconductor.core.features.GeoPoint
import android.os.Parcelable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class PolygonState(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 2.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    extra: Parcelable? = null,
) {
    val id =
        (
            id ?: polygonId(
                listOf(
                    listHashCode(points),
                    strokeColor.hashCode(),
                    strokeWidth.hashCode(),
                    fillColor.hashCode(),
                    geodesic.hashCode(),
                    extra?.hashCode() ?: 0,
                ),
            )
        ).toString()
    var strokeColor by mutableStateOf(strokeColor)
    var strokeWidth by mutableStateOf(strokeWidth)
    var fillColor by mutableStateOf(fillColor)
    var geodesic by mutableStateOf(geodesic)
    var points by StateFlowDelegate<List<GeoPoint>>(points)
    var extra by mutableStateOf(extra)

    private fun polygonId(hashCodes: List<Int>): Int =
        hashCodes.reduce { result, hashCode ->
            31 * result + hashCode
        }

    override fun equals(other: Any?): Boolean {
        val otherState = (other as? PolygonState) ?: return false
        return hashCode() == otherState.hashCode()
    }

    override fun hashCode(): Int {
        var result = extra?.hashCode() ?: 0
        result = 31 * result + this@PolygonState.strokeColor.hashCode()
        result = 31 * result + this@PolygonState.strokeWidth.hashCode()
        result = 31 * result + this@PolygonState.fillColor.hashCode()
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

    fun fingerPrint(): PolygonFingerPrint =
        PolygonFingerPrint(
            id = this.id.hashCode(),
            strokeColor = this@PolygonState.strokeColor.hashCode(),
            strokeWidth = this@PolygonState.strokeWidth.hashCode(),
            fillColor = this@PolygonState.fillColor.hashCode(),
            geodesic = geodesic.toString().hashCode(),
            points = listHashCode(points),
            extra = extra?.hashCode() ?: 0,
        )

    fun asFlow(): Flow<PolygonFingerPrint> = snapshotFlow { fingerPrint() }.distinctUntilChanged()
}

data class PolygonFingerPrint(
    val id: Int,
    val strokeColor: Int,
    val strokeWidth: Int,
    val fillColor: Int,
    val geodesic: Int,
    val points: Int,
    val extra: Int,
)

data class PolygonEvent(
    val state: PolygonState,
    val clicked: GeoPoint?,
)

typealias OnPolygonEventHandler = (PolygonEvent) -> Unit
