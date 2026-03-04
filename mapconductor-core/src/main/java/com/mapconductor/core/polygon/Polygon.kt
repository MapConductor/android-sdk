package com.mapconductor.core.polygon

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.ComponentState
import com.mapconductor.core.StateFlowDelegate
import com.mapconductor.core.features.GeoPointInterface
import java.io.Serializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class PolygonState(
    points: List<GeoPointInterface>,
    holes: List<List<GeoPointInterface>> = emptyList(),
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 2.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    zIndex: Int = 0,
    extra: Serializable? = null,
    onClick: OnPolygonEventHandler? = null,
) : ComponentState {
    override val id =
        (
            id ?: polygonId(
                listOf(
                    listHashCode(points),
                    nestedListHashCode(holes),
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
    var zIndex by mutableStateOf(zIndex)
    var points by StateFlowDelegate<List<GeoPointInterface>>(points)
    var holes by StateFlowDelegate<List<List<GeoPointInterface>>>(holes)
    var extra by mutableStateOf(extra)
    var onClick by mutableStateOf(onClick)

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
        result = 31 * result + zIndex.hashCode()
        result = 31 * result + points.hashCode()
        result = 31 * result + holes.hashCode()
        return result
    }

    private fun <T> listHashCode(list: List<T>): Int {
        var result = 0
        list.forEach {
            result = 31 * result + it.hashCode()
        }
        return result
    }

    private fun <T> nestedListHashCode(list: List<List<T>>): Int {
        var result = 0
        list.forEach { inner ->
            result = 31 * result + listHashCode(inner)
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
            zIndex = zIndex,
            points = listHashCode(points),
            holes = nestedListHashCode(holes),
            extra = extra?.hashCode() ?: 0,
        )

    fun copy(
        points: List<GeoPointInterface> = this.points,
        holes: List<List<GeoPointInterface>> = this.holes,
        id: String? = this.id,
        strokeColor: Color = this.strokeColor,
        strokeWidth: Dp = this.strokeWidth,
        fillColor: Color = this.fillColor,
        geodesic: Boolean = this.geodesic,
        zIndex: Int = this.zIndex,
        extra: Serializable? = this.extra,
        onClick: OnPolygonEventHandler? = this.onClick,
    ): PolygonState =
        PolygonState(
            points = points,
            holes = holes,
            id = id,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            fillColor = fillColor,
            geodesic = geodesic,
            zIndex = zIndex,
            extra = extra,
            onClick = onClick,
        )

    fun asFlow(): Flow<PolygonFingerPrint> = snapshotFlow { fingerPrint() }.distinctUntilChanged()
}

data class PolygonFingerPrint(
    val id: Int,
    val strokeColor: Int,
    val strokeWidth: Int,
    val fillColor: Int,
    val geodesic: Int,
    val zIndex: Int,
    val points: Int,
    val holes: Int,
    val extra: Int,
)

data class PolygonEvent(
    val state: PolygonState,
    val clicked: GeoPointInterface,
)

typealias OnPolygonEventHandler = (PolygonEvent) -> Unit
