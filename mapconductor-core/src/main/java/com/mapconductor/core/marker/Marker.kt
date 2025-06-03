package com.mapconductor.core.marker

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.MapOverlay
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.IdentifiedPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.ByteArrayOutputStream
import java.util.UUID

// ------- Core Types ----------
typealias OnMarkerClickHandler = (MarkerState) -> Unit

data class MarkerHandlers(
    val onClick: OnMarkerClickHandler? = {},
)

class MarkerState(
    val id: String = UUID.randomUUID().toString(),
    position: GeoPoint,
    var extra: Parcelable? = null,
    var visible: Boolean = true,
    icon: MarkerIcon? = null,
) {

    // -- position and positionState properties --
    private val _position = mutableStateOf(position)
    val positionState: State<GeoPoint> get() = _position
    var position: GeoPoint
        get() = _position.value
        set(value) {
            if (!_position.value.equals(value)) {
                _position.value = value
            }
        }

    // -- icon and iconState properties --
    private val _icon = mutableStateOf<MarkerIcon?>(icon)
    val iconState: State<MarkerIcon?> get() = _icon
    var icon: MarkerIcon?
        get() = _icon.value
        set(value) {
            if (_icon.value != value) {
                _icon.value = value
            }
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

class MarkerOverlay(
    override val flow: StateFlow<List<MarkerEntry>>,
) : MapOverlay<MarkerEntry> {

    override suspend fun render(data: List<MarkerEntry>, controller: MapViewController) {
        controller.addMarkers(data)
    }
}

val LocalMarkerCollector = compositionLocalOf<MutableStateFlow<List<MarkerEntry>>> {
    error("Marker must be under the <MapView />")
}
