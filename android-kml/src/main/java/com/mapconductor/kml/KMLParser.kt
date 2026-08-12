package com.mapconductor.kml

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Parses OGC KML 2.2 documents — and KMZ archives — into [KMLFeature] models for rendering
 * through [KMLLayer].
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
 * Nested `<Document>` and `<Folder>` containers are walked with a loop and a depth counter —
 * never by recursion — so arbitrarily deep hierarchies cannot overflow the call stack.
 * `<NetworkLink>` references are collected into [KMLDocument.networkLinks] (not fetched here);
 * use [KMLLoader] to fetch and merge them.
 */
object KMLParser {
    /** Parses a KML string into a list of static features. */
    fun parse(kml: String): List<KMLFeature> = parseDocument(kml.byteInputStream(Charsets.UTF_8)).features

    /**
     * Parses a KML or KMZ [InputStream] into a list of static features.
     * The stream is fully consumed but not closed — callers own the stream lifecycle.
     */
    fun parse(inputStream: InputStream): List<KMLFeature> = parseDocument(inputStream).features

    /**
     * Parses a KML or KMZ [InputStream] into a [KMLDocument], keeping unresolved
     * `<NetworkLink>` references alongside the parsed features.
     *
     * KMZ input is detected by the ZIP signature and the first `.kml` entry in the archive
     * (conventionally `doc.kml`) is used as the document.
     * The stream is fully consumed but not closed — callers own the stream lifecycle.
     */
    fun parseDocument(inputStream: InputStream): KMLDocument {
        val stream = if (inputStream.markSupported()) inputStream else BufferedInputStream(inputStream)
        return if (hasZipSignature(stream)) parseKmz(stream) else parseKml(stream)
    }

    private fun hasZipSignature(stream: InputStream): Boolean {
        stream.mark(4)
        val signature = ByteArray(4)
        var read = 0
        while (read < signature.size) {
            val n = stream.read(signature, read, signature.size - read)
            if (n < 0) break
            read += n
        }
        stream.reset()
        return read == signature.size &&
            signature[0] == 0x50.toByte() &&
            signature[1] == 0x4B.toByte() &&
            signature[2] == 0x03.toByte() &&
            signature[3] == 0x04.toByte()
    }

    private fun parseKmz(stream: InputStream): KMLDocument {
        // ZipInputStream は close せずに使う。close すると呼び出し側のストリームまで閉じてしまう。
        val zip = ZipInputStream(stream)
        var entry = zip.nextEntry
        while (entry != null) {
            if (!entry.isDirectory && entry.name.endsWith(".kml", ignoreCase = true)) {
                return parseKml(zip)
            }
            entry = zip.nextEntry
        }
        throw IOException("KMZ archive contains no .kml entry")
    }

    private fun parseKml(inputStream: InputStream): KMLDocument {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        val styles = HashMap<String, KMLStyle>()
        val styleMaps = HashMap<String, String>()
        val placemarks = ArrayList<RawPlacemark>()
        val networkLinks = ArrayList<KMLNetworkLink>()

        // Advance to the first start tag (typically <kml>) and walk its subtree.
        var event = parser.eventType
        while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT) {
            event = parser.next()
        }
        if (event == XmlPullParser.START_TAG) {
            walkDocument(parser, styles, styleMaps, placemarks, networkLinks)
        }

        return KMLDocument(
            features = placemarks.mapNotNull { it.toFeature(styles, styleMaps) },
            networkLinks = networkLinks,
        )
    }

    /**
     * Walks every container in the document with a single loop and a depth counter.
     * `<Document>` / `<Folder>` / nested `<kml>` are transparent: entering one only increments
     * [depth], so container nesting consumes no call-stack frames. Every handled child
     * (Style / StyleMap / Placemark / NetworkLink) consumes its own subtree including the
     * END_TAG, which keeps the depth accounting balanced.
     */
    private fun walkDocument(
        parser: XmlPullParser,
        styles: HashMap<String, KMLStyle>,
        styleMaps: HashMap<String, String>,
        placemarks: ArrayList<RawPlacemark>,
        networkLinks: ArrayList<KMLNetworkLink>,
    ) {
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG ->
                    when (parser.name) {
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
                        "NetworkLink" -> readNetworkLink(parser)?.let { networkLinks.add(it) }
                        "Document", "Folder", "kml" -> depth++
                        else -> KMLXmlSupport.skip(parser)
                    }
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> return
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

    /** Reads a `<NetworkLink>`; the href lives in `<Link>` (or the legacy `<Url>`). */
    private fun readNetworkLink(parser: XmlPullParser): KMLNetworkLink? {
        var href: String? = null
        var visibility = true
        KMLXmlSupport.forEachChild(parser) { tag ->
            when (tag) {
                "Link", "Url" ->
                    KMLXmlSupport.forEachChild(parser) { child ->
                        if (child == "href") {
                            href = KMLXmlSupport.readText(parser).trim()
                        } else {
                            KMLXmlSupport.skip(parser)
                        }
                    }
                "visibility" -> visibility = KMLXmlSupport.readText(parser).trim() != "0"
                else -> KMLXmlSupport.skip(parser)
            }
        }
        return href?.takeIf { it.isNotEmpty() }?.let { KMLNetworkLink(href = it, visibility = visibility) }
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
