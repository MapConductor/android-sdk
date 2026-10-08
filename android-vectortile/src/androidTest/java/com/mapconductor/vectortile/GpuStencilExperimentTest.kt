package com.mapconductor.vectortile

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES30
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.zip.GZIPInputStream

/** Real Shortbread forest: linear fan packing + GPU parity, compared to earcut. */
@RunWith(AndroidJUnit4::class)
class GpuStencilExperimentTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private fun asset(name: String): ByteArray {
        val path = "stencil-experiment/$name"
        return instrumentation.context.assets.open(path).use { stream ->
            if (name.endsWith(".gzip")) GZIPInputStream(stream).use { it.readBytes() }
            else stream.readBytes()
        }
    }
    private fun direct(bytes: ByteArray) = ByteBuffer.allocateDirect(bytes.size)
        .order(ByteOrder.LITTLE_ENDIAN).apply { put(bytes); rewind() }
    private fun ms(start: Long) = (System.nanoTime() - start) / 1_000_000.0

    @Test
    fun realForestWithStencilMatchesEarcutAndMeasuresDeviceCost() {
        val output = File(instrumentation.targetContext.getExternalFilesDir(null), "stencil-experiment")
        output.mkdirs()
        var started = System.nanoTime()
        if (InstrumentationRegistry.getArguments().getString("skipBaseline") != "true") {
            val mvt = asset("8-226-100.mvt")
            val native = VectorTileRenderer.create(asset("style.json").decodeToString(), 512)
            val plan = JSONArray(native.plan(8, 226, 100))
            val lengths = IntArray(plan.length()) {
                if (plan.getJSONObject(it).optBoolean("labelsOnly", false)) 0 else mvt.size
            }
            started = System.nanoTime()
            val baseline = TessellatedTile(native.tessellate(8, 226, 100, 512, mvt, lengths))
            val baselineMessage = "native total=" + ms(started) + "ms decode=" + baseline.decodeMs +
                "ms earcut=" + baseline.tessellateMs + "ms fill=" + baseline.fillMs + "ms"
            Log.i("GpuStencilExperiment", baselineMessage)
            File(output, "baseline.txt").writeText(baselineMessage)
            try {
                val golden = direct(asset("forest-earcut.bin.gzip")).asFloatBuffer()
                assertEquals(golden.capacity(), baseline.vertexFloatCount)
                var changed = 0
                for (i in 0 until golden.capacity()) {
                    if (golden.get(i) != baseline.packed.get(baseline.vertexOffset + i)) changed++
                }
                assertEquals("desktop reference differs from device native triangles", 0, changed)
            } finally {
                baseline.close()
                native.close()
            }
        }

        started = System.nanoTime()
        val input = direct(asset("forest-rings.bin.gzip"))
        val extent = input.int.toFloat()
        val color = FloatArray(4) { input.float }
        val rings = ArrayList<FloatArray>()
        var triangles = 0
        repeat(input.int) {
            val count = input.int
            rings.add(FloatArray(count * 2) { input.float })
            triangles += maxOf(0, count - 2)
        }
        val fans = ByteBuffer.allocateDirect(triangles * 18 * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        for (ring in rings) {
            for (i in 1 until ring.size / 2 - 1) {
                fans.put(ring[0]).put(ring[1]).put(color)
                fans.put(ring[i * 2]).put(ring[i * 2 + 1]).put(color)
                fans.put(ring[(i + 1) * 2]).put(ring[(i + 1) * 2 + 1]).put(color)
            }
        }
        fans.rewind()
        val packingMs = ms(started)
        val reference = direct(asset("forest-earcut.bin.gzip")).asFloatBuffer()
        val cover = ByteBuffer.allocateDirect(6 * 6 * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        for ((x, y) in listOf(0f to 0f, extent to 0f, 0f to extent,
                             0f to extent, extent to 0f, extent to extent)) {
            cover.put(x).put(y).put(color)
        }
        cover.rewind()

        EglOffscreen.create().use { context ->
            assertEquals(3, context.esVersion)
            context.prepare(512, 4)
            val ids = IntArray(1)
            GLES20.glGenRenderbuffers(1, ids, 0)
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, ids[0])
            if (context.activeSamples > 0) {
                GLES30.glRenderbufferStorageMultisample(GLES20.GL_RENDERBUFFER,
                    context.activeSamples, GLES30.GL_DEPTH24_STENCIL8, 512, 512)
            } else {
                GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,
                    GLES30.GL_DEPTH24_STENCIL8, 512, 512)
            }
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_STENCIL_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, ids[0])
            assertEquals(GLES20.GL_FRAMEBUFFER_COMPLETE,
                GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER))
            SolidBatchRenderer().use { renderer ->
                renderer.initialise()
                fun draw(
                    stencil: Boolean,
                    geometry: FloatBuffer = if (stencil) fans else reference,
                    drawExtent: Float = extent,
                    viewport: Int = 512,
                ): ByteBuffer {
                    context.bindDrawTarget()
                    GLES20.glViewport(0, 0, viewport, viewport)
                    GLES20.glDisable(GLES20.GL_CULL_FACE)
                    GLES20.glDisable(GLES20.GL_DEPTH_TEST)
                    GLES20.glDisable(GLES20.GL_STENCIL_TEST)
                    GLES20.glColorMask(true, true, true, true)
                    renderer.clear(0f, 0f, 0f, 0f)
                    val vertices = geometry
                    renderer.upload(vertices, 0, vertices.capacity())
                    if (stencil) {
                        GLES20.glEnable(GLES20.GL_STENCIL_TEST)
                        GLES20.glStencilMask(1)
                        GLES20.glClearStencil(0)
                        GLES20.glClear(GLES20.GL_STENCIL_BUFFER_BIT)
                        GLES20.glStencilFunc(GLES20.GL_ALWAYS, 0, 1)
                        GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_INVERT)
                        GLES20.glColorMask(false, false, false, false)
                    }
                    renderer.draw(listOf(SolidBatchRenderer.Batch(0, vertices.capacity() / 6)), drawExtent)
                    if (stencil) {
                        GLES20.glColorMask(true, true, true, true)
                        GLES20.glStencilFunc(GLES20.GL_EQUAL, 1, 1)
                        GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_KEEP)
                        renderer.upload(cover, 0, cover.capacity())
                        renderer.draw(listOf(SolidBatchRenderer.Batch(0, 6)), drawExtent)
                        GLES20.glDisable(GLES20.GL_STENCIL_TEST)
                    }
                    val pixels = context.readPixels()
                    assertEquals(GLES20.GL_NO_ERROR, GLES20.glGetError())
                    return pixels
                }
                fun save(name: String, pixels: ByteBuffer) {
                    val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
                    pixels.rewind()
                    bitmap.copyPixelsFromBuffer(pixels)
                    File(output, name).outputStream().use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    bitmap.recycle()
                }
                val referencePixels = draw(false)
                save("earcut.png", referencePixels)
                val candidatePixels = draw(true)
                save("stencil.png", candidatePixels)
                var seamPixels = 0
                val stitched = ByteBuffer.allocateDirect(512 * 512 * 4)
                for (ty in 0..1) {
                    for (tx in 0..1) {
                        val shifted = ByteBuffer.allocateDirect(fans.capacity() * 4)
                            .order(ByteOrder.nativeOrder()).asFloatBuffer()
                        for (i in 0 until fans.capacity()) {
                            val shift = when (i % 6) {
                                0 -> tx * extent / 2
                                1 -> ty * extent / 2
                                else -> 0f
                            }
                            shifted.put(fans.get(i) - shift)
                        }
                        shifted.rewind()
                        val tile = draw(true, shifted, extent / 2, 256)
                        for (y in 0 until 256) {
                            for (x in 0 until 256) {
                                for (c in 0..3) {
                                    val src = (y * 512 + x) * 4 + c
                                    val dst = ((y + ty * 256) * 512 + x + tx * 256) * 4 + c
                                    stitched.put(dst, tile.get(src))
                                }
                            }
                        }
                    }
                }
                for (i in 0 until 512 * 512) {
                    val offset = i * 4
                    if ((0..3).any { stitched.get(offset + it) != candidatePixels.get(offset + it) }) {
                        seamPixels++
                    }
                }
                save("stitched.png", stitched)
                Log.i("GpuStencilExperiment", "four quadrants seamPixels=" + seamPixels)
                assertEquals("tile cuts changed coverage", 0, seamPixels)
                var different = 0
                var difference = 0L
                for (i in 0 until 512 * 512) {
                    val a = referencePixels.get(i * 4 + 3).toInt() and 255
                    val b = candidatePixels.get(i * 4 + 3).toInt() and 255
                    if (a != b) different++
                    difference += kotlin.math.abs(a - b)
                }
                val meanAlphaError = difference.toDouble() / (512 * 512)
                val times = mutableListOf<Double>()
                val referenceTimes = mutableListOf<Double>()
                repeat(7) {
                    context.finish()
                    var start = System.nanoTime()
                    draw(false)
                    referenceTimes.add(ms(start))
                    context.finish()
                    start = System.nanoTime()
                    draw(true)
                    times.add(ms(start))
                }
                val message = "MSAA=" + context.activeSamples + " packing=" + packingMs +
                    "ms stencil upload+GPU+read median=" + times.sorted()[3] +
                    "ms earcut prepared upload+GPU+read median=" + referenceTimes.sorted()[3] +
                    "ms differentPixels=" + different + " meanAlphaError=" + meanAlphaError +
                    " fanTriangles=" + triangles + " seamPixels=" + seamPixels
                Log.i("GpuStencilExperiment", message)
                File(output, "results.txt").writeText(message)
                assertTrue("coverage differs: " + message, meanAlphaError < 1.0)
            }
            context.bindDrawTarget()
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_STENCIL_ATTACHMENT, GLES20.GL_RENDERBUFFER, 0)
            GLES20.glDeleteRenderbuffers(1, ids, 0)
        }
    }
}
