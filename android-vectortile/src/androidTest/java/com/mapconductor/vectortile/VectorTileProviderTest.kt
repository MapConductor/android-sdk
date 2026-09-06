package com.mapconductor.vectortile

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapconductor.core.tileserver.TileRequest
import com.mapconductor.core.tileserver.TileServerRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URL
import android.graphics.BitmapFactory

/**
 * Exercises the path a map backend actually takes: provider ->
 * `LocalTileServer` route -> URL template -> HTTP -> PNG.
 *
 * Source tiles come from a bundled asset rather than the network, so the test
 * measures the SDK plumbing and not connectivity.
 */
@RunWith(AndroidJUnit4::class)
class VectorTileProviderTest {
    private lateinit var styleJson: String
    private lateinit var tileBytes: ByteArray
    private var provider: VectorTileProvider? = null
    private val routeId = "vectortile-test"

    @Before
    fun setUp() {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        styleJson = assets.open("demo-style.json").use { it.readBytes().decodeToString() }
        tileBytes = assets.open("tile-0-0-0.pbf").use { it.readBytes() }
        provider =
            VectorTileProvider.create(
                styleJson = styleJson,
                tileSize = 512,
                // Serve the bundled tile for whatever the plan asks for.
                fetchTile = { tileBytes },
            )
    }

    @After
    fun tearDown() {
        TileServerRegistry.get().unregister(routeId)
        provider?.close()
    }

    /**
     * A map that has moved on closes the connection, and the tile it no longer
     * wants must not be drawn: the render slots and the GL thread are shared,
     * so a doomed tile is drawn *instead of* one that is still on screen.
     */
    @Test
    fun givesUpOnATileTheMapNoLongerWants() {
        val subject = provider!!
        assertEquals(null, subject.renderTile(TileRequest(x = 0, y = 0, z = 0)) { true })

        // Cancelled means not drawn, not merely not returned -- the same tile
        // must still be renderable afterwards, from scratch rather than from a
        // cache entry the cancelled call left behind.
        assertNotNull(subject.renderTile(TileRequest(x = 0, y = 0, z = 0)) { false })
    }

    /**
     * The sprite is the one style asset whose location is known before a tile
     * is drawn, so it goes through the renderer rather than through a tile's
     * demands -- and the whole point of it is that icons draw at all.
     */
    @Test
    fun takesASpriteSheetAndReportsWhenItStillNeedsOne() {
        val subject =
            VectorTileRenderer.create(
                """
                {
                  "version": 8,
                  "sprite": [{ "id": "basics", "url": "https://example.test/sprites/basics" }],
                  "sources": {},
                  "layers": []
                }
                """.trimIndent(),
            )
        subject.use {
            assertEquals(
                listOf(
                    "https://example.test/sprites/basics.json",
                    "https://example.test/sprites/basics.png",
                ),
                it.spriteUrls(1),
            )
            assertEquals(
                listOf(
                    "https://example.test/sprites/basics@2x.json",
                    "https://example.test/sprites/basics@2x.png",
                ),
                it.spriteUrls(2),
            )
            assertTrue("the style names a sprite", it.needsSprite())

            val sheet = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
            sheet.eraseColor(android.graphics.Color.BLACK)
            val png = java.io.ByteArrayOutputStream()
            sheet.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, png)

            val icons =
                it.addSprite(
                    """{"icon-atm": {"x": 0, "y": 0, "width": 8, "height": 8, "pixelRatio": 1}}""",
                    png.toByteArray(),
                )
            assertEquals(1, icons)
            assertTrue("the sheet is in", !it.needsSprite())
        }
    }

    /** A cancellation that arrives after the work is done still returns it. */
    @Test
    fun keepsATileWhoseCancellationCameTooLate() {
        val subject = provider!!
        var calls = 0
        // False for every checkpoint, true only afterwards.
        val png = subject.renderTile(TileRequest(x = 0, y = 0, z = 0)) { calls++ > 100 }
        assertNotNull(png)
        assertTrue("no cancellation checkpoints were reached", calls > 0)
    }

    @Test
    fun servesAPngThroughTheLocalTileServer() {
        val server = TileServerRegistry.get()
        server.register(routeId, provider!!)

        val template = server.urlTemplate(routeId, 512)
        assertTrue("unexpected template: $template", template.contains("/tiles/$routeId/512/"))

        val url =
            template
                .replace("{z}", "0")
                .replace("{x}", "0")
                .replace("{y}", "0")

        val connection = URL(url).openConnection() as HttpURLConnection
        val png =
            try {
                assertEquals(200, connection.responseCode)
                connection.inputStream.use { it.readBytes() }
            } finally {
                connection.disconnect()
            }

        assertTrue("expected a real PNG, got ${png.size} bytes", png.size > 10_000)
        val bitmap = BitmapFactory.decodeByteArray(png, 0, png.size)
        assertNotNull("tile did not decode", bitmap)
        assertEquals(512, bitmap.width)

        // The demo style paints an opaque background everywhere; a blank
        // response would fail this.
        var distinct = HashSet<Int>()
        for (y in 0 until 512 step 8) {
            for (x in 0 until 512 step 8) distinct.add(bitmap.getPixel(x, y))
        }
        assertTrue("expected varied colours, got ${distinct.size}", distinct.size > 10)
    }

    @Test
    fun rendersDirectlyThroughTheProviderInterface() {
        val png = provider!!.renderTile(TileRequest(x = 0, y = 0, z = 0))
        assertNotNull(png)
        assertTrue(png!!.size > 10_000)
    }

    @Test
    fun reportsStyleDiagnostics() {
        // Point labels are drawn now, so a style made of them has nothing to
        // report. This test asserted the opposite -- that symbol layers were
        // not drawn -- and went on passing after that stopped being true,
        // because the message it looked for merely had to contain "symbol".
        assertEquals(emptyList<String>(), provider!!.diagnostics())

        provider!!.setStyle(
            """
            {
              "version": 8,
              "sources": { "src": { "type": "vector", "url": "mapbox://styles" } },
              "layers": [
                { "id": "hills", "type": "hillshade", "source": "src" },
                { "id": "orphan", "type": "fill", "source": "missing", "source-layer": "x" }
              ]
            }
            """.trimIndent(),
        )

        val messages = provider!!.diagnostics()
        assertTrue(
            "expected the undrawable layer type to be reported, got $messages",
            messages.any { it.contains("hillshade") },
        )
        assertTrue(
            "expected the missing source to be reported, got $messages",
            messages.any { it.contains("orphan") && it.contains("missing") },
        )
        assertTrue(
            "expected the unfetchable source URL to be reported, got $messages",
            messages.any { it.contains("mapbox:") },
        )
    }

    @Test
    fun restylingChangesOutputWithoutRefetching() {
        var fetches = 0
        val subject =
            VectorTileProvider.create(
                styleJson = styleJson,
                tileSize = 256,
                fetchTile = {
                    fetches += 1
                    tileBytes
                },
            )
        subject.use {
            val before = it.renderTile(TileRequest(x = 0, y = 0, z = 0))!!
            val afterFirstRender = fetches

            it.setStyle(
                """
                {"version":8,"sources":{},"layers":[
                  {"id":"bg","type":"background","paint":{"background-color":"#ff0000"}}
                ]}
                """.trimIndent(),
            )
            val after = it.renderTile(TileRequest(x = 0, y = 0, z = 0))!!

            assertTrue("restyle should change the pixels", !before.contentEquals(after))
            // Geometry is unchanged, so the cache serves the second render.
            assertEquals(afterFirstRender, fetches)
        }
    }

    @Test
    fun closedProviderStopsServing() {
        val subject = VectorTileProvider.create(styleJson, fetchTile = { tileBytes })
        subject.close()
        subject.close()
        assertEquals(null, subject.renderTile(TileRequest(x = 0, y = 0, z = 0)))
    }
}
