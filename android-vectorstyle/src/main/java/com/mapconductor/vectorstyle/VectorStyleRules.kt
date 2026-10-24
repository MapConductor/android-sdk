package com.mapconductor.vectorstyle

import com.mapconductor.core.map.StyleMutation
import org.json.JSONArray
import org.json.JSONObject

/**
 * Declarative adjustments to a MapLibre style.
 *
 * A map in this SDK is drawn one of two ways: by a renderer that takes a
 * vector style directly (MapLibre, Mapbox, MapTiler), or by rasterising the
 * style to PNG tiles on the device and handing those to a backend that
 * cannot read a style at all (Google Maps, HERE, ArcGIS...). Both start from
 * the same `style.json`, so adjusting the style belongs *before* that fork
 * rather than on either side of it.
 *
 * [compile] is that step. From one pass over one style it produces both the
 * adjusted document — for a renderer to read, or for the rasteriser to draw
 * — and the per-layer deltas a live renderer can be told about without
 * reloading anything. The two cannot disagree, because they come from the
 * same evaluation.
 *
 * ```kotlin
 * val compiled = VectorStyleRules.compile(styleJson, rules)
 * compiled.diagnostics.forEach(::println)   // what matched, and what did not
 * ```
 *
 * The rules themselves are JSON. That is the definition rather than a
 * serialisation of something else: a rule set can be written by hand, stored,
 * shipped from a server or diffed, and it is exactly what the compiler reads
 * on all three platforms.
 */
object VectorStyleRules {
    /**
     * The rules document version this build understands.
     *
     * A document claiming another version is refused rather than partly
     * applied — a rule that silently does nothing is the failure this whole
     * design exists to avoid.
     */
    @JvmStatic
    val schemaVersion: Int by lazy { NativeStyleCompiler.nativeSchemaVersion() }

    /**
     * Applies [rulesJson] to [styleJson].
     *
     * @throws VectorStyleException when either document cannot be read: a
     *   style that is not JSON, a rules document from a newer build, a
     *   selector this version does not know.
     */
    @JvmStatic
    fun compile(
        styleJson: String,
        rulesJson: String,
    ): CompiledStyle {
        val answer = JSONObject(NativeStyleCompiler.nativeCompile(styleJson, rulesJson))
        answer.optString("error").takeIf { it.isNotEmpty() }?.let { throw VectorStyleException(it) }
        return CompiledStyle(
            styleJson = answer.getJSONObject("style").toString(),
            mutations = StyleMutation.parseList(answer.optJSONArray("mutations")?.toString() ?: "[]"),
            affects = StyleAffects.of(answer.optString("affects")),
            patchable = answer.optBoolean("patchable", true),
            diagnostics = answer.optJSONArray("diagnostics").toStringList(),
        )
    }

    /**
     * Every layer in the style, with the role this would give it.
     *
     * Rules are written against someone else's style, so being able to ask
     * what is in it — and on what evidence a layer counts as a road — is not
     * a debugging aid, it is how the rules get written in the first place.
     *
     * @throws VectorStyleException when the style cannot be read.
     */
    @JvmStatic
    fun describe(styleJson: String): List<StyleLayerInfo> {
        val text = NativeStyleCompiler.nativeDescribe(styleJson)
        if (text.trimStart().startsWith("{")) {
            throw VectorStyleException(JSONObject(text).optString("error", "the style could not be read"))
        }
        val array = JSONArray(text)
        return (0 until array.length()).map { index ->
            val entry = array.getJSONObject(index)
            StyleLayerInfo(
                id = entry.optString("id"),
                kind = entry.optString("kind"),
                sourceLayer = entry.optString("sourceLayer").takeIf { it.isNotEmpty() },
                roles = entry.optJSONArray("roles").toStringList(),
                evidence = entry.optJSONArray("evidence").toStringList(),
            )
        }
    }

    private fun JSONArray?.toStringList(): List<String> =
        if (this == null) emptyList() else (0 until length()).map { optString(it) }
}

/** What came out of [VectorStyleRules.compile]. */
data class CompiledStyle(
    /** The adjusted document, to hand to a renderer or to rasterise. */
    val styleJson: String,
    /**
     * The per-layer deltas, for a map that can change a loaded style in
     * place rather than reading the document again.
     */
    val mutations: List<StyleMutation>,
    /**
     * Which half of a split raster layer has to be redrawn. A rule that only
     * recolours text need not invalidate every tile on screen.
     */
    val affects: StyleAffects,
    /**
     * False when the mutations alone cannot reproduce the document — a rule
     * added a layer — so a live map has to be given the document instead of
     * being patched.
     */
    val patchable: Boolean,
    /**
     * What the app should know: how many layers each rule matched, a rule
     * that matched none, a change the rasteriser will not draw, an
     * expression it cannot evaluate.
     *
     * Worth surfacing rather than keeping internal. A rule written against
     * the wrong tile schema produces a perfectly good map with nothing
     * changed on it, and nothing else says so.
     */
    val diagnostics: List<String>,
)

/** Which half of a split raster layer a change reaches. */
enum class StyleAffects {
    NONE,
    GROUND,
    LABELS,
    BOTH,
    ;

    internal companion object {
        fun of(name: String): StyleAffects =
            when (name) {
                "ground" -> GROUND
                "labels" -> LABELS
                "both" -> BOTH
                else -> NONE
            }
    }
}

/** One layer of a style, and the role the compiler would give it. */
data class StyleLayerInfo(
    val id: String,
    /** The layer's `type`: `background`, `fill`, `line`, `circle`, `symbol`, or another. */
    val kind: String,
    val sourceLayer: String?,
    /** Most specific last: a road casing is both `road` and `road-casing`. */
    val roles: List<String>,
    /**
     * How each role was decided, in the same order as [roles]:
     * `shortbread:streets` from the schema's table, `id~*-casing` from the
     * layer's name, `kind:symbol` from its type.
     *
     * The id heuristics are the part most likely to be wrong on a style
     * nobody has tried yet, so what they decided is said out loud.
     */
    val evidence: List<String>,
)

/** A style or a rules document that could not be read. */
class VectorStyleException(
    message: String,
) : IllegalArgumentException(message)
