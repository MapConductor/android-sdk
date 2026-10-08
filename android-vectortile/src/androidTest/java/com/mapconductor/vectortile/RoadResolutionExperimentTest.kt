package com.mapconductor.vectortile

import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Same geometry, extent and style widths; only the GPU raster size changes. */
@RunWith(AndroidJUnit4::class)
class RoadResolutionExperimentTest {
    @Test
    fun compareRoadRasterResolutionWithTheSameTriangles() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val assets = instrumentation.context.assets
        val folder = "road-resolution-experiment"
        val style = assets.open("$folder/style.json").use { it.readBytes().decodeToString() }
        val mvt = assets.open("$folder/11-1808-807.mvt").use { it.readBytes() }
        val output = File(instrumentation.targetContext.getExternalFilesDir(null), folder)
        output.mkdirs()
        VectorTileRenderer.create(style, 512).use { native ->
            val plan = JSONArray(native.plan(11, 1808, 807))
            val lengths = IntArray(plan.length()) {
                if (plan.getJSONObject(it).optBoolean("labelsOnly", false)) 0 else mvt.size
            }
            TessellatedTile(native.tessellate(11, 1808, 807, 512, mvt, lengths)).use { tile ->
                EglOffscreen.create().use { context ->
                    SolidBatchRenderer().use { renderer ->
                        renderer.initialise()
                        renderer.upload(tile.packed, tile.vertexOffset, tile.vertexFloatCount)
                        val results = StringBuilder("ES=" + context.esVersion + "\n")
                        for (size in listOf(512, 1024)) {
                            context.prepare(size, 4)
                            val color = tile.background ?: floatArrayOf(0f, 0f, 0f, 0f)
                            fun draw() {
                                context.bindDrawTarget()
                                renderer.clear(color[0], color[1], color[2], color[3])
                                renderer.draw(tile.batches, tile.extent)
                            }
                            draw()
                            val pixels = context.readPixels()
                            assertEquals(GLES20.GL_NO_ERROR, GLES20.glGetError())
                            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                            bitmap.copyPixelsFromBuffer(pixels)
                            File(output, "geometry-" + size + ".png").outputStream().use {
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                            }
                            bitmap.recycle()
                            val times = mutableListOf<Double>()
                            repeat(7) {
                                context.finish()
                                val start = System.nanoTime()
                                draw()
                                context.readPixels()
                                times.add((System.nanoTime() - start) / 1_000_000.0)
                            }
                            results.append("size=").append(size)
                                .append(" MSAA=").append(context.activeSamples)
                                .append(" draw+resolve+read median=").append(times.sorted()[3])
                                .append("ms\n")
                        }
                        Log.i("RoadResolutionExperiment", results.toString())
                        File(output, "results.txt").writeText(results.toString())
                    }
                }
            }
        }
    }
}
