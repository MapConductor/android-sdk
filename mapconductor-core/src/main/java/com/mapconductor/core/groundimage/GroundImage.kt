package com.mapconductor.core.groundimage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.features.GeoRectBounds
import java.io.Serializable
import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class GroundImageState(
    bounds: GeoRectBounds,
    image: Drawable,
    opacity: Float = 1.0f,
    id: String? = null,
    extra: Serializable? = null,
) {
    val id = (id ?: generateId(bounds, image, opacity, extra)).toString()

//    var bounds by StateFlowDelegate(bounds)
    var bounds by mutableStateOf(bounds)
    var image by mutableStateOf(image)
    var opacity by mutableStateOf(opacity)
    var extra by mutableStateOf(extra)

    fun fingerPrint(): GroundImageFingerPrint =
        GroundImageFingerPrint(
            id = id.hashCode(),
            bounds = bounds.hashCode(),
            image = image.hashCode(),
            opacity = opacity.hashCode(),
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
        extra: Serializable?,
    ): Int {
        var result = bounds.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + opacity.hashCode()
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
    val extra: Int,
)

data class GroundImageEvent(
    val state: GroundImageState,
    val clicked: GeoPointImpl?,
)

typealias OnGroundImageEventHandler = (GroundImageEvent) -> Unit
