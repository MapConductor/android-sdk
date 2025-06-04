package com.mapconductor.here

import com.here.sdk.core.Anchor2D
import com.here.sdk.mapview.ImageFormat
import com.here.sdk.mapview.MapImage
import com.mapconductor.core.marker.BitmapIcon

internal fun BitmapIcon.toMapImage(): MapImage =
    MapImage(
        this.toByteArray(),
        ImageFormat.PNG,
        this.bitmap.width.toLong(),
        this.bitmap.height.toLong(),
    )

internal fun BitmapIcon.toAnchor2D(): Anchor2D = Anchor2D(this.anchor.x.toDouble(), this.anchor.y.toDouble())
