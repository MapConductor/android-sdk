package com.mapconductor.vectorstyle

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapconductor.core.map.StyleMutation
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The whole chain, on a device: Rust compiler, JNI, Kotlin.
 *
 * What is being checked is not the compiler — that has its own tests and
 * golden files in Rust — but that this binding carries the answer across
 * unchanged. The numbers asserted here are the ones
 * `crates/mvt-style/tests/golden/versatiles-road-emphasis.json` records, so
 * a binding that quietly loses or mangles something fails rather than
 * producing a slightly different map.
 *
 * The same assertions exist in `bindings/js-style/test/compile.test.mjs`
 * and in `VectorStyleRulesTests` on iOS.
 */
@RunWith(AndroidJUnit4::class)
class VectorStyleRulesTest {
    private val style: String by lazy {
        InstrumentationRegistry
            .getInstrumentation()
            .context.assets
            .open("versatiles-colorful.json")
            .use { it.readBytes().decodeToString() }
    }

    private val roadEmphasis =
        """
        {
          "schemaVersion": 1,
          "rules": [
            { "selector": "all", "patch": { "color": "#000000" } },
            { "selector": { "role": "label" }, "patch": { "visible": false } },
            { "selector": { "role": "road-casing" }, "patch": { "color": "#303030" } },
            { "selector": { "role": "road" }, "patch": { "color": "#ffffff", "widthScale": 1.3 } }
          ]
        }
        """.trimIndent()

    @Test
    fun adjustsARealStyleAndSaysWhatItDid() {
        val compiled = VectorStyleRules.compile(style, roadEmphasis)

        assertEquals(StyleAffects.BOTH, compiled.affects)
        assertTrue("this style has a background already", compiled.patchable)
        // The same counts the Rust golden records, reached through JNI.
        assertTrue(
            compiled.diagnostics.joinToString("\n"),
            compiled.diagnostics.any { it.contains("schema: shortbread") },
        )
        assertTrue(
            compiled.diagnostics.joinToString("\n"),
            compiled.diagnostics.any { it.contains("rule 3 matched 77 layers") },
        )

        assertTrue("only ${compiled.mutations.size} mutations", compiled.mutations.size > 400)
        // Every mutation carries what was there before, or nothing could be
        // taken back when the rules change.
        for (mutation in compiled.mutations.take(50)) {
            assertNotNull(mutation.reversed())
        }

        val layers = JSONObject(compiled.styleJson).getJSONArray("layers")
        assertEquals("no layer was added or lost", 280, layers.length())
    }

    @Test
    fun leavesAloneWhatNoRuleNamed() {
        val compiled =
            VectorStyleRules.compile(
                style,
                """{"schemaVersion":1,"rules":[{"selector":{"role":"water"},"patch":{"color":"#000"}}]}""",
            )
        val before = JSONObject(style)
        val after = JSONObject(compiled.styleJson)
        assertEquals(before.getJSONObject("sources").toString(), after.getJSONObject("sources").toString())
        assertEquals(before.optString("name"), after.optString("name"))
    }

    @Test
    fun describesWhatIsInAStyle() {
        val layers = VectorStyleRules.describe(style)
        assertEquals(280, layers.size)
        val casing = layers.first { it.id.endsWith(":outline") && it.roles.contains("road") }
        assertEquals(listOf("road", "road-casing"), casing.roles)
        assertEquals("shortbread:streets", casing.evidence.first())
    }

    /**
     * A rule set written for a newer build must be refused loudly, not
     * applied in part.
     */
    @Test
    fun refusesARulesDocumentItDoesNotUnderstand() {
        val error =
            assertThrows(VectorStyleException::class.java) {
                VectorStyleRules.compile(style, """{"schemaVersion":99,"rules":[]}""")
            }
        assertTrue(error.message, error.message!!.contains("schemaVersion"))
    }

    @Test
    fun refusesAStyleItCannotRead() {
        assertThrows(VectorStyleException::class.java) {
            VectorStyleRules.compile("{ not json", """{"schemaVersion":1,"rules":[]}""")
        }
        assertThrows(VectorStyleException::class.java) {
            VectorStyleRules.describe("{ not json")
        }
    }

    @Test
    fun reportsTheRulesVersionItWasBuiltWith() {
        assertEquals(1, VectorStyleRules.schemaVersion)
    }

    /**
     * The mutations have to survive the trip through core's parser with
     * their values intact — a colour that lost its quotes is a colour no
     * renderer can read back.
     */
    @Test
    fun mutationsArriveAsCoreTypesWithTheirValues() {
        val compiled =
            VectorStyleRules.compile(
                style,
                """{"schemaVersion":1,"rules":[{"selector":{"layerId":"background"},"patch":{"color":"#010203"}}]}""",
            )
        val paint = compiled.mutations.filterIsInstance<StyleMutation.SetPaint>().single()
        assertEquals("background", paint.layerId)
        assertEquals("background-color", paint.key)
        assertEquals("\"#010203\"", paint.value)
        // The document says `rgb(248,244,240)`; what comes back is the same
        // colour written the one way every renderer can read. MapLibre on
        // iOS parses the colour itself, so the compiler canonicalises every
        // one that crosses the boundary.
        assertEquals("\"#f8f4f0\"", paint.previous)
    }
}
