package com.mapconductor.core.features

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoRectBoundsTest {
    @Test
    fun testIntersects_normalCase() {
        val bounds1 = GeoRectBounds()
        bounds1.extend(GeoPoint(10.0, 10.0))
        bounds1.extend(GeoPoint(20.0, 20.0))

        val bounds2 = GeoRectBounds()
        bounds2.extend(GeoPoint(15.0, 15.0))
        bounds2.extend(GeoPoint(25.0, 25.0))

        assertTrue("Overlapping bounds should intersect", bounds1.intersects(bounds2))
        assertTrue("Intersect should be symmetric", bounds2.intersects(bounds1))
    }

    @Test
    fun testIntersects_noOverlap() {
        val bounds1 = GeoRectBounds()
        bounds1.extend(GeoPoint(10.0, 10.0))
        bounds1.extend(GeoPoint(20.0, 20.0))

        val bounds2 = GeoRectBounds()
        bounds2.extend(GeoPoint(30.0, 30.0))
        bounds2.extend(GeoPoint(40.0, 40.0))

        assertFalse("Non-overlapping bounds should not intersect", bounds1.intersects(bounds2))
        assertFalse("Intersect should be symmetric", bounds2.intersects(bounds1))
    }

    // TODO: Add tests for dateline-crossing intersection once GeoRectBounds.intersects() is fixed
    // The current implementation has issues with dateline-crossing bounds
}
