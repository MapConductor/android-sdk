package com.mapconductor.kml

import org.xmlpull.v1.XmlPullParser
import android.graphics.Color

/**
 * KML を読むときの XML 走査ヘルパと、座標・色の解釈。
 *
 * `XmlPullParser` は「今どの要素にいるか」を呼び出し側が管理する必要があり、
 * 消費し忘れると次の兄弟要素を取り違える。[forEachChild] を通すことで
 * 「子を 1 つ処理したら必ずその END_TAG まで消費する」という約束を 1 箇所に閉じ込める。
 */
internal object KMLXmlSupport {
    /**
     * Invokes [block] for each direct child START_TAG of the element the parser is currently on,
     * then consumes the element's own END_TAG. [block] must fully consume its child element
     * (via [readText], [skip], or a nested [forEachChild]).
     */
    inline fun forEachChild(
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
    fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.next()
        }
        return result
    }

    /** Skips the current element and all of its descendants. */
    fun skip(parser: XmlPullParser) {
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> return
            }
        }
    }

    val WHITESPACE = Regex("\\s+")

    /** Parses whitespace-separated `lon,lat[,alt]` tuples. */
    fun parseCoordinates(text: String): List<LonLat> {
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
    fun parseKmlColor(hex: String): Int? {
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
}
