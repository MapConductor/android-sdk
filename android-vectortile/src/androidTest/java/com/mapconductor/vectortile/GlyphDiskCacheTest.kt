package com.mapconductor.vectortile

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class GlyphDiskCacheTest {
    private lateinit var directory: File

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        directory = File(context.cacheDir, "glyph-cache-test-${System.nanoTime()}")
        directory.mkdirs()
    }

    private fun cache(budgetBytes: Long = 1L * 1024 * 1024) = GlyphDiskCache(directory, budgetBytes)

    @Test
    fun returnsWhatItWasGiven() {
        val subject = cache()
        val url = "https://example.test/font/0-255.pbf"
        assertNull(subject.get(url))

        subject.put(url, byteArrayOf(1, 2, 3, 4))
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), subject.get(url))
        assertNull(subject.get("https://example.test/font/256-511.pbf"))
    }

    /**
     * The point of the cache: a new process knows nothing about which ranges
     * it wants until it has drawn a tile, so a warm start has to be able to
     * hand back everything without being asked for it by name.
     */
    @Test
    fun handsBackEveryStoredRangeWithoutBeingAskedForOne() {
        val subject = cache()
        val stored = (0 until 5).map { "https://example.test/font/$it.pbf" to byteArrayOf(it.toByte()) }
        stored.forEach { (url, bytes) -> subject.put(url, bytes) }

        val seen = mutableListOf<Byte>()
        val loaded = GlyphDiskCache(directory, 1L * 1024 * 1024).warm { seen.add(it[0]) }

        assertEquals(5, loaded)
        assertEquals(stored.map { it.second[0] }.toSet(), seen.toSet())
    }

    /** A range that will not parse must not stop the rest from loading. */
    @Test
    fun keepsGoingWhenOneRangeIsRejected() {
        val subject = cache()
        repeat(4) { subject.put("https://example.test/font/$it.pbf", byteArrayOf(it.toByte())) }

        var seen = 0
        val loaded =
            GlyphDiskCache(directory, 1L * 1024 * 1024).warm {
                seen++
                if (seen == 2) throw IllegalStateException("range would not parse")
            }

        assertEquals(4, seen)
        assertEquals(3, loaded)
    }

    /**
     * The budget has to be generous enough to hold what the next launch will
     * ask for -- an 8MB one dropped 26 of Tokyo's 69 ranges and the map went
     * back to fetching them -- but it still has to bound the directory.
     */
    @Test
    fun staysWithinItsBudget() {
        // Sweeps run every 16 writes, so 32 of these cross the budget twice.
        val subject = GlyphDiskCache(directory, 64L * 1024)
        repeat(32) { index ->
            subject.put("https://example.test/font/$index.pbf", ByteArray(8 * 1024) { index.toByte() })
        }

        val total = directory.listFiles().orEmpty().sumOf { it.length() }
        assertTrue("directory grew to $total bytes, past its 64KB budget", total <= 64L * 1024)
        assertTrue("the sweep emptied the directory instead of trimming it", total > 0)
    }
}
