package com.mapconductor.vectorstyle

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The builder writes the canonical JSON, exactly.
 *
 * Asserted as text rather than through a parser on purpose: the text *is*
 * the contract, the compiler on three platforms reads it, and a stray comma
 * or an unquoted colour is the kind of thing a round-trip through a lenient
 * parser would hide.
 */
class StyleRulesBuilderTest {
    @Test
    fun writesTheExampleTheDesignWasArguedFrom() {
        val rules =
            StyleRules.build {
                all { color = Color.Black }
                role(LayerRole.LABEL) { visible = false }
                role(LayerRole.ROAD_CASING) { color = Color(0xFF303030) }
                role(LayerRole.ROAD) {
                    color = Color.White
                    widthScale = 1.3f
                }
            }

        assertEquals(
            """{"schemaVersion":1,"rules":[""" +
                """{"selector":"all","patch":{"color":"#000000"}},""" +
                """{"selector":{"role":"label"},"patch":{"visible":false}},""" +
                """{"selector":{"role":"road-casing"},"patch":{"color":"#303030"}},""" +
                """{"selector":{"role":"road"},"patch":{"color":"#ffffff","widthScale":1.3}}""" +
                """]}""",
            rules.json,
        )
    }

    @Test
    fun writesNoRulesAtAll() {
        assertEquals("""{"schemaVersion":1,"rules":[]}""", StyleRules.build { }.json)
        assertEquals(StyleRules.NONE.json, StyleRules.build { }.json)
    }

    @Test
    fun carriesAlphaThroughAsRgba() {
        val rules = StyleRules.build { all { color = Color(0x80112233) } }
        assertEquals(
            """{"schemaVersion":1,"rules":[{"selector":"all","patch":{"color":"rgba(17,34,51,0.502)"}}]}""",
            rules.json,
        )
    }

    @Test
    fun composesSelectors() {
        val rules =
            StyleRules.build {
                where(
                    StyleSelector.AllOf(
                        listOf(
                            StyleSelector.Role(LayerRole.ROAD),
                            StyleSelector.Not(StyleSelector.Role(LayerRole.ROAD_CASING)),
                        ),
                    ),
                ) { color = Color.White }
                where(
                    StyleSelector.AnyOf(
                        listOf(StyleSelector.Kind(StyleLayerKind.SYMBOL), StyleSelector.LayerId("poi-*")),
                    ),
                ) { visible = false }
            }

        assertEquals(
            """{"schemaVersion":1,"rules":[""" +
                """{"selector":{"allOf":[{"role":"road"},{"not":{"role":"road-casing"}}]},""" +
                """"patch":{"color":"#ffffff"}},""" +
                """{"selector":{"anyOf":[{"kind":"symbol"},{"layerId":"poi-*"}]},""" +
                """"patch":{"visible":false}}""" +
                """]}""",
            rules.json,
        )
    }

    @Test
    fun writesTheColourFilters() {
        val rules =
            StyleRules.build {
                all { invertLightness() }
                role(LayerRole.LABEL) { desaturate(0.5f) }
                role(LayerRole.WATER) { mix(Color.Black, amount = 0.25f) }
            }
        assertEquals(
            """{"schemaVersion":1,"rules":[""" +
                """{"selector":"all","patch":{"colorFilter":{"invertLightness":true}}},""" +
                """{"selector":{"role":"label"},"patch":{"colorFilter":{"desaturate":0.5}}},""" +
                """{"selector":{"role":"water"},""" +
                """"patch":{"colorFilter":{"mix":{"color":"#000000","amount":0.25}}}}""" +
                """]}""",
            rules.json,
        )
    }

    /**
     * The escape hatch has to produce usable JSON for both an expression and
     * a plain string — a `text-field` that lost its quotes is a style no
     * renderer can read.
     */
    @Test
    fun writesRawPropertiesEitherWay() {
        val rules =
            StyleRules.build {
                kind(StyleLayerKind.SYMBOL) {
                    property("text-field", """["get","name:ja"]""")
                    propertyText("text-transform", "uppercase")
                }
            }
        assertEquals(
            """{"schemaVersion":1,"rules":[{"selector":{"kind":"symbol"},"patch":{"properties":{""" +
                """"text-field":["get","name:ja"],"text-transform":"uppercase"}}}]}""",
            rules.json,
        )
    }

    @Test
    fun namesASchemaWhenToldTo() {
        val named = StyleRules.build { schema = "openmaptiles" }
        assertEquals("""{"schemaVersion":1,"schema":"openmaptiles","rules":[]}""", named.json)

        val own = StyleRules.build { customSchema(mapOf("parcel" to listOf("zoning", "lots"))) }
        assertEquals(
            """{"schemaVersion":1,"schema":{"roles":{"parcel":["zoning","lots"]}},"rules":[]}""",
            own.json,
        )
    }

    /** A layer id with a quote in it must not end the JSON string. */
    @Test
    fun escapesWhatItIsGiven() {
        val rules = StyleRules.build { layerId("""say "hi"\n""") { visible = true } }
        assertEquals(
            """{"schemaVersion":1,"rules":[{"selector":{"layerId":"say \"hi\"\\n"},""" +
                """"patch":{"visible":true}}]}""",
            rules.json,
        )
    }

    @Test
    fun parseKeepsWhatItWasGiven() {
        val text = """{"schemaVersion":1,"rules":[{"selector":"all","patch":{}}]}"""
        assertEquals(text, StyleRules.parse(text).json)
        assertEquals(StyleRules.parse(text), StyleRules.parse(text))
    }
}
