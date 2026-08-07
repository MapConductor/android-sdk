package com.mapconductor.kml

import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import android.util.Xml

/**
 * Parses OGC KML 2.2 documents into [KMLFeature] models for rendering through [KMLLayer].
 *
 * The parser uses [XmlPullParser] to walk the whole document into memory, collecting shared
 * `<Style>` / `<StyleMap>` definitions and `<Placemark>` geometries, then resolves each
 * placemark's `styleUrl` reference to a concrete style.
 *
 * Supported geometries: `Point`, `LineString`, `LinearRing`, `Polygon`
 * (with `innerBoundaryIs` holes), and `MultiGeometry`.
 * Supported styling: `LineStyle` (color, width), `PolyStyle` (color, fill, outline),
 * and `IconStyle` (color). KML `aabbggrr` colors are converted to Android ARGB.
 *
 * Nested `<Document>` and `<Folder>` containers are traversed recursively.
 */
object KMLParser {
    /** Parses a KML string into a list of static features. */
    fun parse(kml: String): List<KMLFeature> = parse(kml.byteInputStream(Charsets.UTF_8))

    /**
     * Parses a KML [InputStream] into a list of static features.
     * The stream is fully consumed but not closed — callers own the stream lifecycle.
     */
    fun parse(inputStream: InputStream): List<KMLFeature> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        val styles = HashMap<String, KMLStyle>()
        val styleMaps = HashMap<String, String>()
        val placemarks = ArrayList<RawPlacemark>()

        // Advance to the first start tag (typically <kml>) and walk its subtree.
        var event = parser.eventType
        while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT) {
            event = parser.next()
        }
        if (event == XmlPullParser.START_TAG) {
            walkContainer(parser, styles, styleMaps, placemarks)
        }

        return placemarks.mapNotNull { it.toFeature(styles, styleMaps) }
    }

    /**
     * Walks the children of the container element the parser is currently positioned on,
     * dispatching styles, style maps, placemarks, and nested containers.
     */
    private fun walkContainer(
        parser: XmlPullParser,
        styles: HashMap<String, KMLStyle>,
        styleMaps: HashMap<String, String>,
        placemarks: ArrayList<RawPlacemark>,
    ) {
        KMLXmlSupport.forEachChild(parser) { name ->
            when (name) {
                "Style" -> {
                    val id = parser.getAttributeValue(null, "id")
                    val style = KMLStyleReader.readStyle(parser)
                    if (id != null) styles[id] = style
                }
                "StyleMap" -> {
                    val id = parser.getAttributeValue(null, "id")
                    val normal = KMLStyleReader.readStyleMap(parser)
                    if (id != null && normal != null) styleMaps[id] = normal
                }
                "Placemark" -> placemarks.add(readPlacemark(parser))
                "Document", "Folder", "kml" -> walkContainer(parser, styles, styleMaps, placemarks)
                else -> KMLXmlSupport.skip(parser)
            }
        }
    }

    private fun readPlacemark(parser: XmlPullParser): RawPlacemark {
        var name: String? = null
        var description: String? = null
        var styleUrl: String? = null
        var inlineStyle: KMLStyle? = null
        var geometry: KMLGeometry? = null
        val properties = LinkedHashMap<String, Any?>()

        KMLXmlSupport.forEachChild(parser) { tag ->
            when (tag) {
                "name" -> name = KMLXmlSupport.readText(parser).trim()
                "description" -> description = KMLXmlSupport.readText(parser).trim()
                "styleUrl" -> styleUrl = KMLXmlSupport.readText(parser).trim().removePrefix("#")
                "Style" -> inlineStyle = KMLStyleReader.readStyle(parser)
                "ExtendedData" -> readExtendedData(parser, properties)
                "Point", "LineString", "LinearRing", "Polygon", "MultiGeometry" ->
                    geometry = KMLGeometryReader.readGeometry(parser, tag)
                else -> KMLXmlSupport.skip(parser)
            }
        }

        name?.takeIf { it.isNotEmpty() }?.let { properties.putIfAbsent("name", it) }
        description?.takeIf { it.isNotEmpty() }?.let { properties.putIfAbsent("description", it) }

        return RawPlacemark(
            geometry = geometry,
            styleUrl = styleUrl,
            inlineStyle = inlineStyle,
            properties = properties,
        )
    }

    private fun readExtendedData(
        parser: XmlPullParser,
        properties: LinkedHashMap<String, Any?>,
    ) {
        KMLXmlSupport.forEachChild(parser) { name ->
            when (name) {
                "Data" -> {
                    val key = parser.getAttributeValue(null, "name")
                    var value: String? = null
                    KMLXmlSupport.forEachChild(parser) { child ->
                        if (child ==
                            "value"
                        ) {
                            value = KMLXmlSupport.readText(parser).trim()
                        } else {
                            KMLXmlSupport.skip(parser)
                        }
                    }
                    if (key != null) properties[key] = value
                }
                "SchemaData" ->
                    KMLXmlSupport.forEachChild(parser) { child ->
                        if (child == "SimpleData") {
                            val key = parser.getAttributeValue(null, "name")
                            val value = KMLXmlSupport.readText(parser).trim()
                            if (key != null) properties[key] = value
                        } else {
                            KMLXmlSupport.skip(parser)
                        }
                    }
                else -> KMLXmlSupport.skip(parser)
            }
        }
    }
}
