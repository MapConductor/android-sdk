package com.mapconductor.kml

import org.junit.Assert.assertEquals
import org.junit.Test

class KMLStyleProviderTest {
    @Test
    fun defaultProviderUsesFeatureValuesBeforeLayerDefaults() {
        val defaults = KMLTileRenderer.LayerStyle(1, 2, 3f, 4f)
        val feature =
            KMLFeature(
                geometry = KMLGeometry.Empty,
                strokeColor = 10,
                pointRadius = 40f,
            )

        val style = DefaultKMLStyleProvider.getStyle(feature, defaults)

        assertEquals(10, style.strokeColor)
        assertEquals(2, style.fillColor)
        assertEquals(3f, style.strokeWidth)
        assertEquals(40f, style.pointRadius)
    }
}
