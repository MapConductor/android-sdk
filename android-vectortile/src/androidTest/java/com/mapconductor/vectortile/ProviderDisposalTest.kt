package com.mapconductor.vectortile

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapconductor.core.tileserver.TileRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock

@RunWith(AndroidJUnit4::class)
class ProviderDisposalTest {
    @Test
    fun uiDisposalDoesNotWaitForAnInFlightNativeCallOrFreeItsHandle() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val style = instrumentation.context.assets.open("demo-style.json").use { it.readBytes().decodeToString() }
        val provider = VectorTileProvider.create(styleJson = style, renderMode = VectorTileProvider.RenderMode.GPU, fetchTile = { null })
        val renderer = VectorTileProvider::class.java.getDeclaredField("renderer").apply { isAccessible = true }.get(provider)
        val lock = VectorTileRenderer::class.java.getDeclaredField("liveCalls").apply { isAccessible = true }.get(renderer) as ReentrantReadWriteLock
        val handle = VectorTileRenderer::class.java.getDeclaredField("handle").apply { isAccessible = true }.get(renderer) as AtomicLong
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val reader = Thread {
            lock.readLock().lock()
            try {
                entered.countDown()
                // Watchdog ensures a broken synchronous disposal cannot hang the test.
                release.await(3, TimeUnit.SECONDS)
            } finally {
                lock.readLock().unlock()
            }
        }
        reader.start()
        assertTrue(entered.await(3, TimeUnit.SECONDS))
        try {
            var durationMs = 0L
            instrumentation.runOnMainSync {
                val start = System.nanoTime()
                provider.closeAsync()
                provider.closeAsync()
                durationMs = (System.nanoTime() - start) / 1_000_000
            }
            assertTrue("UI disposal blocked for ${durationMs}ms", durationMs < 500)
            assertTrue("native handle freed while a call was active", handle.get() != 0L)
            assertNull(provider.geometryTiles.renderTile(TileRequest(x = 0, y = 0, z = 0)))
        } finally {
            release.countDown()
            reader.join(4000)
        }
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        while (handle.get() != 0L && System.nanoTime() < deadline) Thread.sleep(10)
        assertEquals("native renderer was not released", 0L, handle.get())
        provider.close()
    }
}
