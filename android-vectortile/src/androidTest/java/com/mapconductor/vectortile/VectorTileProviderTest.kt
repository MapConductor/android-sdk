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
        val messages = provider!!.diagnostics()
        assertTrue(
            "expected symbol to be reported, got $messages",
            messages.any { it.contains("symbol") },
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
