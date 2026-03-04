package com.mapconductor.core.groundimage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.mapconductor.core.ComponentState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import java.io.Serializable
import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class GroundImageState(
    bounds: GeoRectBounds,
    image: Drawable,
    opacity: Float = 1.0f,
    tileSize: Int = GroundImageTileProvider.DEFAULT_TILE_SIZE,
    id: String? = null,
    extra: Serializable? = null,
    onClick: OnGroundImageEventHandler? = null,
) : ComponentState {
    override val id = (id ?: generateId(bounds, image, opacity, tileSize, extra)).toString()

//    var bounds by StateFlowDelegate(bounds)
    var bounds by mutableStateOf(bounds)
    var image by mutableStateOf(image)
    var opacity by mutableStateOf(opacity)
    var tileSize by mutableStateOf(tileSize)
    var extra by mutableStateOf(extra)
    var onClick by mutableStateOf(onClick)

    fun fingerPrint(): GroundImageFingerPrint =
        GroundImageFingerPrint(
            id = id.hashCode(),
            bounds = bounds.hashCode(),
            image = image.hashCode(),
            opacity = opacity.hashCode(),
            tileSize = tileSize.hashCode(),
            extra = extra?.hashCode() ?: 0,
        )

    fun asFlow(): Flow<GroundImageFingerPrint> =
        snapshotFlow {
            fingerPrint()
        }.distinctUntilChanged()

    private fun generateId(
        bounds: GeoRectBounds,
        image: Drawable,
        opacity: Float,
        tileSize: Int,
        extra: Serializable?,
    ): Int {
        var result = bounds.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + opacity.hashCode()
        result = 31 * result + tileSize.hashCode()
        result = 31 * result + (extra?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean = (other as? GroundImageState)?.hashCode() == this.hashCode()

    override fun hashCode(): Int = fingerPrint().hashCode()
}

data class GroundImageFingerPrint(
    val id: Int,
    val bounds: Int,
    val image: Int,
    val opacity: Int,
    val tileSize: Int,
    val extra: Int,
)

data class GroundImageEvent(
    val state: GroundImageState,
    val clicked: GeoPoint?,
)

typealias OnGroundImageEventHandler = (GroundImageEvent) -> Unit
