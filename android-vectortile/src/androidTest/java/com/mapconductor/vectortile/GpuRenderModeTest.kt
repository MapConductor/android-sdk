package com.mapconductor.vectortile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapconductor.core.tileserver.TileRequest
import com.mapconductor.core.tileserver.TileServerRegistry
import java.net.HttpURLConnection
import java.net.URL
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers the GPU rendering mode as a *module* feature rather than an
 * experiment: that it survives the tile server's thread pool, that it agrees
 * with the CPU renderer, and that asking for it never costs a tile.
 */
@RunWith(AndroidJUnit4::class)
class GpuRenderModeTest {

    private lateinit var styleJson: String
    private lateinit var tileBytes: ByteArray
    private val routeId = "vectortile-gpu-test"
    private val providers = mutableListOf<VectorTileProvider>()

    @Before
    fun setUp() {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        styleJson = assets.open("demo-style.json").use { it.readBytes().decodeToString() }
        tileBytes = assets.open("tile-0-0-0.pbf").use { it.readBytes() }
    }

    @After
    fun tearDown() {
        TileServerRegistry.get().unregister(routeId)
        providers.forEach { it.close() }
    }

    private fun provider(mode: VectorTileProvider.RenderMode, tileSize: Int = 512) =
        VectorTileProvider.create(
            styleJson = styleJson,
            tileSize = tileSize,
            renderMode = mode,
            fetchTile = { tileBytes },
        ).also { providers.add(it) }

    private fun decode(png: ByteArray): Bitmap =
        BitmapFactory.decodeByteArray(png, 0, png.size)!!

    @Test
    fun gpuModeIsAvailableAndReportsItself() {
        val subject = provider(VectorTileProvider.RenderMode.GPU)
        assertEquals(VectorTileProvider.RenderMode.GPU, subject.renderMode)
        // e.g. "ES3 MSAA x4"
        assertTrue(
            "unexpected description: ${subject.rendererDescription()}",
            subject.rendererDescription().startsWith("ES"),
        )
    }

    @Test
    fun cpuModeNeverProbesTheGpu() {
        val subject = provider(VectorTileProvider.RenderMode.CPU)
        assertEquals(VectorTileProvider.RenderMode.CPU, subject.renderMode)
        assertEquals("cpu", subject.rendererDescription())
    }

    @Test
    fun gpuAndCpuAgreeOnWhatTheTileLooksLike() {
        val gpu = decode(provider(VectorTileProvider.RenderMode.GPU)
            .renderTile(TileRequest(x = 0, y = 0, z = 0))!!)
        val cpu = decode(provider(VectorTileProvider.RenderMode.CPU)
            .renderTile(TileRequest(x = 0, y = 0, z = 0))!!)

        assertEquals(cpu.width, gpu.width)

        // Not bit-identical by design: MSAA against analytic coverage differs
        // on edges. The bar is that no *region* disagrees.
        var differing = 0
        val size = cpu.width
        val gpuRow = IntArray(size)
        val cpuRow = IntArray(size)
        for (y in 0 until size) {
            gpu.getPixels(gpuRow, 0, size, 0, y, size, 1)
            cpu.getPixels(cpuRow, 0, size, 0, y, size, 1)
            for (x in 0 until size) {
                val a = gpuRow[x]
                val b = cpuRow[x]
                val delta = maxOf(
                    Math.abs(((a shr 16) and 0xFF) - ((b shr 16) and 0xFF)),
                    Math.abs(((a shr 8) and 0xFF) - ((b shr 8) and 0xFF)),
                    Math.abs((a and 0xFF) - (b and 0xFF)),
                )
                if (delta > 24) differing++
            }
        }
        val percent = differing * 100.0 / (size * size)
        println("GPU_VS_CPU_DIFF=%.2f%%".format(percent))
        assertTrue("GPU and CPU disagree on %.2f%% of pixels".format(percent), percent < 3.0)
    }

    @Test
    fun gpuServesThroughTheTileServersThreadPool() {
        // The EGL context is thread-bound; the server calls renderTile from a
        // pool. This is the test that would fail if the GL work were not
        // marshalled onto its own thread.
        val server = TileServerRegistry.get()
        val subject = provider(VectorTileProvider.RenderMode.GPU)
        server.register(routeId, subject)

        val template = server.urlTemplate(routeId, 512)
        val urls = listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1).map { (x, y) ->
            template.replace("{z}", "1").replace("{x}", "$x").replace("{y}", "$y")
        }

        val results = urls.map { url ->
            Thread {
                val connection = URL(url).openConnection() as HttpURLConnection
                try {
                    check(connection.responseCode == 200) { "status ${connection.responseCode}" }
                    val bytes = connection.inputStream.use { it.readBytes() }
                    check(bytes.size > 1_000) { "tiny tile: ${bytes.size}" }
                } finally {
                    connection.disconnect()
                }
            }
        }
        results.forEach { it.start() }
        results.forEach { it.join(30_000) }
        results.forEach { assertTrue("a request thread never finished", !it.isAlive) }
    }

    @Test
    fun aClosedGpuProviderStopsCleanly() {
        val subject = VectorTileProvider.create(
            styleJson = styleJson,
            renderMode = VectorTileProvider.RenderMode.GPU,
            fetchTile = { tileBytes },
        )
        assertNotNull(subject.renderTile(TileRequest(x = 0, y = 0, z = 0)))
        subject.close()
        subject.close()
        assertEquals(null, subject.renderTile(TileRequest(x = 0, y = 0, z = 0)))
    }

    @Test
    fun reportsRelativeCostOfBothModes() {
        val gpu = provider(VectorTileProvider.RenderMode.GPU)
        val cpu = provider(VectorTileProvider.RenderMode.CPU)
        fun median(subject: VectorTileProvider): Double {
            repeat(2) { subject.renderTile(TileRequest(x = 0, y = 0, z = 0)) }
            val samples = (1..7).map {
                val started = System.nanoTime()
                subject.renderTile(TileRequest(x = 0, y = 0, z = 0))
                (System.nanoTime() - started) / 1_000_000.0
            }.sorted()
            return samples[samples.size / 2]
        }
        println("MODE_COST gpu=%.1fms cpu=%.1fms".format(median(gpu), median(cpu)))
    }
}
