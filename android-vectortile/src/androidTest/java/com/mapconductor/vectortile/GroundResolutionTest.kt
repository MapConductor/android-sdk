package com.mapconductor.vectortile

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapconductor.core.tileserver.TileRequest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class GroundResolutionTest {
    @Test
    fun higherResolutionUsesSeparateCacheAndPreservesSourceCoordinates() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val style = context.assets.open("demo-style.json").use { it.readBytes().decodeToString() }
        val tile = context.assets.open("tile-0-0-0.pbf").use { it.readBytes() }
        val cache = File(context.cacheDir, "resolution-${System.nanoTime()}")
        val urls = mutableListOf<String>()
        val request = TileRequest(x = 0, y = 0, z = 0)
        try {
            for (ratio in 1..2) {
                val provider = VectorTileProvider.create(
                    styleJson = style,
                    tileSize = 512,
                    renderMode = VectorTileProvider.RenderMode.GPU,
                    diskCacheDir = cache,
                    geometryPixelRatio = ratio,
                    fetchTile = { url -> urls.add(url); tile },
                )
                try {
                    val png = provider.geometryTiles.renderTile(request)!!
                    val bitmap = BitmapFactory.decodeByteArray(png, 0, png.size)!!
                    assertEquals(512 * ratio, bitmap.width)
                    assertEquals(512 * ratio, bitmap.height)
                    bitmap.recycle()
                    assertEquals(1L, provider.gpuRenders)
                    assertEquals(0L, provider.gpuFallbacks)
                    if (ratio == 2) {
                        repeat(2) {
                            val full = provider.renderTile(request)!!
                            val decoded = BitmapFactory.decodeByteArray(full, 0, full.size)!!
                            assertEquals(1024, decoded.width)
                            decoded.recycle()
                        }
                        assertEquals(0L, provider.gpuFallbacks)
                    }
                } finally {
                    provider.close()
                }
            }
            assertEquals(1, urls.distinct().size)
        } finally {
            cache.deleteRecursively()
        }
    }
}
