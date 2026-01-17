package com.mapconductor.core.circle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.ComponentState
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.marker.MarkerState
import java.io.Serializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class CircleState(
    center: GeoPointInterface,
    radiusMeters: Double,
    geodesic: Boolean = true,
    clickable: Boolean = true,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 1.dp,
    fillColor: Color =
        Color(
            red = 255,
            green = 255,
            blue = 255,
            alpha = 127,
        ),
    id: String? = null,
    zIndex: Int? = null,
    extra: Serializable? = null,
    onClick: OnCircleEventHandler? = null,
) : ComponentState {
    var center by mutableStateOf(center)
    var clickable by mutableStateOf(clickable)
    var radiusMeters by mutableStateOf(radiusMeters)
    var geodesic by mutableStateOf(geodesic)
    var strokeColor by mutableStateOf(strokeColor)
    var strokeWidth by mutableStateOf(strokeWidth)
    var fillColor by mutableStateOf(fillColor)
    var extra by mutableStateOf(extra)
    var zIndex by mutableStateOf<Int?>(zIndex)
    var onClick by mutableStateOf(onClick)

    override val id =
        (
            id ?: circleId(
                listOf(
                    center.hashCode(),
                    radiusMeters.hashCode(),
                    clickable.hashCode(),
                    geodesic.hashCode(),
                    extra?.hashCode() ?: 0,
                    strokeColor.hashCode(),
                    strokeWidth.hashCode(),
                    fillColor.hashCode(),
                    zIndex.hashCode(),
                ),
            )
        ).toString()

    private fun circleId(hashCodes: List<Int>): Int =
        hashCodes.reduce { result, hashCode ->
            31 * result + hashCode
        }

    fun fingerPrint(): CircleFingerPrint =
        CircleFingerPrint(
            id = this.id.hashCode(),
            center = center.hashCode(),
            radiusMeters = radiusMeters.hashCode(),
            clickable = clickable.hashCode(),
            geodesic = geodesic.hashCode(),
            strokeColor = strokeColor.hashCode(),
            strokeWidth = strokeWidth.hashCode(),
            fillColor = fillColor.hashCode(),
            zIndex = zIndex.hashCode(),
            extra = extra?.hashCode() ?: 0,
        )

    fun asFlow(): Flow<CircleFingerPrint> = snapshotFlow { fingerPrint() }.distinctUntilChanged()

    fun copy(
        center: GeoPointInterface = this.center,
        radiusMeters: Double = this.radiusMeters,
        geodesic: Boolean = this.geodesic,
        strokeColor: Color = this.strokeColor,
        strokeWidth: Dp = this.strokeWidth,
        fillColor: Color =
            Color(
                red = 255,
                green = 255,
                blue = 255,
                alpha = 127,
            ),
        id: String? = this.id,
        zIndex: Int? = this.zIndex,
        extra: Serializable? = this.extra,
        onClick: OnCircleEventHandler? = this.onClick,
    ): CircleState =
        CircleState(
            center = center,
            clickable = clickable,
            radiusMeters = radiusMeters,
            geodesic = geodesic,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            fillColor = fillColor,
            id = id,
            zIndex = zIndex,
            extra = extra,
            onClick = onClick,
        )

    override fun equals(other: Any?): Boolean {
        val otherState = (other as? MarkerState) ?: return false
        return hashCode() == otherState.hashCode()
    }

    override fun hashCode(): Int {
        var result = extra?.hashCode() ?: 0
        result = 31 * result + center.hashCode()
        result = 31 * result + clickable.hashCode()
        result = 31 * result + geodesic.hashCode()
        result = 31 * result + radiusMeters.hashCode()
        result = 31 * result + strokeColor.hashCode()
        result = 31 * result + strokeWidth.hashCode()
        result = 31 * result + fillColor.hashCode()
        result = 31 * result + zIndex.hashCode()
        return result
    }
}

data class CircleFingerPrint(
    val id: Int,
    val center: Int,
    val radiusMeters: Int,
    val clickable: Int,
    val geodesic: Int,
    val strokeColor: Int,
    val strokeWidth: Int,
    val fillColor: Int,
    val zIndex: Int,
    val extra: Int,
)

data class CircleEvent(
    val state: CircleState,
    val clicked: GeoPointInterface,
)

typealias OnCircleEventHandler = (CircleEvent) -> Unit
