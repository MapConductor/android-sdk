package com.mapconductor.kml

import org.xmlpull.v1.XmlPullParser

/**
 * `<Point>` / `<LineString>` / `<LinearRing>` / `<Polygon>` / `<MultiGeometry>` の読み取り。
 *
 * `<Polygon>` は外環（outerBoundaryIs）に続けて穴（innerBoundaryIs）を並べた形で持つ。
 * 描画側は「最初の環が外、以降が穴」という約束で扱うので、その順序をここで作る。
 */
internal object KMLGeometryReader {
    fun readGeometry(
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
                KMLXmlSupport.skip(parser)
                null
            }
        }

    fun readPolygon(parser: XmlPullParser): KMLGeometry? {
        var outer: List<LonLat>? = null
        val inners = ArrayList<List<LonLat>>()
        KMLXmlSupport.forEachChild(parser) { name ->
            when (name) {
                "outerBoundaryIs" -> outer = readBoundary(parser)
                "innerBoundaryIs" -> readBoundary(parser)?.let { inners.add(it) }
                else -> KMLXmlSupport.skip(parser)
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
    fun readBoundary(parser: XmlPullParser): List<LonLat>? {
        var coords: List<LonLat>? = null
        KMLXmlSupport.forEachChild(parser) { name ->
            if (name == "LinearRing") {
                coords = readCoordinatesChild(parser)
            } else {
                KMLXmlSupport.skip(parser)
            }
        }
        return coords
    }

    /**
     * `<MultiGeometry>` はネスト可能なので、明示スタック + ループで読む（再帰しない）。
     * リーフ要素の読み取りは自分の END_TAG まで消費するため、このループに見える
     * END_TAG は MultiGeometry 自身の閉じタグだけになる。
     */
    fun readMultiGeometry(parser: XmlPullParser): KMLGeometry {
        val stack = ArrayDeque<ArrayList<KMLGeometry>>()
        stack.addLast(ArrayList())
        while (true) {
            when (parser.next()) {
                XmlPullParser.START_TAG ->
                    when (val name = parser.name) {
                        "MultiGeometry" -> stack.addLast(ArrayList())
                        "Point", "LineString", "LinearRing", "Polygon" ->
                            readGeometry(parser, name)?.let { stack.last().add(it) }
                        else -> KMLXmlSupport.skip(parser)
                    }
                XmlPullParser.END_TAG -> {
                    val closed = KMLGeometry.GeometryCollection(stack.removeLast())
                    if (stack.isEmpty()) return closed
                    stack.last().add(closed)
                }
                XmlPullParser.END_DOCUMENT -> return KMLGeometry.GeometryCollection(stack.first())
            }
        }
    }

    /** Reads the `<coordinates>` child of the current geometry element. */
    fun readCoordinatesChild(parser: XmlPullParser): List<LonLat> {
        var coords: List<LonLat> = emptyList()
        KMLXmlSupport.forEachChild(parser) { name ->
            if (name == "coordinates") {
                coords = KMLXmlSupport.parseCoordinates(KMLXmlSupport.readText(parser))
            } else {
                KMLXmlSupport.skip(parser)
            }
        }
        return coords
    }
}
