package com.mapconductor.core.marker

import android.os.Parcelable
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointBuilder

// ------- DSL Marker ---------
@DslMarker
annotation class MarkerDsl

@MarkerDsl
class MarkerBuilder {
    private val point = GeoPointBuilder()
    private val extraData: Parcelable? = null
    private val iconData: MarkerIconProp? = null

    var onClick: MarkerClickHandler = {}

    fun position(block: GeoPointBuilder.() -> Unit) {
        point.apply(block)
    }

    fun extra(block: Parcelable?.() -> Unit) {
        extraData.apply(block)
    }

    fun icon(block: MarkerIconProp?.() -> Unit) {
        iconData.apply(block)
    }

    fun build(): MarkerEntry {
        val initialPosition = GeoPoint(
            latitude = point.latitude,
            longitude = point.longitude,
            altitude = point.altitude ?: 0.0,
        )

        val state = MarkerState(
            position = initialPosition,
            extra = extraData,
            icon = iconData,
        )

        val handlers = MarkerHandlers(
            onClick = onClick,
        )

        return MarkerEntry(
            state = state,
            handlers = handlers,
        )
    }
}