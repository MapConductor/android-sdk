package com.mapconductor.kml

import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import android.graphics.Color
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

        val styles = HashMap<String, Style>()
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
        styles: HashMap<String, Style>,
        styleMaps: HashMap<String, String>,
        placemarks: ArrayList<RawPlacemark>,
    ) {
        forEachChild(parser) { name ->
            when (name) {
                "Style" -> {
                    val id = parser.getAttributeValue(null, "id")
                    val style = readStyle(parser)
                    if (id != null) styles[id] = style
                }
                "StyleMap" -> {
                    val id = parser.getAttributeValue(null, "id")
                    val normal = readStyleMap(parser)
                    if (id != null && normal != null) styleMaps[id] = normal
                }
                "Placemark" -> placemarks.add(readPlacemark(parser))
                "Document", "Folder", "kml" -> walkContainer(parser, styles, styleMaps, placemarks)
                else -> skip(parser)
            }
        }
    }

    private fun readStyle(parser: XmlPullParser): Style {
        val style = Style()
        forEachChild(parser) { name ->
            when (name) {
                "LineStyle" ->
                    forEachChild(parser) { child ->
                        when (child) {
                            "color" -> style.lineColor = parseKmlColor(readText(parser))
                            "width" -> style.lineWidth = readText(parser).trim().toFloatOrNull()
                            else -> skip(parser)
                        }
                    }
                "PolyStyle" ->
                    forEachChild(parser) { child ->
                        when (child) {
                            "color" -> style.polyColor = parseKmlColor(readText(parser))
                            "fill" -> style.fill = readText(parser).trim() != "0"
                            "outline" -> style.outline = readText(parser).trim() != "0"
                            else -> skip(parser)
                        }
                    }
                "IconStyle" ->
                    forEachChild(parser) { child ->
                        when (child) {
                            "color" -> style.iconColor = parseKmlColor(readText(parser))
                            else -> skip(parser)
                        }
                    }
                else -> skip(parser)
            }
        }
        return style
    }

    /** Reads a `<StyleMap>` and returns the `normal`-key styleUrl id (without the leading `#`). */
    private fun readStyleMap(parser: XmlPullParser): String? {
        var normal: String? = null
        forEachChild(parser) { name ->
            if (name == "Pair") {
                var key: String? = null
                var url: String? = null
                forEachChild(parser) { child ->
                    when (child) {
                        "key" -> key = readText(parser).trim()
                        "styleUrl" -> url = readText(parser).trim().removePrefix("#")
                        else -> skip(parser)
                    }
                }
                if (key == "normal") normal = url
            } else {
                skip(parser)
            }
        }
        return normal
    }

    private fun readPlacemark(parser: XmlPullParser): RawPlacemark {
        var name: String? = null
        var description: String? = null
        var styleUrl: String? = null
        var inlineStyle: Style? = null
        var geometry: KMLGeometry? = null
        val properties = LinkedHashMap<String, Any?>()

        forEachChild(parser) { tag ->
            when (tag) {
                "name" -> name = readText(parser).trim()
                "description" -> description = readText(parser).trim()
                "styleUrl" -> styleUrl = readText(parser).trim().removePrefix("#")
                "Style" -> inlineStyle = readStyle(parser)
                "ExtendedData" -> readExtendedData(parser, properties)
                "Point", "LineString", "LinearRing", "Polygon", "MultiGeometry" ->
                    geometry = readGeometry(parser, tag)
                else -> skip(parser)
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

    private fun readGeometry(
        parser: XmlPullParser,
        type: String,
    ): KMLGeometry? =
        when (type) {
            "Point" -> {
                val coords = readCoordinatesChild(parser)
                coords.firstOrNull()?.let { KMLGeometry.Point(it.longitude, it.latitude) }
            }
            "LineString", "LinearRing" -> {
                val coords = readCoordinatesChild(parser)
                if (coords.isEmpty()) null else KMLGeometry.LineString(coords)
            }
            "Polygon" -> readPolygon(parser)
            "MultiGeometry" -> readMultiGeometry(parser)
            else -> {
                skip(parser)
                null
            }
        }

    private fun readPolygon(parser: XmlPullParser): KMLGeometry? {
        var outer: List<LonLat>? = null
        val inners = ArrayList<List<LonLat>>()
        forEachChild(parser) { name ->
            when (name) {
                "outerBoundaryIs" -> outer = readBoundary(parser)
                "innerBoundaryIs" -> readBoundary(parser)?.let { inners.add(it) }
                else -> skip(parser)
            }
        }
        val exterior = outer ?: return null
        if (exterior.isEmpty()) return null
        val rings = ArrayList<List<LonLat>>(1 + inners.size)
        rings.add(exterior)
        rings.addAll(inners)
        return KMLGeometry.Polygon(rings)
    }

    /** Reads an `<outerBoundaryIs>` / `<innerBoundaryIs>` wrapper containing a `<LinearRing>`. */
    private fun readBoundary(parser: XmlPullParser): List<LonLat>? {
        var coords: List<LonLat>? = null
        forEachChild(parser) { name ->
            if (name == "LinearRing") {
                coords = readCoordinatesChild(parser)
            } else {
                skip(parser)
            }
        }
        return coords
    }

    private fun readMultiGeometry(parser: XmlPullParser): KMLGeometry {
        val parts = ArrayList<KMLGeometry>()
        forEachChild(parser) { name ->
            when (name) {
                "Point", "LineString", "LinearRing", "Polygon", "MultiGeometry" ->
                    readGeometry(parser, name)?.let { parts.add(it) }
                else -> skip(parser)
            }
        }
        return KMLGeometry.GeometryCollection(parts)
    }

    /** Reads the `<coordinates>` child of the current geometry element. */
    private fun readCoordinatesChild(parser: XmlPullParser): List<LonLat> {
        var coords: List<LonLat> = emptyList()
        forEachChild(parser) { name ->
            if (name == "coordinates") {
                coords = parseCoordinates(readText(parser))
            } else {
                skip(parser)
            }
        }
        return coords
    }

    private fun readExtendedData(
        parser: XmlPullParser,
        properties: LinkedHashMap<String, Any?>,
    ) {
        forEachChild(parser) { name ->
            when (name) {
                "Data" -> {
                    val key = parser.getAttributeValue(null, "name")
                    var value: String? = null
                    forEachChild(parser) { child ->
                        if (child == "value") value = readText(parser).trim() else skip(parser)
                    }
                    if (key != null) properties[key] = value
                }
                "SchemaData" ->
                    forEachChild(parser) { child ->
                        if (child == "SimpleData") {
                            val key = parser.getAttributeValue(null, "name")
                            val value = readText(parser).trim()
                            if (key != null) properties[key] = value
                        } else {
                            skip(parser)
                        }
                    }
                else -> skip(parser)
            }
        }
    }

    // ── Coordinate & color helpers ────────────────────────────────────────────

    /** Parses whitespace-separated `lon,lat[,alt]` tuples. */
    private fun parseCoordinates(text: String): List<LonLat> {
        val result = ArrayList<LonLat>()
        for (token in text.trim().split(WHITESPACE)) {
            if (token.isBlank()) continue
            val parts = token.split(',')
            if (parts.size < 2) continue
            val lon = parts[0].trim().toDoubleOrNull() ?: continue
            val lat = parts[1].trim().toDoubleOrNull() ?: continue
            result.add(LonLat(longitude = lon, latitude = lat))
        }
        return result
    }

    /** Converts a KML `aabbggrr` (or `bbggrr`) hex color to an Android ARGB int. */
    private fun parseKmlColor(hex: String): Int? {
        val h = hex.trim()
        return runCatching {
            when (h.length) {
                8 -> {
                    val a = h.substring(0, 2).toInt(16)
                    val b = h.substring(2, 4).toInt(16)
                    val g = h.substring(4, 6).toInt(16)
                    val r = h.substring(6, 8).toInt(16)
                    Color.argb(a, r, g, b)
                }
                6 -> {
                    val b = h.substring(0, 2).toInt(16)
                    val g = h.substring(2, 4).toInt(16)
                    val r = h.substring(4, 6).toInt(16)
                    Color.argb(255, r, g, b)
                }
                else -> null
            }
        }.getOrNull()
    }

    // ── XmlPullParser traversal helpers ───────────────────────────────────────

    /**
     * Invokes [block] for each direct child START_TAG of the element the parser is currently on,
     * then consumes the element's own END_TAG. [block] must fully consume its child element
     * (via [readText], [skip], or a nested [forEachChild]).
     */
    private inline fun forEachChild(
        parser: XmlPullParser,
        block: (name: String) -> Unit,
    ) {
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> {
                    depth++
                    val name = parser.name
                    block(name)
                    // block is expected to leave the parser on this child's END_TAG.
                    depth--
                }
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> return
            }
        }
    }

    /** Reads the text content of the current element and consumes its END_TAG. */
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.next()
        }
        return result
    }

    /** Skips the current element and all of its descendants. */
    private fun skip(parser: XmlPullParser) {
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> return
            }
        }
    }

    private val WHITESPACE = Regex("\\s+")

    // ── Intermediate models ───────────────────────────────────────────────────

    private class Style {
        var lineColor: Int? = null
        var lineWidth: Float? = null
        var polyColor: Int? = null
        var iconColor: Int? = null
        var fill: Boolean = true
        var outline: Boolean = true
    }

    private class RawPlacemark(
        val geometry: KMLGeometry?,
        val styleUrl: String?,
        val inlineStyle: Style?,
        val properties: Map<String, Any?>,
    ) {
        fun toFeature(
            styles: Map<String, Style>,
            styleMaps: Map<String, String>,
        ): KMLFeature? {
            val geom = geometry ?: return null
            val style = inlineStyle ?: resolveStyle(styleUrl, styles, styleMaps)

            val strokeColor =
                when {
                    style == null -> null
                    !style.outline -> Color.TRANSPARENT
                    else -> style.lineColor
                }
            val fillColor =
                when {
                    style == null -> null
                    !style.fill -> Color.TRANSPARENT
                    else -> style.polyColor ?: style.iconColor
                }

            return KMLFeature(
                id = null,
                geometry = geom,
                properties = properties,
                strokeColor = strokeColor,
                fillColor = fillColor,
                strokeWidth = style?.lineWidth,
            )
        }

        private fun resolveStyle(
            url: String?,
            styles: Map<String, Style>,
            styleMaps: Map<String, String>,
        ): Style? {
            if (url == null) return null
            styles[url]?.let { return it }
            styleMaps[url]?.let { normal -> return styles[normal] }
            return null
        }
    }
}
