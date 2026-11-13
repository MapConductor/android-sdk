package com.mapconductor.vectorstyle

import androidx.compose.ui.graphics.Color

/**
 * What to change about a style, and where.
 *
 * ```kotlin
 * val rules = StyleRules.build {
 *     all { color = Color.Black }
 *     role(LayerRole.LABEL) { visible = false }
 *     role(LayerRole.ROAD_CASING) { color = Color(0xFF303030) }
 *     role(LayerRole.ROAD) { color = Color.White; widthScale = 1.3f }
 * }
 * ```
 *
 * Rules apply in order and override each other property by property, the way
 * CSS does: the last rule to name a colour wins, and a rule that says nothing
 * about width leaves the width alone. Reordering the two road rules above
 * gives a different map, which is the point — only the app knows whether the
 * casings should keep their own colour.
 *
 * ## It is JSON underneath, on purpose
 *
 * [json] is the real definition, not a serialisation of the builder. The same
 * text is what the compiler reads on Android, iOS and the web, so a rule set
 * can be written by hand, stored, shipped from a server, or diffed between
 * two versions of an app — and [parse] takes it straight back. The builder is
 * sugar over that, there so the common case is checked by the compiler rather
 * than by a typo.
 *
 * Nothing is validated here. A rule set is checked when it is compiled
 * against a style, which is the only place that can tell whether
 * `role("road")` means anything.
 */
class StyleRules private constructor(
    /** The canonical form. */
    val json: String,
) {
    override fun equals(other: Any?): Boolean = other is StyleRules && other.json == json

    override fun hashCode(): Int = json.hashCode()

    override fun toString(): String = json

    companion object {
        /** No adjustments: the style as its author wrote it. */
        @JvmField
        val NONE: StyleRules = StyleRules("""{"schemaVersion":1,"rules":[]}""")

        /**
         * Takes a rules document as it stands.
         *
         * Not checked here — a document is checked when it is compiled
         * against a style, and [VectorStyleRules.compile] throws then.
         */
        @JvmStatic
        fun parse(json: String): StyleRules = StyleRules(json)

        /** Writes a rules document with the compiler checking the shape. */
        @JvmStatic
        fun build(block: StyleRulesBuilder.() -> Unit): StyleRules =
            StyleRules(StyleRulesBuilder().apply(block).toJson())
    }
}

/**
 * The roles the built-in schema profiles assign.
 *
 * Strings rather than an enum: a style cut to a schema nobody here has heard
 * of can name its own roles through [StyleRulesBuilder.schema], and adding a
 * role to the built-in tables must not be a breaking change on three
 * platforms.
 */
object LayerRole {
    const val BACKGROUND = "background"
    const val WATER = "water"
    const val WATERWAY = "waterway"
    const val LAND = "land"
    const val LANDUSE = "landuse"
    const val PARK = "park"
    const val BUILDING = "building"
    const val ROAD = "road"

    /**
     * The wide dark line drawn under a road to give it an edge.
     *
     * A style draws every road twice, and "make roads white" applied to both
     * halves gives a white slab rather than a road. Nothing in the tile
     * distinguishes them — only the layer's name does — so this is the one
     * role decided by a naming convention, and the compiler reports how many
     * layers it found.
     */
    const val ROAD_CASING = "road-casing"
    const val RAIL = "rail"
    const val TRANSIT = "transit"
    const val BOUNDARY = "boundary"
    const val AEROWAY = "aeroway"
    const val LABEL = "label"
    const val POI = "poi"
}

/** A style layer's `type`, for [StyleSelector.Kind]. */
enum class StyleLayerKind(
    internal val wire: String,
) {
    BACKGROUND("background"),
    FILL("fill"),
    LINE("line"),
    CIRCLE("circle"),
    SYMBOL("symbol"),

    /** Everything else: raster, hillshade, heatmap, fill-extrusion, sky. */
    OTHER("other"),
}

/** Which layers a rule reaches. */
sealed interface StyleSelector {
    /** Every layer in the style, including the background. */
    object All : StyleSelector

    /** What the layer *is*, across schemas that name their sources differently. */
    data class Role(
        val role: String,
    ) : StyleSelector

    /** The layer's `id`, as a glob: `*` for any run, `?` for one character. */
    data class LayerId(
        val glob: String,
    ) : StyleSelector

    data class SourceLayer(
        val name: String,
    ) : StyleSelector

    data class Kind(
        val kind: StyleLayerKind,
    ) : StyleSelector

    data class AnyOf(
        val of: List<StyleSelector>,
    ) : StyleSelector

    data class AllOf(
        val of: List<StyleSelector>,
    ) : StyleSelector

    data class Not(
        val of: StyleSelector,
    ) : StyleSelector
}

/** Builds the `rules` array. */
class StyleRulesBuilder internal constructor() {
    /**
     * Which tile schema the style's `source-layer` names come from.
     *
     * Left alone it is worked out from the style, which is right for
     * OpenMapTiles, Shortbread and Mapbox Streets. Name one to be sure, or
     * supply [customSchema] for a schema the compiler has never seen.
     */
    var schema: String? = null

    private var custom: Map<String, List<String>>? = null
    private val rules = mutableListOf<String>()

    /**
     * Teaches the compiler a schema of your own: role name to the
     * `source-layer` names that carry it.
     */
    fun customSchema(roles: Map<String, List<String>>) {
        custom = roles
    }

    fun all(patch: StylePatchBuilder.() -> Unit) = where(StyleSelector.All, patch)

    fun role(
        role: String,
        patch: StylePatchBuilder.() -> Unit,
    ) = where(StyleSelector.Role(role), patch)

    fun layerId(
        glob: String,
        patch: StylePatchBuilder.() -> Unit,
    ) = where(StyleSelector.LayerId(glob), patch)

    fun sourceLayer(
        name: String,
        patch: StylePatchBuilder.() -> Unit,
    ) = where(StyleSelector.SourceLayer(name), patch)

    fun kind(
        kind: StyleLayerKind,
        patch: StylePatchBuilder.() -> Unit,
    ) = where(StyleSelector.Kind(kind), patch)

    /** For a selector built by hand: [StyleSelector.AllOf], [StyleSelector.Not] and friends. */
    fun where(
        selector: StyleSelector,
        patch: StylePatchBuilder.() -> Unit,
    ) {
        val body = StylePatchBuilder().apply(patch).toJson()
        rules += """{"selector":${selector.toJson()},"patch":$body}"""
    }

    internal fun toJson(): String =
        buildString {
            append("""{"schemaVersion":1""")
            custom?.let { roles ->
                append(""","schema":{"roles":{""")
                append(
                    roles.entries.joinToString(",") { (role, layers) ->
                        """${quote(role)}:[${layers.joinToString(",") { quote(it) }}]"""
                    },
                )
                append("}}")
            } ?: schema?.let { append(""","schema":${quote(it)}""") }
            append(""","rules":[""")
            append(rules.joinToString(","))
            append("]}")
        }
}

/** Builds one rule's `patch`. */
class StylePatchBuilder internal constructor() {
    private val fields = mutableListOf<String>()
    private val properties = mutableListOf<String>()

    /** `layout.visibility`. */
    var visible: Boolean? = null
        set(value) {
            field = value
            value?.let { fields += """"visible":$it""" }
        }

    /**
     * The layer's colour, whichever property that is for its type:
     * `fill-color` on a fill, `line-color` on a line, `text-color` and
     * `icon-color` on a symbol, `background-color` on the background.
     */
    var color: Color? = null
        set(value) {
            field = value
            value?.let { fields += """"color":${quote(it.toStyleText())}""" }
        }

    /** `*-opacity`, 0 to 1. */
    var opacity: Float? = null
        set(value) {
            field = value
            value?.let { fields += """"opacity":$it""" }
        }

    /**
     * Multiplies how fat the layer is drawn: `line-width` on a line,
     * `circle-radius` on a circle.
     *
     * A multiplier rather than a value because the width is almost always an
     * expression over zoom, and replacing it with a number would throw that
     * curve away.
     */
    var widthScale: Float? = null
        set(value) {
            field = value
            value?.let { fields += """"widthScale":$it""" }
        }

    var minZoom: Float? = null
        set(value) {
            field = value
            value?.let { fields += """"minZoom":$it""" }
        }

    var maxZoom: Float? = null
        set(value) {
            field = value
            value?.let { fields += """"maxZoom":$it""" }
        }

    /** Drains the colour the style chose, 0 to 1. */
    fun desaturate(amount: Float) {
        fields += """"colorFilter":{"desaturate":$amount}"""
    }

    fun darken(amount: Float) {
        fields += """"colorFilter":{"darken":$amount}"""
    }

    fun lighten(amount: Float) {
        fields += """"colorFilter":{"lighten":$amount}"""
    }

    /**
     * Flips how light each colour is and keeps its hue: the cheapest way to
     * get a dark basemap out of a light one without naming a single colour.
     */
    fun invertLightness() {
        fields += """"colorFilter":{"invertLightness":true}"""
    }

    /** Blends what is there towards [target]. */
    fun mix(
        target: Color,
        amount: Float = 0.5f,
    ) {
        fields += """"colorFilter":{"mix":{"color":${quote(target.toStyleText())},"amount":$amount}}"""
    }

    /** Replaces the layer's filter, as a MapLibre filter expression in JSON. */
    fun filter(json: String) {
        fields += """"filter":$json"""
    }

    /**
     * Any style-spec property by name, as JSON. The last word: applied after
     * everything above, including over a [color] in the same rule.
     *
     * The escape hatch for what the fields above do not name —
     * `text-field`, `fill-pattern`, `line-dasharray`, a `fill-extrusion`
     * height. Note that a property the SDK's own rasteriser cannot draw will
     * show only on a map that renders the style itself; the compiler says so
     * in its diagnostics.
     */
    fun property(
        key: String,
        valueJson: String,
    ) {
        properties += """${quote(key)}:$valueJson"""
    }

    /** [property] for a plain text value, quoted for you. */
    fun propertyText(
        key: String,
        value: String,
    ) = property(key, quote(value))

    internal fun toJson(): String {
        val all = fields.toMutableList()
        if (properties.isNotEmpty()) all += """"properties":{${properties.joinToString(",")}}"""
        return "{${all.joinToString(",")}}"
    }
}

internal fun StyleSelector.toJson(): String =
    when (this) {
        StyleSelector.All -> "\"all\""
        is StyleSelector.Role -> """{"role":${quote(role)}}"""
        is StyleSelector.LayerId -> """{"layerId":${quote(glob)}}"""
        is StyleSelector.SourceLayer -> """{"sourceLayer":${quote(name)}}"""
        is StyleSelector.Kind -> """{"kind":${quote(kind.wire)}}"""
        is StyleSelector.AnyOf -> """{"anyOf":[${of.joinToString(",") { it.toJson() }}]}"""
        is StyleSelector.AllOf -> """{"allOf":[${of.joinToString(",") { it.toJson() }}]}"""
        is StyleSelector.Not -> """{"not":${of.toJson()}}"""
    }

/**
 * `#rrggbb`, or `rgba(...)` when it is not fully opaque.
 *
 * Compose keeps its channels as floats in whatever colour space the caller
 * used; a style wants eight-bit sRGB, so the conversion happens here rather
 * than being left to whoever reads the JSON.
 */
internal fun Color.toStyleText(): String {
    val srgb = convert(androidx.compose.ui.graphics.colorspace.ColorSpaces.Srgb)
    val r = (srgb.red * 255f).toInt().coerceIn(0, 255)
    val g = (srgb.green * 255f).toInt().coerceIn(0, 255)
    val b = (srgb.blue * 255f).toInt().coerceIn(0, 255)
    return if (srgb.alpha >= 1f) {
        String.format("#%02x%02x%02x", r, g, b)
    } else {
        "rgba($r,$g,$b,${"%.3f".format(srgb.alpha).trimEnd('0').trimEnd('.')})"
    }
}

/**
 * A JSON string literal.
 *
 * Written here rather than through `org.json`, which is a throwing stub in
 * JVM unit tests — the builder has to be testable without a device.
 */
internal fun quote(text: String): String =
    buildString(text.length + 2) {
        append('"')
        for (c in text) {
            when (c) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (c < ' ') append("\\u%04x".format(c.code)) else append(c)
            }
        }
        append('"')
    }
