package com.mapconductor.marker.strategy

import com.mapconductor.core.features.GeoPoint

/**
 * Simple test utility to verify NativeMarkerIndex functionality
 */
object NativeMarkerIndexTest {
    fun runBasicTest(): String =
        try {
            val nativeIndex = NativeMarkerIndex.create(baseHexSideLength = 1000, zoom = 15.0)

            // Add some test markers
            nativeIndex.registerMarker("marker1", GeoPoint(37.7749, -122.4194), true) // San Francisco
            nativeIndex.registerMarker("marker2", GeoPoint(40.7128, -74.0060), true) // New York
            nativeIndex.registerMarker("marker3", GeoPoint(34.0522, -118.2437), true) // Los Angeles

            // Test findNearest
            val queryPoint = GeoPoint(37.7849, -122.4094) // Close to San Francisco
            val nearestId = nativeIndex.findNearest(queryPoint)

            val result =
                buildString {
                    append("Test Results:\n")
                    append("Total markers: ${nativeIndex.markerCount()}\n")
                    append("Query point: (${queryPoint.latitude}, ${queryPoint.longitude})\n")
                    append("Nearest marker: $nearestId\n")

                    if (nearestId != null) {
                        append("✅ findNearest returned: $nearestId")
                    } else {
                        append("❌ findNearest returned null")
                    }
                }

            nativeIndex.destroy()
            result
        } catch (e: Exception) {
            "❌ Test failed with exception: ${e.message}"
        }
}
