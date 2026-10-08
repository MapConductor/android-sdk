package com.mapconductor.vectortile

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

@RunWith(AndroidJUnit4::class)
class SourceTileSharingTest {
    @Test
    fun concurrentRequestsShareOneDiskReadAndOneByteArray() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.cacheDir, "source-sharing-${System.nanoTime()}")
        val url = "https://example.test/4/8/6.mvt"
        val bytes = ByteArray(1024 * 1024) { (it % 251).toByte() }
        StyleAssetCache(File(directory, "sources"), 4L * 1024 * 1024).put(url, bytes)
        val networkReads = AtomicLong()
        val workers = Executors.newFixedThreadPool(16)
        try {
            VectorTileProvider.create(
                styleJson = """{"version":8,"sources":{},"layers":[]}""",
                renderMode = VectorTileProvider.RenderMode.CPU,
                diskCacheDir = directory,
                fetchTile = {
                    networkReads.incrementAndGet()
                    null
                },
            ).use { provider ->
                // Exercise only the source stage so rendering/caches cannot
                // hide duplicate reads of the same source file.
                val sourceTile = VectorTileProvider::class.java.getDeclaredMethod(
                    "sourceTile", String::class.java, Array<AtomicLong>::class.java,
                ).apply { isAccessible = true }
                val barrier = CyclicBarrier(16)
                val stats = Array(7) { AtomicLong() }
                val pending = (0 until 16).map {
                    workers.submit<ByteArray?> {
                        barrier.await(10, TimeUnit.SECONDS)
                        sourceTile.invoke(provider, url, stats) as ByteArray?
                    }
                }
                val results = pending.map { it.get(15, TimeUnit.SECONDS) }
                assertEquals("network reads for a cached source", 0L, networkReads.get())
                // STAT_DISK is the second entry in the provider's fetch stats.
                android.util.Log.i("SourceTileSharingTest", "diskReads=${stats[1].get()} requests=16")
                assertEquals("duplicate disk reads", 1L, stats[1].get())
                for (result in results) assertSame("source bytes were allocated again", results[0], result)
                org.junit.Assert.assertArrayEquals(bytes, results[0])
            }
        } finally {
            workers.shutdownNow()
            directory.deleteRecursively()
        }
    }
}
