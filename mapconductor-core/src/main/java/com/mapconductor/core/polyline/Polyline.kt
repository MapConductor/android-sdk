package com.mapconductor.core.polyline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapconductor.core.marker.MarkerState
import android.graphics.Color
import android.os.Parcelable

class PolylineState(
    id: String? = null,
    var extra: Parcelable? = null,
    color: Int = Color.BLACK,
    width: Float = 2f,
    geometric: Boolean = false,
) {
    val id: String = id ?: hashCode().toString()
    var color by mutableStateOf(color)
    var width by mutableStateOf(width)
    var geometric by mutableStateOf(geometric)

    override fun equals(other: Any?): Boolean {
        val otherState = (other as? MarkerState) ?: return false
        return hashCode() == otherState.hashCode()
    }

    override fun hashCode(): Int {
        var result = extra?.hashCode() ?: 0
        result = 31 * result + color.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + geometric.hashCode()
        return result
    }
}

typealias OnPolylineEventHandler = (PolylineState) -> Unit
