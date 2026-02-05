package com.mapconductor.core.polyline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.ComponentState
import com.mapconductor.core.features.GeoPointInterface
import java.io.Serializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class PolylineState(
    points: List<GeoPointInterface>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    geodesic: Boolean = false,
    zIndex: Int = 0,
    extra: Serializable? = null,
    onClick: OnPolylineEventHandler? = null,
) : ComponentState {
    override val id =
        (
            id ?: polylineId(
                listOf(
                    listHashCode(points),
                    strokeColor.hashCode(),
                    strokeWidth.hashCode(),
                    geodesic.hashCode(),
                    extra?.hashCode() ?: 0,
                ),
            )
        ).toString()
    var strokeColor by mutableStateOf(strokeColor)
    var strokeWidth by mutableStateOf(strokeWidth)
    var geodesic by mutableStateOf(geodesic)
    var zIndex by mutableStateOf(zIndex)
    var points by mutableStateOf<List<GeoPointInterface>>(points)
    var extra by mutableStateOf(extra)
    var onClick by mutableStateOf(onClick)

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
        result = 31 * result + this@PolylineState.strokeColor.hashCode()
        result = 31 * result + this@PolylineState.strokeWidth.hashCode()
        result = 31 * result + geodesic.hashCode()
        result = 31 * result + zIndex.hashCode()
        result = 31 * result + listHashCode(points)
        return result
    }

    fun copy(
        points: List<GeoPointInterface> = this.points,
        id: String? = this.id,
        strokeColor: Color = this.strokeColor,
        strokeWidth: Dp = this.strokeWidth,
        geodesic: Boolean = this.geodesic,
        zIndex: Int = this.zIndex,
        extra: Serializable? = this.extra,
        onClick: OnPolylineEventHandler? = this.onClick,
    ): PolylineState =
        PolylineState(
            points = points,
            id = id,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            geodesic = geodesic,
            zIndex = zIndex,
            extra = extra,
            onClick = onClick,
        )

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
            strokeColor = this@PolylineState.strokeColor.hashCode(),
            strokeWidth = this@PolylineState.strokeWidth.hashCode(),
            geodesic = geodesic.toString().hashCode(),
            zIndex = zIndex.hashCode(),
            points = listHashCode(points),
            extra = extra?.hashCode() ?: 0,
        )

    fun asFlow(): Flow<PolylineFingerPrint> = snapshotFlow { fingerPrint() }.distinctUntilChanged()
}

data class PolylineFingerPrint(
    val id: Int,
    val strokeColor: Int,
    val strokeWidth: Int,
    val geodesic: Int,
    val zIndex: Int,
    val points: Int,
    val extra: Int,
)

data class PolylineEvent(
    val state: PolylineState,
    val clicked: GeoPointInterface,
)

typealias OnPolylineEventHandler = (PolylineEvent) -> Unit
