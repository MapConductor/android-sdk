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
import android.graphics.Bitmap
import android.graphics.BitmapFactory

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

    private fun provider(
        mode: VectorTileProvider.RenderMode,
        tileSize: Int = 512,
    ) = VectorTileProvider
        .create(
            styleJson = styleJson,
            tileSize = tileSize,
            renderMode = mode,
            fetchTile = { tileBytes },
        ).also { providers.add(it) }

    private fun decode(png: ByteArray): Bitmap = BitmapFactory.decodeByteArray(png, 0, png.size)!!

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
    fun gpuModeActuallyDrawsOnTheGpu() {
        // The guard the first version of this suite lacked. Every other GPU
        // test passes just as well when the path falls back to the CPU on
        // every tile, which is precisely the failure worth catching.
        val subject = provider(VectorTileProvider.RenderMode.GPU)
        repeat(3) { assertNotNull(subject.renderTile(TileRequest(x = 0, y = 0, z = 0))) }
        assertEquals("tiles fell back to the CPU", 0L, subject.gpuFallbacks)
        assertEquals(3L, subject.gpuRenders)
    }

    @Test
    fun gpuAndCpuAgreeOnWhatTheTileLooksLike() {
        val gpu =
            decode(
                provider(VectorTileProvider.RenderMode.GPU)
                    .renderTile(TileRequest(x = 0, y = 0, z = 0))!!,
            )
        val cpu =
            decode(
                provider(VectorTileProvider.RenderMode.CPU)
                    .renderTile(TileRequest(x = 0, y = 0, z = 0))!!,
            )
        assertEquals(cpu.width, gpu.width)

        // Compared as 8x8 block averages, not pixel by pixel. The two
        // rasterisers genuinely differ on edges — MSAA against analytic
        // coverage — and this test tile is a whole world at z0, which is
        // almost entirely coastline: 3.4% of its pixels sit on an edge, while
        // a normal street tile measures 0.05%. Averaging asks the question the
        // claim actually makes, that no *region* disagrees.
        val size = cpu.width
        val block = 8
        val blocks = size / block
        val gpuRow = IntArray(size)
        val cpuRow = IntArray(size)
        val gpuSums = Array(blocks) { IntArray(blocks * 3) }
        val cpuSums = Array(blocks) { IntArray(blocks * 3) }

        for (y in 0 until size) {
            gpu.getPixels(gpuRow, 0, size, 0, y, size, 1)
            cpu.getPixels(cpuRow, 0, size, 0, y, size, 1)
            val by = y / block
            for (x in 0 until size) {
                val bx = x / block
                for (channel in 0..2) {
                    val shift = 16 - channel * 8
                    gpuSums[by][bx * 3 + channel] += (gpuRow[x] shr shift) and 0xFF
                    cpuSums[by][bx * 3 + channel] += (cpuRow[x] shr shift) and 0xFF
                }
            }
        }

        val pixelsPerBlock = block * block
        var worstBlock = 0
        var differingBlocks = 0
        for (by in 0 until blocks) {
            for (bx in 0 until blocks) {
                var worstChannel = 0
                for (channel in 0..2) {
                    val a = gpuSums[by][bx * 3 + channel] / pixelsPerBlock
                    val b = cpuSums[by][bx * 3 + channel] / pixelsPerBlock
                    worstChannel = maxOf(worstChannel, Math.abs(a - b))
                }
                if (worstChannel > 24) differingBlocks++
                worstBlock = maxOf(worstBlock, worstChannel)
            }
        }
        val percent = differingBlocks * 100.0 / (blocks * blocks)
        println("GPU_VS_CPU_BLOCKS=%.2f%% worst=%d".format(percent, worstBlock))
        assertTrue(
            "%.2f%% of 8x8 blocks disagree (worst channel delta %d)".format(percent, worstBlock),
            percent < 1.0,
        )
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
        val urls =
            listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1).map { (x, y) ->
                template.replace("{z}", "1").replace("{x}", "$x").replace("{y}", "$y")
            }

        val results =
            urls.map { url ->
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
        val subject =
            VectorTileProvider.create(
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
            val samples =
                (1..7)
                    .map {
                        val started = System.nanoTime()
                        subject.renderTile(TileRequest(x = 0, y = 0, z = 0))
                        (System.nanoTime() - started) / 1_000_000.0
                    }.sorted()
            return samples[samples.size / 2]
        }
        println("MODE_COST gpu=%.1fms cpu=%.1fms".format(median(gpu), median(cpu)))
    }
}
