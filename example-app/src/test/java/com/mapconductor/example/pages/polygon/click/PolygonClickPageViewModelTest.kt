package com.mapconductor.example.pages.polygon.click

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.MarkerAnimation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolygonClickPageViewModelTest {
    @Test
    fun `info bubble appears only after the current marker drop finishes`() {
        val viewModel = PolygonClickPageViewModel()

        viewModel.onMapClicked(GeoPoint(35.0, 139.0))
        val firstMarker = requireNotNull(viewModel.markerState.value)

        assertEquals(MarkerAnimation.Drop, firstMarker.getAnimation())
        assertFalse(viewModel.showInfoBubble.value)

        viewModel.onMapClicked(GeoPoint(36.0, 140.0))
        val secondMarker = requireNotNull(viewModel.markerState.value)
        assertNotEquals(firstMarker.id, secondMarker.id)

        firstMarker.onAnimateEnd?.invoke(firstMarker)
        assertFalse(viewModel.showInfoBubble.value)

        secondMarker.onAnimateEnd?.invoke(secondMarker)
        assertTrue(viewModel.showInfoBubble.value)
    }
}
