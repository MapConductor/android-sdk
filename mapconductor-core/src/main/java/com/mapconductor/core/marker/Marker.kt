package com.mapconductor.core.marker

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.IGeoPoint
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

// ------- Core Types ----------
class MarkerState(
    position: IGeoPoint,
    id: String? = null,
    var extra: Parcelable? = null,
    icon: MarkerIcon? = null,
    animation: MarkerAnimation? = null,
    clickable: Boolean = true,
    draggable: Boolean = false,
) {
    val id =
        (
            id ?: markerId(
                listOf(
                    position.hashCode(),
                    extra?.hashCode() ?: 0,
                    icon?.hashCode() ?: 0,
                    clickable.hashCode(),
                    draggable.hashCode(),
                ),
            )
        ).toString()

    private fun markerId(hashCodes: List<Int>): Int =
        hashCodes.reduce { result, hashCode ->
            31 * result + hashCode
        }

    var icon by mutableStateOf<MarkerIcon?>(icon)
    var clickable by mutableStateOf(clickable)
    var draggable by mutableStateOf(draggable)

    private var dragPosition: IGeoPoint = position
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
        id: String? = this.id,
        position: IGeoPoint = this.position,
        extra: Parcelable? = this.extra,
        icon: MarkerIcon? = this.icon,
        clickable: Boolean? = this.clickable,
        draggable: Boolean? = this.draggable,
    ): MarkerState =
        MarkerState(
            id = id, // Keep marker id
            position = position,
            extra = extra,
            icon = icon,
            clickable = clickable ?: this.clickable,
            draggable = draggable ?: this.draggable,
        )

    override fun equals(other: Any?): Boolean {
        val otherState = (other as? MarkerState) ?: return false
        return hashCode() == otherState.hashCode()
    }

    override fun hashCode(): Int {
        var result = extra?.hashCode() ?: 0
        result = 31 * result + clickable.hashCode()
        result = 31 * result + draggable.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + (ResourceProvider.spToPx(1.0).hashCode() ?: 0)
        return result
    }

    fun fingerPrint(): MarkerFingerPrint =
        MarkerFingerPrint(
            this.id.hashCode(),
            ResourceProvider.spToPx(1.0).hashCode(),
            icon.hashCode(),
            clickable.hashCode(),
            draggable.hashCode(),
            internalPosition.hashCode(),
            animation.hashCode(),
        )

    fun asFlow(): Flow<MarkerFingerPrint> = snapshotFlow { fingerPrint() }.distinctUntilChanged()
}

data class MarkerFingerPrint(
    val id: Int,
    val displayMetrics: Int,
    val icon: Int?,
    val clickable: Int,
    val draggable: Int,
    val position: Int,
    val animation: Int?,
)
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
