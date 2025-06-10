package com.mapconductor.core.marker

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.features.GeoPoint
import java.io.ByteArrayOutputStream
import java.util.UUID
import android.graphics.Bitmap
import android.os.Parcelable

// ------- Core Types ----------
class MarkerState(
    val id: String = UUID.randomUUID().toString(),
    position: GeoPoint,
    var extra: Parcelable? = null,
    icon: MarkerIcon? = null,
) {
    // -- position and positionState properties --
    private val _position = mutableStateOf(position)
    var position: GeoPoint
        get() = _position.value
        set(value) {
            if (!_position.value.equals(value)) {
                _position.value = value
            }
        }

    // -- icon and iconState properties --
    private val _icon = mutableStateOf<MarkerIcon?>(icon)
    var icon: MarkerIcon?
        get() = _icon.value
        set(value) {
            if (_icon.value != value) {
                _icon.value = value
            }
        }

    fun copy(
        position: GeoPoint = this.position,
        extra: Parcelable? = this.extra,
        icon: MarkerIcon? = this.icon,
    ): MarkerState =
        MarkerState(
            id = this.id, // Keep marker id
            position = position,
            extra = extra,
            icon = icon,
        )

    override fun equals(other: Any?): Boolean {
        val otherState = (other as? MarkerState) ?: return false
        return position == otherState.position &&
            id == otherState.id &&
            extra == otherState.extra &&
            icon == otherState.icon
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (extra?.hashCode() ?: 0)
        result = 31 * result + _position.hashCode()
        result = 31 * result + _icon.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        return result
    }
}

typealias OnMarkerEventHandler = (MarkerState) -> Unit

data class BitmapIcon(
    val bitmap: Bitmap,
    val anchor: Offset,
    val size: Size,
) {
    fun toByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}
