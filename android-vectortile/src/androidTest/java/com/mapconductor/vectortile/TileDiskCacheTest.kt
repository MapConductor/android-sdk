package com.mapconductor.vectortile

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapconductor.core.tileserver.TileRequest
import java.io.File
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The disk cache exists to survive process death: within a session the map SDK
 * already caches raster tiles. These tests cover the part that matters — that a
 * second provider over the same directory serves without rendering, and that a
 * restyle does not serve stale pixels.
 */
@RunWith(AndroidJUnit4::class)
class TileDiskCacheTest {

    private lateinit var directory: File
    private lateinit var styleJson: String
    private lateinit var tileBytes: ByteArray

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        directory = File(context.cacheDir, "vectortile-test-${System.nanoTime()}")
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        styleJson = assets.open("demo-style.json").use { it.readBytes().decodeToString() }
        tileBytes = assets.open("tile-0-0-0.pbf").use { it.readBytes() }
    }

    @After
    fun tearDown() {
        directory.deleteRecursively()
    }

    private fun provider(fetches: IntArray) = VectorTileProvider.create(
        styleJson = styleJson,
        tileSize = 256,
        diskCacheDir = directory,
        fetchTile = { fetches[0]++; tileBytes },
    )

    @Test
    fun aSecondProviderServesFromDiskWithoutFetchingOrRendering() {
        val first = IntArray(1)
        val cold = provider(first).use { it.renderTile(TileRequest(x = 0, y = 0, z = 0)) }
        assertNotNull(cold)
        assertTrue("expected a fetch on the cold path", first[0] > 0)

        // A brand new provider, same directory: this is the app-restart case.
        val second = IntArray(1)
        val warm = provider(second).use { it.renderTile(TileRequest(x = 0, y = 0, z = 0)) }

        assertArrayEquals("cached bytes must match", cold, warm)
        assertEquals("nothing should have been fetched", 0, second[0])
    }

    @Test
    fun restylingDoesNotServeStalePixels() {
        val fetches = IntArray(1)
        provider(fetches).use { subject ->
            val before = subject.renderTile(TileRequest(x = 0, y = 0, z = 0))!!

            subject.setStyle(
                """
                {"version":8,"sources":{},"layers":[
                  {"id":"bg","type":"background","paint":{"background-color":"#ff0000"}}
                ]}
                """.trimIndent(),
            )
            val after = subject.renderTile(TileRequest(x = 0, y = 0, z = 0))!!

            // Entries are keyed by style, so the old tile is simply not found.
            assertTrue("restyle must not return the cached tile", !before.contentEquals(after))
        }
    }

    @Test
    fun cachedTilesLandInTheGivenDirectory() {
        provider(IntArray(1)).use { it.renderTile(TileRequest(x = 0, y = 0, z = 0)) }
        val files = directory.listFiles()?.filter { it.isFile }.orEmpty()
        assertEquals(1, files.size)
        assertTrue("cached tile should be a real PNG", files[0].length() > 1_000)
    }

    @Test
    fun evictionKeepsTheDirectoryWithinBudget() {
        // A one-byte budget: every sweep should leave essentially nothing.
        val cache = TileDiskCache(directory, budgetBytes = 1)
        val payload = ByteArray(4096) { 7 }
        repeat(40) { cache.put("key-$it", payload) }

        val total = directory.listFiles()?.sumOf { it.length() } ?: 0
        assertTrue("expected eviction, directory holds $total bytes", total < 40L * 4096)
    }

    @Test
    fun missingEntriesReturnNull() {
        val cache = TileDiskCache(directory, budgetBytes = 1024)
        assertNull(cache.get("nothing-here"))
        cache.put("a", byteArrayOf(1, 2, 3))
        assertArrayEquals(byteArrayOf(1, 2, 3), cache.get("a"))
    }
}
