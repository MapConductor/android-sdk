package com.mapconductor.core.raster

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import java.io.Serializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class RasterLayerState(
    source: RasterSource,
    opacity: Float = 1.0f,
    visible: Boolean = true,
    id: String? = null,
    extra: Serializable? = null,
) {
    val id =
        (
            id ?: rasterLayerId(
                listOf(
                    source.hashCode(),
                    opacity.hashCode(),
                    visible.hashCode(),
                    extra?.hashCode() ?: 0,
                ),
            )
        ).toString()

    var source by mutableStateOf(source)
    var opacity by mutableStateOf(opacity)
    var visible by mutableStateOf(visible)
    var extra by mutableStateOf(extra)

    private fun rasterLayerId(hashCodes: List<Int>): Int =
        hashCodes.reduce { result, hashCode ->
            31 * result + hashCode
        }

    override fun equals(other: Any?): Boolean {
        val otherState = (other as? RasterLayerState) ?: return false
        return hashCode() == otherState.hashCode()
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + opacity.hashCode()
        result = 31 * result + visible.hashCode()
        result = 31 * result + (extra?.hashCode() ?: 0)
        return result
    }

    fun copy(
        source: RasterSource = this.source,
        opacity: Float = this.opacity,
        visible: Boolean = this.visible,
        id: String? = this.id,
        extra: Serializable? = this.extra,
    ): RasterLayerState =
        RasterLayerState(
            source = source,
            opacity = opacity,
            visible = visible,
            id = id,
            extra = extra,
        )

    fun fingerPrint(): RasterLayerFingerPrint =
        RasterLayerFingerPrint(
            id = id.hashCode(),
            source = source.hashCode(),
            opacity = opacity.hashCode(),
            visible = visible.hashCode(),
            extra = extra?.hashCode() ?: 0,
        )

    fun asFlow(): Flow<RasterLayerFingerPrint> =
        snapshotFlow { fingerPrint() }
            .distinctUntilChanged()
}

data class RasterLayerFingerPrint(
    val id: Int,
    val source: Int,
    val opacity: Int,
    val visible: Int,
    val extra: Int,
)

data class RasterLayerEvent(
    val state: RasterLayerState,
)

typealias OnRasterLayerEventHandler = (RasterLayerEvent) -> Unit
