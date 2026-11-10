package com.mapconductor.example.pages.polyline.boston

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BostonRoadsDataTest {
    /**
     * Two roads (fields name | road class | lines = 56, 1e-5 degree), written by
     * the generator (the React sample's tools/boston/fixture.py). The same bytes
     * are decoded by the React and iOS tests.
     */
    private val fixture =
        hex(
            "4d4350470200000002000000380510436166c3a92053747265657420e29895010103b284850483b8e30614140a2800060102" +
                "ef56973e1313",
        )

    private fun hex(text: String) = text.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    @Test
    fun `decodes the name, the class and the open path of each road`() {
        val roads = decodeRoads(fixture)
        assertEquals(2, roads.size)

        val first = roads[0]
        assertEquals("road-0", first.id)
        assertEquals("Café Street ☕", first.name)
        assertEquals(RoadClass.PRIMARY, first.roadClass)
        // Three points, left open: no closing point added.
        assertArrayEquals(
            doubleArrayOf(-71.0605, 42.35545, -71.0604, 42.35555, -71.0602, 42.3556),
            first.path,
            1e-9,
        )

        val second = roads[1]
        assertEquals("road-1", second.id)
        assertNull(second.name)
        assertEquals(RoadClass.PATH, second.roadClass)
        assertArrayEquals(doubleArrayOf(-71.1, 42.3, -71.1001, 42.2999), second.path, 1e-9)
    }

    @Test
    fun `the bundled roads file has every road of the city`() {
        val bytes = File("src/main/assets/boston/boston-city-roads.bin").readBytes()
        assertEquals(5, bytes[13].toInt())
        val roads = decodeRoads(bytes)
        assertEquals(54645, roads.size)
        assertEquals(234765, roads.sumOf { it.path.size / 2 })
        assertEquals(
            mapOf(
                RoadClass.HIGHWAY to 911,
                RoadClass.PRIMARY to 2320,
                RoadClass.SECONDARY to 2829,
                RoadClass.TERTIARY to 1229,
                RoadClass.LOCAL to 11226,
                RoadClass.SERVICE to 9950,
                RoadClass.PATH to 26180,
            ),
            roads.groupingBy { it.roadClass }.eachCount(),
        )
        assertEquals(18838, roads.count { it.name != null })
        assertTrue(roads.all { it.path.size >= 4 })
        val first = roads.first()
        assertEquals("Saint Vincent Section", first.name)
        assertEquals(RoadClass.SERVICE, first.roadClass)
        assertArrayEquals(doubleArrayOf(-71.1858, 42.28788, -71.1858, 42.28794), first.path.copyOfRange(0, 4), 1e-9)
    }

    @Test
    fun `length is the geodesic length of the points`() {
        // About 13.6 m north-east, then 17.6 m east.
        val road = decodeRoads(fixture)[0]
        assertEquals(31.0, lengthMetres(road), 1.5)
    }
}
