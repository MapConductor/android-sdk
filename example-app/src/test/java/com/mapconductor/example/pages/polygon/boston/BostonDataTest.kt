package com.mapconductor.example.pages.polygon.boston

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class BostonDataTest {
    /**
     * Two polygons with every field (fields = 15), written by the generator
     * (pack.py write_v2). The same bytes are decoded by the React and iOS tests.
     */
    private val fixture =
        (
            "4d43504702000000020000000f7c020109436166c3a920e298950204f4abb228a7b0e24300d00fd00f0000cf0f03db0bd804" +
                "d8040000a006000802000103ffea068ff40414142700"
        ).chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    @Test
    fun `decodes every field and closes each ring again`() {
        val polygons = decodeMcpg(fixture, "t")
        assertEquals(2, polygons.size)

        val first = polygons[0]
        assertEquals("t-0", first.id)
        assertEquals(12.3, first.height!!, 0.0)
        assertEquals(LandUse.CONDO, first.use)
        assertEquals(1, first.level)
        assertEquals("Café ☕", first.name)
        assertEquals(2, first.rings.size)
        assertArrayEquals(
            doubleArrayOf(
                -71.0605, 42.35545, -71.0595, 42.35545, -71.0595, 42.35645, -71.0605, 42.35645, -71.0605, 42.35545,
            ),
            first.rings[0],
            1e-9,
        )
        assertArrayEquals(
            doubleArrayOf(-71.0602, 42.3557, -71.0602, 42.356, -71.0598, 42.356, -71.0602, 42.3557),
            first.rings[1],
            1e-9,
        )

        val second = polygons[1]
        assertEquals("t-1", second.id)
        assertNull(second.height)
        assertEquals(LandUse.UNKNOWN, second.use)
        assertEquals(2, second.level)
        assertNull(second.name)
        assertArrayEquals(
            doubleArrayOf(-71.1, 42.3, -71.09999, 42.30001, -71.09999, 42.29999, -71.1, 42.3),
            second.rings.single(),
            1e-9,
        )
    }

    @Test
    fun `the bundled downtown file has every building of the three areas`() {
        val polygons = decodeMcpg(File("src/main/assets/boston/boston-downtown.bin").readBytes(), "downtown")
        assertEquals(9467, polygons.size)
        assertEquals(73289, polygons.sumOf { b -> b.rings.sumOf { it.size / 2 } })
        assertEquals(9504, polygons.sumOf { it.rings.size })
        assertEquals(mapOf(0 to 605, 1 to 2222, 2 to 6640), polygons.groupingBy { it.level }.eachCount())
        assertEquals(362, polygons.count { it.height == null })
        assertEquals(762, polygons.count { it.name != null })
        assertEquals(9467, polygons.map { it.id }.toSet().size)
        val first = polygons.first()
        assertEquals(5.2, first.height!!, 0.0)
        assertEquals(2, first.level)
        assertArrayEquals(doubleArrayOf(-71.076105, 42.373684), first.rings[0].copyOfRange(0, 2), 1e-9)
        assertArrayEquals(first.rings[0].copyOfRange(0, 2), first.rings[0].copyOfRange(16, 18), 0.0)
    }

    @Test
    fun `area is the outline less its holes`() {
        val polygon = decodeMcpg(fixture, "t")[0]
        val outline = Building("o", 0, null, null, listOf(polygon.rings[0]))
        val hole = Building("h", 0, null, null, listOf(polygon.rings[1]))
        assertEquals(areaSquareMetres(outline) - areaSquareMetres(hole), areaSquareMetres(polygon), 1e-6)
        // About 82 m x 111 m.
        assertEquals(9100.0, areaSquareMetres(outline), 100.0)
    }
}
