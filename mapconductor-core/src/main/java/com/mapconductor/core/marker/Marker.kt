package com.mapconductor.core.marker

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
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
    animation: MarkerAnimation? = null,
    draggable: Boolean = false,
) {
    var icon by mutableStateOf<MarkerIcon?>(icon)
    var draggable by mutableStateOf(draggable)

    private var dragPosition: GeoPoint = position
    private var _isDragging by mutableStateOf(false)
    var isDragging: Boolean
        get() = _isDragging
        internal set(value) {
            _isDragging = value

            Snapshot.withoutReadObservation {
                dragPosition = position
            }
        }

    var animation by mutableStateOf(animation)

    var position by mutableStateOf(position)

    internal val internalPosition by derivedStateOf {
        if (isDragging) dragPosition else position
    }

    fun copy(
        position: GeoPoint = this.position,
        extra: Parcelable? = this.extra,
        icon: MarkerIcon? = this.icon,
        draggable: Boolean? = this.draggable,
    ): MarkerState =
        MarkerState(
            id = this.id, // Keep marker id
            position = position,
            extra = extra,
            icon = icon,
            draggable = draggable ?: this.draggable,
        )

    override fun equals(other: Any?): Boolean {
        val otherState = (other as? MarkerState) ?: return false
        return hashCode() == otherState.hashCode()
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (extra?.hashCode() ?: 0)
        result = 31 * result + draggable.hashCode()
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
