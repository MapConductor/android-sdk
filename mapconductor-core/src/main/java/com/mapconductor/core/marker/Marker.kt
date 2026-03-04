package com.mapconductor.core.marker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.ComponentState
import com.mapconductor.core.features.GeoPointInterface
import java.io.ByteArrayOutputStream
import java.io.Serializable
import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

// ------- Core Types ----------
class MarkerState(
    position: GeoPointInterface,
    id: String? = null,
    var extra: Serializable? = null,
    icon: MarkerIconInterface? = null,
    animation: MarkerAnimation? = null,
    zIndex: Int? = null,
    clickable: Boolean = true,
    draggable: Boolean = false,
    onClick: OnMarkerEventHandler? = null,
    onDragStart: OnMarkerEventHandler? = null,
    onDrag: OnMarkerEventHandler? = null,
    onDragEnd: OnMarkerEventHandler? = null,
    onAnimateStart: OnMarkerEventHandler? = null,
    onAnimateEnd: OnMarkerEventHandler? = null,
) : ComponentState {
    override val id =
        (
            id ?: markerId(
                listOf(
                    position.hashCode(),
                    extra?.hashCode() ?: 0,
                    icon?.hashCode() ?: 0,
                    clickable.hashCode(),
                    draggable.hashCode(),
                    animation?.hashCode() ?: 0,
                ),
            )
        ).toString()

    private fun markerId(hashCodes: List<Int>): Int =
        hashCodes.reduce { result, hashCode ->
            31 * result + hashCode
        }

    var icon by mutableStateOf<MarkerIconInterface?>(icon)
    var clickable by mutableStateOf(clickable)
    var draggable by mutableStateOf(draggable)
    var onClick by mutableStateOf(onClick)
    var onDragStart by mutableStateOf(onDragStart)
    var onDrag by mutableStateOf(onDrag)
    var onDragEnd by mutableStateOf(onDragEnd)
    var onAnimateStart by mutableStateOf(onAnimateStart)
    var onAnimateEnd by mutableStateOf(onAnimateEnd)
    var zIndex by mutableStateOf<Int?>(zIndex)

    private var internalAnimation by mutableStateOf<MarkerAnimation?>(animation)

    fun animate(animation: MarkerAnimation?) {
        internalAnimation = animation
    }

    fun getAnimation(): MarkerAnimation? = internalAnimation

    private val currentPosition = mutableStateOf(position)
    var position: GeoPointInterface
        get() {
            return currentPosition.value
        }
        set(value) {
            currentPosition.value = value
        }

    fun copy(
        id: String? = this.id,
        position: GeoPointInterface = this.position,
        extra: Serializable? = this.extra,
        icon: MarkerIconInterface? = this.icon,
        zIndex: Int? = this.zIndex,
        clickable: Boolean? = this.clickable,
        draggable: Boolean? = this.draggable,
        onClick: OnMarkerEventHandler? = this.onClick,
        onDragStart: OnMarkerEventHandler? = this.onDragStart,
        onDrag: OnMarkerEventHandler? = this.onDrag,
        onDragEnd: OnMarkerEventHandler? = this.onDragEnd,
        onAnimateStart: OnMarkerEventHandler? = this.onAnimateStart,
        onAnimateEnd: OnMarkerEventHandler? = this.onAnimateEnd,
    ): MarkerState =
        MarkerState(
            id = id, // Keep marker id
            position = position,
            extra = extra,
            icon = icon,
            zIndex = zIndex,
            clickable = clickable ?: this.clickable,
            draggable = draggable ?: this.draggable,
            onClick = onClick,
            onDragStart = onDragStart,
            onDrag = onDrag,
            onDragEnd = onDragEnd,
            onAnimateStart = onAnimateStart,
            onAnimateEnd = onAnimateEnd,
        )

    override fun equals(other: Any?): Boolean {
        val otherState = (other as? MarkerState) ?: return false
        return hashCode() == otherState.hashCode()
    }

    override fun hashCode(): Int {
        var result = extra?.hashCode() ?: 0
        result = 31 * result + clickable.hashCode()
        result = 31 * result + draggable.hashCode()
        result = 31 * result + currentPosition.value.latitude.hashCode()
        result = 31 * result + currentPosition.value.longitude.hashCode()
        result = 31 * result + currentPosition.value.altitude.hashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + zIndex.hashCode()
        return result
    }

    fun fingerPrint(): MarkerFingerPrint =
        MarkerFingerPrint(
            this.id.hashCode(),
            icon.hashCode(),
            clickable.hashCode(),
            draggable.hashCode(),
            currentPosition.value.latitude.hashCode(),
            currentPosition.value.longitude.hashCode(),
            internalAnimation?.hashCode() ?: 1,
            zIndex.hashCode(),
        )

    fun asFlow(): Flow<MarkerFingerPrint> = snapshotFlow { fingerPrint() }.distinctUntilChanged()
}

data class MarkerFingerPrint(
    val id: Int,
    val icon: Int?,
    val clickable: Int,
    val draggable: Int,
    val latitude: Int,
    val longitude: Int,
    val animation: Int?,
    val zIndex: Int,
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
