package com.mapconductor.vectortile

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapconductor.core.tileserver.TileRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import android.graphics.BitmapFactory
import android.util.Log

@RunWith(AndroidJUnit4::class)
class RenderAdmissionTest {
    private fun asset(name: String): ByteArray =
        InstrumentationRegistry
            .getInstrumentation()
            .context.assets
            .open(name)
            .use { it.readBytes() }

    private fun forestStyle(
        color: String,
        background: Boolean = false,
    ): String =
        """
        {"version":8,"sources":{"forest":{"type":"vector","tiles":["https://example.invalid/{z}/{x}/{y}.mvt"],"maxzoom":14}},
         "layers":[${if (background) """{"id":"background","type":"background","paint":{"background-color":"$color"}},""" else ""}
         {"id":"forest","type":"fill","source":"forest","source-layer":"land","filter":["==","kind","forest"],"paint":{"fill-color":"$color"}}]}
        """.trimIndent()

    @Test
    fun complexForestUsesCpuWithoutGpuFailure() {
        val tile = asset("stencil-experiment/8-226-100.mvt")
        val style = forestStyle("#80a040")
        VectorTileRenderer.create(style, 512).use { renderer ->
            assertTrue("large visible forests must bypass triangulation", renderer.needsCpu(8, listOf(tile)))
        }
        VectorTileProvider
            .create(
                styleJson = style,
                tileSize = 512,
                geometryPixelRatio = 1,
                renderMode = VectorTileProvider.RenderMode.GPU,
                fetchTile = { tile },
            ).use { provider ->
                val started = System.nanoTime()
                val png = provider.geometryTiles.renderTile(TileRequest(x = 226, y = 100, z = 8))!!
                val elapsedMs = (System.nanoTime() - started) / 1_000_000
                val bitmap = BitmapFactory.decodeByteArray(png, 0, png.size)!!
                try {
                    assertEquals(512, bitmap.width)
                    assertTrue(
                        (0 until 512 step 8).any { y ->
                            (0 until 512 step 8).any { x ->
                                bitmap.getPixel(x, y) ushr 24 !=
                                    0
                            }
                        },
                    )
                } finally {
                    bitmap.recycle()
                }
                assertEquals(0L, provider.gpuRenders)
                assertEquals(0L, provider.gpuFallbacks)
                Log.i("RenderAdmissionTest", "complex forest CPU render=${elapsedMs}ms bytes=${png.size}")
                assertTrue("complex forest stalled for ${elapsedMs}ms", elapsedMs < 5_000)
            }
    }

    @Test
    fun forestMatchingOpaqueBackgroundNeedsNoTriangles() {
        val tile = asset("stencil-experiment/8-226-100.mvt")
        VectorTileRenderer.create(forestStyle("#000000", background = true), 512).use { renderer ->
            TessellatedTile(renderer.tessellate(8, 226, 100, 512, tile, intArrayOf(tile.size))).use { triangles ->
                assertEquals(0, triangles.vertexFloatCount)
                assertNotNull(triangles.background)
            }
        }
    }

    @Test
    fun cancelledGeometryLeavesOccupiedCpuSlots() = cancelWhileSlotsOccupied("renderSlots", gpu = false, labels = false)

    @Test
    fun cancelledLabelsLeaveOccupiedCpuSlots() = cancelWhileSlotsOccupied("renderSlots", gpu = false, labels = true)

    @Test
    fun cancelledGeometryLeavesOccupiedPreparationSlots() =
        cancelWhileSlotsOccupied("gpuPrepareSlots", gpu = true, labels = false)

    private fun cancelWhileSlotsOccupied(
        field: String,
        gpu: Boolean,
        labels: Boolean,
    ) {
        val tile = asset("tile-0-0-0.pbf")
        val style = asset("demo-style.json").decodeToString()
        val provider =
            VectorTileProvider.create(
                styleJson = style,
                tileSize = 256,
                geometryPixelRatio = 1,
                renderMode = if (gpu) VectorTileProvider.RenderMode.GPU else VectorTileProvider.RenderMode.CPU,
                fetchTile = { tile },
            )
        val slots =
            provider.javaClass
                .getDeclaredField(field)
                .apply { isAccessible = true }
                .get(provider) as Semaphore
        val permits = slots.availablePermits()
        slots.acquire(permits)
        val workers = Executors.newSingleThreadExecutor()
        val cancelled = AtomicBoolean(false)
        val source = if (labels) provider.labelTiles else provider.geometryTiles
        val request = TileRequest(x = 0, y = 0, z = 0)
        try {
            val result = workers.submit<ByteArray?> { source.renderTile(request) { cancelled.get() } }
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
            while (!slots.hasQueuedThreads() && !result.isDone && System.nanoTime() < deadline) Thread.sleep(5)
            assertTrue("tile never waited for $field", slots.hasQueuedThreads())
            cancelled.set(true)
            assertNull("cancelled tile must return before the occupied slots open", result.get(1, TimeUnit.SECONDS))
        } finally {
            slots.release(permits)
            workers.shutdownNow()
            assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS))
            provider.close()
        }
    }

    @Test
    fun cancelledGpuWaiterLeavesBeforeCommittedDrawFinishes() {
        val rasterizer = GpuTileRasterizer.createOrNull(256)!!
        val gl =
            rasterizer.javaClass
                .getDeclaredField("thread")
                .apply { isAccessible = true }
                .get(rasterizer) as ExecutorService
        val slot =
            rasterizer.javaClass
                .getDeclaredField("drawSlot")
                .apply { isAccessible = true }
                .get(rasterizer) as Semaphore
        val blocked = CountDownLatch(1)
        val release = CountDownLatch(1)
        val workers = Executors.newFixedThreadPool(2)
        val tile = asset("tile-0-0-0.pbf")
        val renderer = VectorTileRenderer.create(asset("demo-style.json").decodeToString(), 256)

        fun triangles() = TessellatedTile(renderer.tessellate(0, 0, 0, 256, tile, intArrayOf(tile.size)))
        try {
            gl.submit {
                blocked.countDown()
                release.await()
            }
            assertTrue(blocked.await(5, TimeUnit.SECONDS))
            val owner = workers.submit<ByteArray?> { rasterizer.renderPng(triangles()) }
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
            while (slot.availablePermits() != 0 && System.nanoTime() < deadline) Thread.sleep(5)
            assertEquals(0, slot.availablePermits())
            val cancelled = AtomicBoolean(false)
            val waiter =
                workers.submit<ByteArray?> {
                    rasterizer
                        .renderPng(triangles(), isCancelled = { cancelled.get() })
                }
            while (!slot.hasQueuedThreads() && !waiter.isDone && System.nanoTime() < deadline) Thread.sleep(5)
            assertTrue(slot.hasQueuedThreads())
            cancelled.set(true)
            assertNull(waiter.get(1, TimeUnit.SECONDS))
            assertTrue("committed draw must retain its tile and pixels", !owner.isDone)
            release.countDown()
            assertNotNull(owner.get(10, TimeUnit.SECONDS))
            assertNotNull(rasterizer.renderPng(triangles()))
        } finally {
            release.countDown()
            workers.shutdownNow()
            workers.awaitTermination(10, TimeUnit.SECONDS)
            rasterizer.close()
            renderer.close()
        }
    }
}
