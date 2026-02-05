package com.mapconductor.core.raster

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.mapconductor.core.ComponentState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class RasterLayerState(
    source: RasterLayerSource,
    opacity: Float = 1.0f,
    visible: Boolean = true,
    zIndex: Int = 0,
    userAgent: String? = null,
    debug: Boolean = false,
    id: String? = null,
    extraHeaders: Map<String, String>? = null,
) : ComponentState {
    override val id =
        (
            id ?: rasterLayerId(
                listOf(
                    source.hashCode(),
                    opacity.hashCode(),
                    visible.hashCode(),
                    debug.hashCode(),
                    extraHeaders?.hashCode() ?: 0,
                ),
            )
        ).toString()

    var source by mutableStateOf(source)
    var opacity by mutableStateOf(opacity)
    var visible by mutableStateOf(visible)
    var zIndex by mutableStateOf(zIndex)
    var userAgent by mutableStateOf(userAgent)
    var debug by mutableStateOf(debug)
    var extraHeaders by mutableStateOf(extraHeaders)

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
        result = 31 * result + zIndex.hashCode()
        result = 31 * result + debug.hashCode()
        result = 31 * result + (extraHeaders?.hashCode() ?: 0)
        result = 31 * result + (userAgent?.hashCode() ?: 0)
        return result
    }

    fun copy(
        source: RasterLayerSource = this.source,
        opacity: Float = this.opacity,
        visible: Boolean = this.visible,
        zIndex: Int = this.zIndex,
        debug: Boolean = this.debug,
        userAgent: String? = this.userAgent,
        id: String? = this.id,
        extraHeaders: Map<String, String>? = this.extraHeaders,
    ): RasterLayerState =
        RasterLayerState(
            source = source,
            opacity = opacity,
            visible = visible,
            zIndex = zIndex,
            userAgent = userAgent,
            debug = debug,
            id = id,
            extraHeaders = extraHeaders,
        )

    fun fingerPrint(): RasterLayerFingerPrint =
        RasterLayerFingerPrint(
            id = id.hashCode(),
            source = source.hashCode(),
            opacity = opacity.hashCode(),
            visible = visible.hashCode(),
            zIndex = zIndex.hashCode(),
            userAgent = userAgent?.hashCode() ?: 0,
            debug = debug.hashCode(),
            extra = extraHeaders?.hashCode() ?: 0,
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
    val zIndex: Int,
    val userAgent: Int,
    val debug: Int,
    val extra: Int,
)

data class RasterLayerEvent(
    val state: RasterLayerState,
)

typealias OnRasterLayerEventHandler = (RasterLayerEvent) -> Unit
