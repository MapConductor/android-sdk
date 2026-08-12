package com.mapconductor.kml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class KMLParserTest {
    private fun kml(body: String): String =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <kml xmlns="http://www.opengis.net/kml/2.2"><Document>$body</Document></kml>
        """.trimIndent()

    @Test
    fun parsesPlacemarkWithSharedStyle() {
        val features =
            KMLParser.parse(
                kml(
                    """
                    <Style id="s"><LineStyle><color>ff0000ff</color><width>3</width></LineStyle></Style>
                    <Placemark>
                      <name>line</name>
                      <styleUrl>#s</styleUrl>
                      <LineString><coordinates>139.7,35.6,0 139.8,35.7,0</coordinates></LineString>
                    </Placemark>
                    """,
                ),
            )

        assertEquals(1, features.size)
        val feature = features[0]
        assertEquals("line", feature.properties["name"])
        assertEquals(3f, feature.strokeWidth)
        val line = feature.geometry as KMLGeometry.LineString
        assertEquals(2, line.coordinates.size)
        assertEquals(139.7, line.coordinates[0].longitude, 1e-9)
    }

    @Test
    fun deeplyNestedFoldersDoNotOverflowTheStack() {
        val depth = 20_000
        val sb = StringBuilder("""<?xml version="1.0"?><kml>""")
        repeat(depth) { sb.append("<Folder>") }
        sb.append("<Placemark><Point><coordinates>139.7,35.6</coordinates></Point></Placemark>")
        repeat(depth) { sb.append("</Folder>") }
        sb.append("</kml>")

        val features = KMLParser.parse(sb.toString())

        assertEquals(1, features.size)
        assertTrue(features[0].geometry is KMLGeometry.Point)
    }

    @Test
    fun deeplyNestedMultiGeometryDoesNotOverflowTheStack() {
        val depth = 20_000
        val sb = StringBuilder("""<?xml version="1.0"?><kml><Placemark>""")
        repeat(depth) { sb.append("<MultiGeometry>") }
        sb.append("<Point><coordinates>139.7,35.6</coordinates></Point>")
        repeat(depth) { sb.append("</MultiGeometry>") }
        sb.append("</Placemark></kml>")

        val features = KMLParser.parse(sb.toString())

        assertEquals(1, features.size)
        var geometry = features[0].geometry
        var unwrapped = 0
        while (geometry is KMLGeometry.GeometryCollection) {
            assertEquals(1, geometry.geometries.size)
            geometry = geometry.geometries[0]
            unwrapped++
        }
        assertEquals(depth, unwrapped)
        assertTrue(geometry is KMLGeometry.Point)
    }

    @Test
    fun multiGeometryKeepsSiblingLeavesAndNesting() {
        val features =
            KMLParser.parse(
                kml(
                    """
                    <Placemark><MultiGeometry>
                      <Point><coordinates>1,2</coordinates></Point>
                      <MultiGeometry>
                        <LineString><coordinates>1,2 3,4</coordinates></LineString>
                      </MultiGeometry>
                      <Polygon><outerBoundaryIs><LinearRing>
                        <coordinates>0,0 1,0 1,1 0,0</coordinates>
                      </LinearRing></outerBoundaryIs></Polygon>
                    </MultiGeometry></Placemark>
                    """,
                ),
            )

        val collection = features[0].geometry as KMLGeometry.GeometryCollection
        assertEquals(3, collection.geometries.size)
        assertTrue(collection.geometries[0] is KMLGeometry.Point)
        val nested = collection.geometries[1] as KMLGeometry.GeometryCollection
        assertTrue(nested.geometries[0] is KMLGeometry.LineString)
        assertTrue(collection.geometries[2] is KMLGeometry.Polygon)
    }

    @Test
    fun collectsNetworkLinksIncludingLegacyUrlTag() {
        val document =
            KMLParser.parseDocument(
                kml(
                    """
                    <NetworkLink><Link><href>https://example.com/a.kml</href></Link></NetworkLink>
                    <Folder>
                      <NetworkLink><visibility>0</visibility><Link><href>hidden.kml</href></Link></NetworkLink>
                      <NetworkLink><Url><href>legacy.kml</href></Url></NetworkLink>
                    </Folder>
                    <Placemark><Point><coordinates>1,2</coordinates></Point></Placemark>
                    """,
                ).byteInputStream(),
            )

        assertEquals(1, document.features.size)
        assertEquals(3, document.networkLinks.size)
        assertEquals("https://example.com/a.kml", document.networkLinks[0].href)
        assertEquals(false, document.networkLinks[1].visibility)
        assertEquals("legacy.kml", document.networkLinks[2].href)
    }

    @Test
    fun parsesKmzArchiveUsingFirstKmlEntry() {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            zip.putNextEntry(ZipEntry("images/icon.png"))
            zip.write(byteArrayOf(1, 2, 3))
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("doc.kml"))
            zip.write(
                kml("<Placemark><Point><coordinates>139.7,35.6</coordinates></Point></Placemark>").toByteArray(),
            )
            zip.closeEntry()
        }

        val features = KMLParser.parse(ByteArrayInputStream(bytes.toByteArray()))

        assertEquals(1, features.size)
        assertTrue(features[0].geometry is KMLGeometry.Point)
    }

    @Test
    fun kmzWithoutKmlEntryThrows() {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            zip.putNextEntry(ZipEntry("readme.txt"))
            zip.write("no kml here".toByteArray())
            zip.closeEntry()
        }

        var thrown: Throwable? = null
        try {
            KMLParser.parse(ByteArrayInputStream(bytes.toByteArray()))
        } catch (e: IOException) {
            thrown = e
        }
        assertTrue(thrown is IOException)
    }
}
