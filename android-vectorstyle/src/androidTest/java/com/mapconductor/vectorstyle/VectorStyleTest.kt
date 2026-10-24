package com.mapconductor.vectorstyle

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mapconductor.core.map.AttributionRule
import com.mapconductor.core.map.MapServiceRegistry
import com.mapconductor.core.map.MapStyleHost
import com.mapconductor.core.map.MapStyleInstallation
import com.mapconductor.core.map.MutableMapServiceRegistry
import com.mapconductor.core.map.StyleMutation
import com.mapconductor.core.map.StyleMutationStore
import com.mapconductor.core.map.VectorStyleMutationSupportKey
import com.mapconductor.core.map.VectorStyleSupport
import com.mapconductor.core.map.VectorStyleSupportKey
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.tileserver.LocalTileServer
import com.mapconductor.core.tileserver.TileServerRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * What [VectorStyle] does to a map, against a map that is only a recorder.
 *
 * No provider is involved on purpose. What is being checked is the contract
 * every provider will be held to — which route a backend's capabilities put
 * it on, that changing the rules does not reload anything, and that
 * disposing leaves nothing behind — and a fake host is the only way to check
 * all of those without three real maps.
 */
@RunWith(AndroidJUnit4::class)
class VectorStyleTest {
    private val style =
        """
        {"version":8,"name":"test",
         "sources":{"s":{"type":"vector","tiles":["https://x/{z}/{x}/{y}"],"attribution":"© Someone"}},
         "layers":[
           {"id":"bg","type":"background","paint":{"background-color":"#f8f4f0"}},
           {"id":"water","type":"fill","source":"s","source-layer":"water","paint":{"fill-color":"#a0c8f0"}},
           {"id":"roads","type":"line","source":"s","source-layer":"transportation",
            "paint":{"line-color":"#888","line-width":2}},
           {"id":"labels","type":"symbol","source":"s","source-layer":"place","paint":{"text-color":"#333"}}
         ]}
        """.trimIndent()

    private fun rules(color: String) =
        StyleRules.parse("""{"schemaVersion":1,"rules":[{"selector":{"role":"road"},"patch":{"color":"$color"}}]}""")

    // ---- the fake map ------------------------------------------------

    private class Host(
        vector: Boolean,
        mutations: Boolean,
        override val vectorStyleUrl: String? = null,
    ) : MapStyleHost {
        val registry = MutableMapServiceRegistry()
        val shownStyles = mutableListOf<String?>()
        val applied = mutableListOf<List<StyleMutation>>()
        val rasters = mutableListOf<String>()
        val diagnostics = mutableListOf<String>()

        /** Set to fail the mutations whose key matches, as iOS MapLibre does. */
        var refuse: (StyleMutation) -> Boolean = { false }

        val store =
            StyleMutationStore { list ->
                applied += list
                list.filter(refuse)
            }

        init {
            if (vector) {
                registry.put(
                    VectorStyleSupportKey,
                    object : VectorStyleSupport {
                        override fun showStyle(
                            styleUrl: String,
                            attributionRules: List<AttributionRule>,
                        ) {
                            shownStyles += styleUrl
                            credited += attributionRules.map { it.attribution }
                        }

                        override fun clearStyle() {
                            shownStyles += null
                        }
                    },
                )
            }
            if (mutations) registry.put(VectorStyleMutationSupportKey, store)
        }

        val credited = mutableListOf<String>()
        var styleLoadedBlock: (() -> Unit)? = null

        override val serviceRegistry: MapServiceRegistry get() = registry
        override val tileServer: LocalTileServer get() = TileServerRegistry.get()

        override fun onStyleLoaded(block: () -> Unit): MapStyleInstallation {
            styleLoadedBlock = block
            return MapStyleInstallation.of { styleLoadedBlock = null }
        }

        override fun upsertRaster(state: RasterLayerState) {
            rasters += state.id
        }

        override fun removeRaster(id: String) {
            rasters -= id
        }

        override fun report(diagnostics: List<String>) {
            this.diagnostics += diagnostics
        }
    }

    private class Rasteriser : VectorStyleRasteriser {
        val installed = mutableListOf<String>()
        val restyled = mutableListOf<String>()
        var disposals = 0

        override fun install(
            host: MapStyleHost,
            styleJson: String,
            affects: StyleAffects,
        ): VectorStyleRasterisation {
            installed += styleJson
            return object : VectorStyleRasterisation {
                override fun restyle(
                    styleJson: String,
                    affects: StyleAffects,
                ) {
                    restyled += styleJson
                }

                override fun dispose() {
                    disposals += 1
                }
            }
        }
    }

    /** The work happens on a worker; wait for it to land rather than guess. */
    private fun settle(
        host: Host,
        until: () -> Boolean,
    ) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
        while (!until() && System.nanoTime() < deadline) Thread.sleep(10)
        assertTrue("nothing happened: ${host.diagnostics}", until())
    }

    // ---- the routes --------------------------------------------------

    /**
     * A map that reads styles and takes patches gets the author's document
     * and the differences on top -- not the adjusted document. The next rule
     * change produces differences against the original, and a map holding an
     * already-adjusted document would read them as something else.
     */
    @Test
    fun aMapThatReadsStylesIsGivenTheOriginalAndTheDifferences() {
        val host = Host(vector = true, mutations = true)
        val installation =
            VectorStyle(document = VectorStyleSource.Text(style), rules = rules("#ffffff"))
                .install(host)
        try {
            settle(host) { host.applied.isNotEmpty() }

            assertEquals(1, host.shownStyles.size)
            val served = host.tileServer.let { _ -> host.shownStyles.single()!! }
            assertTrue("expected a served document, got $served", served.contains("/docs/"))
            val paint =
                host.applied
                    .flatten()
                    .filterIsInstance<StyleMutation.SetPaint>()
                    .single()
            assertEquals("roads", paint.layerId)
            assertEquals("\"#ffffff\"", paint.value)
            // The document says `#888`; what comes back is the same colour
            // in the one form every renderer can read. MapLibre on iOS
            // parses the colour itself, so the compiler canonicalises every
            // one that crosses the boundary.
            assertEquals("\"#888888\"", paint.previous)
            assertTrue("the style's credit was not carried", host.credited.contains("© Someone"))
        } finally {
            installation.dispose()
        }
    }

    /**
     * The point of the whole design: moving a slider sends new differences
     * and the map is never handed a document again.
     */
    @Test
    fun changingTheRulesDoesNotReloadAnything() {
        val host = Host(vector = true, mutations = true)
        val first = VectorStyle(document = VectorStyleSource.Text(style), rules = rules("#ffffff"))
        val installation = first.install(host)
        try {
            settle(host) { host.applied.isNotEmpty() }
            val servedOnce = host.shownStyles.size

            val again = VectorStyle(document = VectorStyleSource.Text(style), rules = rules("#ff0000"))
            assertTrue("the installed style refused an adjustment-only change", installation.update(again))
            settle(host) { host.applied.size > 1 }

            assertEquals("the map was handed a document again", servedOnce, host.shownStyles.size)
            val last =
                host.applied
                    .last()
                    .filterIsInstance<StyleMutation.SetPaint>()
                    .single()
            assertEquals("\"#ff0000\"", last.value)
            assertEquals("still relative to the author's style", "\"#888888\"", last.previous)
        } finally {
            installation.dispose()
        }
    }

    /** A different document is a different style, so the view has to redo it. */
    @Test
    fun aDifferentDocumentIsNotAnUpdate() {
        val host = Host(vector = true, mutations = true)
        val installation =
            VectorStyle(document = VectorStyleSource.Text(style), rules = rules("#ffffff")).install(host)
        try {
            settle(host) { host.applied.isNotEmpty() }
            assertFalse(
                installation.update(
                    VectorStyle(document = VectorStyleSource.Url("https://example.test/s.json")),
                ),
            )
        } finally {
            installation.dispose()
        }
    }

    /**
     * A map that reads styles but cannot be patched has only one way to show
     * an adjustment: the adjusted document. Every rule change reloads, which
     * is why the capability is worth implementing.
     */
    @Test
    fun aMapThatCannotBePatchedIsGivenTheAdjustedDocument() {
        val host = Host(vector = true, mutations = false)
        val installation =
            VectorStyle(document = VectorStyleSource.Text(style), rules = rules("#ffffff")).install(host)
        try {
            settle(host) { host.shownStyles.isNotEmpty() }
            assertTrue(host.applied.isEmpty())
            // What was served is the adjusted document, not the original.
            val body = fetchServed(host.shownStyles.single()!!)
            assertTrue("the original was served instead: $body", body.contains("\"#ffffff\""))
        } finally {
            installation.dispose()
        }
    }

    @Test
    fun aMapThatCannotReadStylesGoesThroughTheRasteriser() {
        val host = Host(vector = false, mutations = false)
        val rasteriser = Rasteriser()
        val installation =
            VectorStyle(
                document = VectorStyleSource.Text(style),
                rules = rules("#ffffff"),
                rasteriser = rasteriser,
            ).install(host)
        try {
            settle(host) { rasteriser.installed.isNotEmpty() }
            assertTrue(rasteriser.installed.single().contains("\"#ffffff\""))
            assertTrue("the map was handed a style it cannot read", host.shownStyles.isEmpty())

            // A rule change re-rasterises; it does not install again.
            assertTrue(
                installation.update(
                    VectorStyle(
                        document = VectorStyleSource.Text(style),
                        rules = rules("#ff0000"),
                        rasteriser = rasteriser,
                    ),
                ),
            )
            settle(host) { rasteriser.restyled.isNotEmpty() }
            assertEquals(1, rasteriser.installed.size)
            assertTrue(rasteriser.restyled.single().contains("\"#ff0000\""))
        } finally {
            installation.dispose()
            assertEquals(1, rasteriser.disposals)
        }
    }

    /**
     * A backend that can do neither is told so. Left to itself it would show
     * an ordinary map and the adjustments would simply not be there, which
     * is the failure this whole design is built against.
     */
    @Test
    fun aMapWithNeitherIsToldSoRatherThanLeftBlank() {
        val host = Host(vector = false, mutations = false)
        val installation = VectorStyle(document = VectorStyleSource.Text(style)).install(host)
        try {
            settle(host) { host.diagnostics.any { it.contains("no rasteriser") } }
        } finally {
            installation.dispose()
        }
    }

    /** Adjusting "whatever is showing" needs something to be showing. */
    @Test
    fun adjustingTheCurrentDesignNeedsAStyleToAdjust() {
        val host = Host(vector = true, mutations = true, vectorStyleUrl = null)
        val installation = VectorStyle(rules = rules("#ffffff")).install(host)
        try {
            settle(host) { host.diagnostics.any { it.contains("not drawing a vector style") } }
            assertTrue(host.applied.isEmpty())
        } finally {
            installation.dispose()
        }
    }

    // ---- what the map would not take ---------------------------------

    @Test
    fun whatTheMapRefusesIsReportedAndTheRestStays() {
        val host = Host(vector = true, mutations = true)
        host.refuse = { true }
        val installation =
            VectorStyle(document = VectorStyleSource.Text(style), rules = rules("#ffffff")).install(host)
        try {
            settle(host) { host.diagnostics.any { it.contains("were not applied") } }
            // REPORT is the default, so the document was not re-served.
            assertEquals(1, host.shownStyles.size)
        } finally {
            installation.dispose()
        }
    }

    @Test
    fun reloadPolicyHandsOverTheAdjustedDocumentInstead() {
        val host = Host(vector = true, mutations = true)
        host.refuse = { true }
        val installation =
            VectorStyle(
                document = VectorStyleSource.Text(style),
                rules = rules("#ffffff"),
                onUnsupported = UnsupportedPolicy.RELOAD,
            ).install(host)
        try {
            settle(host) { host.shownStyles.size >= 2 }
            val body = fetchServed(host.shownStyles.last()!!)
            assertTrue("the adjusted document was not served: $body", body.contains("\"#ffffff\""))
        } finally {
            installation.dispose()
        }
    }

    // ---- taking it off again -----------------------------------------

    @Test
    fun disposingLeavesTheMapAsItWasFound() {
        val host = Host(vector = true, mutations = true)
        val installation =
            VectorStyle(document = VectorStyleSource.Text(style), rules = rules("#ffffff")).install(host)
        settle(host) { host.applied.isNotEmpty() }
        val served = host.shownStyles.single()!!

        installation.dispose()

        assertTrue("the style was not cleared", host.shownStyles.contains(null))
        assertTrue("the adjustments were not taken off", host.store.applied.isEmpty())
        assertNull("the document is still being served", fetchServedOrNull(served))
        assertNull("the style-loaded hook is still attached", host.styleLoadedBlock)
    }

    /** Disposing twice is what a view does on a fast remount. */
    @Test
    fun disposingTwiceIsHarmless() {
        val host = Host(vector = true, mutations = true)
        val installation =
            VectorStyle(document = VectorStyleSource.Text(style), rules = rules("#ffffff")).install(host)
        settle(host) { host.applied.isNotEmpty() }
        installation.dispose()
        installation.dispose()
        assertEquals(1, host.shownStyles.count { it == null })
    }

    @Test
    fun theDiagnosticsAlwaysSayWhatTheRulesMatched() {
        val host = Host(vector = true, mutations = true)
        val heard = mutableListOf<String>()
        val latch = CountDownLatch(1)
        val installation =
            VectorStyle(
                document = VectorStyleSource.Text(style),
                rules = rules("#ffffff"),
                onDiagnostics = {
                    heard += it
                    latch.countDown()
                },
            ).install(host)
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS))
            assertNotNull(heard.firstOrNull { it.contains("matched 1 layers") })
        } finally {
            installation.dispose()
        }
    }

    // ---- helpers -----------------------------------------------------

    private fun fetchServed(url: String): String = fetchServedOrNull(url) ?: error("nothing served at $url")

    private fun fetchServedOrNull(url: String): String? =
        runCatching {
            (java.net.URL(url).openConnection() as java.net.HttpURLConnection).run {
                try {
                    if (responseCode !in 200..299) return null
                    inputStream.use { it.readBytes().decodeToString() }
                } finally {
                    disconnect()
                }
            }
        }.getOrNull()
}
