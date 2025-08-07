package com.mapconductor.core.groundimage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import android.os.Parcelable
import com.mapconductor.core.StateFlowDelegate
import com.mapconductor.core.features.GeoRectBounds
import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class GroundImageState(
    bounds: GeoRectBounds,
    image: Drawable,
    alpha: Float = 1.0f,
    id: String? = null,
    extra: Parcelable? = null,
) {
    val id = (id ?: generateId(bounds, image, alpha, extra)).toString()

    var bounds by StateFlowDelegate(bounds)
    var image by mutableStateOf(image)
    var alpha by mutableStateOf(alpha)
    var extra by mutableStateOf(extra)

    fun fingerPrint(): GroundImageFingerPrint = GroundImageFingerPrint(
        id = id.hashCode(),
        bounds = bounds.hashCode(),
        image = image.hashCode(),
        alpha = alpha.hashCode(),
        extra = extra?.hashCode() ?: 0,
    )

    fun asFlow(): Flow<GroundImageFingerPrint> =
        snapshotFlow { fingerPrint() }.distinctUntilChanged()

    private fun generateId(
        bounds: GeoRectBounds,
        image: Drawable,
        alpha: Float,
        extra: Parcelable?
    ): Int {
        var result = bounds.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + alpha.hashCode()
        result = 31 * result + (extra?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean =
        (other as? GroundImageState)?.hashCode() == this.hashCode()

    override fun hashCode(): Int = fingerPrint().hashCode()
}

data class GroundImageFingerPrint(
    val id: Int,
    val bounds: Int,
    val image: Int,
    val alpha: Int,
    val extra: Int,
)

data class GroundImageClickEvent(
    val state: GroundImageState,
    val bounds: GeoRectBounds,
)

typealias OnGroundImageEventHandler = (GroundImageClickEvent) -> Unit
