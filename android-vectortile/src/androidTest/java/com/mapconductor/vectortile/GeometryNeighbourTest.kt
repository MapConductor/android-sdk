package com.mapconductor.vectortile

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapconductor.core.tileserver.TileRequest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Collections

@RunWith(AndroidJUnit4::class)
class GeometryNeighbourTest {
    @Test
    fun geometrySkipsNeighboursWithoutChangingPixelsOrLabelFetches() {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        // Keep symbol layers (and therefore the neighbour plan), but do not
        // fetch fonts: FULL then has the same pixels as GEOMETRY.
        val style = JSONObject(assets.open("demo-style.json").bufferedReader().use { it.readText() })
        style.remove("glyphs")
        val bytes = assets.open("tile-0-0-0.pbf").use { it.readBytes() }
        for (mode in listOf(VectorTileProvider.RenderMode.CPU, VectorTileProvider.RenderMode.GPU)) {
            for (request in listOf(
                TileRequest(z = 4, x = 8, y = 6),
                TileRequest(z = 14, x = 8000, y = 6000),
                TileRequest(z = 2, x = 0, y = 1),
            )) {
                val fetched = Collections.synchronizedSet(HashSet<String>())
                val plan =
                    VectorTileRenderer.create(style.toString(), VectorTileProvider.DEFAULT_TILE_SIZE).use {
                        JSONArray(it.plan(request.z, request.x, request.y))
                    }
                val entries = (0 until plan.length()).map { plan.getJSONObject(it) }
                val central = entries.filter { !it.getBoolean("labelsOnly") }.map { it.getString("url") }.toSet()
                val all = entries.map { it.getString("url") }.toSet()
                if (request.z > 6) {
                    assertEquals("overzoom already uses only the parent", central, all)
                } else {
                    assertTrue("fixture must include neighbours", all.size > central.size)
                }
                VectorTileProvider
                    .create(
                        styleJson = style.toString(),
                        renderMode = mode,
                        fetchTile = { url ->
                            fetched.add(url)
                            bytes
                        },
                    ).use { provider ->
                        assertEquals(mode, provider.renderMode)
                        val geometry = provider.geometryTiles.renderTile(request)
                        assertNotNull(geometry)
                        assertEquals("geometry fetched label-only neighbours", central, fetched.toSet())
                        // The independently served label route must still fetch
                        // the full ring, even after geometry filled its cache.
                        assertNotNull(provider.labelTiles.renderTile(request))
                        assertEquals(all, fetched.toSet())
                        assertArrayEquals(
                            "dropping neighbours changed geometry",
                            provider.renderTile(request),
                            geometry,
                        )
                        if (mode == VectorTileProvider.RenderMode.GPU) {
                            assertEquals(2L, provider.gpuRenders)
                            assertEquals(0L, provider.gpuFallbacks)
                        }
                    }
            }
        }
    }
}
