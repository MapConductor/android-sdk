package com.mapconductor.core.marker

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.IdentifiedPoint
import java.io.ByteArrayOutputStream
import java.util.UUID
import android.graphics.Bitmap
import android.os.Parcelable

// ------- Core Types ----------
typealias OnMarkerClickHandler = (MarkerState) -> Unit

data class MarkerHandlers(
    val onClick: OnMarkerClickHandler? = {},
)

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
        icon: MarkerIcon? = this.icon
    ): MarkerState {
        return MarkerState(
            id = this.id,  // Keep marker id
            position = position,
            extra = extra,
            icon = icon,
        )
    }

    override fun equals(other: Any?): Boolean {
        val otherState = (other as? MarkerState) ?: return false
        return position == otherState.position &&
            id == otherState.id &&
            extra == otherState.extra &&
            icon == otherState.icon
    }

    //    companion object {
//        val Saver: Saver<MarkerState, Bundle> = Saver(
//            save = { state ->
//                Bundle().apply {
//                    val position = state.position
//                    putDoubleArray("position", doubleArrayOf(
//                        position.latitude,
//                        position.longitude,
//                        position.altitude,
//                    ))
//
//                    state.getIcon()?.let { icon ->
//                        putInt("resId", icon.resId)
//                    }
//                }
//            },
//            restore = { stored ->
//                val icon = stored.getInt("resId").let { resId ->
//                    ResourceIcon(resId = resId)
//                }
//                val position = stored.getDoubleArray("position")!!.let { latLngAlt ->
//                    GeoPoint(
//                        latitude = latLngAlt[0],
//                        longitude = latLngAlt[1],
//                        altitude = latLngAlt[2],
//                    )
//                }
//                MarkerState(
//                    initialPosition = position,
//                    initialIcon = icon,
//                )
//            }
//        )
//    }
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

data class MarkerEntry(
    val state: MarkerState,
    val handlers: MarkerHandlers,
) : IdentifiedPoint {
    override val id: String get() = state.id
    override val point: IGeoPoint get() = state.position
}

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
