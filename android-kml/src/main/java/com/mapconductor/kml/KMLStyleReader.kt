package com.mapconductor.kml

import org.xmlpull.v1.XmlPullParser
import android.graphics.Color

/**
 * `<Style>` / `<StyleMap>` の読み取りと、プレースマークへの解決。
 *
 * KML はスタイルを文書の別の場所で定義し、`styleUrl` で参照する。さらに
 * `<StyleMap>` は normal / highlight の 2 状態を持つ。ここでは normal だけを採り、
 * 「参照 → StyleMap → 実体」の 2 段の間接を [RawPlacemark.toFeature] で辿る。
 */
internal object KMLStyleReader {
    fun readStyle(parser: XmlPullParser): KMLStyle {
        val style = KMLStyle()
        KMLXmlSupport.forEachChild(parser) { name ->
            when (name) {
                "LineStyle" ->
                    KMLXmlSupport.forEachChild(parser) { child ->
                        when (child) {
                            "color" -> style.lineColor = KMLXmlSupport.parseKmlColor(KMLXmlSupport.readText(parser))
                            "width" -> style.lineWidth = KMLXmlSupport.readText(parser).trim().toFloatOrNull()
                            else -> KMLXmlSupport.skip(parser)
                        }
                    }
                "PolyStyle" ->
                    KMLXmlSupport.forEachChild(parser) { child ->
                        when (child) {
                            "color" -> style.polyColor = KMLXmlSupport.parseKmlColor(KMLXmlSupport.readText(parser))
                            "fill" -> style.fill = KMLXmlSupport.readText(parser).trim() != "0"
                            "outline" -> style.outline = KMLXmlSupport.readText(parser).trim() != "0"
                            else -> KMLXmlSupport.skip(parser)
                        }
                    }
                "IconStyle" ->
                    KMLXmlSupport.forEachChild(parser) { child ->
                        when (child) {
                            "color" -> style.iconColor = KMLXmlSupport.parseKmlColor(KMLXmlSupport.readText(parser))
                            else -> KMLXmlSupport.skip(parser)
                        }
                    }
                else -> KMLXmlSupport.skip(parser)
            }
        }
        return style
    }

    /** Reads a `<StyleMap>` and returns the `normal`-key styleUrl id (without the leading `#`). */
    fun readStyleMap(parser: XmlPullParser): String? {
        var normal: String? = null
        KMLXmlSupport.forEachChild(parser) { name ->
            if (name == "Pair") {
                var key: String? = null
                var url: String? = null
                KMLXmlSupport.forEachChild(parser) { child ->
                    when (child) {
                        "key" -> key = KMLXmlSupport.readText(parser).trim()
                        "styleUrl" -> url = KMLXmlSupport.readText(parser).trim().removePrefix("#")
                        else -> KMLXmlSupport.skip(parser)
                    }
                }
                if (key == "normal") normal = url
            } else {
                KMLXmlSupport.skip(parser)
            }
        }
        return normal
    }
}

internal class KMLStyle {
    var lineColor: Int? = null
    var lineWidth: Float? = null
    var polyColor: Int? = null
    var iconColor: Int? = null
    var fill: Boolean = true
    var outline: Boolean = true
}

internal class RawPlacemark(
    val geometry: KMLGeometry?,
    val styleUrl: String?,
    val inlineStyle: KMLStyle?,
    val properties: Map<String, Any?>,
) {
    fun toFeature(
        styles: Map<String, KMLStyle>,
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
        styles: Map<String, KMLStyle>,
        styleMaps: Map<String, String>,
    ): KMLStyle? {
        if (url == null) return null
        styles[url]?.let { return it }
        styleMaps[url]?.let { normal -> return styles[normal] }
        return null
    }
}
