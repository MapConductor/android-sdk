package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds

fun expandBounds(
    bounds: GeoRectBounds,
    margin: Double,
): GeoRectBounds {
    if (bounds.isEmpty) return bounds

    val span = bounds.toSpan() ?: return bounds
    val center = bounds.center ?: return bounds

    val latMargin = span.latitude * margin / 2.0
    val lngMargin = span.longitude * margin / 2.0

    val expandedBounds = GeoRectBounds()
    expandedBounds.extend(
        GeoPoint(
            center.latitude - span.latitude / 2.0 - latMargin,
            center.longitude - span.longitude / 2.0 - lngMargin,
        ),
    )
    expandedBounds.extend(
        GeoPoint(
            center.latitude + span.latitude / 2.0 + latMargin,
            center.longitude + span.longitude / 2.0 + lngMargin,
        ),
    )

    return expandedBounds
}
