package com.mapconductor.core.polyline

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PolylineEntityTest {
    @Test
    fun testBoundsCalculation() {
        val points =
            listOf(
                GeoPoint(35.0, 139.0),
                GeoPoint(36.0, 140.0),
                GeoPoint(34.0, 138.0),
            )

        val state =
            PolylineState(
                points = points,
                strokeColor = Color.Red,
                strokeWidth = 2.dp,
            )

        val entity =
            PolylineEntity(
                polyline = "test_polyline",
                state = state,
            )

        val bounds = entity.bounds

        assertTrue(
            "Bounds should contain all points",
            points.all { bounds.contains(it) },
        )

        assertEquals("South bound should be minimum latitude", 34.0, bounds.southWest!!.latitude, 0.001)
        assertEquals("North bound should be maximum latitude", 36.0, bounds.northEast!!.latitude, 0.001)
        assertEquals("West bound should be minimum longitude", 138.0, bounds.southWest!!.longitude, 0.001)
        assertEquals("East bound should be maximum longitude", 140.0, bounds.northEast!!.longitude, 0.001)
    }

    @Test
    fun testBoundsLazyCalculation() {
        val initialPoints =
            listOf(
                GeoPoint(35.0, 139.0),
                GeoPoint(36.0, 140.0),
            )

        val state =
            PolylineState(
                points = initialPoints,
                strokeColor = Color.Red,
                strokeWidth = 2.dp,
            )

        val entity =
            PolylineEntity(
                polyline = "test_polyline",
                state = state,
            )

        val firstBounds = entity.bounds
        val secondBounds = entity.bounds

        // Should return the same cached instance
        assertTrue("Bounds should be cached", firstBounds === secondBounds)

        // Modify points and verify bounds are recalculated
        state.points =
            listOf(
                GeoPoint(35.0, 139.0),
                GeoPoint(36.0, 140.0),
                GeoPoint(37.0, 141.0), // Add new point
            )

        val newBounds = entity.bounds

        // Should be a different instance with updated bounds
        assertNotSame("Bounds should be recalculated when points change", firstBounds, newBounds)
        assertTrue(
            "New bounds should contain the new point",
            newBounds.contains(GeoPoint(37.0, 141.0)),
        )
    }

    @Test
    fun testBoundsWithSinglePoint() {
        val singlePoint = listOf(GeoPoint(35.0, 139.0))

        val state =
            PolylineState(
                points = singlePoint,
                strokeColor = Color.Blue,
                strokeWidth = 1.dp,
            )

        val entity =
            PolylineEntity(
                polyline = "test_polyline",
                state = state,
            )

        val bounds = entity.bounds

        assertTrue("Bounds should contain the single point", bounds.contains(singlePoint[0]))
        assertEquals("Southwest should equal the point", singlePoint[0], bounds.southWest)
        assertEquals("Northeast should equal the point", singlePoint[0], bounds.northEast)
    }
}
